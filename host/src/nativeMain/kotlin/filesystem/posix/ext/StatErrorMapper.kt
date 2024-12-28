/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.ext

import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks

internal fun StatError.toFdAttributesError(): FdAttributesError = when (this) {
    is AccessDenied -> this
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is IoError -> this
    is NameTooLong -> InvalidArgument(message)
    is NoEntry -> InvalidArgument(message)
    is NotCapable -> InvalidArgument(message)
    is NotDirectory -> InvalidArgument(message)
    is TooManySymbolicLinks -> this
    is Nfile -> InvalidArgument(message)
}
