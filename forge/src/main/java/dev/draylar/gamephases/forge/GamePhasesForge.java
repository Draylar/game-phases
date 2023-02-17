package dev.draylar.gamephases.forge;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.draylar.gamephases.GamePhasesClient;
import dev.draylar.gamephases.GamePhases;
import dev.draylar.gamephases.kube.GamePhasesEventJS;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("gamephases")
public class GamePhasesForge {

    public GamePhasesForge() {
        GamePhases.initialize();
        MinecraftForge.EVENT_BUS.register(this);
        if(Platform.getEnvironment() == Env.CLIENT) {
            GamePhasesClient.initialize();
        }
    }

    @SubscribeEvent
    public void teleport(EntityTravelToDimensionEvent event) {
        if(event.getEntity() instanceof PlayerEntity player) {
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(event.getDimension())).allMatch(phase -> phase.hasUnlocked(player));
            if(!allowed) {
                event.setCanceled(true);
            }
        }
    }
}
