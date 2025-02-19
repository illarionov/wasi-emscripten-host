/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.ext

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.fdresource.nio.NioFileStat.ATTR_UNI_CTIME
import at.released.weh.filesystem.fdresource.nio.NioFileStat.ATTR_UNI_INO
import at.released.weh.filesystem.fdresource.nio.ReadAttributesError
import at.released.weh.filesystem.fdresource.nio.readAttributeMapIfSupported
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit

internal fun Path.readOrGenerateInode(
    basicFileAttributes: BasicFileAttributes,
    followSymlinks: Boolean,
): Either<ReadAttributesError, Long> = either {
    val unixAttrs = readAttributeMapIfSupported("unix:$ATTR_UNI_INO,$ATTR_UNI_CTIME", followSymlinks).bind()
    readOrGenerateInode(basicFileAttributes, unixAttrs)
}

// XXX: unixAttrs and basicFileAttrs.fileKey are not available on Windows.
// Should we try to read Windows File id somehow?
internal fun Path.readOrGenerateInode(
    basicFileAttributes: BasicFileAttributes,
    unixAttrs: Map<String, Any?>,
): Long {
    val unixIno = unixAttrs[ATTR_UNI_INO] as? Long
    if (unixIno != null) {
        return unixIno
    }
    val fileKeyHashCode = basicFileAttributes.fileKey()?.hashCode()?.toLong()
    if (fileKeyHashCode != null) {
        return fileKeyHashCode
    }

    val cTimeFileTime = (unixAttrs[ATTR_UNI_CTIME] ?: basicFileAttributes.creationTime()) as? FileTime
    return generateFictiveInode(this, cTimeFileTime?.to(TimeUnit.NANOSECONDS) ?: 0)
}

@Suppress("MagicNumber")
internal fun generateFictiveInode(
    path: Path,
    fileCreationTime: Long,
): Long {
    val absolutePath: String = path.toAbsolutePath().toString()
    var code: Long = 0
    absolutePath.forEach { ch -> code = 31 * code + ch.code }
    code = 31 * code + fileCreationTime
    return code
}
