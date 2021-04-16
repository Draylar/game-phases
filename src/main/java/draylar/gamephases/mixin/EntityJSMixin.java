package draylar.gamephases.mixin;

import dev.latvian.kubejs.entity.EntityJS;
import draylar.gamephases.GamePhases;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityJS.class)
public class EntityJSMixin {

    @Shadow @Final public Entity minecraftEntity;

    public boolean hasPhase(String phase) {
        return GamePhases.PHASES.get(minecraftEntity).has(phase);
    }
}
