/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.cwd

import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError

internal fun ResolvePathError.toCommonError(): ResolveRelativePathErrors = when (this) {
    is ResolvePathError.EmptyPath -> InvalidArgument(message)
    is ResolvePathError.FileDescriptorNotOpen -> BadFileDescriptor(message)
    is ResolvePathError.InvalidPath -> BadFileDescriptor(message)
    is ResolvePathError.NotDirectory -> NotDirectory(message)
    is ResolvePathError.RelativePath -> InvalidArgument(message)
}
