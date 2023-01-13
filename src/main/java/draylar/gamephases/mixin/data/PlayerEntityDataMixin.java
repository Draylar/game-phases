package draylar.gamephases.mixin.data;

import draylar.gamephases.impl.PlayerDataProvider;
import draylar.gamephases.network.ServerNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityDataMixin extends LivingEntity implements PlayerDataProvider {

    @Unique final String PHASES_KEY = "Phases";
    @Unique private final Map<String, Boolean> phases = new HashMap<>();

    protected PlayerEntityDataMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * @param phase phase to check for unlock status
     * @return {@code true} if the {@link net.minecraft.entity.player.PlayerEntity} this component is associated with has unlocked the given phase.
     */
    @Unique
    @Override
    public boolean phases$has(String phase) {
        return phases.getOrDefault(phase, false);
    }

    /**
     * Configures the status of the specified phase for the {@link net.minecraft.entity.player.PlayerEntity} this phase tracker is associated with.
     *
     * @param phase  phase to configure
     * @param status new status of the phase
     */
    @Unique
    @Override
    public void phases$set(String phase, boolean status) {
        phases.put(phase, status);
        phases$sync();
    }

    @Unique
    @Override
    public void phases$set(Map<String, Boolean> all) {
        phases.clear();
        phases.putAll(all);
    }

    @Unique
    @Override
    public void phases$sync() {
        // Sync S2C if we are on the server.
        if(!world.isClient) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            ServerNetworking.sendPhaseSync(player, phases);
        }
    }

    @Unique
    @Override
    public Map<String, Boolean> phase$getUnlocked() {
        return phases;
    }

    @Unique
    @Override
    public void phase$copyFrom(PlayerDataProvider data) {
        this.phases.clear();
        this.phases.putAll(data.phase$getUnlocked());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList list = new NbtList();

        phases.forEach((phase, unlocked) -> {
            NbtCompound c = new NbtCompound();
            c.putString("Phase", phase);
            c.putBoolean("Unlocked", unlocked);
            list.add(c);
        });

        nbt.put(PHASES_KEY, list);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void readNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList list = nbt.getList(PHASES_KEY, NbtType.COMPOUND);
        phases.clear();

        list.forEach(element -> {
            NbtCompound c = (NbtCompound) element;
            phases.put(c.getString("Phase"), c.getBoolean("Unlocked"));
        });
    }
}
