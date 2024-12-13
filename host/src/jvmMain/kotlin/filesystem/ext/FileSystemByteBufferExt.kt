/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.ext

import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import java.nio.ByteBuffer

internal fun FileSystemByteBuffer.asByteBuffer(): ByteBuffer = ByteBuffer.wrap(this.array, offset, length)
