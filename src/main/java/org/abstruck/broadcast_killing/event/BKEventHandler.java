package org.abstruck.broadcast_killing.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.abstruck.broadcast_killing.BroadcastKilling;
import org.abstruck.broadcast_killing.core.BKPacket;
import org.abstruck.broadcast_killing.core.PickupItemGetter;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = BroadcastKilling.MOD_ID)
public class BKEventHandler {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();

        Player killer = getActualKiller(source);
        ItemStack weapon = getActualWeapon(source, killer);

        BroadcastKilling.LOGGER.info(source.getMsgId());

        if (null == source.getEntity() && victim instanceof Player) {
            BKPacket packet = new BKPacket(UUID.randomUUID(), victim.getUUID(), "", victim.getName().getString(), weapon);
            BroadcastKilling.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }

        if (null != killer) {
            BKPacket packet = new BKPacket(killer, victim, weapon);
            BroadcastKilling.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
    }

    private static Player getActualKiller(DamageSource source) {
        if (source.getEntity() instanceof Player player) {
            return player;
        }

        if (source.getDirectEntity() instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    private static ItemStack getActualWeapon(DamageSource source, Player killer) {

        if (source.getDirectEntity() instanceof PickupItemGetter arrow) {
            return arrow.broadcast_killing$getPublicPickupItem();
        }

        if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
            return new ItemStack(Items.FLINT_AND_STEEL);
        }

        if (source.is(DamageTypes.LAVA)) {
            return new ItemStack(Items.LAVA_BUCKET);
        }

        if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
            return new ItemStack(Blocks.TNT.asItem());
        }

        return null != killer ? killer.getMainHandItem() : ItemStack.EMPTY;
    }

}
