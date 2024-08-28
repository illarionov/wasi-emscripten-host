/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package at.released.weh.gradle.multiplatform

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import com.android.build.api.dsl.TestOptions
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin

/*
 * Convention plugin that configures Android instrumented tests
 */
listOf(
    AppPlugin::class.java,
    LibraryPlugin::class.java,
).forEach { _ ->
    extensions.configure(CommonExtension::class.java) {
        defaultConfig {
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        testOptions.configureManagedDevices()
    }
}

private fun TestOptions.configureManagedDevices() {
    managedDevices {
        devices.maybeCreate<ManagedVirtualDevice>("pixel2api30").apply {
            device = "Pixel 2"
            apiLevel = 30
            systemImageSource = "aosp-atd"
        }
    }
}
