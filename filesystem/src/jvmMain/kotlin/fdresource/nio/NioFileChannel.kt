/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.model.FdFlag.FD_APPEND
import at.released.weh.filesystem.model.Fdflags
import java.nio.channels.FileChannel
import java.util.concurrent.locks.Lock
import kotlin.concurrent.withLock
import java.nio.file.Path as NioPath

@InternalWasiEmscriptenHostApi
public class NioFileChannel(
    path: NioPath,
    public val channel: FileChannel,
    fdFlags: Fdflags,
    public val rights: FdRightsBlock,
    internal val fdresourceLock: Lock,
) {
    private var _path: NioPath = path
    public val path: NioPath
        get() = fdresourceLock.withLock { _path }

    private var _fdFlags: Fdflags = fdFlags
    public val fdFlags: Fdflags
        get() = fdresourceLock.withLock { _fdFlags }

    internal inline fun updateFdFlags(valueFactory: (Fdflags) -> Fdflags): Unit = fdresourceLock.withLock {
        _fdFlags = valueFactory(_fdFlags)
    }

    internal inline fun updatePath(valueFactory: (NioPath) -> NioPath): Unit = fdresourceLock.withLock {
        _path = valueFactory(path)
    }
}

internal fun NioFileChannel.isInAppendMode(): Boolean = fdFlags and FD_APPEND == FD_APPEND
