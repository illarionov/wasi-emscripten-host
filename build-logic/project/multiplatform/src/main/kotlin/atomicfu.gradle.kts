/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.multiplatform

/*
 * Convention plugin that configures Atomicfu
 */
plugins {
    id("org.jetbrains.kotlinx.atomicfu")
}

atomicfu {
    transformJvm = true
    jvmVariant = "FU"
}
