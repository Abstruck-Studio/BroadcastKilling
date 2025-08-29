package org.abstruck.broadcast_killing.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.abstruck.broadcast_killing.client.BKOverlay;

import java.util.UUID;
import java.util.function.Supplier;

public class BKPacket {
    private final UUID killerUuid;
    private final UUID victimUuid;
    private final String killerName;
    private final String victimName;
    private final ItemStack weapon;

    public BKPacket(Player killer, Entity victim, ItemStack weapon) {
        this.killerUuid = killer.getUUID();
        this.victimUuid = victim.getUUID();
        this.killerName = killer.getName().getString();
        this.victimName = victim.getName().getString();
        this.weapon = weapon;
    }

    public BKPacket(UUID killerUuid, UUID victimUuid, String killerName, String victimName, ItemStack weapon) {
        this.killerUuid = killerUuid;
        this.victimUuid = victimUuid;
        this.killerName = killerName;
        this.victimName = victimName;
        this.weapon = weapon;
    }

    public static void encode(BKPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.killerUuid);
        buffer.writeUUID(packet.victimUuid);
        buffer.writeUtf(packet.killerName);
        buffer.writeUtf(packet.victimName);
        buffer.writeItem(packet.weapon);
    }

    public static BKPacket decode(FriendlyByteBuf buffer) {
        return new BKPacket(
                buffer.readUUID(),
                buffer.readUUID(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readItem()
        );
    }

    public static void handle(BKPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;

            if (player != null) {
                boolean isPlayerKill = packet.killerUuid.equals(player.getUUID());
                boolean isPlayerVictim = packet.victimUuid.equals(player.getUUID());

                BKOverlay.addEntry(
                        packet.killerName,
                        packet.victimName,
                        packet.weapon,
                        isPlayerKill,
                        isPlayerVictim
                );
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
