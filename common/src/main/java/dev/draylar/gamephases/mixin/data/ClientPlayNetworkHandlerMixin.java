package dev.draylar.gamephases.mixin.data;

import dev.draylar.gamephases.GamePhases;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onPlayerRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setId(I)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void gamePhases$copyClientPhaseData(PlayerRespawnS2CPacket packet, CallbackInfo ci, RegistryKey registryKey, RegistryEntry registryEntry, ClientPlayerEntity oldPlayer, int i, String string, ClientPlayerEntity newPlayer) {
        GamePhases.getPhaseData(newPlayer).phase$copyFrom(GamePhases.getPhaseData(oldPlayer));
    }
}
