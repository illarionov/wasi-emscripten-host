/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.fdresource.NioDirectoryFdResource
import at.released.weh.filesystem.fdresource.nio.readFileType
import at.released.weh.filesystem.fdresource.nio.readInode
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd
import kotlinx.io.IOException
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.NotDirectoryException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

internal class NioReadDirFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<ReadDirFd, ReadDirError, DirEntrySequence> {
    override fun invoke(input: ReadDirFd): Either<ReadDirError, DirEntrySequence> {
        return fsState.executeWithResource(input.fd) { resource ->
            if (resource !is NioDirectoryFdResource) {
                return@executeWithResource BadFileDescriptor("${input.fd} is not a directory").left()
            }
            val rootPath = resource.realPath
            val stream = Either.catch { Files.newDirectoryStream(rootPath) }
                .mapLeft { it.toReadDirError() }
                .getOrElse {
                    return@executeWithResource it.left()
                }

            NioDirEntrySequence(resource, stream).right()
        }
    }

    private class NioDirEntrySequence(
        private val resource: NioDirectoryFdResource,
        private val stream: DirectoryStream<Path>,
    ) : DirEntrySequence {
        private val instanceLock: Any = Any()
        private var instance: NioDirectoryIterator? = null
        private val isClosed: AtomicBoolean = AtomicBoolean(false)

        override fun iterator(): Iterator<DirEntry> = synchronized(instanceLock) {
            check(instance == null) { "Iterator should be used only once" }
            NioDirectoryIterator(resource, stream.iterator(), isClosed::get).also {
                instance = it
            }
        }

        @Throws(IOException::class)
        override fun close() {
            isClosed.set(true)
            stream.close()
        }

        override fun toString(): String {
            return "NioDirEntrySequence(root=`${resource.realPath}`)"
        }
    }

    @Suppress("IteratorNotThrowingNoSuchElementException")
    private class NioDirectoryIterator(
        private val directoryFdResource: NioDirectoryFdResource,
        private val directoryIterator: Iterator<Path>,
        private val streamIsClosed: () -> Boolean,
    ) : Iterator<DirEntry> {
        private var specialDirPosition: Int = 0

        override fun hasNext(): Boolean {
            return if (specialDirPosition < 2) {
                true
            } else {
                return directoryIterator.hasNext()
            }
        }

        override fun next(): DirEntry {
            return if (specialDirPosition < 2) {
                nextSpecialEntry()
            } else {
                nextEntry()
            }
        }

        private fun nextSpecialEntry(): DirEntry {
            check(!streamIsClosed()) { "Stream is closed" }

            val current = specialDirPosition
            specialDirPosition += 1

            return when (current) {
                0 -> dirEntryPath(".", directoryFdResource.realPath)
                1 -> dirEntryPath("..", directoryFdResource.realPath.resolve(".."))
                else -> error("Incorrect special dir index $current")
            }
        }

        @Throws(NoSuchElementException::class)
        private fun nextEntry(): DirEntry {
            val next = directoryIterator.next()
            val relativePath = if (next.isAbsolute) {
                directoryFdResource.realPath.relativize(next)
            } else {
                next
            }.toString()
            return dirEntryPath(relativePath, next)
        }

        private fun dirEntryPath(
            name: String,
            realPath: Path,
        ): DirEntry {
            val fileType = realPath.readFileType().getOrElse { throw IOException("Can not read file type: $it") }
            val inode = realPath.readInode().getOrElse { 0 }
            return DirEntry(
                name = name,
                type = fileType,
                inode = inode,
                cookie = 0, // TODO
            )
        }
    }

    private companion object {
        internal fun Throwable.toReadDirError(): ReadDirError = when {
            this is NotDirectoryException -> NotDirectory("Path is not a directory")
            this is IOException -> IoError("Error: ${this.message}")
            else -> BadFileDescriptor("Error: ${this.message}")
        }
    }
}
