/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.linux.fdresource.LinuxDirectoryFdResource
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.ReadDirResult
import at.released.weh.filesystem.linux.native.ReadDirResult.EndOfStream
import at.released.weh.filesystem.linux.native.ReadDirResult.Entry
import at.released.weh.filesystem.linux.native.ReadDirResult.Error
import at.released.weh.filesystem.linux.native.linuxOpenDir
import at.released.weh.filesystem.linux.native.linuxReadDir
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition
import at.released.weh.filesystem.posix.NativeDirectoryFd
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.io.IOException
import platform.posix.DIR
import platform.posix.EBADF
import platform.posix.EBUSY
import platform.posix.EINTR
import platform.posix.EMFILE
import platform.posix.closedir
import platform.posix.dup
import platform.posix.errno
import platform.posix.seekdir
import platform.posix.strerror

internal class LinuxReadDirFd(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<ReadDirFd, ReadDirError, DirEntrySequence> {
    override fun invoke(input: ReadDirFd): Either<ReadDirError, DirEntrySequence> {
        return fsState.executeWithResource(input.fd) { resource ->
            if (resource !is LinuxDirectoryFdResource) {
                return@executeWithResource BadFileDescriptor("${input.fd} is not a directory").left()
            }

            // need a dup since closedir() closes the underlying file descriptor
            val dirFdDup = dup(resource.nativeFd.linuxFd)
            if (dirFdDup == -1) {
                return@executeWithResource errno.dupErrorToReadDirError().left()
            }

            linuxOpenDir(NativeDirectoryFd(dirFdDup)).map { dirPointer ->
                LinuxDirEntrySequence(resource, dirPointer, input.startPosition)
            }
        }
    }

    private class LinuxDirEntrySequence(
        private val resource: LinuxDirectoryFdResource,
        private val dir: CPointer<DIR>,
        private val startPosition: DirSequenceStartPosition,
    ) : DirEntrySequence {
        private val isClosed: AtomicBoolean = atomic(false)

        override fun iterator(): Iterator<DirEntry> {
            if (startPosition is DirSequenceStartPosition.Cookie) {
                // XXX: location returned by the telldir is valid only within the same opened descriptor DIR.
                seekdir(dir, startPosition.cookie)
            }
            val firstEntry = linuxReadDir(dir)
            return LinuxDirectoryIterator(dir, firstEntry, isClosed::value)
        }

        override fun close() {
            isClosed.value = true
            val resultCode = closedir(dir)
            if (resultCode != 0) {
                val err = errno
                throw IOException("Can not close directory: $err (${strerror(err)})")
            }
        }

        override fun toString(): String {
            return "LinuxDirEntrySequence(${resource.virtualPath}, dir=$dir, isClosed=$isClosed)"
        }
    }

    private class LinuxDirectoryIterator(
        private val dir: CPointer<DIR>,
        private var next: ReadDirResult,
        private val streamIsClosed: () -> Boolean,
        private val nextDirProvider: (CPointer<DIR>) -> ReadDirResult = ::linuxReadDir,
    ) : Iterator<DirEntry> {
        override fun hasNext(): Boolean {
            return next != EndOfStream
        }

        override fun next(): DirEntry {
            check(!streamIsClosed()) { "Stream is closed" }

            when (val current: ReadDirResult = next) {
                EndOfStream -> throw NoSuchElementException()

                is Entry -> {
                    next = nextDirProvider(dir)
                    return current.entry
                }

                is Error -> {
                    next = EndOfStream
                    throw IOException("Can not read directory: ${current.error}")
                }
            }
        }
    }

    private companion object {
        private fun Int.dupErrorToReadDirError(): ReadDirError = when (this) {
            EBADF -> BadFileDescriptor("Bad file descriptor")
            EBUSY -> IoError("EBUSY")
            EINTR -> IoError("Interrupted by signal")
            EMFILE -> Mfile("Too many open files")
            else -> IoError("Other erorr `$this` (${strerror(this)?.toKStringFromUtf8()})")
        }
    }
}
