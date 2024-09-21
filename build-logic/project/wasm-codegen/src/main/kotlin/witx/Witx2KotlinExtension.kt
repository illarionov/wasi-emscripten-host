/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

internal fun Project.createWitx2KotlinExtension(): Witx2KotlinExtension {
    return extensions.create<Witx2KotlinExtension>("witx2kotlin")
}

public open class Witx2KotlinExtension @Inject constructor(
    objects: ObjectFactory,
    projectLayout: ProjectLayout,
) {
    public val specRoot: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.projectDirectory.dir("witx"))
    public val typenamesSpec: RegularFileProperty = objects.fileProperty()
        .convention(specRoot.file("typenames.witx"))
    public val functionsSpec: RegularFileProperty = objects.fileProperty()
        .convention(specRoot.file("wasi_snapshot_preview1.witx"))
    public val outputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(projectLayout.buildDirectory.dir("generated/witx"))
    public val typenamesPackage: Property<String> = objects.property<String>()
        .convention("at.released.weh.wasi.preview1.gen.type")
    public val functionsPackage: Property<String> = objects.property<String>()
        .convention("at.released.weh.wasi.preview1.gen.func")
}
