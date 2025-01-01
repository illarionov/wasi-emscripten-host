/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.multiplatform

/*
 * Convention plugin that configures Poko - a Gradle plugin used for generating data model classes.
 */
plugins {
    id("dev.drewhamilton.poko")
}

poko {
    pokoAnnotation = "at/released/weh/common/api/WasiEmscriptenHostDataModel"
}