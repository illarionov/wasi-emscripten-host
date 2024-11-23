/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.readwrite

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CurrentPosition
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.Position
import at.released.weh.filesystem.windows.win32api.model.errorcode.Win32ErrorCode
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.memset
import platform.windows.DWORDVar
import platform.windows.ERROR_HANDLE_EOF
import platform.windows.ERROR_INSUFFICIENT_BUFFER
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.ERROR_IO_PENDING
import platform.windows.ERROR_NOT_ENOUGH_QUOTA
import platform.windows.ERROR_OPERATION_ABORTED
import platform.windows.GetOverlappedResult
import platform.windows.HANDLE
import platform.windows.OVERLAPPED
import platform.windows.ReadFile

internal val SINGLE_EMPTY_IOVEC = listOf(FileSystemByteBuffer(ByteArray(0), 0, 0))

internal fun HANDLE.read(
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
): Either<ReadError, ULong> {
    val nonEmptyIovecs = iovecs.ifEmpty { SINGLE_EMPTY_IOVEC }
    return when (strategy) {
        CurrentPosition -> readChangePosition(nonEmptyIovecs)
        is Position -> readDoNotChangePosition(strategy.position, nonEmptyIovecs)
    }
}

private fun HANDLE.readChangePosition(iovecs: List<FileSystemByteBuffer>): Either<ReadError, ULong> = memScoped {
    var totalBytesRead = 0UL
    val bytesRead: DWORDVar = alloc()
    for (iovec in iovecs) {
        val bytesReadOrError = iovec.array.usePinned { pinnedBuffer ->
            val result = ReadFile(
                this@readChangePosition,
                pinnedBuffer.addressOf(iovec.offset),
                iovec.length.toUInt(),
                bytesRead.ptr,
                null,
            )
            if (result != 0) {
                bytesRead.value.toLong()
            } else {
                -1L
            }
        }
        if (bytesReadOrError < 0L) {
            return Win32ErrorCode.getLast().toReadError().left()
        }
        totalBytesRead += bytesReadOrError.toULong()
        if (bytesReadOrError < iovec.length) {
            break
        }
    }

    return totalBytesRead.right()
}

// TODO: test
@Suppress("LoopWithTooManyJumpStatements")
private fun HANDLE.readDoNotChangePosition(
    offset: Long,
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> = memScoped {
    var totalBytesRead = 0UL
    val overlapped: OVERLAPPED = alloc<OVERLAPPED>()
    val bytesRead: DWORDVar = alloc()
    var currentOffset: ULong = offset.toULong()
    for (iovec in iovecs) {
        memset(overlapped.ptr, 0, sizeOf<OVERLAPPED>().toULong())
        overlapped.Offset = (currentOffset and 0xff_ff_ff_ffUL).toUInt()
        overlapped.OffsetHigh = (currentOffset shr 32 and 0xff_ff_ff_ffUL).toUInt()

        val readResult = iovec.array.usePinned { pinnedBuffer ->
            val resultRaw = ReadFile(
                this@readDoNotChangePosition,
                pinnedBuffer.addressOf(iovec.offset),
                iovec.length.toUInt(),
                bytesRead.ptr,
                overlapped.ptr,
            )
            if (resultRaw != 0) {
                if (GetOverlappedResult(this@readDoNotChangePosition, overlapped.ptr, bytesRead.ptr, 0) == 0) {
                    error("Failed to get overlapped result")
                }
                ReadFileResult.ReadSuccess(bytesRead.value.toULong())
            } else {
                val lastError = Win32ErrorCode.getLast()
                if (lastError.code.toInt() == ERROR_HANDLE_EOF) {
                    ReadFileResult.EndOfFile
                } else {
                    ReadFileResult.ReadError(lastError)
                }
            }
        }
        when (readResult) {
            ReadFileResult.EndOfFile -> break
            is ReadFileResult.ReadSuccess -> {
                totalBytesRead += readResult.readBytes
                if (readResult.readBytes < iovec.length.toULong()) {
                    break
                }
                currentOffset += readResult.readBytes
            }

            is ReadFileResult.ReadError -> return readResult.error.toReadError().left()
        }
    }

    return totalBytesRead.right()
}

private fun Win32ErrorCode.toReadError(): ReadError = when (this.code.toInt()) {
    ERROR_IO_PENDING -> error("Should be handled earlier")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file hande")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Invalid argument in request")
    ERROR_NOT_ENOUGH_QUOTA -> Nxio("Memory quota exceeded")
    ERROR_OPERATION_ABORTED -> Interrupted("Read operation interrupted")
    ERROR_INSUFFICIENT_BUFFER -> Nxio("Buffer too small to read mailslot")
    else -> IoError("Read error. Errno: `$this`")
}

@Suppress("ConvertObjectToDataObject")
private sealed class ReadFileResult {
    object EndOfFile : ReadFileResult()
    class ReadSuccess(val readBytes: ULong) : ReadFileResult()
    class ReadError(val error: Win32ErrorCode) : ReadFileResult()
}
