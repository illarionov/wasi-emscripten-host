/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.lock

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.model.Whence

@WasiEmscriptenHostDataModel
public class Advisorylock(
    public val type: AdvisorylockLockType,
    public val whence: Whence,
    public val start: Long,
    public val length: Long,
)
