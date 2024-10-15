/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chasm

import at.released.weh.gradle.wasm.codegen.chasm.generator.HostFunctionsAdapterGenerator
import at.released.weh.gradle.wasm.codegen.witx.parser.WitxFunctionsParser
import at.released.weh.gradle.wasm.codegen.witx.parser.WitxTypenamesParser
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public abstract class GenerateChasmHostFunctionsBuilderTask : DefaultTask() {
    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val typenames: Map<String, WasiType> = WitxTypenamesParser.parse().associate { it.identifier to it.typedef }
        val functions: List<WasiFunc> = WitxFunctionsParser.parse()
            .filter { it.export !in CUSTOM_FUNCTIONS }
        HostFunctionsAdapterGenerator(
            wasiTypenames = typenames,
            wasiFunctions = functions,
            outputDirectory = outputDirectory.get().asFile,
        ).generate()
    }

    private companion object {
        val CUSTOM_FUNCTIONS = setOf("proc_exit")
    }
}
