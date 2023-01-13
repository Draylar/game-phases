package draylar.gamephases.impl;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Map;

public interface PlayerDataProvider {

    @ApiStatus.Internal
    @Deprecated
    boolean phases$has(String phase);

    @ApiStatus.Internal
    @Deprecated
    void phases$set(String phase, boolean status);

    @ApiStatus.Internal
    @Deprecated
    void phases$set(Map<String, Boolean> all);

    @ApiStatus.Internal
    @Deprecated
    void phases$sync();

    @ApiStatus.Internal
    @Deprecated
    Map<String, Boolean> phase$getUnlocked();

    @ApiStatus.Internal
    @Deprecated
    void phase$copyFrom(PlayerDataProvider data);
}
