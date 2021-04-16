package draylar.gamephases.mixin.block;

import dev.hephaestus.fiblib.api.BlockFibRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Shadow public static List<ItemStack> getDroppedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack) { return null; }
    @Shadow public static void dropStack(World world, BlockPos pos, ItemStack stack) { }

    @Inject(
            method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void beforeDrop(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfo ci) {
        if (world instanceof ServerWorld && entity instanceof ServerPlayerEntity) {
            BlockState newState = BlockFibRegistry.getBlockState(state, (ServerPlayerEntity) entity);

            // If the states do not match (currently being fibbed), cancel the original drop
            if(newState != state) {
                getDroppedStacks(newState, (ServerWorld) world, pos, blockEntity, entity, stack).forEach((itemStack) -> dropStack(world, pos, itemStack));
                newState.onStacksDropped((ServerWorld) world, pos, stack);
                ci.cancel();
            }
        }
    }
}
