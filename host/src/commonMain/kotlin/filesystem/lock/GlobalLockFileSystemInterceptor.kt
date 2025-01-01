/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.lock

import arrow.core.Either
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.FileSystemInterceptor.Chain
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.op.poll.Poll
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/**
 * A file system interceptor that serializes all operations to file system using a single lock.
 * For use as a temporary workaround until synchronization is correctly implemented.
 */
public class GlobalLockFileSystemInterceptor : FileSystemInterceptor {
    private val lock = ReentrantLock()

    override fun <I : Any, E : FileSystemOperationError, R : Any> intercept(
        chain: Chain<I, E, R>,
    ): Either<E, R> {
        return when {
            // Poll operation is excluded from locking as it may run for an indefinitely long time
            chain.operation == Poll -> chain.proceed(chain.input)
            else -> lock.withLock {
                chain.proceed(chain.input)
            }
        }
    }
}
