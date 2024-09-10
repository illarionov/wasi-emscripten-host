/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.documentation.dokkatoo

/*
 * Convention plugin responsible for generating aggregated Dokkatoo documentation
 */
plugins {
    id("at.released.weh.gradle.documentation.dokkatoo.base")
}

dokkatoo {
    dokkatooPublications.configureEach {
        moduleName.set("Wasi-emscripten-host")
        includes.from("FRONTPAGE.md")
    }
}
