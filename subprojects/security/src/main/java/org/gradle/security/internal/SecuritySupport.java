/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.security.internal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.gradle.api.UncheckedIOException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;

public class SecuritySupport {
    private static final int BUFFER = 4096;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.setProperty("crypto.policy", "unlimited");
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static void assertInitialized() {
    }

    public static boolean verify(File file, PGPSignature signature, PGPPublicKey publicKey) throws PGPException {
        signature.init(createContentVerifier(), publicKey);
        byte[] buffer = new byte[BUFFER];
        int len;
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            while ((len = in.read(buffer)) >= 0) {
                signature.update(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return signature.verify();
    }

    private static PGPContentVerifierBuilderProvider createContentVerifier() {
        return new BcPGPContentVerifierBuilderProvider();
    }

    public static PGPSignatureList readSignatures(File file) {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            return readSignatures(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static PGPSignatureList readSignatures(InputStream source) {
        try (InputStream stream = source) {
            PGPObjectFactory objectFactory = new PGPObjectFactory(
                PGPUtil.getDecoderStream(stream), new BcKeyFingerprintCalculator());
            return (PGPSignatureList) objectFactory.nextObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String toHexString(long key) {
        return String.format("%16x", key).trim();
    }
}
