/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.readdir

import arrow.core.Either
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import kotlinx.cinterop.CPointer
import platform.posix.DIR
import platform.posix.dirent

internal actual val dirent.inode: Long get() = this.d_ino.toLong()
internal actual fun getCookie(dir: CPointer<DIR>, dirent: dirent): Either<BadFileDescriptor, Long> =
    dirent.d_off.right()
