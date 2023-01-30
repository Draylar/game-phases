package dev.draylar.gamephases.fabric;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.draylar.gamephases.GamePhasesClient;
import dev.draylar.gamephases.GamePhases;
import net.fabricmc.api.ModInitializer;

public class GamePhasesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        GamePhases.initialize();
        if(Platform.getEnvironment() == Env.CLIENT) {
            GamePhasesClient.initialize();
        }
    }
}
