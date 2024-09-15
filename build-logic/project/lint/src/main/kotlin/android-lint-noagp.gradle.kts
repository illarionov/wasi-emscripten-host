/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.lint

import com.android.build.api.dsl.Lint
import com.android.build.gradle.LintPlugin

/*
 * Convention plugin that configures Android Lint in libraries without the Android Gradle plugin
 */
apply<LintPlugin>()

extensions.configure<Lint> {
    configureCommonAndroidLint()
}
