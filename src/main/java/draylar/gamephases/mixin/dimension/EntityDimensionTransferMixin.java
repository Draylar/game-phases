package draylar.gamephases.mixin.dimension;

import draylar.gamephases.kube.GamePhasesEventJS;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class EntityDimensionTransferMixin {

    @Inject(
            method = "moveToWorld",
            remap = false,
            at = @At(value = "HEAD"),
            cancellable = true)
    private void beforeTeleport(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(destination)).allMatch(phase -> phase.hasUnlocked((PlayerEntity) (Object) this));
        if(!allowed) {
            cir.cancel();
        }
    }
}
