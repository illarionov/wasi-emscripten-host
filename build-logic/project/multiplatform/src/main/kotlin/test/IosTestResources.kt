/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.multiplatform.test

import at.released.weh.gradle.multiplatform.ext.capitalizeAscii
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable

/**
 * Place resources into the test binary's output directory on Apple platforms so that they can be accessed using
 * NSBundle.mainBundle.
 */
public fun Project.setupCopyDirectoryToIosTestResources(
    directory: Provider<Directory>,
    dstDirectory: String = "wasi-testsuite",
    appleTargetsWithResources: Set<String> = setOf("iosSimulatorArm64", "iosArm64", "iosX64"),
) {
    extensions.getByType(KotlinMultiplatformExtension::class.java).targets
        .withType(KotlinNativeTarget::class.java)
        .matching { target -> target.name in appleTargetsWithResources }
        .configureEach {
            configureCopyTestResources(this, directory, dstDirectory)
        }
}

private fun configureCopyTestResources(
    nativeTarget: KotlinNativeTarget,
    directory: Provider<Directory>,
    dstDirectory: String,
) {
    @Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
    val copyResourcesTask = nativeTarget.project.tasks.register(
        "copyTestWehResourcesFor${nativeTarget.name.capitalizeAscii()}",
        Copy::class.java,
    )

    nativeTarget.binaries.withType(TestExecutable::class.java).all {
        val testExec = this
        copyResourcesTask.configure {
            from(directory)
            into(testExec.outputDirectory.resolve(dstDirectory))
        }

        testExec.linkTaskProvider.configure {
            dependsOn(copyResourcesTask)
        }
    }
}
