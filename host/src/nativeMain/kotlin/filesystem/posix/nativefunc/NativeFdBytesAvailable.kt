/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.nativefunc

import arrow.core.Either
import at.released.weh.filesystem.posix.NativeFileFd

internal expect fun nativeFdBytesAvailable(fd: NativeFileFd): Either<Int, Int>
