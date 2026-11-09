package com.example.wolfhorse;

import com.example.wolfhorse.entity.WolfHorseEntity;
import com.example.wolfhorse.item.FurArmorMaterial;
import com.example.wolfhorse.item.DireWolfBackpackItem;
import com.example.wolfhorse.item.FurVariant;
import com.example.wolfhorse.item.HornItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.sounds.SoundEvent;
import com.example.wolfhorse.item.LargeBoneTier;
import net.minecraft.world.item.SwordItem;
import com.example.wolfhorse.item.PeltItem;
import com.example.wolfhorse.item.WolfWhistleItem;
import com.example.wolfhorse.network.ModNetwork;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod(WolfHorseMod.MODID)
public class WolfHorseMod {
    public static final String MODID = "fotm";

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    /** One DireWolf howl (eight variations, see assets/fotm/sounds.json). */
    public static final RegistryObject<SoundEvent> DIREWOLF_HOWL = SOUNDS.register("direwolf_howl",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "direwolf_howl")));

    // ---------- "Roughed up" den floor blocks: break one for 1-2 items of den loot ----------
    public static final RegistryObject<Block> ROUGHED_UP_PODZOL = roughedUp("roughed_up_podzol",
            MapColor.PODZOL, 0.5F, SoundType.ROOTED_DIRT);
    public static final RegistryObject<Block> ROUGHED_UP_COARSE_DIRT = roughedUp("roughed_up_coarse_dirt",
            MapColor.DIRT, 0.5F, SoundType.GRAVEL);
    public static final RegistryObject<Block> ROUGHED_UP_SNOW = roughedUp("roughed_up_snow",
            MapColor.SNOW, 0.3F, SoundType.SNOW);
    public static final RegistryObject<Item> ROUGHED_UP_PODZOL_ITEM = blockItem(ROUGHED_UP_PODZOL);
    public static final RegistryObject<Item> ROUGHED_UP_COARSE_DIRT_ITEM = blockItem(ROUGHED_UP_COARSE_DIRT);
    public static final RegistryObject<Item> ROUGHED_UP_SNOW_ITEM = blockItem(ROUGHED_UP_SNOW);

    private static RegistryObject<Block> roughedUp(String id, MapColor color, float strength, SoundType sound) {
        return BLOCKS.register(id, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(color).strength(strength).sound(sound)));
    }

    private static RegistryObject<Item> blockItem(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // The Plains DireWolf (called wolf_horse before 1.22; old worlds are converted on load).
    public static final RegistryObject<EntityType<WolfHorseEntity>> PLAINS_DIREWOLF = direwolf("plains_direwolf");
    public static final RegistryObject<EntityType<WolfHorseEntity>> DARK_FOREST_DIREWOLF = direwolf("dark_forest_direwolf");
    public static final RegistryObject<EntityType<WolfHorseEntity>> TUNDRA_DIREWOLF = direwolf("tundra_direwolf");

    public static final RegistryObject<Item> PLAINS_DIREWOLF_SPAWN_EGG =
            ITEMS.register("plains_direwolf_spawn_egg",
                    () -> new ForgeSpawnEggItem(PLAINS_DIREWOLF, 0xD2B48C, 0x4A4A4A, new Item.Properties()));
    public static final RegistryObject<Item> DARK_FOREST_DIREWOLF_SPAWN_EGG =
            ITEMS.register("dark_forest_direwolf_spawn_egg",
                    () -> new ForgeSpawnEggItem(DARK_FOREST_DIREWOLF, 0x6B4A2F, 0x1A1A1A, new Item.Properties()));
    public static final RegistryObject<Item> TUNDRA_DIREWOLF_SPAWN_EGG =
            ITEMS.register("tundra_direwolf_spawn_egg",
                    () -> new ForgeSpawnEggItem(TUNDRA_DIREWOLF, 0xF2F2F2, 0x8C8C8C, new Item.Properties()));

    public static final RegistryObject<Item> DIREWOLF_BONE =
            ITEMS.register("direwolf_bone", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BLOODY_BONE =
            ITEMS.register("bloody_bone", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> DENSE_BONE =
            ITEMS.register("dense_bone", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BLOOD_HUNT_HORN = ITEMS.register("blood_hunt_horn",
            () -> new HornItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), true));
    public static final RegistryObject<Item> HOWLING_HORN = ITEMS.register("howling_horn",
            () -> new HornItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), false));
    public static final RegistryObject<Item> FANG =
            ITEMS.register("fang", () -> new Item(new Item.Properties()));
    /** Iron sword stats: 6 damage, 1.6 attack speed, 250 durability. */
    public static final RegistryObject<Item> LARGE_BONE_SWORD = ITEMS.register("large_bone_sword",
            () -> new SwordItem(LargeBoneTier.INSTANCE, 3, -2.4F, new Item.Properties()));
    public static final RegistryObject<Item> DIREWOLF_BACKPACK = ITEMS.register("direwolf_backpack",
            () -> new DireWolfBackpackItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WOLF_WHISTLE =
            ITEMS.register("wolf_whistle", () -> new WolfWhistleItem(new Item.Properties().stacksTo(1)));

    // ---------- Furs, one per DireWolf type ("fur" is the original id, now Plains Fur) ----------
    public static final RegistryObject<Item> FUR =
            ITEMS.register("fur", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DARK_FOREST_FUR =
            ITEMS.register("dark_forest_fur", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TUNDRA_FUR =
            ITEMS.register("tundra_fur", () -> new Item(new Item.Properties()));

    // ---------- Fur armor sets (iron-level stats). Plains keeps the original "fur_*" ids ----------
    public static final RegistryObject<Item> FUR_HELMET = armor("fur_helmet", FurArmorMaterial.PLAINS, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> FUR_CHESTPLATE = armor("fur_chestplate", FurArmorMaterial.PLAINS, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> FUR_LEGGINGS = armor("fur_leggings", FurArmorMaterial.PLAINS, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> FUR_BOOTS = armor("fur_boots", FurArmorMaterial.PLAINS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> DARK_FOREST_FUR_HELMET = armor("dark_forest_fur_helmet", FurArmorMaterial.DARK_FOREST, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> DARK_FOREST_FUR_CHESTPLATE = armor("dark_forest_fur_chestplate", FurArmorMaterial.DARK_FOREST, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> DARK_FOREST_FUR_LEGGINGS = armor("dark_forest_fur_leggings", FurArmorMaterial.DARK_FOREST, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> DARK_FOREST_FUR_BOOTS = armor("dark_forest_fur_boots", FurArmorMaterial.DARK_FOREST, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> TUNDRA_FUR_HELMET = armor("tundra_fur_helmet", FurArmorMaterial.TUNDRA, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> TUNDRA_FUR_CHESTPLATE = armor("tundra_fur_chestplate", FurArmorMaterial.TUNDRA, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> TUNDRA_FUR_LEGGINGS = armor("tundra_fur_leggings", FurArmorMaterial.TUNDRA, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> TUNDRA_FUR_BOOTS = armor("tundra_fur_boots", FurArmorMaterial.TUNDRA, ArmorItem.Type.BOOTS);

    // ---------- Pelts (Plains keeps the original "pelt" id) ----------
    public static final RegistryObject<Item> PELT =
            ITEMS.register("pelt", () -> new PeltItem(new Item.Properties().stacksTo(1), FurVariant.PLAINS));
    public static final RegistryObject<Item> DARK_FOREST_PELT =
            ITEMS.register("dark_forest_pelt", () -> new PeltItem(new Item.Properties().stacksTo(1), FurVariant.DARK_FOREST));
    public static final RegistryObject<Item> TUNDRA_PELT =
            ITEMS.register("tundra_pelt", () -> new PeltItem(new Item.Properties().stacksTo(1), FurVariant.TUNDRA));

    // ---------- Armor trims (patterns defined in data/fotm/trim_pattern/) ----------
    public static final ResourceKey<TrimPattern> PELT_TRIM =
            ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(MODID, "pelt"));
    public static final ResourceKey<TrimPattern> SCARF_TRIM =
            ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(MODID, "scarf"));
    public static final RegistryObject<Item> PELT_ARMOR_TRIM_SMITHING_TEMPLATE =
            ITEMS.register("pelt_armor_trim_smithing_template",
                    () -> SmithingTemplateItem.createArmorTrimTemplate(PELT_TRIM));
    public static final ResourceKey<TrimPattern> FUR_TRIM =
            ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(MODID, "fur"));
    public static final RegistryObject<Item> FUR_ARMOR_TRIM_SMITHING_TEMPLATE =
            ITEMS.register("fur_armor_trim_smithing_template",
                    () -> SmithingTemplateItem.createArmorTrimTemplate(FUR_TRIM));
    public static final ResourceKey<TrimPattern> FUR_WRAPS_TRIM =
            ResourceKey.create(Registries.TRIM_PATTERN, new ResourceLocation(MODID, "fur_wraps"));
    public static final RegistryObject<Item> FUR_WRAPS_ARMOR_TRIM_SMITHING_TEMPLATE =
            ITEMS.register("fur_wraps_armor_trim_smithing_template",
                    () -> SmithingTemplateItem.createArmorTrimTemplate(FUR_WRAPS_TRIM));
    public static final RegistryObject<Item> SCARF_ARMOR_TRIM_SMITHING_TEMPLATE =
            ITEMS.register("scarf_armor_trim_smithing_template",
                    () -> SmithingTemplateItem.createArmorTrimTemplate(SCARF_TRIM));

    /** Everything in the order it appears in the Fangs of the Mountains creative tab. */
    private static List<RegistryObject<Item>> tabItems() {
        return List.of(
                WOLF_WHISTLE, DIREWOLF_BACKPACK, HOWLING_HORN, BLOOD_HUNT_HORN, LARGE_BONE_SWORD,
                DIREWOLF_BONE, DENSE_BONE, BLOODY_BONE, FANG,
                DARK_FOREST_FUR, FUR, TUNDRA_FUR,
                DARK_FOREST_FUR_HELMET, DARK_FOREST_FUR_CHESTPLATE, DARK_FOREST_FUR_LEGGINGS, DARK_FOREST_FUR_BOOTS, DARK_FOREST_PELT,
                FUR_HELMET, FUR_CHESTPLATE, FUR_LEGGINGS, FUR_BOOTS, PELT,
                TUNDRA_FUR_HELMET, TUNDRA_FUR_CHESTPLATE, TUNDRA_FUR_LEGGINGS, TUNDRA_FUR_BOOTS, TUNDRA_PELT,
                FUR_ARMOR_TRIM_SMITHING_TEMPLATE, FUR_WRAPS_ARMOR_TRIM_SMITHING_TEMPLATE, PELT_ARMOR_TRIM_SMITHING_TEMPLATE, SCARF_ARMOR_TRIM_SMITHING_TEMPLATE,
                ROUGHED_UP_PODZOL_ITEM, ROUGHED_UP_COARSE_DIRT_ITEM, ROUGHED_UP_SNOW_ITEM,
                DARK_FOREST_DIREWOLF_SPAWN_EGG, PLAINS_DIREWOLF_SPAWN_EGG, TUNDRA_DIREWOLF_SPAWN_EGG);
    }

    /** Creative tab with everything from Fangs of the Mountains. */
    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("fangs_of_the_mountains",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.fotm"))
                    .icon(() -> new ItemStack(WOLF_WHISTLE.get()))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((params, out) -> tabItems().stream().filter(WolfHorseMod::shownInCreative)
                            .forEach(item -> out.accept(item.get())))
                    .build());

    private static final List<RegistryObject<Item>> FUR_ARMOR_PIECES = List.of(
            DARK_FOREST_FUR_HELMET, DARK_FOREST_FUR_CHESTPLATE, DARK_FOREST_FUR_LEGGINGS, DARK_FOREST_FUR_BOOTS,
            FUR_HELMET, FUR_CHESTPLATE, FUR_LEGGINGS, FUR_BOOTS,
            TUNDRA_FUR_HELMET, TUNDRA_FUR_CHESTPLATE, TUNDRA_FUR_LEGGINGS, TUNDRA_FUR_BOOTS);

    /** Fur armor sets are hidden in TRIM mode, the Fur Armor Trim template in SET mode. */
    private static boolean shownInCreative(RegistryObject<Item> item) {
        if (FUR_ARMOR_PIECES.contains(item)) {
            return WolfHorseConfig.furArmorSetsEnabled();
        }
        if (item == FUR_ARMOR_TRIM_SMITHING_TEMPLATE || item == FUR_WRAPS_ARMOR_TRIM_SMITHING_TEMPLATE) {
            return WolfHorseConfig.furArmorTrimEnabled();
        }
        return true;
    }

    private static RegistryObject<Item> armor(String id, FurArmorMaterial material, ArmorItem.Type type) {
        return ITEMS.register(id, () -> new ArmorItem(material, type, new Item.Properties()));
    }

    private static RegistryObject<EntityType<WolfHorseEntity>> direwolf(String id) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder
                .of(WolfHorseEntity::new, MobCategory.CREATURE)
                .sized(1.3964844F, 1.6F)
                .clientTrackingRange(10)
                .build(id));
    }

    public static List<RegistryObject<EntityType<WolfHorseEntity>>> allDirewolves() {
        return List.of(PLAINS_DIREWOLF, DARK_FOREST_DIREWOLF, TUNDRA_DIREWOLF);
    }

    public WolfHorseMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WolfHorseConfig.SPEC,
                "fangs-of-the-mountains.toml");
        CraftingHelper.register(FurArmorModeCondition.SERIALIZER);
        ENTITY_TYPES.register(modBus);
        BLOCKS.register(modBus);
        SOUNDS.register(modBus);
        ITEMS.register(modBus);
        TABS.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerAttributes);
        modBus.addListener(this::registerSpawnPlacements);
        modBus.addListener(this::addToCreativeTab);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        for (var type : allDirewolves()) {
            event.put(type.get(), WolfHorseEntity.createAttributes().build());
        }
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        for (var type : allDirewolves()) {
            event.register(type.get(),
                    SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    WolfHorseEntity::checkDirewolfSpawnRules,
                    SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        var tab = event.getTabKey();
        if (tab == CreativeModeTabs.SPAWN_EGGS) {
            List.of(DARK_FOREST_DIREWOLF_SPAWN_EGG, PLAINS_DIREWOLF_SPAWN_EGG, TUNDRA_DIREWOLF_SPAWN_EGG).forEach(event::accept);
        } else if (tab == CreativeModeTabs.INGREDIENTS) {
            List.of(DARK_FOREST_FUR, FUR, TUNDRA_FUR, DIREWOLF_BONE, DENSE_BONE, BLOODY_BONE, FANG, FUR_ARMOR_TRIM_SMITHING_TEMPLATE, FUR_WRAPS_ARMOR_TRIM_SMITHING_TEMPLATE,
                    PELT_ARMOR_TRIM_SMITHING_TEMPLATE, SCARF_ARMOR_TRIM_SMITHING_TEMPLATE)
                    .stream().filter(WolfHorseMod::shownInCreative).forEach(event::accept);
        } else if (tab == CreativeModeTabs.NATURAL_BLOCKS) {
            List.of(ROUGHED_UP_PODZOL_ITEM, ROUGHED_UP_COARSE_DIRT_ITEM, ROUGHED_UP_SNOW_ITEM).forEach(event::accept);
        } else if (tab == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(WOLF_WHISTLE);
            event.accept(HOWLING_HORN);
            event.accept(BLOOD_HUNT_HORN);
            event.accept(DIREWOLF_BACKPACK);
        } else if (tab == CreativeModeTabs.COMBAT) {
            event.accept(LARGE_BONE_SWORD);
            List.of(DARK_FOREST_FUR_HELMET, DARK_FOREST_FUR_CHESTPLATE, DARK_FOREST_FUR_LEGGINGS, DARK_FOREST_FUR_BOOTS, DARK_FOREST_PELT,
                    FUR_HELMET, FUR_CHESTPLATE, FUR_LEGGINGS, FUR_BOOTS, PELT,
                    TUNDRA_FUR_HELMET, TUNDRA_FUR_CHESTPLATE, TUNDRA_FUR_LEGGINGS, TUNDRA_FUR_BOOTS, TUNDRA_PELT)
                    .stream().filter(WolfHorseMod::shownInCreative).forEach(event::accept);
        }
    }
}
