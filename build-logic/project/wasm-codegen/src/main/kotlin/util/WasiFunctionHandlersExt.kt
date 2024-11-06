/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.util

internal object WasiFunctionHandlersExt {
    internal val WASI_MEMORY_READER_FUNCTIONS = setOf("fd_read", "fd_pread")
    internal val WASI_MEMORY_WRITER_FUNCTIONS = setOf("fd_write", "fd_pwrite")
    internal val NO_MEMORY_FUNCTIONS = setOf(
        "fd_advise",
        "fd_allocate",
        "fd_close",
        "fd_datasync",
        "fd_fdstat_set_flags",
        "fd_filestat_set_size",
        "fd_filestat_set_times",
        "fd_renumber",
        "fd_sync",
        "proc_exit",
        "sched_yield",
    )
}
