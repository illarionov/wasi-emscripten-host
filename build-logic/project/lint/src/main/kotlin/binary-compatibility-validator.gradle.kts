/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.lint

/*
 * Convention plugin that configures Kotlinx Binary Compatibility Validator
 */
plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
}

apiValidation {
    nonPublicMarkers.add("at.released.weh.common.InternalWasiEmscriptenHostApi")
}
