/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("EMPTY_PRIMARY_CONSTRUCTOR")

package at.released.weh.test.ignore.annotations

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreJs()

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreJvm()

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreNative()

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreApple()

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreIos()

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreMacos()

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreLinux()

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreMingw()

@Target(CLASS, FUNCTION)
public expect annotation class IgnoreWasmJs()
