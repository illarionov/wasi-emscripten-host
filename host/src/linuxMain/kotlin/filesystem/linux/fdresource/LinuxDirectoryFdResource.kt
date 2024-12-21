/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.fdresource

import arrow.core.Either
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.linux.native.linuxChmodFd
import at.released.weh.filesystem.linux.native.linuxChownFd
import at.released.weh.filesystem.linux.native.linuxFdAttributes
import at.released.weh.filesystem.linux.native.linuxSetTimestamp
import at.released.weh.filesystem.linux.native.linuxStatFd
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryChannel
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryFdResource

internal class LinuxDirectoryFdResource(
    channel: PosixDirectoryChannel,
) : PosixDirectoryFdResource(channel), FdResource {
    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> {
        return linuxFdAttributes(channel.nativeFd, channel.rights)
    }

    override fun stat(): Either<StatError, StructStat> = linuxStatFd(channel.nativeFd.linuxFd)

    override fun chmod(mode: Int): Either<ChmodError, Unit> = linuxChmodFd(channel.nativeFd, mode)

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> = linuxChownFd(channel.nativeFd, owner, group)

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        return linuxSetTimestamp(channel.nativeFd, atimeNanoseconds, mtimeNanoseconds)
    }
}
