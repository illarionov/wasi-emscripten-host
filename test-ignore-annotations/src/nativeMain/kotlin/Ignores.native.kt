/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.ignore.annotations

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreJs actual constructor()

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreJvm actual constructor()

public actual typealias IgnoreNative = kotlin.test.Ignore

@Target(CLASS, FUNCTION)
public actual annotation class IgnoreWasmJs actual constructor()
