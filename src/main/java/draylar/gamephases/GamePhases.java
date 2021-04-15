package draylar.gamephases;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import draylar.gamephases.cca.PhaseComponent;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class GamePhases implements ModInitializer, EntityComponentInitializer {

    public static final ComponentKey<PhaseComponent> PHASES = ComponentRegistryV3.INSTANCE.getOrCreate(id("phases"), PhaseComponent.class);

    @Override
    public void onInitialize() {

    }

    public static Identifier id(String name) {
        return new Identifier("gamephases", name);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, PHASES, PhaseComponent::new);
    }
}
