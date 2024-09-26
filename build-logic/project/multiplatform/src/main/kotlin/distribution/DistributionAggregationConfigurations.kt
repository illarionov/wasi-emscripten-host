/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.multiplatform.distribution

import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.named

@Suppress("UnstableApiUsage")
internal class DistributionAggregationConfigurations(
    objects: ObjectFactory,
    configurations: ConfigurationContainer,
) {
    val mavenSnapshotAggregation = configurations.dependencyScope("mavenSnapshotAggregation")
    val mavenSnapshotAggregationFiles = configurations.resolvable("mavenSnapshotAggregationFiles") {
        extendsFrom(mavenSnapshotAggregation.get())
        attributes {
            setupMavenSnapshotAggregationAttributes(objects)
        }
    }

    companion object {
        fun AttributeContainer.setupMavenSnapshotAggregationAttributes(objects: ObjectFactory) {
            attribute(USAGE_ATTRIBUTE, objects.named("weh-runtime"))
            attribute(CATEGORY_ATTRIBUTE, objects.named("local-maven-snapshot"))
        }
    }
}
