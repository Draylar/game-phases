package draylar.gamephases.api;

import dev.hephaestus.fiblib.api.BlockFib;
import dev.hephaestus.fiblib.api.BlockFibRegistry;
import draylar.gamephases.GamePhases;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Phase {

    private final String id;
    @Nullable private final RecipeManager recipeManager;

    // Blacklists on registry entries
    private final Set<Item> blacklistedItems;
    private final Set<Block> blacklistedBlocks;
    private final Set<String> blacklistedDimensions;
    private final Set<Pair<EntityType<?>, Integer>> blacklistedEntities;

    // Blacklists on tags
    private final Set<String> blacklistedItemTags;
    private final Set<String> blacklistedBlockTags;

    private Phase(String id, @Nullable RecipeManager recipeManager, Set<Item> blacklistedItems, Set<Block> blacklistedBlocks, Set<String> blacklistedDimensions, Set<Pair<EntityType<?>, Integer>> blacklistedEntities, Set<String> blacklistedItemTags, Set<String> blacklistedBlockTags) {
        this.id = id;
        this.recipeManager = recipeManager;

        this.blacklistedItems = blacklistedItems;
        this.blacklistedBlocks = blacklistedBlocks;
        this.blacklistedDimensions = blacklistedDimensions;
        this.blacklistedEntities = blacklistedEntities;
        this.blacklistedItemTags = blacklistedItemTags;
        this.blacklistedBlockTags = blacklistedBlockTags;
    }

    public Phase(String id, @Nullable RecipeManager recipeManager) {
        this.id = id;
        this.recipeManager = recipeManager;

        this.blacklistedItems = new HashSet<>();
        this.blacklistedBlocks = new HashSet<>();
        this.blacklistedDimensions = new HashSet<>();
        this.blacklistedEntities = new HashSet<>();
        this.blacklistedItemTags = new HashSet<>();
        this.blacklistedBlockTags = new HashSet<>();
    }

    /**
     * @see Phase#item(Item, boolean)
     */
    public Phase item(Item item) {
        return item(item, true);
    }

    /**
     * Adds an {@link Item} to this {@link Phase}. If a player has not passed this {@code Phase}, the given item:
     * <ul>
     *     <li>Is hidden from REI</li>
     *     <li>Will not be usable if somehow obtained</li>
     *     <li>Has an obfuscated stack title</li>
     *     <li>Is not usable in recipes</li>
     *     <li>Not able to be crafted</li>
     * </ul>
     *
     * <p>
     * If {@code restrictRecipes} is set to {@code true}, all {@code Item} instances in {@link Registry#ITEM} will be
     *  chain-blocked if they are associated to the root {@code Item} in a crafting recipe.
     * For example, if the specified {@code Item} is {@code minecraft:iron_ingot} and {@code restrictRecipes} is set to {@code true},
     *  {@code minecraft:iron_pickaxe} will be blocked as well.
     *
     * @param item            {@code Item} instance to block while the player does not have this {@code Phase} unlocked
     * @param restrictRecipes whether {@link Item} instances associated by a recipe should be blocked as well
     * @return the mutated {@code Phase} being operated on
     */
    public Phase item(Item item, boolean restrictRecipes) {
        blacklistedItems.add(item);

        // Search through all crafting recipes.
        // For each recipe that uses the provided item as a recipe ingredient, disable the result.
        // TODO: nested crafting checks (eg. Iron Ingot -> Iron Pickaxe -> Diamond Pickaxe)?
        if(restrictRecipes && recipeManager != null) {
            recipeManager.listAllOfType(RecipeType.CRAFTING).forEach(craftingRecipe -> {
                craftingRecipe.getIngredients().stream().map(Ingredient::getMatchingStacks).forEach(ingredients -> {
                    for (ItemStack stack : ingredients) {
                        if(stack.getItem().equals(item)) {
                            blacklistedItems.add(craftingRecipe.getOutput().getItem());
                        }
                    }
                });
            });
        }

        return this;
    }

    public Phase itemTag(String tagId) {
        return itemTag(tagId, true);
    }

    /**
     * Adds all {@link Item} instances stored in the item {@link Tag} represented by the id in {@link ItemTags}, if it exists, to this {@link Phase}.
     *
     * @return this phase
     * @see Phase#item(Item, boolean)
     */
    public Phase itemTag(String tagId, boolean restrictRecipes) {
        // this is horrific, but don't know a better way to do it
        List<TagKey<Item>> tags = Registry.ITEM.streamTags().toList();
        for (TagKey<Item> key : tags) {

            // Match against x:path, or namespace:path given tagId
            // Search Item registry for any items matching this tag, and add to blacklist
            if(key.id().toString().equals(tagId) || key.id().getPath().equals(tagId)) {
                for (Item item : Registry.ITEM) {
                    if(item.getRegistryEntry().isIn(key)) {
                        item(item, restrictRecipes);
                    }
                }

                // only process 1 iteration
                return this;
            }
        }

        // No return early means we did not find a tag match - log in chat to help catch potential issues.
        GamePhases.LOGGER.warn(String.format("Item tag '%s' was referenced in phase '%s', but the tag is not present!", tagId, id));
        return this;
    }

    public Phase blockTag(String tagId, Block replacement) {
        List<TagKey<Block>> tags = Registry.BLOCK.streamTags().toList();
        for (TagKey<Block> key : tags) {
            if(key.id().toString().equals(tagId) || key.id().getPath().equals(tagId)) {
                for (Block block : Registry.BLOCK) {
                    if(block.getRegistryEntry().isIn(key)) {
                        block(block, replacement);
                    }
                }

                return this;
            }
        }

        GamePhases.LOGGER.warn(String.format("Block tag '%s' was referenced in phase '%s', but the tag is not present!", tagId, id));
        return this;
    }

    /**
     * Adds a {@link Block} to this {@link Phase}. If a player has not passed this {@code Phase}, the given block:
     * <ul>
     *     <li>Will be disguised as the specified replacement</li>
     *     <li>Has modified drops and behavior to model the replacement</li>
     * </ul>
     */
    public Phase block(Block block, Block replacement) {
        blacklistedBlocks.add(block);

        // fib the block
        BlockFib fib = BlockFib.builder(block, replacement)
                .withCondition(player -> !GamePhases.getPhaseData(player).has(this.id))
                .modifiesDrops()
                .build();

        Identifier inID = Registry.BLOCK.getId(block);
        Identifier outID = Registry.BLOCK.getId(replacement);
        Identifier id = new Identifier(getId(), String.format("%s_%s", inID.getPath(), outID.getPath()));
        BlockFibRegistry.register(id, fib);
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
     * <ul>minecraft:creeper, 128
     * <ul>minecraft:creeper, 70
     * <ul>Result: 70
     *
     * @param type type restrictions to check for minimum radius
     * @return the minimum spawn restriction of the given type, or -1 if it is not restricted
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
     * @return a {@link NbtCompound} with the data of this {@link Phase} serialized inside it.
     * @see Phase#fromTag(NbtCompound)
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
            blockList.add(NbtString.of(id.toString()));
        });

        // write blacklisted dimensions
        NbtList dimensionList = new NbtList();
        blacklistedDimensions.forEach(dimension -> {
            dimensionList.add(NbtString.of(id));
        });

        // write blacklisted entities
        NbtList entityList = new NbtList();
        blacklistedEntities.forEach(entity -> {
            NbtCompound compound = new NbtCompound();
            compound.putString("ID", Registry.ENTITY_TYPE.getId(entity.getLeft()).toString());
            compound.putInt("Range", entity.getRight());
            entityList.add(compound);
        });

        // Write item tags
        NbtList itemTagList = new NbtList();
        blacklistedItemTags.forEach(itemTag -> itemTagList.add(NbtString.of(itemTag)));

        // Write block tags
        NbtList blockTagList = new NbtList();
        blacklistedBlockTags.forEach(blockTag -> blockTagList.add(NbtString.of(blockTag)));

        tag.put("Items", itemList);
        tag.put("Blocks", blockList);
        tag.put("Dimensions", dimensionList);
        tag.put("Entities", entityList);
        tag.put("ItemTags", itemTagList);
        tag.put("BlockTags", blockTagList);
        return tag;
    }

    public static Phase fromTag(NbtCompound tag) {
        String id = tag.getString("ID");
        NbtList items = tag.getList("Items", NbtType.COMPOUND);
        NbtList blocks = tag.getList("Blocks", NbtType.COMPOUND);
        NbtList dimensions = tag.getList("Dimensions", NbtType.COMPOUND);
        NbtList entities = tag.getList("Entities", NbtType.COMPOUND);
        NbtList itemTags = tag.getList("ItemTags", NbtType.COMPOUND);
        NbtList blockTags = tag.getList("BlockTags", NbtType.COMPOUND);

        // read items
        Set<Item> readItems = new HashSet<>();
        items.forEach(element -> readItems.add(Registry.ITEM.get(new Identifier(element.asString()))));

        // read blocks
        Set<Block> readBlocks = new HashSet<>();
        blocks.forEach(element -> readBlocks.add(Registry.BLOCK.get(new Identifier(element.asString()))));

        // read dimensions
        Set<String> readDimensions = new HashSet<>();
        dimensions.forEach(element -> readDimensions.add(element.asString()));

        // read entities
        Set<Pair<EntityType<?>, Integer>> readEntities = new HashSet<>();
        dimensions.forEach(element -> {
            NbtCompound compound = (NbtCompound) element;
            readEntities.add(new Pair<>(Registry.ENTITY_TYPE.get(new Identifier(compound.getString("ID"))), compound.getInt("Range")));
        });

        // Tag tags
        Set<String> readItemTags = new HashSet<>();
        itemTags.forEach(element -> readItemTags.add(element.asString()));

        Set<String> readBlockTags = new HashSet<>();
        blockTags.forEach(element -> readBlockTags.add(element.asString()));

        return new Phase(id, null, readItems, readBlocks, readDimensions, readEntities, readItemTags, readBlockTags);
    }
}
