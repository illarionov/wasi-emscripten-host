/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

package at.released.weh.gradle.multiplatform.distribution

import at.released.weh.gradle.multiplatform.publish.createWehVersionsExtension
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.register

private val rootVersion = createWehVersionsExtension().rootVersion

private val downloadableReleaseDirName: Provider<String> = rootVersion.map {
    "wasi-emscripten-host-$it"
}
private val distributionDir: Provider<Directory> = layout.buildDirectory.dir("distribution")
private val aggregateConfigurations = DistributionAggregationConfigurations(objects, configurations)

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
tasks.register<Zip>("foldDistribution") {
    archiveBaseName = "maven-wasi-emscripten-host"
    archiveVersion = rootVersion
    destinationDirectory = distributionDir

    from(aggregateConfigurations.mavenSnapshotAggregationFiles.get().asFileTree)
    into(downloadableReleaseDirName)

    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
