/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor

internal object Messages {
    internal fun fileDescriptorNotOpenedMessage(@IntFileDescriptor fd: FileDescriptor) =
        "File descriptor `$fd` is not opened"
}
