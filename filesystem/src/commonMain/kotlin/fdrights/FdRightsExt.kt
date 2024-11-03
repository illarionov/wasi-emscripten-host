/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdrights

import at.released.weh.filesystem.fdrights.FdRightsFlag.DIRECTORY_BASE_RIGHTS
import at.released.weh.filesystem.fdrights.FdRightsFlag.DIRECTORY_INHERITING_RIGHTS
import at.released.weh.filesystem.fdrights.FdRightsFlag.FILE_BASE_RIGHTS

internal fun FdRightsBlock.getChildFileRights(
    requestedRights: FdRightsBlock? = null,
): FdRightsBlock {
    val childRequest = requestedRights ?: FdRightsBlock.FILE_BASE_RIGHTS_BLOCK
    return FdRightsBlock(
        rights = this.rightsInheriting and childRequest.rights and FILE_BASE_RIGHTS,
        rightsInheriting = this.rightsInheriting and childRequest.rightsInheriting and FILE_BASE_RIGHTS,
    )
}

internal fun FdRightsBlock.getChildDirectoryRights(
    requestedRights: FdRightsBlock? = null,
): FdRightsBlock {
    val childRequest = requestedRights ?: FdRightsBlock.DIRECTORY_BASE_RIGHTS_BLOCK
    return FdRightsBlock(
        rights = rightsInheriting and childRequest.rights and DIRECTORY_BASE_RIGHTS,
        rightsInheriting = rightsInheriting and childRequest.rightsInheriting and DIRECTORY_INHERITING_RIGHTS,
    )
}
