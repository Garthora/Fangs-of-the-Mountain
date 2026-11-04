package com.example.wolfhorse;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
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

@Mod(WolfHorseMod.MODID)
public class WolfHorseMod {
    public static final String MODID = "wolfhorse";

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<EntityType<WolfHorseEntity>> WOLF_HORSE =
            ENTITY_TYPES.register("wolf_horse", () -> EntityType.Builder
                    .of(WolfHorseEntity::new, MobCategory.CREATURE)
                    .sized(1.3964844F, 1.6F)
                    .clientTrackingRange(10)
                    .build("wolf_horse"));

    // Grey/brown egg, like a wolf's colours
    public static final RegistryObject<Item> WOLF_HORSE_SPAWN_EGG =
            ITEMS.register("wolf_horse_spawn_egg",
                    () -> new ForgeSpawnEggItem(WOLF_HORSE, 0xD7D3D2, 0x7A5A3A, new Item.Properties()));

    public WolfHorseMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modBus);
        ITEMS.register(modBus);

        modBus.addListener(this::registerAttributes);
        modBus.addListener(this::registerSpawnPlacements);
        modBus.addListener(this::addToCreativeTab);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WOLF_HORSE.get(), WolfHorseEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(WOLF_HORSE.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(WOLF_HORSE_SPAWN_EGG);
        }
    }
}
