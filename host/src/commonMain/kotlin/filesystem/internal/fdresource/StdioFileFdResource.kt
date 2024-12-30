/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.fdresource

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.FadviseError
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NonblockingPollError
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.error.SetFdFlagsError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.SyncError
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.fdrights.FdRights
import at.released.weh.filesystem.fdrights.FdRightsFlag.FD_DATASYNC
import at.released.weh.filesystem.fdrights.FdRightsFlag.FD_READ
import at.released.weh.filesystem.fdrights.FdRightsFlag.FD_SYNC
import at.released.weh.filesystem.fdrights.FdRightsFlag.FD_WRITE
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_STDERR_FD
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_STDIN_FD
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_STDOUT_FD
import at.released.weh.filesystem.internal.fdresource.stdio.StdioReadWriteError
import at.released.weh.filesystem.internal.fdresource.stdio.StdioReadWriteError.Closed
import at.released.weh.filesystem.internal.fdresource.stdio.flushNoThrow
import at.released.weh.filesystem.internal.fdresource.stdio.transferFrom
import at.released.weh.filesystem.internal.fdresource.stdio.transferTo
import at.released.weh.filesystem.model.FdFlag
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.Filetype.CHARACTER_DEVICE
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.fadvise.Advice
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.poll.Event.FileDescriptorEvent
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.stdio.ExhaustedRawSource
import at.released.weh.filesystem.stdio.SinkProvider
import at.released.weh.filesystem.stdio.StandardInputOutput
import at.released.weh.filesystem.stdio.StdioSource
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.io.IOException
import kotlinx.io.RawSink
import kotlinx.io.RawSource

internal class StdioFileFdResource(
    val sourceProvider: StdioSource.Provider,
    val sinkProvider: SinkProvider,
) : FdResource {
    private val writeLock: ReentrantLock = ReentrantLock()
    private val readLock: ReentrantLock = ReentrantLock()
    private var isOpen: Boolean = true
    private var source: RawSource? = null
    private var sink: RawSink? = null

    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> {
        return FdAttributesResult(
            type = CHARACTER_DEVICE,
            flags = FdFlag.FD_APPEND,
            rights = STDIO_FD_RIGHTS,
            inheritingRights = 0,
        ).right()
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> {
        val sink = writeLock.withLock {
            getOrOpenSinkUnsafe()
        }
        return sink
            .flatMap { rawSync -> rawSync.flushNoThrow() }
            .mapLeft { err: StdioReadWriteError ->
                when (err) {
                    is StdioReadWriteError.Closed -> BadFileDescriptor(err.message)
                    is StdioReadWriteError.IoError -> IoError(err.message)
                }
            }
    }

    override fun fadvise(offset: Long, length: Long, advice: Advice): Either<FadviseError, Unit> {
        return BadFileDescriptor("Can not ser advice on stdio descriptor").left()
    }

    override fun fallocate(offset: Long, length: Long): Either<FallocateError, Unit> {
        return BadFileDescriptor("Can not fallocate on stdio descriptor").left()
    }

    override fun truncate(length: Long): Either<TruncateError, Unit> {
        return BadFileDescriptor("Can not truncate stdio descriptor").left()
    }

    override fun chmod(mode: Int): Either<ChmodError, Unit> {
        return NotSupported("Can not change mode of the stdio descriptor").left()
    }

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> {
        return NotSupported("Can not change owner/group of the stdio descriptor").left()
    }

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        return BadFileDescriptor("Can not change timestamp of the stdio descriptor").left()
    }

    override fun setFdFlags(flags: Fdflags): Either<SetFdFlagsError, Unit> {
        return BadFileDescriptor("Can not change flags of the stdio descriptor").left()
    }

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return NotSupported("Can not lock on stdio descriptor").left()
    }

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return NotSupported("Can not lock on stdio descriptor").left()
    }

    override fun stat(): Either<StatError, StructStat> {
        return InvalidArgument("Can not stat on stdio descriptor").left()
    }

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        return BadFileDescriptor("Can not seek on stdio descriptor").left()
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        val source = readLock.withLock { getOrOpenSourceUnsafe() }

        return source
            .mapLeft { error ->
                when (error) {
                    is StdioReadWriteError.Closed -> BadFileDescriptor(error.message)
                    is StdioReadWriteError.IoError -> IoError(error.message)
                }
            }.flatMap {
                it.transferTo(iovecs)
            }
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        val sink = writeLock.withLock { getOrOpenSinkUnsafe() }
        return sink
            .mapLeft { error ->
                when (error) {
                    is StdioReadWriteError.Closed -> BadFileDescriptor(error.message)
                    is StdioReadWriteError.IoError -> IoError(error.message)
                }
            }.flatMap {
                it.transferFrom(cIovecs)
            }
    }

    private fun getOrOpenSinkUnsafe(): Either<StdioReadWriteError, RawSink> {
        if (!isOpen) {
            return Closed("Stdio file descriptor is closed").left()
        }

        this.sink?.let {
            return it.right()
        }

        return try {
            sinkProvider.open().right()
        } catch (ioe: IOException) {
            StdioReadWriteError.IoError("Can not open sync: ${ioe.message}").left()
        }.onRight {
            this.sink = it
        }
    }

    private fun getOrOpenSourceUnsafe(): Either<StdioReadWriteError, RawSource> {
        if (!isOpen) {
            return Closed("Stdio file descriptor is closed").left()
        }

        this.source?.let {
            return it.right()
        }

        return try {
            sourceProvider.open().right()
        } catch (ioe: IOException) {
            StdioReadWriteError.IoError("Can not open source: ${ioe.message}").left()
        }.onRight {
            this.source = it
        }
    }

    override fun close(): Either<CloseError, Unit> {
        val activeSources = writeLock.withLock {
            readLock.withLock {
                if (!isOpen) {
                    return Unit.right()
                }
                isOpen = false
                val activeSources = Pair(source, sink)
                source = null
                sink = null
                activeSources
            }
        }

        val sourceCloseStatus: Either<Throwable, Unit> = Either.catch {
            activeSources.first?.close()
        }
        val sinkCloseStatus: Either<Throwable, Unit> = Either.catch {
            activeSources.second?.close()
        }
        return Either
            .zipOrAccumulate(sourceCloseStatus, sinkCloseStatus, { _, _ -> })
            .mapLeft { errors: NonEmptyList<Throwable> ->
                IoError("Can not close source or sink. Errors: ${errors.map { it.message }}")
            }
    }

    override fun pollNonblocking(
        subscription: FileDescriptorSubscription,
    ): Either<NonblockingPollError, FileDescriptorEvent> {
        // TODO
        return FileDescriptorEvent(
            errno = FileSystemErrno.SUCCESS,
            userdata = subscription.userdata,
            fileDescriptor = subscription.fileDescriptor,
            type = subscription.type,
            bytesAvailable = 0,
            isHangup = false,
        ).right()
    }

    companion object {
        const val STDIO_FD_RIGHTS: FdRights = FD_DATASYNC or FD_READ or FD_SYNC or FD_WRITE

        fun StandardInputOutput.toFileDescriptorMap(): Map<FileDescriptor, StdioFileFdResource> {
            val stdInStdOut = StdioFileFdResource(
                sourceProvider = this.stdinProvider,
                sinkProvider = this.stdoutProvider,
            )
            val stdErr = StdioFileFdResource(
                sourceProvider = StdioSource.Provider(::ExhaustedRawSource),
                sinkProvider = this.stderrProvider,
            )
            return mapOf(
                WASI_STDIN_FD to stdInStdOut,
                WASI_STDOUT_FD to stdInStdOut,
                WASI_STDERR_FD to stdErr,
            )
        }
    }
}
