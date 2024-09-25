/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator.ext

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName

internal val JVM_STATIC_CLASS_NAME = JvmStatic::class.asClassName()

private val annotationRetentionClassName = AnnotationRetention::class.asClassName()
private val annotationTargetClassName = AnnotationTarget::class.asClassName()

fun createRetentionAnnotation(
    retention: AnnotationRetention,
): AnnotationSpec = AnnotationSpec.builder(Retention::class)
    .addMember("%M", MemberName(annotationRetentionClassName, retention.name))
    .build()

fun createTargetAnnotation(
    target: List<AnnotationTarget>,
): AnnotationSpec {
    val builder = AnnotationSpec.builder(Target::class)
    target.forEach {
        builder.addMember("%M", MemberName(annotationTargetClassName, it.name))
    }
    return builder.build()
}
