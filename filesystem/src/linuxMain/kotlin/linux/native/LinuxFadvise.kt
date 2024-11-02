/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FadviseError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.op.fadvise.Advice
import at.released.weh.filesystem.op.fadvise.Advice.DONTNEED
import at.released.weh.filesystem.op.fadvise.Advice.NOREUSE
import at.released.weh.filesystem.op.fadvise.Advice.NORMAL
import at.released.weh.filesystem.op.fadvise.Advice.RANDOM
import at.released.weh.filesystem.op.fadvise.Advice.SEQUENTIAL
import at.released.weh.filesystem.op.fadvise.Advice.WILLNEED
import at.released.weh.filesystem.posix.NativeFileFd
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EBADF
import platform.posix.EINVAL
import platform.posix.ESPIPE
import platform.posix.POSIX_FADV_DONTNEED
import platform.posix.POSIX_FADV_NOREUSE
import platform.posix.POSIX_FADV_NORMAL
import platform.posix.POSIX_FADV_RANDOM
import platform.posix.POSIX_FADV_SEQUENTIAL
import platform.posix.POSIX_FADV_WILLNEED
import platform.posix.posix_fadvise
import platform.posix.strerror

internal fun linuxFadvise(
    fd: NativeFileFd,
    offset: Long,
    length: Long,
    advice: Advice,
): Either<FadviseError, Unit> {
    val resultCode = posix_fadvise(fd.fd, offset, length, advice.toPosixAdvice())
    return if (resultCode == 0) {
        Either.Right(Unit)
    } else {
        Either.Left(fadviseCodeToFadviseError(resultCode))
    }
}

private fun Advice.toPosixAdvice(): Int = when (this) {
    NORMAL -> POSIX_FADV_NORMAL
    SEQUENTIAL -> POSIX_FADV_SEQUENTIAL
    RANDOM -> POSIX_FADV_RANDOM
    WILLNEED -> POSIX_FADV_WILLNEED
    DONTNEED -> POSIX_FADV_DONTNEED
    NOREUSE -> POSIX_FADV_NOREUSE
}

private fun fadviseCodeToFadviseError(code: Int): FadviseError = when (code) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINVAL -> InvalidArgument("Incorrect offset or length")
    ESPIPE -> BadFileDescriptor("Can not fallocate on a pipe")
    else -> InvalidArgument("Other error $code (${strerror(code)?.toKStringFromUtf8()})")
}
