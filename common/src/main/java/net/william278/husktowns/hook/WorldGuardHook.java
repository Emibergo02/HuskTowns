/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.husktowns.hook;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.william278.husktowns.HuskTowns;
import net.william278.husktowns.claim.Chunk;
import org.jetbrains.annotations.NotNull;


public abstract class WorldGuardHook extends Hook {
    public final static StateFlag CLAIMING = new StateFlag("husktowns-claim", false);
    public WorldGuardHook(@NotNull HuskTowns plugin) {
        super(plugin, "WorldGuard");
        enable();
    }

    @Override
    protected void onEnable() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        registry.register(CLAIMING);
    }

    public abstract boolean isChunkInRestrictedRegion(@NotNull Chunk chunk, @NotNull String worldName);
}
