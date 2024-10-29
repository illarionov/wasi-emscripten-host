/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.model.FdFlag.FD_APPEND
import at.released.weh.filesystem.model.Fdflags
import java.nio.channels.FileChannel
import java.nio.file.Path as NioPath

@InternalWasiEmscriptenHostApi
public data class NioFileChannel(
    val path: NioPath,
    val channel: FileChannel,
    val fdFlags: Fdflags,
)

internal fun NioFileChannel.isInAppendMode(): Boolean = fdFlags and FD_APPEND == FD_APPEND
