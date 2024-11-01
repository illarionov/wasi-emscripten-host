/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.host.clock.Clock
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Fstflags
import at.released.weh.wasi.preview1.type.FstflagsFlag.ATIM
import at.released.weh.wasi.preview1.type.FstflagsFlag.ATIM_NOW
import at.released.weh.wasi.preview1.type.FstflagsFlag.MTIM
import at.released.weh.wasi.preview1.type.FstflagsFlag.MTIM_NOW
import at.released.weh.wasi.preview1.type.FstflagsType
import at.released.weh.wasi.preview1.type.Timestamp
import at.released.weh.wasi.preview1.type.TimestampType
import kotlin.experimental.and
import kotlin.experimental.or

internal fun validateFstflags(
    flags: Fstflags,
): Either<Errno, Unit> {
    if (flags and (ATIM_NOW or ATIM) == ATIM_NOW or ATIM) {
        return Errno.INVAL.left()
    }
    if (flags and (MTIM_NOW or MTIM) == MTIM_NOW or MTIM) {
        return Errno.INVAL.left()
    }
    return Unit.right()
}

internal fun getRequestedAtimeMtime(
    clock: Clock,
    @TimestampType atime: Timestamp,
    @TimestampType mtime: Timestamp,
    @FstflagsType fstflags: Fstflags,
): Either<Errno, AtimeMtimeRequest> {
    validateFstflags(fstflags).onLeft { return it.left() }

    val timeNow = if (fstflags and (ATIM_NOW or MTIM_NOW) != 0.toShort()) {
        clock.getCurrentTimeEpochNanoseconds()
    } else {
        0
    }
    val atimeNanoseconds = when {
        fstflags and ATIM_NOW == ATIM_NOW -> timeNow
        fstflags and ATIM == ATIM -> atime
        else -> null
    }
    val mtimeNanoseconds = when {
        fstflags and MTIM_NOW == MTIM_NOW -> timeNow
        fstflags and MTIM == MTIM -> mtime
        else -> null
    }
    return AtimeMtimeRequest(atimeNanoseconds, mtimeNanoseconds).right()
}

internal class AtimeMtimeRequest(
    val atimeNanoseconds: Long?,
    val mtimeNanoseconds: Long?,
)
