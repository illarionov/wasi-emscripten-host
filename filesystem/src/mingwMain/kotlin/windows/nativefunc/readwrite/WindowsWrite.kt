/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.readwrite

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoBufferSpace
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.Overflow
import at.released.weh.filesystem.error.Pipe
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.model.Whence.END
import at.released.weh.filesystem.model.Whence.SET
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CurrentPosition
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.Position
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.getFilePointer
import at.released.weh.filesystem.windows.win32api.setFilePointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.posix.memset
import platform.windows.DWORDVar
import platform.windows.ERROR_INSUFFICIENT_BUFFER
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.ERROR_INVALID_USER_BUFFER
import platform.windows.ERROR_IO_PENDING
import platform.windows.ERROR_NOT_ENOUGH_MEMORY
import platform.windows.ERROR_NOT_ENOUGH_QUOTA
import platform.windows.ERROR_OPERATION_ABORTED
import platform.windows.GetOverlappedResult
import platform.windows.HANDLE
import platform.windows.OVERLAPPED
import platform.windows.WriteFile

internal fun HANDLE.write(
    iovecs: List<FileSystemByteBuffer>,
    strategy: ReadWriteStrategy,
    writeInAppendMode: Boolean,
): Either<WriteError, ULong> {
    val nonEmptyIovecs = iovecs.ifEmpty { SINGLE_EMPTY_IOVEC }
    return when (strategy) {
        CurrentPosition -> if (writeInAppendMode) {
            writeDoNotChangePosition(0xff_ff_ff_ff_ff_ff_ff_ffU, nonEmptyIovecs)
                .flatMap { bytesWritten ->
                    setFilePointer(0, END)
                        .mapLeft(SeekError::toWriteError)
                        .map { bytesWritten }
                }
        } else {
            writeChangePosition(nonEmptyIovecs)
        }

        is Position -> writeDoNotChangePosition(strategy.position.toULong(), nonEmptyIovecs)
    }
}

internal fun HANDLE.writeChangePosition(cIovecs: List<FileSystemByteBuffer>): Either<WriteError, ULong> = memScoped {
    var totalBytesWritten = 0UL
    val bytesWritten: DWORDVar = alloc()
    for (ciovec in cIovecs) {
        val bytesWrittenOrError = ciovec.array.usePinned { pinnedBuffer ->
            val address = if (ciovec.array.isNotEmpty()) {
                pinnedBuffer.addressOf(ciovec.offset)
            } else {
                null
            }

            val result = WriteFile(
                this@writeChangePosition,
                address,
                ciovec.length.toUInt(),
                bytesWritten.ptr,
                null,
            )
            if (result != 0) {
                bytesWritten.value.toLong()
            } else {
                -1L
            }
        }
        if (bytesWrittenOrError < 0L) {
            return Win32ErrorCode.getLast().toWriteError().left()
        }
        totalBytesWritten += bytesWrittenOrError.toULong()
        if (bytesWrittenOrError < ciovec.length) {
            break
        }
    }

    return totalBytesWritten.right()
}

internal fun HANDLE.writeDoNotChangePosition(
    offset: ULong,
    iovecs: List<FileSystemByteBuffer>,
): Either<WriteError, ULong> {
    val initialPosition = getFilePointer()
        .mapLeft(SeekError::toWriteError)
        .getOrElse { return it.left() }

    val totalBytesWritten = memScoped {
        var totalBytesWritten = 0UL
        val overlapped: OVERLAPPED = alloc<OVERLAPPED>()
        val bytesWritten: DWORDVar = alloc()
        var currentOffset: ULong = offset
        for (ciovec in iovecs) {
            memset(overlapped.ptr, 0, sizeOf<OVERLAPPED>().toULong())
            overlapped.Offset = (currentOffset and 0xff_ff_ff_ffUL).toUInt()
            overlapped.OffsetHigh = (currentOffset shr 32 and 0xff_ff_ff_ffUL).toUInt()

            val bytesWrittenOrError = ciovec.array.usePinned { pinnedBuffer ->
                val address = if (ciovec.array.isNotEmpty()) {
                    pinnedBuffer.addressOf(ciovec.offset)
                } else {
                    null
                }

                val resultRaw = WriteFile(
                    this@writeDoNotChangePosition,
                    address,
                    ciovec.length.toUInt(),
                    null,
                    overlapped.ptr,
                )
                if (resultRaw != 0) {
                    if (GetOverlappedResult(this@writeDoNotChangePosition, overlapped.ptr, bytesWritten.ptr, 0) == 0) {
                        error("Failed to get overlapped result")
                    }
                    bytesWritten.value.toLong()
                } else {
                    -1L
                }
            }
            if (bytesWrittenOrError < 0L) {
                return Win32ErrorCode.getLast().toWriteError().left()
            }
            totalBytesWritten += bytesWrittenOrError.toULong()
            currentOffset += bytesWrittenOrError.toULong()
            if (bytesWrittenOrError < ciovec.length) {
                break
            }
        }

        totalBytesWritten
    }
    return setFilePointer(initialPosition, SET)
        .mapLeft(SeekError::toWriteError)
        .map { totalBytesWritten }
}

private fun Win32ErrorCode.toWriteError(): WriteError = when (this.code.toInt()) {
    // TODO
    ERROR_IO_PENDING -> error("Should be handled earlier")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file hande")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Invalid argument in request")
    ERROR_NOT_ENOUGH_QUOTA -> DiskQuota("Memory quota exceeded")
    ERROR_OPERATION_ABORTED -> Interrupted("Read operation interrupted")
    ERROR_INSUFFICIENT_BUFFER -> NoBufferSpace("Buffer too small to read mailslot")
    ERROR_INVALID_USER_BUFFER -> NoBufferSpace("To many asynchronous requests")
    ERROR_NOT_ENOUGH_MEMORY -> NoBufferSpace("No memory")
    else -> IoError("Read error. Errno: `$this`")
}

private fun SeekError.toWriteError(): WriteError = when (this) {
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is Nxio -> this
    is Overflow -> InvalidArgument(this.message)
    is Pipe -> this
}
