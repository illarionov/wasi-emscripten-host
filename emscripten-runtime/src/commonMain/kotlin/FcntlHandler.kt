/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.emcripten.runtime.ext.negativeErrnoCode
import at.released.weh.emcripten.runtime.include.Fcntl
import at.released.weh.emcripten.runtime.include.StructFlock
import at.released.weh.emcripten.runtime.include.StructFlock.Companion.STRUCT_FLOCK_SIZE
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.op.lock.AddAdvisoryLockFd
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.lock.AdvisorylockLockType
import at.released.weh.filesystem.op.lock.RemoveAdvisoryLockFd
import at.released.weh.wasi.filesystem.common.Errno.INVAL
import at.released.weh.wasi.filesystem.common.Fd
import at.released.weh.wasi.filesystem.common.Whence
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readPtr
import at.released.weh.wasm.core.memory.sourceWithMaxSize
import kotlinx.io.buffered

internal class FcntlHandler(
    private val fileSystem: FileSystem,
) {
    private val handlers: Map<UInt, FcntlOperationHandler> = mapOf(
        Fcntl.F_SETLK to FcntlSetLockOperation(),
    )

    fun invoke(
        memory: ReadOnlyMemory,
        @Fd fd: Int,
        operation: UInt,
        thirdArg: Int?,
    ): Int {
        val handler = handlers[operation] ?: return -INVAL.code
        return handler.invoke(memory, fd, thirdArg)
    }

    internal fun interface FcntlOperationHandler {
        fun invoke(
            memory: ReadOnlyMemory,
            @Fd fd: Int,
            varArgs: Int?,
        ): Int
    }

    @Suppress("OBJECT_IS_PREFERRED")
    internal inner class FcntlSetLockOperation : FcntlOperationHandler {
        override fun invoke(
            memory: ReadOnlyMemory,
            @Fd fd: Int,
            varArgs: Int?,
        ): Int {
            @IntWasmPtr(StructFlock::class)
            val structStatPtr: WasmPtr = memory.readPtr(checkNotNull(varArgs))
            val flock = memory.sourceWithMaxSize(structStatPtr, STRUCT_FLOCK_SIZE).buffered().use {
                StructFlock.unpack(it)
            }
            val advisoryLock = flock.toAdvisoryLock().getOrElse {
                return -it.errno.code
            }
            return when (flock.l_type) {
                Fcntl.F_RDLCK, Fcntl.F_WRLCK -> fileSystem.execute(
                    AddAdvisoryLockFd,
                    AddAdvisoryLockFd(fd, advisoryLock),
                ).negativeErrnoCode()

                Fcntl.F_UNLCK -> fileSystem.execute(
                    RemoveAdvisoryLockFd,
                    RemoveAdvisoryLockFd(fd, advisoryLock),
                ).negativeErrnoCode()

                else -> -INVAL.code
            }
        }
    }

    private companion object {
        fun StructFlock.toAdvisoryLock(): Either<InvalidArgument, Advisorylock> {
            val type = when (this.l_type) {
                Fcntl.F_RDLCK -> AdvisorylockLockType.READ
                Fcntl.F_WRLCK -> AdvisorylockLockType.WRITE
                Fcntl.F_UNLCK -> AdvisorylockLockType.WRITE
                else -> return InvalidArgument("Incorrect l_type").left()
            }
            val whence = Whence.fromIdOrNull(this.l_whence.toInt())
                ?: return InvalidArgument("Incorrect whence `${this.l_whence}`").left()

            return Advisorylock(
                type = type,
                whence = whence,
                start = this.l_start,
                length = this.l_len,
            ).right()
        }
    }
}
