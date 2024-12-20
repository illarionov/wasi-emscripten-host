/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.fdresource

import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.posix.NativeDirectoryFd

internal data class PosixDirectoryChannel(
    val nativeFd: NativeDirectoryFd,
    val isPreopened: Boolean = false,
    val virtualPath: VirtualPath,
    val rights: FdRightsBlock,
)
