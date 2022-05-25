package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.item.ManaHolderItem;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemCreativeUnlocker extends Item implements ManaHolderItem {
    public ItemCreativeUnlocker(Properties properties) {
        super(properties);
    }

    @Override
    public int getMana(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxMana(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setMana(ItemStack stack, int mana) {
    }

    @Override
    public boolean manaProvider(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return true;
    }

    @Override
    public int withdrawMana(ItemStack stack, int cost, boolean simulate) {
        return cost < 0 ? 1 : cost;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level instanceof ServerLevel slevel) {
            var rootAdv = slevel.getServer().getAdvancements().getAdvancement(modLoc("root"));
            if (rootAdv != null) {
                var children = new ArrayList<Advancement>();
                children.add(rootAdv);
                addChildren(rootAdv, children);

                var adman = ((ServerPlayer) player).getAdvancements();

                for (var kid : children) {
                    var progress = adman.getOrStartProgress(kid);
                    if (!progress.isDone()) {
                        for (String crit : progress.getRemainingCriteria()) {
                            adman.award(kid, crit);
                        }
                    }
                }
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
        TooltipFlag isAdvanced) {
        tooltipComponents.add(new TranslatableComponent("item.hexcasting.creative_unlocker.tooltip.0"));
        tooltipComponents.add(new TranslatableComponent("item.hexcasting.creative_unlocker.tooltip.1"));
    }

    private static void addChildren(Advancement root, List<Advancement> out) {
        for (Advancement kiddo : root.getChildren()) {
            out.add(kiddo);
            addChildren(kiddo, out);
        }
    }
}