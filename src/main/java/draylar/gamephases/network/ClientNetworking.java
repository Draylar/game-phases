package draylar.gamephases.network;

import draylar.gamephases.GamePhases;
import draylar.gamephases.GamePhasesClient;
import draylar.gamephases.api.Phase;
import draylar.gamephases.compat.REICompat;
import draylar.gamephases.impl.PlayerDataProvider;
import draylar.gamephases.kube.GamePhasesEventJS;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.HashMap;
import java.util.Map;

public class ClientNetworking {

    public static void initialize() {
        registerPhaseSyncHandler();
        registerAllPhaseSyncHandler();
    }

    private static void registerPhaseSyncHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ServerNetworking.UNLOCKED_PHASE_SYNC, (client, handler, buf, responseSender) -> {
            int elements = buf.readInt();
            Map<String, Boolean> phases = new HashMap<>();
            for (int i = 0; i < elements; i++) {
                phases.put(buf.readString(), buf.readBoolean());
            }

            // Sync phases to client player object.
            client.execute(() -> {
                if(client.player == null) {
                    GamePhasesClient.cachedPhasedata = phases;
                } else {
                    ((PlayerDataProvider) client.player).phases$set(phases);
                }

                // REI Compatibility
                if(FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
                    REICompat.refreshHiddenItems();
                }
            });
        });
    }

    private static void registerAllPhaseSyncHandler() {
        ClientPlayNetworking.registerGlobalReceiver(GamePhases.ALL_PHASE_SYNC_ID, (client, handler, buf, responseSender) -> {
            // clear existing phases
            GamePhasesEventJS.getPhases().clear();

            // read phases and add to local collection
            NbtCompound compound = buf.readNbt();
            NbtList phases = compound.getList("Phases", NbtType.COMPOUND);
            phases.forEach(phaseTag -> {
                NbtCompound inner = (NbtCompound) phaseTag;
                String id = inner.getString("ID");
                Phase phase = Phase.fromTag(inner.getCompound("PhaseData"));
                GamePhasesEventJS.getPhases().put(id, phase);
            });
        });
    }
}
