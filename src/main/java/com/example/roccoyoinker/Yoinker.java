package com.example.roccoyoinker;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.google.inject.Inject;
import net.runelite.api.events.GameTick;
import net.runelite.api.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.Optional;

@PluginDescriptor(name = "Yoinker", description = "", enabledByDefault = false, tags = {"Testing"})
public class Yoinker extends Plugin {

    @Inject
    private Client client;

    @Subscribe
    private void onGameTick(GameTick event) {
        if (EthanApiPlugin.isMoving()) {
            Inventory.search().withId(ItemID.STRANGE_FRUIT).first().ifPresent(fruit -> {
                MousePackets.queueClickPacket();
                InventoryInteraction.useItem(fruit, "Eat");
            });

            Inventory.search().withAction("Eat").first().ifPresent(fruit -> {
                MousePackets.queueClickPacket();
                InventoryInteraction.useItem(fruit, "Drop");
            });

            Inventory.search().withId(ItemID.COOKING_APPLE).first().ifPresent(fruit -> {
                MousePackets.queueClickPacket();
                InventoryInteraction.useItem(fruit, "Drop");
            });

            Inventory.search().withId(ItemID.GOLOVANOVA_FRUIT_TOP).first().ifPresent(fruit -> {
                MousePackets.queueClickPacket();
                InventoryInteraction.useItem(fruit, "Drop");
            });

            Inventory.search().withId(ItemID.REDBERRIES).first().ifPresent(fruit -> {
                MousePackets.queueClickPacket();
                InventoryInteraction.useItem(fruit, "Drop");
            });



            return;
        }
        Optional<TileObject> stall = TileObjects.search().nameContains("Fruit Stall").withAction("Steal-from").nearestToPlayer();
        if(stall.isPresent()) {
            MousePackets.queueClickPacket();
            TileObjectInteraction.interact(stall.get(), "Steal-from");
        }

    }

}
