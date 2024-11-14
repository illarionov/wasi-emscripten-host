/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen

import at.released.weh.gradle.wasi.testsuite.codegen.generator.WasmRuntimeBindings
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.SetProperty
import org.gradle.kotlin.dsl.create

internal fun Project.createTestGeneratorExtension(): TestGeneratorExtension {
    return extensions.create<TestGeneratorExtension>("wasiTestsuiteTestGen").apply {
        this.runtimes.convention(WasmRuntimeBindings.values().toList())
    }
}

public interface TestGeneratorExtension {
    val wasiTestsuiteTestsRoot: DirectoryProperty
    val assemblyscriptIgnores: ListProperty<TestIgnore>
    val cIgnores: ListProperty<TestIgnore>
    val rustIgnores: ListProperty<TestIgnore>
    val runtimes: SetProperty<WasmRuntimeBindings>
}
