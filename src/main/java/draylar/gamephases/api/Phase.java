package draylar.gamephases.api;

import dev.hephaestus.fiblib.api.BlockFib;
import dev.hephaestus.fiblib.api.BlockFibRegistry;
import draylar.gamephases.GamePhases;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class Phase {

    private final String id;
    private final List<Item> blacklistedItems;
    private final List<Block> blacklistedBlocks;
    private final List<String> blacklistedDimensions;

    private Phase(String id, List<Item> blacklistedItems, List<Block> blacklistedBlocks, List<String> blacklistedDimensions) {
        this.id = id;
        this.blacklistedItems = blacklistedItems;
        this.blacklistedBlocks = blacklistedBlocks;
        this.blacklistedDimensions = blacklistedDimensions;
    }

    public Phase(String id) {
        this.id = id;
        this.blacklistedItems = new ArrayList<>();
        this.blacklistedBlocks = new ArrayList<>();
        this.blacklistedDimensions = new ArrayList<>();
    }

    public Phase item(Item item) {
        blacklistedItems.add(item);
        return this;
    }

    public Phase block(Block block, Block replacement) {
        blacklistedBlocks.add(block);

        // fib the block
        BlockFib fib = BlockFib.builder(block, replacement)
                .withCondition(player -> !GamePhases.getPhaseData(player).has(this.id))
                .build();

        Identifier phaseID = new Identifier(getId());
        Identifier inID = Registry.BLOCK.getId(block);
        Identifier outID = Registry.BLOCK.getId(replacement);
        Identifier id = new Identifier(phaseID.getNamespace(), String.format("%s_%s_%s", phaseID.getPath(), inID.getPath(), outID.getPath()));
        BlockFibRegistry.register(id, fib);

        return this;
    }

    public Phase dimension(String dimension) {
        blacklistedDimensions.add(dimension);
        return this;
    }

    public boolean disallows(Item item) {
        return blacklistedItems.contains(item);
    }

    public boolean disallows(ServerWorld world) {
        return blacklistedDimensions.contains(world.getRegistryKey().getValue().toString());
    }

    public boolean hasUnlocked(PlayerEntity player) {
        return GamePhases.getPhaseData(player).has(this.id);
    }

    public String getId() {
        return id;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ID", id);

        // write blacklisted items
        ListTag itemList = new ListTag();
        blacklistedItems.forEach(item -> {
            Identifier id = Registry.ITEM.getId(item);
            itemList.add(StringTag.of(id.toString()));
        });

        // write blacklisted blocks
        ListTag blockList = new ListTag();
        blacklistedBlocks.forEach(block -> {
            Identifier id = Registry.BLOCK.getId(block);
            itemList.add(StringTag.of(id.toString()));
        });

        // write blacklisted dimensions
        ListTag dimensionList = new ListTag();
        blacklistedDimensions.forEach(dimension -> {
            itemList.add(StringTag.of(id));
        });

        tag.put("Items", itemList);
        tag.put("Blocks", blockList);
        tag.put("Dimensions", dimensionList);
        return tag;
    }

    public static Phase fromTag(CompoundTag tag) {
        String id = tag.getString("ID");
        ListTag items = tag.getList("Items", NbtType.COMPOUND);
        ListTag blocks = tag.getList("Blocks", NbtType.COMPOUND);
        ListTag dimensions = tag.getList("Dimensions", NbtType.COMPOUND);

        // read items
        List<Item> readItems = new ArrayList<>();
        items.forEach(element -> readItems.add(Registry.ITEM.get(new Identifier(element.asString()))));

        // read blocks
        List<Block> readBlocks = new ArrayList<>();
        blocks.forEach(element -> readBlocks.add(Registry.BLOCK.get(new Identifier(element.asString()))));

        // read dimensions
        List<String> readDimensions = new ArrayList<>();
        dimensions.forEach(element -> readDimensions.add(element.asString()));

        return new Phase(id, readItems, readBlocks, readDimensions);
    }
}
