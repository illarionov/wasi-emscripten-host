/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator.ext

import at.released.weh.gradle.wasm.codegen.witx.parser.model.Identifier
import com.squareup.kotlinpoet.ClassName
import java.util.Locale

internal fun Identifier.className(
    typenamesPackage: String,
): ClassName = ClassName(typenamesPackage, this.toUpperCamelCaseClassName())

internal fun Identifier.markerAnnotationClassName(
    typenamesPackage: String,
): ClassName = ClassName(typenamesPackage, this.toUpperCamelCaseClassName() + "Def")

internal fun Identifier.flagsObjectName(
    typenamesPackage: String,
): ClassName = ClassName(typenamesPackage, this.toUpperCamelCaseClassName() + "Flag")

internal fun Identifier.flagsMarkerAnnotationClassName(
    typenamesPackage: String,
): ClassName = markerAnnotationClassName(typenamesPackage)

/**
 * iovec_array -> IovecArray
 */
fun Identifier.toUpperCamelCaseClassName(): String = this.splitToWords().joinToString("") { substring ->
    substring.replaceFirstChar { it.titlecase(Locale.getDefault()) }
}

/**
 * pr_name_len -> prNameLen
 */
fun Identifier.toCamelCasePropertyName(): String = this.splitToWords()
    .mapIndexed { index: Int, word: String ->
        if (index == 0) {
            word
        } else {
            word.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
    }.joinToString("")

/**
 * 2big_list -> TOO_BIG_LIST
 */
fun Identifier.toUppercaseWithUnderscores(): String =
    this.splitToWords().joinToString("_", transform = String::uppercase)

private fun Identifier.splitToWords(): List<String> {
    require(this.isNotEmpty())

    val startNonDigit = this.indexOfFirst { !it.isDigit() }
    val listPrefix = if (startNonDigit == 0) {
        emptyList()
    } else {
        listOf(startNumberToText(substring(0, startNonDigit)))
    }

    return listPrefix + this
        .substring(startNonDigit)
        .split("_")
        .filter(String::isNotEmpty)
}

private fun startNumberToText(
    startNumber: String,
): String {
    if (startNumber == "2") {
        return "Too"
    }
    return "N$startNumber"
}
