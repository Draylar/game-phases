package dev.draylar.gamephases.mixin.client;

import dev.draylar.gamephases.GamePhasesClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void gamePhases$clientJoinCallback(GameJoinS2CPacket packet, CallbackInfo ci) {
        GamePhasesClient.loadCachedPhaseData();
    }
}
