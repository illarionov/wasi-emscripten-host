/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.internal.delegatefs.DelegateOperationsFileSystem
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.op.checkaccess.CheckAccess
import at.released.weh.filesystem.op.chmod.Chmod
import at.released.weh.filesystem.op.chmod.ChmodFd
import at.released.weh.filesystem.op.chown.Chown
import at.released.weh.filesystem.op.chown.ChownFd
import at.released.weh.filesystem.op.close.CloseFd
import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import at.released.weh.filesystem.op.lock.AddAdvisoryLockFd
import at.released.weh.filesystem.op.lock.RemoveAdvisoryLockFd
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.WriteFd
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import at.released.weh.filesystem.op.settimestamp.SetTimestampFd
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StatFd
import at.released.weh.filesystem.op.sync.SyncFd
import at.released.weh.filesystem.op.truncate.TruncateFd
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.posix.PosixCloseFd
import at.released.weh.filesystem.posix.base.PosixFileSystemState

public class LinuxFileSystemImpl(
    interceptors: List<FileSystemInterceptor>,
) : FileSystem {
    private val fsState = PosixFileSystemState()
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>> = mapOf(
        Open to LinuxOpen(fsState),
        CloseFd to PosixCloseFd(fsState),
        AddAdvisoryLockFd to LinuxAddAdvisoryLockFd,
        RemoveAdvisoryLockFd to LinuxRemoveAdvisoryLockFd,
        CheckAccess to LinuxCheckAccess,
        Chmod to LinuxChmod,
        ChmodFd to LinuxChmodFd,
        Chown to LinuxChown,
        ChownFd to LinuxChownFd,
        GetCurrentWorkingDirectory to LinuxGetCurrentWorkingDirectory,
        Mkdir to LinuxMkdir,
        ReadFd to LinuxReadFd,
        ReadLink to LinuxReadLink,
        SeekFd to LinuxSeekFd,
        SetTimestamp to LinuxSetTimestamp,
        SetTimestampFd to LinuxSetTimestampFd,
        Stat to LinuxStat,
        StatFd to LinuxStatFd,
        SyncFd to LinuxSync,
        TruncateFd to LinuxTruncateFd,
        UnlinkFile to LinuxUnlinkFile,
        UnlinkDirectory to LinuxUnlinkDirectory,
        WriteFd to LinuxWriteFd,
    )
    private val fsAdapter = DelegateOperationsFileSystem(operations, interceptors)

    override fun close() {
        fsState.close()
    }

    override fun <I : Any, E : FileSystemOperationError, R : Any> execute(
        operation: FileSystemOperation<I, E, R>,
        input: I,
    ): Either<E, R> = fsAdapter.execute(operation, input)

    override fun isOperationSupported(operation: FileSystemOperation<*, *, *>): Boolean {
        return fsAdapter.isOperationSupported(operation)
    }
}
