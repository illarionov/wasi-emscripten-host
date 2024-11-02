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
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.op.checkaccess.CheckAccess
import at.released.weh.filesystem.op.chmod.Chmod
import at.released.weh.filesystem.op.chmod.ChmodFd
import at.released.weh.filesystem.op.chown.Chown
import at.released.weh.filesystem.op.chown.ChownFd
import at.released.weh.filesystem.op.close.CloseFd
import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import at.released.weh.filesystem.op.fallocate.FallocateFd
import at.released.weh.filesystem.op.fdattributes.FdAttributes
import at.released.weh.filesystem.op.lock.AddAdvisoryLockFd
import at.released.weh.filesystem.op.lock.RemoveAdvisoryLockFd
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.prestat.PrestatFd
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.WriteFd
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.filesystem.op.setfdflags.SetFdFlags
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import at.released.weh.filesystem.op.settimestamp.SetTimestampFd
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StatFd
import at.released.weh.filesystem.op.symlink.Symlink
import at.released.weh.filesystem.op.sync.SyncFd
import at.released.weh.filesystem.op.truncate.TruncateFd
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.stdio.StandardInputOutput

public class LinuxFileSystemImpl(
    interceptors: List<FileSystemInterceptor>,
    stdio: StandardInputOutput,
    isRootAccessAllowed: Boolean,
    currentWorkingDirectory: String?,
    preopenedDirectories: List<PreopenedDirectory>,
) : FileSystem {
    private val fsState = LinuxFileSystemState.create(
        stdio = stdio,
        isRootAccessAllowed = isRootAccessAllowed,
        currentWorkingDirectory = currentWorkingDirectory ?: "",
        preopenedDirectories = preopenedDirectories,
    )
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>> = mapOf(
        Open to LinuxOpen(fsState),
        CloseFd to LinuxCloseFd(fsState),
        AddAdvisoryLockFd to LinuxAddAdvisoryLockFd(fsState),
        RemoveAdvisoryLockFd to LinuxRemoveAdvisoryLockFd(fsState),
        CheckAccess to LinuxCheckAccess(fsState),
        Chmod to LinuxChmod(fsState),
        ChmodFd to LinuxChmodFd(fsState),
        Chown to LinuxChown(fsState),
        ChownFd to LinuxChownFd(fsState),
        FallocateFd to LinuxFallocate(fsState),
        FdAttributes to LinuxFdAttributes(fsState),
        GetCurrentWorkingDirectory to LinuxGetCurrentWorkingDirectory(),
        Symlink to LinuxSymlink(fsState),
        Mkdir to LinuxMkdir(fsState),
        PrestatFd to LinuxPrestatFd(fsState),
        ReadFd to LinuxReadFd(fsState),
        ReadDirFd to LinuxReadDirFd(fsState),
        ReadLink to LinuxReadLink(fsState),
        SeekFd to LinuxSeekFd(fsState),
        SetFdFlags to LinuxSetFdFlags(fsState),
        SetTimestamp to LinuxSetTimestamp(fsState),
        SetTimestampFd to LinuxSetTimestampFd(fsState),
        Stat to LinuxStat(fsState),
        StatFd to LinuxStatFd(fsState),
        SyncFd to LinuxSync(fsState),
        TruncateFd to LinuxTruncateFd(fsState),
        UnlinkFile to LinuxUnlinkFile(fsState),
        UnlinkDirectory to LinuxUnlinkDirectory(fsState),
        WriteFd to LinuxWriteFd(fsState),
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
