/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.fdattributes

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

@WasiEmscriptenHostDataModel
public class FdAttributes(
    @IntFileDescriptor
    public val fd: FileDescriptor,
) {
    public companion object : FileSystemOperation<FdAttributes, FdAttributesError, FdAttributesResult> {
        override val tag: String = "fdattributes"
    }
}
