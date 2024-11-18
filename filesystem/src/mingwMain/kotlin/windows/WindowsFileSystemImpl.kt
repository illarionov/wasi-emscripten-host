/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.internal.delegatefs.DelegateOperationsFileSystem
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.op.close.CloseFd
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.stdio.StandardInputOutput
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsFileSystemImpl(
    interceptors: List<FileSystemInterceptor>,
    stdio: StandardInputOutput,
    isRootAccessAllowed: Boolean,
    currentWorkingDirectory: String?,
    preopenedDirectories: List<PreopenedDirectory>,
) : FileSystem {
    private val fsState = WindowsFileSystemState.create(
        stdio = stdio,
        isRootAccessAllowed = isRootAccessAllowed,
        currentWorkingDirectory = currentWorkingDirectory ?: "",
        preopenedDirectories = preopenedDirectories,
    )
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>> = mapOf(
        Open to WindowsOpen(fsState),
        CloseFd to WindowsCloseFd(fsState),
//        AddAdvisoryLockFd to LinuxAddAdvisoryLockFd(fsState),
//        RemoveAdvisoryLockFd to LinuxRemoveAdvisoryLockFd(fsState),
//        CheckAccess to LinuxCheckAccess(fsState),
//        Chmod to LinuxChmod(fsState),
//        ChmodFd to LinuxChmodFd(fsState),
//        Chown to LinuxChown(fsState),
//        ChownFd to LinuxChownFd(fsState),
//        FadviseFd to LinuxFadviseFd(fsState),
//        FallocateFd to LinuxFallocate(fsState),
//        FdAttributes to LinuxFdAttributes(fsState),
//        Fdrenumber to LinuxFdrenumber(fsState),
//        GetCurrentWorkingDirectory to LinuxGetCurrentWorkingDirectory(),
//        Hardlink to LinuxHardlink(fsState),
//        Mkdir to LinuxMkdir(fsState),
//        PrestatFd to LinuxPrestatFd(fsState),
//        Symlink to LinuxSymlink(fsState),
//        ReadFd to LinuxReadFd(fsState),
//        ReadDirFd to LinuxReadDirFd(fsState),
//        ReadLink to LinuxReadLink(fsState),
//        Rename to LinuxRename(fsState),
//        SeekFd to LinuxSeekFd(fsState),
//        SetFdFlags to LinuxSetFdFlags(fsState),
//        SetTimestamp to LinuxSetTimestamp(fsState),
//        SetTimestampFd to LinuxSetTimestampFd(fsState),
//        Stat to LinuxStat(fsState),
//        StatFd to LinuxStatFd(fsState),
//        SyncFd to LinuxSync(fsState),
//        TruncateFd to LinuxTruncateFd(fsState),
//        UnlinkFile to LinuxUnlinkFile(fsState),
//        UnlinkDirectory to LinuxUnlinkDirectory(fsState),
//        WriteFd to LinuxWriteFd(fsState),
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
