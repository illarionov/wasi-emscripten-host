/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdrights

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.fdrights.FdRightsFlag.DIRECTORY_BASE_RIGHTS
import at.released.weh.filesystem.fdrights.FdRightsFlag.DIRECTORY_INHERITING_RIGHTS
import at.released.weh.filesystem.fdrights.FdRightsFlag.FILE_BASE_RIGHTS

@WasiEmscriptenHostDataModel
public class FdRightsBlock(
    @FdRightsType
    public val rights: FdRights,

    @FdRightsType
    public val rightsInheriting: FdRights,
) {
    override fun toString(): String {
        return "FdRightsBlock(rights=0x${rights.toString(16)}, " +
                "rightsInheriting=0x${rightsInheriting.toString(16)})"
    }

    internal companion object {
        val FILE_BASE_RIGHTS_BLOCK = FdRightsBlock(
            FILE_BASE_RIGHTS,
            FILE_BASE_RIGHTS,
        )
        val DIRECTORY_BASE_RIGHTS_BLOCK = FdRightsBlock(
            DIRECTORY_BASE_RIGHTS,
            DIRECTORY_INHERITING_RIGHTS,
        )
    }
}
