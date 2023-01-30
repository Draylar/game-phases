package dev.draylar.gamephases.mixin.event.attack;

import dev.architectury.event.EventResult;
import dev.draylar.gamephases.arch.AttackEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"), cancellable = true)
    private void gamePhases$onClientAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        EventResult result = AttackEvent.ATTACK_ENTITY.invoker().attack(
                player,
                player.world,
                Hand.MAIN_HAND,
                target,
                null
        );

        if(result.isFalse()) {
            ci.cancel();
        }
    }
}
