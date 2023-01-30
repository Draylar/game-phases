package dev.draylar.gamephases.mixin.data;

import dev.draylar.gamephases.GamePhases;
import dev.draylar.gamephases.impl.PlayerDataProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityDataMixin {

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void gamePhases$copyPhaseData(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        PlayerDataProvider data = GamePhases.getPhaseData(oldPlayer);
        GamePhases.getPhaseData((PlayerEntity) (Object) this).phase$copyFrom(data);
    }
}
