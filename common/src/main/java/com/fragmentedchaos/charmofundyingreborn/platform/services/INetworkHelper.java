package com.fragmentedchaos.charmofundyingreborn.platform.services;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** 从服务端通知客户端播放图腾复活动画。 */
public interface INetworkHelper {

    void sendTotemUse(ServerPlayer player, ItemStack stack);

    /** 双端都要注册：服务端负责编码，客户端负责解码。 */
    default void registerPayloadType() {}

    /** 由平台客户端初始化调用。 */
    void registerClientHandler();
}
