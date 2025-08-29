package org.abstruck.broadcast_killing.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.abstruck.broadcast_killing.BroadcastKilling;
import org.abstruck.broadcast_killing.core.BKPacket;

@Mod.EventBusSubscriber(modid = BroadcastKilling.MOD_ID)
public class BKEventHandler {
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player killer) {
            LivingEntity victim = event.getEntity();

            ItemStack weapon = killer.getMainHandItem();

            BKPacket packet = new BKPacket(killer, victim, weapon);

            BroadcastKilling.CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
        }
    }
}
