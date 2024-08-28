/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.multiplatform

import at.released.weh.gradle.multiplatform.graalvm.GraalvmCompilerJvmArgumentsProvider
import at.released.weh.gradle.multiplatform.graalvm.isRunningOnGraalVm

configurations {
    dependencyScope("graalvmCompiler") {
        defaultDependencies {
            add(versionCatalogs.named("libs").findLibrary("graalvm.compiler").get().get())
        }
    }
    resolvable("graalvmCompilerClasspath") {
        extendsFrom(configurations["graalvmCompiler"])
    }
}

val graalVmJvmArgsProvider = GraalvmCompilerJvmArgumentsProvider(
    configurations["graalvmCompilerClasspath"],
    isRunningOnGraalVm,
)

tasks.withType<JavaExec>().configureEach {
    jvmArgumentProviders += graalVmJvmArgsProvider
}
