/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.fdresource

import arrow.core.Either
import at.released.weh.filesystem.apple.ext.posixFd
import at.released.weh.filesystem.apple.nativefunc.appleChmodFd
import at.released.weh.filesystem.apple.nativefunc.appleChownFd
import at.released.weh.filesystem.apple.nativefunc.appleFdAttributes
import at.released.weh.filesystem.apple.nativefunc.appleSetTimestamp
import at.released.weh.filesystem.apple.nativefunc.appleStatFd
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryChannel
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryFdResource
import at.released.weh.filesystem.posix.nativefunc.posixClose

internal class AppleDirectoryFdResource(
    channel: PosixDirectoryChannel,
) : PosixDirectoryFdResource(channel), FdResource {
    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> {
        return appleFdAttributes(channel.nativeFd, channel.rights)
    }

    override fun stat(): Either<StatError, StructStat> {
        return appleStatFd(channel.nativeFd.posixFd)
    }

    override fun chmod(mode: Int): Either<ChmodError, Unit> = appleChmodFd(channel.nativeFd, mode)

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> = appleChownFd(channel.nativeFd, owner, group)

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        return appleSetTimestamp(channel.nativeFd, atimeNanoseconds, mtimeNanoseconds)
    }

    override fun close(): Either<CloseError, Unit> = posixClose(channel.nativeFd)
}
