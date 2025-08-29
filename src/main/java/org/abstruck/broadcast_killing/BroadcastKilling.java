package org.abstruck.broadcast_killing;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.abstruck.broadcast_killing.core.BKPacket;
import org.slf4j.Logger;

import static org.antlr.runtime.debug.DebugEventListener.PROTOCOL_VERSION;

@Mod(BroadcastKilling.MOD_ID)
public class BroadcastKilling {
    public static final String MOD_ID = "broadcast_killing";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public BroadcastKilling() {
        MinecraftForge.EVENT_BUS.register(this);
        registerPackets();
    }

    private void registerPackets() {
        int id = 0;
        CHANNEL.registerMessage(id++, BKPacket.class,
                BKPacket::encode, BKPacket::decode, BKPacket::handle);
    }

}
