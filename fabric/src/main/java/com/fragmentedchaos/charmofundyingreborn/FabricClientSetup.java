package com.fragmentedchaos.charmofundyingreborn;

import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import net.fabricmc.api.ClientModInitializer;

public class FabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CharmSlotServices.NETWORK.registerClientHandler();
    }
}
