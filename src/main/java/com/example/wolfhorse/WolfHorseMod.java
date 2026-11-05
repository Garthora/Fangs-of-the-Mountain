package com.example.wolfhorse;

import com.example.wolfhorse.entity.WolfHorseEntity;
import com.example.wolfhorse.item.FurArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Mod(WolfHorseMod.MODID)
public class WolfHorseMod {
    public static final String MODID = "wolfhorse";

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // The original id "wolf_horse" is kept for the Plains DireWolf so existing worlds keep theirs.
    public static final RegistryObject<EntityType<WolfHorseEntity>> PLAINS_DIREWOLF = direwolf("wolf_horse");
    public static final RegistryObject<EntityType<WolfHorseEntity>> FOREST_DIREWOLF = direwolf("forest_direwolf");
    public static final RegistryObject<EntityType<WolfHorseEntity>> TUNDRA_DIREWOLF = direwolf("tundra_direwolf");

    public static final RegistryObject<Item> PLAINS_DIREWOLF_SPAWN_EGG =
            ITEMS.register("wolf_horse_spawn_egg",
                    () -> new ForgeSpawnEggItem(PLAINS_DIREWOLF, 0xD2B48C, 0x4A4A4A, new Item.Properties()));
    public static final RegistryObject<Item> FOREST_DIREWOLF_SPAWN_EGG =
            ITEMS.register("forest_direwolf_spawn_egg",
                    () -> new ForgeSpawnEggItem(FOREST_DIREWOLF, 0x6B4A2F, 0x1A1A1A, new Item.Properties()));
    public static final RegistryObject<Item> TUNDRA_DIREWOLF_SPAWN_EGG =
            ITEMS.register("tundra_direwolf_spawn_egg",
                    () -> new ForgeSpawnEggItem(TUNDRA_DIREWOLF, 0xF2F2F2, 0x8C8C8C, new Item.Properties()));

    // Fur and the Fur armor set (iron-level stats)
    public static final RegistryObject<Item> FUR =
            ITEMS.register("fur", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FUR_HELMET = ITEMS.register("fur_helmet",
            () -> new ArmorItem(FurArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> FUR_CHESTPLATE = ITEMS.register("fur_chestplate",
            () -> new ArmorItem(FurArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> FUR_LEGGINGS = ITEMS.register("fur_leggings",
            () -> new ArmorItem(FurArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> FUR_BOOTS = ITEMS.register("fur_boots",
            () -> new ArmorItem(FurArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new Item.Properties()));

    private static RegistryObject<EntityType<WolfHorseEntity>> direwolf(String id) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder
                .of(WolfHorseEntity::new, MobCategory.CREATURE)
                .sized(1.3964844F, 1.6F)
                .clientTrackingRange(10)
                .build(id));
    }

    public static List<RegistryObject<EntityType<WolfHorseEntity>>> allDirewolves() {
        return List.of(PLAINS_DIREWOLF, FOREST_DIREWOLF, TUNDRA_DIREWOLF);
    }

    public WolfHorseMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modBus);
        ITEMS.register(modBus);

        modBus.addListener(this::registerAttributes);
        modBus.addListener(this::registerSpawnPlacements);
        modBus.addListener(this::addToCreativeTab);
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
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(FOREST_DIREWOLF_SPAWN_EGG);
            event.accept(PLAINS_DIREWOLF_SPAWN_EGG);
            event.accept(TUNDRA_DIREWOLF_SPAWN_EGG);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(FUR);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(FUR_HELMET);
            event.accept(FUR_CHESTPLATE);
            event.accept(FUR_LEGGINGS);
            event.accept(FUR_BOOTS);
        }
    }
}
