package com.example.wolfhorse.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * A fur pelt worn on the back like an elytra. It can't glide.
 * Worn in the chest slot, gives 3 armor (like a leather tunic) and stops powder-snow freezing.
 */
public class PeltItem extends Item implements Equipable {
    // Same UUID vanilla uses for chest-slot armor, so it never stacks with a chestplate.
    private static final UUID CHEST_ARMOR_UUID = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
    private static final double ARMOR = 3.0D;

    private final Multimap<Attribute, AttributeModifier> modifiers = ImmutableMultimap.of(
            Attributes.ARMOR,
            new AttributeModifier(CHEST_ARMOR_UUID, "Pelt armor", ARMOR, AttributeModifier.Operation.ADDITION));

    private final FurVariant variant;

    public PeltItem(Properties properties, FurVariant variant) {
        super(properties);
        this.variant = variant;
    }

    public FurVariant getVariant() {
        return this.variant;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.CHEST ? this.modifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    /** Right-click to put it on (swaps with whatever is in the chest slot). */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }
}
