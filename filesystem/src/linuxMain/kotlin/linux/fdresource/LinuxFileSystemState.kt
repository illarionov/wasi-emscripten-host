/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.fdresource

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.internal.FileDescriptorTable
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource.Companion.initStdioDescriptors
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.BaseDirectory.None
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import at.released.weh.filesystem.platform.linux.AT_FDCWD
import at.released.weh.filesystem.posix.NativeFd
import at.released.weh.filesystem.posix.fdresource.PosixFileFdResource
import at.released.weh.filesystem.stdio.StandardInputOutput
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

internal class LinuxFileSystemState(
    stdio: StandardInputOutput,
) : AutoCloseable {
    private val lock: ReentrantLock = reentrantLock()
    val fileDescriptors: FileDescriptorTable<FdResource> = FileDescriptorTable<FdResource>().apply {
        initStdioDescriptors(this, stdio)
    }

    fun get(
        @IntFileDescriptor fd: FileDescriptor,
    ): FdResource? = lock.withLock {
        fileDescriptors[fd]
    }

    fun addFile(
        nativeFd: NativeFd,
    ): Either<Nfile, Pair<FileDescriptor, LinuxFileFdResource>> = lock.withLock {
        fileDescriptors.allocate { fd ->
            LinuxFileFdResource(nativeFd = nativeFd)
        }
    }

    fun remove(
        @IntFileDescriptor fd: FileDescriptor,
    ): Either<BadFileDescriptor, FdResource> = lock.withLock {
        return fileDescriptors.release(fd)
    }

    inline fun <E : FileSystemOperationError, R : Any> executeWithResource(
        fd: FileDescriptor,
        crossinline block: (fdResource: FdResource) -> Either<E, R>,
    ): Either<E, R> {
        @Suppress("UNCHECKED_CAST")
        val resource: FdResource = get(fd) ?: return (BadFileDescriptor(fileDescriptorNotOpenMessage(fd)) as E).left()
        return block(resource)
    }

    inline fun <E : FileSystemOperationError, R : Any> executeWithBaseDirectoryResource(
        baseDirectory: BaseDirectory,
        crossinline block: (directoryNativeFdOrCwd: NativeFd) -> Either<E, R>,
    ): Either<E, R> {
        val nativeFd: NativeFd = resolveNativeDirectoryFd(baseDirectory)
            .getOrElse { bfe ->
                @Suppress("UNCHECKED_CAST")
                return (bfe as E).left()
            }
        return block(nativeFd)
    }

    fun resolveNativeDirectoryFd(
        directory: BaseDirectory,
    ): Either<BadFileDescriptor, NativeFd> = when (directory) {
        None -> NativeFd(AT_FDCWD).right()
        CurrentWorkingDirectory -> NativeFd(AT_FDCWD).right()
        is DirectoryFd -> lock.withLock {
            val resource = fileDescriptors[directory.fd] as? PosixFileFdResource
            resource?.nativeFd?.right() ?: BadFileDescriptor("FD ${directory.fd} is not a directory").left()
        }
    }

    override fun close() {
        val fdResources: List<FdResource> = lock.withLock {
            fileDescriptors.drain()
        }
        for (fd in fdResources) {
            fd.close().onLeft { /* ignore error */ }
        }
    }
}
