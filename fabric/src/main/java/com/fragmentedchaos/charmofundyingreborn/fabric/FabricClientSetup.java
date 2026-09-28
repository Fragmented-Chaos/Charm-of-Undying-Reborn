package com.fragmentedchaos.charmofundyingreborn.fabric;

import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import net.fabricmc.api.ClientModInitializer;

public class FabricClientSetup implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CharmSlotServices.network().registerClientHandler();
    }
}
