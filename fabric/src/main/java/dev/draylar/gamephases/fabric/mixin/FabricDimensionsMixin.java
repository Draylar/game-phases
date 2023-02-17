package dev.draylar.gamephases.fabric.mixin;

import dev.draylar.gamephases.kube.GamePhasesEventJS;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FabricDimensions.class)
public class FabricDimensionsMixin {

    @Inject(
            method = "teleport",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/dimension/FabricDimensionInternals;changeDimension(Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/entity/Entity;"),
            cancellable = true)
    private static void beforeTeleport(Entity teleported, ServerWorld destination, TeleportTarget target, CallbackInfoReturnable<@Nullable Entity> cir) {
        boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(destination)).allMatch(phase -> phase.hasUnlocked(MinecraftClient.getInstance().player));
        if(!allowed) {
            cir.cancel();
        }
    }
}
