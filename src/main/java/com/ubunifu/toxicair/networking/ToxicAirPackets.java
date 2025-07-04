package com.ubunifu.toxicair.networking;

import com.ubunifu.toxicair.AirHandler;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ToxicAirPackets {
    public static final Identifier TOXICITY_SYNC = new Identifier("toxic-air", "toxicity_sync");

    public static void sendToxicitySync(ServerPlayerEntity player, Map<BlockPos, Float> toxicityMap) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(toxicityMap.size());
        for (Map.Entry<BlockPos, Float> entry : toxicityMap.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            buf.writeFloat(entry.getValue());
        }
        ServerPlayNetworking.send(player, TOXICITY_SYNC, buf);
    }
    public static void registerListener(){
        ClientPlayNetworking.registerGlobalReceiver(TOXICITY_SYNC,((minecraftClient, clientPlayNetworkHandler, packetByteBuf, packetSender) -> {
            int size = packetByteBuf.readVarInt();
            Map<BlockPos, Float> newMap = new HashMap<>();

            for (int i = 0; i < size; i++) {
                BlockPos pos = packetByteBuf.readBlockPos();
                float value = packetByteBuf.readFloat();
                newMap.put(pos, value);
            }

            minecraftClient.execute(() -> {
                ClientWorld clientWorld = minecraftClient.world;
                // Update client cache
                AirHandler.removeWorldMap(clientWorld);
                Long2FloatOpenHashMap map = new Long2FloatOpenHashMap();
                newMap.forEach(((blockPos, aFloat) -> map.put(blockPos.toImmutable().asLong(),(float)aFloat)));
                AirHandler.addWorldMap(clientWorld,map);
            });
        }));
    }
}
