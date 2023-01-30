package dev.draylar.gamephases.mixin.event.attack;

import dev.architectury.event.EventResult;
import dev.draylar.gamephases.arch.AttackEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow public abstract ServerWorld getWorld();

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void gamePhases$onServerAttack(Entity target, CallbackInfo ci) {
        EventResult result = AttackEvent.ATTACK_ENTITY.invoker().attack(
                (PlayerEntity) (Object) this,
                getWorld(),
                Hand.MAIN_HAND,
                target,
                null
        );

        if(result.isFalse()) {
            ci.cancel();
        }
    }
}
