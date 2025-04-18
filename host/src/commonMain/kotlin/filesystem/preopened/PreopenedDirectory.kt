/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.preopened

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.path.virtual.VirtualPath

@WasiEmscriptenHostDataModel
public class PreopenedDirectory(
    public val realPath: String,
    public val virtualPath: VirtualPath,
)
