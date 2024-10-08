/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx

import at.released.weh.gradle.wasm.codegen.witx.generator.Wasi1TypenamesGenerator
import at.released.weh.gradle.wasm.codegen.witx.generator.WasiFunctionsGenerator
import at.released.weh.gradle.wasm.codegen.witx.parser.FunctionsParser
import at.released.weh.gradle.wasm.codegen.witx.parser.TypenamesParser
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiTypename
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.TaskAction

public abstract class GenerateWasi1InterfacesTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(NONE)
    public abstract val typenamesSpec: RegularFileProperty

    @get:Input
    public abstract val typenamesPackage: Property<String>

    @get:InputFile
    @get:PathSensitive(NONE)
    public abstract val functionsSpec: RegularFileProperty

    @get:Input
    public abstract val functionsPackage: Property<String>

    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val typenames: List<WasiTypename> = TypenamesParser.parse(typenamesSpec.get().asFile)
        Wasi1TypenamesGenerator.generate(
            typenames = typenames,
            typenamesPackage = typenamesPackage.get(),
            outputDirectory = outputDirectory.get().asFile,
        )

        val functions = FunctionsParser.parse(functionsSpec.get().asFile)
        WasiFunctionsGenerator(
            functions = functions,
            functionsPackage = functionsPackage.get(),
            outputDirectory = outputDirectory.get().asFile,
            wasiTypes = typenames.associate { it.identifier to it.typedef },
        ).generate()
    }
}
