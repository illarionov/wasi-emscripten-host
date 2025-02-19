/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.FD_READDIR
import at.released.weh.wasi.preview1.ext.DIRENT_PACKED_SIZE
import at.released.weh.wasi.preview1.ext.encodeToBuffer
import at.released.weh.wasi.preview1.ext.packTo
import at.released.weh.wasi.preview1.ext.wasiErrno
import at.released.weh.wasi.preview1.type.Dircookie
import at.released.weh.wasi.preview1.type.DircookieType
import at.released.weh.wasi.preview1.type.Dirent
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Errno.INVAL
import at.released.weh.wasi.preview1.type.Errno.IO
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasi.preview1.type.Filetype
import at.released.weh.wasi.preview1.type.Size
import at.released.weh.wasi.preview1.type.SizeType
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.Sink
import kotlinx.io.buffered

public class FdReaddirFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(FD_READDIR, host) {
    public fun execute(
        memory: Memory,
        @IntFileDescriptor fd: FileDescriptor,
        @IntWasmPtr(Byte::class) bufAddr: WasmPtr,
        @SizeType bufLen: Size,
        @DircookieType cookie: Dircookie,
        @IntWasmPtr(Size::class) expectedSizeAddr: WasmPtr,
    ): Errno {
        val startPosition = if (cookie != 0L) {
            DirSequenceStartPosition.Cookie(cookie)
        } else {
            DirSequenceStartPosition.Start
        }

        return host.fileSystem.execute(ReadDirFd, ReadDirFd(fd, startPosition))
            .mapLeft(FileSystemOperationError::wasiErrno)
            .flatMap { closeableSequence: DirEntrySequence ->
                closeableSequence.use { sequence ->
                    memory.sinkWithMaxSize(bufAddr, bufLen).buffered().use { sink ->
                        packDirEntriesToBuf(sequence, sink, bufLen)
                    }
                }.onRight { bytesWritten ->
                    memory.writeI32(expectedSizeAddr, bytesWritten)
                }
            }.fold(
                ifLeft = { it },
                ifRight = { SUCCESS },
            )
    }

    internal companion object {
        fun DirEntry.toDirEntryWithName(): Pair<Dirent, Buffer> {
            val encodedName = this.name.encodeToBuffer()
            return Dirent(
                dNext = this.cookie,
                dIno = this.inode,
                dNamlen = encodedName.size.toInt(),
                dType = checkNotNull(Filetype.fromCode(this.type.id)) {
                    "Unexpected type ${this.type.id}"
                },
            ) to encodedName
        }

        internal fun packDirEntriesToBuf(
            sequence: Sequence<DirEntry>,
            sink: Sink,
            maxSize: Int,
        ): Either<Errno, Int> = Either.catch {
            var bytesLeft = maxSize

            @Suppress("LoopWithTooManyJumpStatements")
            for (dirEntry: DirEntry in sequence) {
                val (dirent, encodedName) = dirEntry.toDirEntryWithName()

                when {
                    bytesLeft >= DIRENT_PACKED_SIZE -> {
                        dirent.packTo(sink)
                        bytesLeft -= DIRENT_PACKED_SIZE
                    }
                    bytesLeft != 0 -> {
                        val packedDirent = Buffer().also { dirent.packTo(it) }
                        sink.write(packedDirent, bytesLeft.toLong())
                        bytesLeft = 0
                        break
                    }
                    else -> break
                }

                val maxNameLength = dirent.dNamlen.coerceAtMost(bytesLeft)
                sink.write(encodedName, maxNameLength.toLong())
                bytesLeft -= maxNameLength
            }
            maxSize - bytesLeft
        }.mapLeft { throwable ->
            when (throwable) {
                is IOException -> IO
                else -> INVAL
            }
        }
    }
}
