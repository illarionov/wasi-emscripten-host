/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.op.checkaccess.CheckAccess
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.EXECUTABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.READABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.WRITEABLE
import kotlin.io.path.exists
import kotlin.io.path.isExecutable
import kotlin.io.path.isReadable
import kotlin.io.path.isWritable
import at.released.weh.filesystem.error.NotDirectory as BaseNotDirectory

internal class NioCheckAccess(
    private val pathResolver: PathResolver,
) : FileSystemOperationHandler<CheckAccess, CheckAccessError, Unit> {
    override fun invoke(input: CheckAccess): Either<CheckAccessError, Unit> = either {
        val path = pathResolver.resolve(input.path, input.baseDirectory, input.allowEmptyPath)
            .mapLeft { it.toCheckAccessError() }
            .bind()
        if (!path.exists(options = asLinkOptions(input.followSymlinks))) {
            raise(NoEntry("File `$path` not exists"))
        }
        if (input.mode.contains(READABLE) && !path.isReadable()) {
            raise(AccessDenied("File `$path` not readable"))
        }
        if (input.mode.contains(WRITEABLE) && !path.isWritable()) {
            raise(AccessDenied("File `$path` not writable"))
        }
        if (input.mode.contains(EXECUTABLE) && !path.isExecutable()) {
            raise(AccessDenied("File `$path` not executable"))
        }
    }

    private fun ResolvePathError.toCheckAccessError(): CheckAccessError = when (this) {
        is ResolvePathError.EmptyPath -> NoEntry(message)
        is ResolvePathError.FileDescriptorNotOpen -> BadFileDescriptor(message)
        is ResolvePathError.NotDirectory -> BaseNotDirectory(message)
        is ResolvePathError.InvalidPath -> NoEntry(message)
        is ResolvePathError.RelativePath -> NoEntry(message)
    }
}
