/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.wasitypes

import at.released.weh.gradle.wasm.codegen.wasitypes.generator.Wasi1TypenamesGenerator
import at.released.weh.gradle.wasm.codegen.wasitypes.generator.WasiFunctionsGenerator
import at.released.weh.gradle.wasm.codegen.witx.parser.WitxFunctionsParser
import at.released.weh.gradle.wasm.codegen.witx.parser.WitxTypenamesParser
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiTypename
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public abstract class GenerateWasi1InterfacesTask : DefaultTask() {
    @get:Input
    public abstract val typenamesPackage: Property<String>

    @get:Input
    public abstract val functionsPackage: Property<String>

    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val typenames: List<WasiTypename> = WitxTypenamesParser.parse()
        Wasi1TypenamesGenerator.generate(
            typenames = typenames,
            typenamesPackage = typenamesPackage.get(),
            outputDirectory = outputDirectory.get().asFile,
        )

        val functions: List<WasiFunc> = WitxFunctionsParser.parse()
        WasiFunctionsGenerator(
            functions = functions,
            functionsPackage = functionsPackage.get(),
            outputDirectory = outputDirectory.get().asFile,
            wasiTypes = typenames.associate { it.identifier to it.typedef },
        ).generate()
    }
}
