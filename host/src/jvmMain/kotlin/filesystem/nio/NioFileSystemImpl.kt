/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.internal.delegatefs.DelegateOperationsFileSystem
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.op.RunWithChannelFd
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
import at.released.weh.filesystem.op.poll.Poll
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
import at.released.weh.filesystem.path.real.nio.NioPathConverter
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.stdio.StandardInputOutput
import java.nio.file.FileSystems
import java.nio.file.FileSystem as nioFileSystem

internal class NioFileSystemImpl(
    javaFs: nioFileSystem = FileSystems.getDefault(),
    interceptors: List<FileSystemInterceptor>,
    stdio: StandardInputOutput,
    isRootAccessAllowed: Boolean,
    currentWorkingDirectory: CurrentWorkingDirectoryConfig,
    preopenedDirectories: List<PreopenedDirectory>,
) : FileSystem {
    private val fsState = NioFileSystemState.create(
        stdio,
        isRootAccessAllowed,
        currentWorkingDirectory,
        preopenedDirectories,
        javaFs,
    )
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>> = mapOf(
        Open to NioOpen(fsState),
        CloseFd to NioCloseFd(fsState),
        AddAdvisoryLockFd to NioAddAdvisoryLockFd(fsState),
        RemoveAdvisoryLockFd to NioRemoveAdvisoryLockFd(fsState),
        CheckAccess to NioCheckAccess(fsState.pathResolver),
        Chmod to NioChmod(fsState),
        ChmodFd to NioChmodFd(fsState),
        Chown to NioChown(fsState),
        ChownFd to NioChownFd(fsState),
        FadviseFd to NioFadviseFd(fsState),
        FallocateFd to NioFallocate(fsState),
        FdAttributes to NioFdAttributes(fsState),
        Fdrenumber to NioFdrenumber(fsState),
        GetCurrentWorkingDirectory to NioGetCurrentWorkingDirectory(fsState.pathResolver, NioPathConverter(javaFs)),
        Hardlink to NioHardlink(fsState),
        Mkdir to NioMkdir(fsState),
        Poll to NioPoll(fsState),
        PrestatFd to NioPrestatFd(fsState),
        ReadFd to NioReadFd(fsState),
        ReadDirFd to NioReadDirFd(fsState),
        ReadLink to NioReadLink(fsState),
        Rename to NioRename(fsState),
        SeekFd to NioSeekFd(fsState),
        SetFdFlags to NioSetFdFlags(fsState),
        SetTimestamp to NioSetTimestamp(fsState),
        SetTimestampFd to NioSetTimestampFd(fsState),
        Stat to NioStat(fsState),
        StatFd to NioStatFd(fsState),
        Symlink to NioSymlink(fsState),
        SyncFd to NioSync(fsState),
        TruncateFd to NioTruncateFd(fsState),
        UnlinkFile to NioUnlinkFile(fsState.pathResolver),
        UnlinkDirectory to NioUnlinkDirectory(fsState.pathResolver),
        WriteFd to NioWriteFd(fsState),
        RunWithChannelFd to NioRunWithRawChannelFd<Any>(fsState),
    )
    private val fsAdapter = DelegateOperationsFileSystem(operations, interceptors)

    override fun close() {
        fsState.close()
    }

    override fun isOperationSupported(
        operation: FileSystemOperation<*, *, *>,
    ): Boolean = fsAdapter.isOperationSupported(operation)

    override fun <I : Any, E : FileSystemOperationError, R : Any> execute(
        operation: FileSystemOperation<I, E, R>,
        input: I,
    ): Either<E, R> = fsAdapter.execute(operation, input)
}
