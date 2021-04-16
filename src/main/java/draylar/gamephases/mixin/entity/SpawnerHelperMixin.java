package draylar.gamephases.mixin.entity;

import draylar.gamephases.kube.GamePhasesEventJS;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnHelper.class)
public class SpawnerHelperMixin {

    @Unique
    private static ServerWorld cachedWorld;

    @Inject(
            method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
            at = @At("HEAD"))
    private static void storeContext(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker, SpawnHelper.Runner runner, CallbackInfo ci) {
        cachedWorld = world;
    }

    @Redirect(method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/SpawnHelper$Checker;test(Lnet/minecraft/entity/EntityType;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/Chunk;)Z"))
    private static boolean testPhaseValidity(SpawnHelper.Checker checker, EntityType<?> type, BlockPos pos, Chunk chunk) {
        // IF the entity has any restrictions:
        // Test the distance of all players in the world to the mob that is spawning.
        // At least one player has to have the appropriate stage and be within the defined radius of the entity spawn for it to work.
        boolean allowed = GamePhasesEventJS.getPhases()
                .values()
                .stream()
                .filter(phase -> phase.disallows(type))
                .allMatch(phase -> {
                    return cachedWorld.getPlayers(player -> Math.sqrt(player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ())) < phase.getRadius(type))
                            .stream()
                            .anyMatch(player1 -> phase.hasUnlocked(player1));
                });

        if(!allowed) {
            System.out.println("Blocked: " + Registry.ENTITY_TYPE.getId(type));
        }

        return checker.test(type, pos, chunk) && allowed;
    }
}
