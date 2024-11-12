/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

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
import at.released.weh.filesystem.op.fadvise.FadviseFd
import at.released.weh.filesystem.op.fallocate.FallocateFd
import at.released.weh.filesystem.op.fdattributes.FdAttributes
import at.released.weh.filesystem.op.fdrenumber.Fdrenumber
import at.released.weh.filesystem.op.hardlink.Hardlink
import at.released.weh.filesystem.op.lock.AddAdvisoryLockFd
import at.released.weh.filesystem.op.lock.RemoveAdvisoryLockFd
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.prestat.PrestatFd
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.WriteFd
import at.released.weh.filesystem.op.rename.Rename
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

internal class AppleFileSystemImpl(
    interceptors: List<FileSystemInterceptor>,
    stdio: StandardInputOutput,
    isRootAccessAllowed: Boolean,
    currentWorkingDirectory: String?,
    preopenedDirectories: List<PreopenedDirectory>,
) : FileSystem {
    private val fsState = AppleFileSystemState.create(
        stdio = stdio,
        isRootAccessAllowed = isRootAccessAllowed,
        currentWorkingDirectory = currentWorkingDirectory ?: "",
        preopenedDirectories = preopenedDirectories,
    )
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>> = mapOf(
        Open to AppleOpen(fsState),
        CloseFd to AppleCloseFd(fsState),
        AddAdvisoryLockFd to AppleAddAdvisoryLockFd(fsState),
        RemoveAdvisoryLockFd to AppleRemoveAdvisoryLockFd(fsState),
        CheckAccess to AppleCheckAccess(fsState),
        Chmod to AppleChmod(fsState),
        ChmodFd to AppleChmodFd(fsState),
        Chown to AppleChown(fsState),
        ChownFd to AppleChownFd(fsState),
        FadviseFd to AppleFadviseFd(fsState),
        FallocateFd to AppleFallocate(fsState),
        FdAttributes to AppleFdAttributes(fsState),
        Fdrenumber to AppleFdrenumber(fsState),
        GetCurrentWorkingDirectory to AppleGetCurrentWorkingDirectory(),
        Hardlink to AppleHardlink(fsState),
        Mkdir to AppleMkdir(fsState),
        PrestatFd to ApplePrestatFd(fsState),
        Symlink to AppleSymlink(fsState),
        ReadFd to AppleReadFd(fsState),
        ReadDirFd to AppleReadDirFd(fsState),
        ReadLink to AppleReadLink(fsState),
        Rename to AppleRename(fsState),
        SeekFd to AppleSeekFd(fsState),
        SetFdFlags to AppleSetFdFlags(fsState),
        SetTimestamp to AppleSetTimestamp(fsState),
        SetTimestampFd to AppleSetTimestampFd(fsState),
        Stat to AppleStat(fsState),
        StatFd to AppleStatFd(fsState),
        SyncFd to AppleSync(fsState),
        TruncateFd to AppleTruncateFd(fsState),
        UnlinkFile to AppleUnlinkFile(fsState),
        UnlinkDirectory to AppleUnlinkDirectory(fsState),
        WriteFd to AppleWriteFd(fsState),
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
