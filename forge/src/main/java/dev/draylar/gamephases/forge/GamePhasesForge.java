package dev.draylar.gamephases.forge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.draylar.gamephases.GamePhasesClient;
import dev.draylar.gamephases.GamePhases;
import net.minecraftforge.fml.common.Mod;

@Mod("gamephases")
public class GamePhasesForge {

    public GamePhasesForge() {
        GamePhases.initialize();
        if(Platform.getEnvironment() == Env.CLIENT) {
            GamePhasesClient.initialize();
        }
    }
}
