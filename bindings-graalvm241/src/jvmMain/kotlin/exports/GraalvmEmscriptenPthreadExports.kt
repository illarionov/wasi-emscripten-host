/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.exports

import at.released.weh.bindings.graalvm241.ext.functionMember
import at.released.weh.bindings.graalvm241.ext.intGlobalMember
import at.released.weh.bindings.graalvm241.ext.optionalFunctionMember
import at.released.weh.host.emscripten.export.pthread.EmscriptenPthreadExports
import org.graalvm.polyglot.Value

internal class GraalvmEmscriptenPthreadExports(
    mainBindings: () -> Value,
) : EmscriptenPthreadExports {
    override var __tls_base: Int by mainBindings.intGlobalMember()
    override val __tls_size: Int by mainBindings.intGlobalMember()
    override val __tls_align: Int by mainBindings.intGlobalMember()
    override var g_needs_dynamic_alloc: Int by mainBindings.intGlobalMember()
    override var thread_ptr: Int by mainBindings.intGlobalMember()
    override var is_main_thread: Int by mainBindings.intGlobalMember()
    override var is_runtime_thread: Int by mainBindings.intGlobalMember()
    override var supports_wait: Int by mainBindings.intGlobalMember()
    override val _emscripten_check_mailbox by mainBindings.functionMember()
    override val _emscripten_run_on_main_thread_js by mainBindings.functionMember()
    override val _emscripten_thread_crashed by mainBindings.functionMember()
    override val _emscripten_thread_exit by mainBindings.functionMember()
    override val _emscripten_thread_free_data by mainBindings.functionMember()
    override val _emscripten_thread_init by mainBindings.functionMember()
    override val _emscripten_tls_init by mainBindings.functionMember()
    override val emscripten_main_runtime_thread_id by mainBindings.functionMember()
    override val emscripten_main_thread_process_queued_calls by mainBindings.functionMember()
    override val pthread_self by mainBindings.functionMember()
    override val pthread_create by mainBindings.optionalFunctionMember()
    override val pthread_exit by mainBindings.optionalFunctionMember()
}
