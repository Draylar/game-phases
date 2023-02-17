package dev.draylar.gamephases.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.draylar.gamephases.GamePhasesClient;
import dev.draylar.gamephases.api.Phase;
import dev.draylar.gamephases.compat.REICompat;
import dev.draylar.gamephases.impl.PlayerDataProvider;
import dev.draylar.gamephases.kube.GamePhasesEventJS;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ClientNetworking {

    public static void initialize() {
        registerPhaseSyncHandler();
        registerAllPhaseSyncHandler();
    }

    private static void registerPhaseSyncHandler() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ServerNetworking.UNLOCKED_PHASE_SYNC, (buf, context) -> {
            int elements = buf.readInt();
            Map<String, Boolean> phases = new HashMap<>();
            for (int i = 0; i < elements; i++) {
                phases.put(buf.readString(), buf.readBoolean());
            }

            // Sync phases to client player object.
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                if(client.player == null) {
                    GamePhasesClient.cachedPhaseData = phases;
                } else {
                    ((PlayerDataProvider) client.player).phases$set(phases);
                }

                // REI Compatibility
                if(Platform.isModLoaded("roughlyenoughitems")) {
                    REICompat.refreshHiddenItems();
                }
            });
        });
    }

    private static void registerAllPhaseSyncHandler() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ServerNetworking.ALL_PHASE_SYNC_ID, (buf, context) -> {
            // clear existing phases
            GamePhasesEventJS.getPhases().clear();

            // read phases and add to local collection
            @Nullable NbtCompound compound = buf.readNbt();
            if(compound == null) {
                return;
            }

            NbtList phases = compound.getList("Phases", NbtElement.COMPOUND_TYPE);
            phases.forEach(phaseTag -> {
                NbtCompound inner = (NbtCompound) phaseTag;
                String id = inner.getString("ID");
                Phase phase = Phase.fromTag(inner.getCompound("PhaseData"));
                GamePhasesEventJS.getPhases().put(id, phase);
            });
        });
    }
}
