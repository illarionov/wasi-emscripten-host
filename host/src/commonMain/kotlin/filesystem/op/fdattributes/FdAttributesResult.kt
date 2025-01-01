/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.fdattributes

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.fdrights.FdRights
import at.released.weh.filesystem.fdrights.FdRightsType
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.model.Filetype

/**
 * File descriptor attributes.
 *
 * @param type File type.
 * @param flags File descriptor flags.
 * @param rights Rights that apply to this file descriptor.
 * @param inheritingRights Maximum set of rights that may be installed on new file descriptors
 * that are created through this file descriptor, e.g., through `path_open`.
 */
@WasiEmscriptenHostDataModel
public class FdAttributesResult(
    public val type: Filetype,
    @FdflagsType public val flags: Fdflags,
    @FdRightsType public val rights: FdRights,
    @FdRightsType public val inheritingRights: FdRights,
)
