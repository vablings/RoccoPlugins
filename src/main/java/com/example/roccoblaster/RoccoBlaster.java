package com.example.roccoblaster;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


@PluginDescriptor(name = "RoccoBlaster", description = "", enabledByDefault = false, tags = {"Testing"})
public class RoccoBlaster extends Plugin {



    @Inject
    Client client;


    public enum State {
        BANKING,
        BANKING_WITH_SIP,

    }

}
