/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils.assertions

import assertk.Assert
import assertk.assertions.isNotNull
import assertk.assertions.isNotZero
import assertk.assertions.isTrue
import assertk.assertions.isZero
import assertk.assertions.prop
import assertk.assertions.support.appendName
import kotlinx.io.files.FileMetadata
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.jvm.JvmName

public fun Assert<Path>.metadata(): Assert<FileMetadata> = transform(appendName("Metadata", separator = ".")) { path ->
    SystemFileSystem.metadataOrNull(path)
}.isNotNull()

@JvmName("metaRegularFile")
public fun Assert<FileMetadata>.isRegularFile(): Unit = prop(FileMetadata::isRegularFile).isTrue()

@JvmName("metaDirectory")
public fun Assert<FileMetadata>.isDirectory(): Unit = prop(FileMetadata::isDirectory).isTrue()

@JvmName("metaFileSize")
public fun Assert<FileMetadata>.fileSize(): Assert<Long> = prop(FileMetadata::size)

public fun Assert<Path>.exists(): Unit = transform(appendName("Path", separator = ".")) { path ->
    SystemFileSystem.exists(path)
}.isTrue()

public fun Assert<Path>.isRegularFile(): Unit = metadata().isRegularFile()
public fun Assert<Path>.isDirectory(): Unit = metadata().isDirectory()
public fun Assert<Path>.fileSize(): Assert<Long> = metadata().fileSize()

public fun Assert<Path>.isEmpty(): Unit = this.fileSize().isZero()
public fun Assert<Path>.isNotEmpty(): Unit = this.fileSize().isNotZero()
