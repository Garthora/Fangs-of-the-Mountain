package com.example.wolfhorse.item;

import com.example.wolfhorse.blood.BloodHuntManager;
import com.example.wolfhorse.blood.BloodHuntType;
import com.example.wolfhorse.blood.HowlSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The two bone horns.
 *  Howling Horn    - a wolf howl, as often as you like (unlimited uses).
 *  Blood Hunt Horn - starts a Blood Hunt where you stand, and the pack comes for you too. Used up.
 */
public class HornItem extends Item {
    private final boolean bloodHunt;

    public HornItem(Properties properties, boolean bloodHunt) {
        super(properties);
        this.bloodHunt = bloodHunt;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 40;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            if (this.bloodHunt) {
                BloodHuntType type = BloodHuntType.forBiome(server, player.blockPosition());
                if (!BloodHuntManager.start(server, player.blockPosition(), type, false, player.getUUID())) {
                    player.displayClientMessage(Component.translatable("message.fotm.bloodhunt.already"), true);
                    return InteractionResultHolder.fail(stack);
                }
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            } else {
                HowlSounds.singleHowl(server, player.position());
                player.getCooldowns().addCooldown(this, 80);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(this.bloodHunt ? "item.fotm.blood_hunt_horn.desc" : "item.fotm.howling_horn.desc")
                .withStyle(this.bloodHunt ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
    }
}
