package draylar.gamephases.mixin;

import draylar.gamephases.GamePhases;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class PlayerEntityMixin {

    public boolean hasPhase(String phase) {
        return GamePhases.PHASES.get(this).has(phase);
    }
}
