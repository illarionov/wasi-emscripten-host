/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.weh.gradle.multiplatform.distribution.aggregate")
}

dependencies {
    mavenSnapshotAggregation(projects.bindingsChasmEmscripten)
    mavenSnapshotAggregation(projects.bindingsChasmWasip1)
    mavenSnapshotAggregation(projects.bindingsChicory)
    mavenSnapshotAggregation(projects.bindingsGraalvm241)
    mavenSnapshotAggregation(projects.commonApi)
    mavenSnapshotAggregation(projects.commonUtil)
    mavenSnapshotAggregation(projects.emscriptenRuntime)
    mavenSnapshotAggregation(projects.host)
    mavenSnapshotAggregation(projects.hostTestFixtures)
    mavenSnapshotAggregation(projects.testIoBootstrap)
    mavenSnapshotAggregation(projects.testLogger)
    mavenSnapshotAggregation(projects.wasmCore)
    mavenSnapshotAggregation(projects.wasmCoreTestFixtures)
    mavenSnapshotAggregation(projects.wasmWasiPreview1Core)
    mavenSnapshotAggregation(projects.wasmWasiPreview1)
}
