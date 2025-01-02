/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.common.api.typedef

/**
 * Annotation to denote that the annotated `long` element represents a logical type
 * and that its value should be one of the explicitly named constants.
 *
 * If the `flag` attribute is set to `true`, multiple constants can be combined.
 *
 * This is similar to the `LongDef` annotation from the `androidx-annotation` package, but with BINARY retention.
 * It is used only as a hint to the reader.
 *
 * @property open Whether any other values are allowed
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class WasiEmscriptenHostLongDef(
    vararg val value: Long = [],
    val flag: Boolean = false,
    val open: Boolean = false,
)
