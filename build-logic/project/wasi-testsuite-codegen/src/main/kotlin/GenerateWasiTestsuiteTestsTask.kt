/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen

import at.released.weh.gradle.wasi.testsuite.codegen.generator.SubtestType
import at.released.weh.gradle.wasi.testsuite.codegen.generator.SubtestType.ASSEMBLYSCRIPT
import at.released.weh.gradle.wasi.testsuite.codegen.generator.SubtestType.C
import at.released.weh.gradle.wasi.testsuite.codegen.generator.SubtestType.RUST
import at.released.weh.gradle.wasi.testsuite.codegen.generator.TestClassGenerator
import at.released.weh.gradle.wasi.testsuite.codegen.generator.WasmRuntimeBindings
import at.released.weh.gradle.wasi.testsuite.codegen.generator.WasmRuntimeBindings.CHASM
import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.setProperty
import java.io.File
import javax.inject.Inject

open class GenerateWasiTestsuiteTestsTask @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout,
) : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(RELATIVE)
    val wasiTestsuiteTestsRoot: DirectoryProperty = objects.directoryProperty()

    @get:Input
    val assemblyscriptIgnores: ListProperty<TestIgnore> = objects.listProperty<TestIgnore>()
        .convention(emptyList())

    @get:Input
    val cIgnores: ListProperty<TestIgnore> = objects.listProperty<TestIgnore>()
        .convention(emptyList())

    @get:Input
    val rustIgnores: ListProperty<TestIgnore> = objects.listProperty<TestIgnore>()
        .convention(emptyList())

    @get:Input
    val runtimes: SetProperty<WasmRuntimeBindings> = objects.setProperty<WasmRuntimeBindings>()
        .convention(WasmRuntimeBindings.values().toList())

    @get:Internal
    val codegenRoot: Provider<Directory> = projectLayout.buildDirectory.dir("generated/wasi-testsuite-tests")

    @get:OutputDirectory
    val commonTestOutputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(codegenRoot.map { it.dir("commonTest") })

    @get:OutputDirectory
    val jvmTestOutputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(codegenRoot.map { it.dir("jvmTest") })

    @TaskAction
    fun generate() {
        val testSpecs: Map<WasmRuntimeBindings, List<FileSpec>> =
            runtimes.get().associateWith { bindings: WasmRuntimeBindings ->
                SubtestType.values().map { subtestType ->
                    TestClassGenerator(
                        runtimeBindings = bindings,
                        subtestType = subtestType,
                        testNames = subtestType.getWasiTestNames(),
                        ignoredTests = subtestType.getIgnoredTests(),
                        generateJvmCompanionObjects = bindings.isJvmOnly,
                    ).generate()
                }
            }

        val commonTestOutputDirectory = commonTestOutputDirectory.get().asFile
        commonTestOutputDirectory.deleteContentRecursively()
        testSpecs[CHASM]?.forEach { it.writeTo(commonTestOutputDirectory) }

        val jvmTestOutputDirectory = jvmTestOutputDirectory.get().asFile
        jvmTestOutputDirectory.deleteContentRecursively()
        testSpecs.filter { it.key.isJvmOnly }.forEach { (_, specs) ->
            specs.forEach {
                it.writeTo(jvmTestOutputDirectory)
            }
        }
    }

    private fun SubtestType.getWasiTestNames(): List<String> {
        return getWasiTestNames(wasiTestsuiteTestsRoot.get().dir(this.testsuiteSubdir).asFile)
    }

    private fun SubtestType.getIgnoredTests(): Set<TestIgnore> = when (this) {
        ASSEMBLYSCRIPT -> assemblyscriptIgnores.get()
        C -> cIgnores.get()
        RUST -> rustIgnores.get()
    }.toSet()

    private companion object {
        private val WasmRuntimeBindings.isJvmOnly: Boolean
            get() = this != CHASM

        private fun File.deleteContentRecursively() = this.walkBottomUp()
            .filter { it != this }
            .forEach(File::delete)

        private fun getWasiTestNames(
            subdirectory: File,
        ): List<String> {
            val wasmFiles = subdirectory.listFiles { _, name: String ->
                name.endsWith(".wasm")
            }
            requireNotNull(wasmFiles) {
                "Can not read $subdirectory"
            }

            return wasmFiles.map { it.name.substringBeforeLast(".wasm") }.sorted()
        }
    }
}
