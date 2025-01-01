/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.common.api

/**
 * Annotation with source retention used to mark data models of the public API for automatic generation
 * of `equals` and `hashCode` methods.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class WasiEmscriptenHostDataModel
