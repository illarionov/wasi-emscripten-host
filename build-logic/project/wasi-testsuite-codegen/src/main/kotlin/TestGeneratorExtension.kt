/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.create

internal fun Project.createTestGeneratorExtension(): TestGeneratorExtension {
    return extensions.create<TestGeneratorExtension>("wasiTestsuiteTestGen")
}

public interface TestGeneratorExtension {
    val wasiTestsuiteTestsRoot: DirectoryProperty
    val assemblyscriptIgnores: ListProperty<String>
    val cIgnores: ListProperty<String>
    val rustIgnores: ListProperty<String>
}
