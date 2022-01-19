package draylar.gamephases.api;

import dev.hephaestus.fiblib.api.BlockFib;
import dev.hephaestus.fiblib.api.BlockFibRegistry;
import draylar.gamephases.GamePhases;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class Phase {

    private final String id;
    private final List<Item> blacklistedItems;
    private final List<Block> blacklistedBlocks;
    private final List<String> blacklistedDimensions;
    private final List<Pair<EntityType<?>, Integer>> blacklistedEntities;

    private Phase(String id, List<Item> blacklistedItems, List<Block> blacklistedBlocks, List<String> blacklistedDimensions, List<Pair<EntityType<?>, Integer>> blacklistedEntities) {
        this.id = id;
        this.blacklistedItems = blacklistedItems;
        this.blacklistedBlocks = blacklistedBlocks;
        this.blacklistedDimensions = blacklistedDimensions;
        this.blacklistedEntities = blacklistedEntities;
    }

    public Phase(String id) {
        this.id = id;
        this.blacklistedItems = new ArrayList<>();
        this.blacklistedBlocks = new ArrayList<>();
        this.blacklistedDimensions = new ArrayList<>();
        this.blacklistedEntities = new ArrayList<>();
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
                .modifiesDrops()
                .build();

        BlockFibRegistry.register(fib);
        return this;
    }

    public Phase dimension(String dimension) {
        blacklistedDimensions.add(dimension);
        return this;
    }

    public Phase entity(String entity) {
        blacklistedEntities.add(new Pair<>(Registry.ENTITY_TYPE.get(new Identifier(entity)), 128));
        return this;
    }

    public Phase entity(String entity, int radius) {
        blacklistedEntities.add(new Pair<>(Registry.ENTITY_TYPE.get(new Identifier(entity)), radius));
        return this;
    }

    /**
     * @param item {@link Item} to check for phase restrictions
     * @return {@code true} if this phase restricts the given {@link Item}, otherwise {@code false}
     */
    public boolean restricts(Item item) {
        return blacklistedItems.contains(item);
    }

    /**
     * @param world {@link ServerWorld} to check for phase restrictions
     * @return {@code true} if this phase restricts the given {@link ServerWorld}/dimension, otherwise {@code false}
     */
    public boolean restricts(ServerWorld world) {
        return blacklistedDimensions.contains(world.getRegistryKey().getValue().toString());
    }

    /**
     * @param type type to check for spawn restrictions
     * @return {@code true} if this phase restricts spawning of the given {@link EntityType}, otherwise {@code false}
     */
    public boolean restricts(EntityType<?> type) {
        return blacklistedEntities.stream().anyMatch(pair -> pair.getLeft().equals(type));
    }

    /**
     * Returns the minimum spawn restriction distance of the given {@link EntityType} in this {@link Phase}, or -1 if the entity is not restricted.
     *
     * <p>
     * Example:
     *   <ul>minecraft:creeper, 128
     *   <ul>minecraft:creeper, 70
     *   <ul>Result: 70
     *
     * @param type type restrictions to check for minimum radius
     * @return  the minimum spawn restriction of the given type, or -1 if it is not restricted
     */
    public int getRadius(EntityType<?> type) {
        return blacklistedEntities.stream()
                .filter(pair -> pair.getLeft().equals(type))
                .map(Pair::getRight)
                .sorted()
                .findFirst()
                .orElse(-1);
    }

    /**
     * @param player player to check for phase status
     * @return {@code true} if the given {@link PlayerEntity} has passed/unlocked this phase, otherwise {@code false}
     */
    public boolean hasUnlocked(PlayerEntity player) {
        return GamePhases.getPhaseData(player).has(this.id);
    }

    /**
     * @return a {@link String} which uniquely identifies this game phase
     */
    public String getId() {
        return id;
    }

    /**
     * @see Phase#fromTag(NbtCompound)
     * @return a {@link NbtCompound} with the data of this {@link Phase} serialized inside it.
     */
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("ID", id);

        // write blacklisted items
        NbtList itemList = new NbtList();
        blacklistedItems.forEach(item -> {
            Identifier id = Registry.ITEM.getId(item);
            itemList.add(NbtString.of(id.toString()));
        });

        // write blacklisted blocks
        NbtList blockList = new NbtList();
        blacklistedBlocks.forEach(block -> {
            Identifier id = Registry.BLOCK.getId(block);
            itemList.add(NbtString.of(id.toString()));
        });

        // write blacklisted dimensions
        NbtList dimensionList = new NbtList();
        blacklistedDimensions.forEach(dimension -> {
            itemList.add(NbtString.of(id));
        });

        // write blacklisted entities
        NbtList entityList = new NbtList();
        blacklistedEntities.forEach(entity -> {
            NbtCompound compound = new NbtCompound();
            compound.putString("ID", Registry.ENTITY_TYPE.getId(entity.getLeft()).toString());
            compound.putInt("Range", entity.getRight());
            entityList.add(compound);
        });

        tag.put("Items", itemList);
        tag.put("Blocks", blockList);
        tag.put("Dimensions", dimensionList);
        tag.put("Entities", entityList);
        return tag;
    }

    public static Phase fromTag(NbtCompound tag) {
        String id = tag.getString("ID");
        NbtList items = tag.getList("Items", NbtType.COMPOUND);
        NbtList blocks = tag.getList("Blocks", NbtType.COMPOUND);
        NbtList dimensions = tag.getList("Dimensions", NbtType.COMPOUND);
        NbtList entities = tag.getList("Entities", NbtType.COMPOUND);

        // read items
        List<Item> readItems = new ArrayList<>();
        items.forEach(element -> readItems.add(Registry.ITEM.get(new Identifier(element.asString()))));

        // read blocks
        List<Block> readBlocks = new ArrayList<>();
        blocks.forEach(element -> readBlocks.add(Registry.BLOCK.get(new Identifier(element.asString()))));

        // read dimensions
        List<String> readDimensions = new ArrayList<>();
        dimensions.forEach(element -> readDimensions.add(element.asString()));

        // read entities
        List<Pair<EntityType<?>, Integer>> readEntities = new ArrayList<>();
        dimensions.forEach(element -> {
            NbtCompound compound = (NbtCompound) element;
            readEntities.add(new Pair<>(Registry.ENTITY_TYPE.get(new Identifier(compound.getString("ID"))), compound.getInt("Range")));
        });

        return new Phase(id, readItems, readBlocks, readDimensions, readEntities);
    }
}
