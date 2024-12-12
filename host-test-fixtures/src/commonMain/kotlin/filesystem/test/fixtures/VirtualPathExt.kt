/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.test.fixtures

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.path.virtual.VirtualPath

@InternalWasiEmscriptenHostApi
public fun String.toVirtualPath(): VirtualPath =
    VirtualPath.create(this).getOrNull() ?: error("Can not convert `$this` to virtual path")
