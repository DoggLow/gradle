/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.nativeplatform.fixtures.app

import static org.gradle.nativeplatform.fixtures.app.SourceFileElement.ofFile

class CppSum extends CppSourceFileElement implements SumElement {
    final SourceFileElement header
    final SourceFileElement source

    CppSum(String publicHeadersDir = "headers") {
        header = ofFile(sourceFile(publicHeadersDir, "sum.h", """
#ifdef _WIN32
#define EXPORT_FUNC __declspec(dllexport)
#else
#define EXPORT_FUNC
#endif

class Sum {
public:
    int EXPORT_FUNC sum(int a, int b);
};
"""))

        source = ofFile(sourceFile("cpp", "sum.cpp", """
#include "sum.h"

int Sum::sum(int a, int b) {
    return a + b;
}
"""))

    }

    @Override
    CppSum asLib() {
        return new CppSum("public")
    }

    @Override
    int sum(int a, int b) {
        return a + b
    }
}
