/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.Companion.wasiPreview1Code
import at.released.weh.wasi.preview1.type.Errno

internal fun FileSystemErrno.toWasiErrno(): Errno = Errno.fromErrNoCode(this.wasiPreview1Code)!!

internal fun FileSystemOperationError.wasiErrno(): Errno = this.errno.toWasiErrno()
