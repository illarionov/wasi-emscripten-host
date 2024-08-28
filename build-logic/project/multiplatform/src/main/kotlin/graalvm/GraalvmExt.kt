/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.multiplatform.graalvm

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.nio.file.Paths
import kotlin.io.path.exists

val Project.isRunningOnGraalVm: Provider<Boolean>
    get() = this.providers.gradleProperty("GRAALVM")
        .map(String::toBoolean)
        .orElse(
            providers
                .systemProperty("java.home")
                .map { javaHome ->
                    Paths.get("$javaHome/lib/graalvm").exists()
                },
        )
        .orElse(false)
