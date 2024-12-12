/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource

import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import java.util.concurrent.locks.Lock

internal interface NioFdResource : FdResource {
    val lock: Lock
    val path: NioRealPath

    fun updatePath(realPath: NioRealPath, virtualPath: VirtualPath)
}
