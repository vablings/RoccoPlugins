package com.example.roccoutils;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.NPCPackets;
import com.example.Packets.WidgetPackets;
import com.example.roccodriftnet.RoccoDriftNetsConfig;
import com.google.inject.Provides;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import com.google.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executors;

@PluginDescriptor(name = "RoccoUtil", description = "", enabledByDefault = false, tags = {"Testing"})
public class RoccoUtil extends Plugin {
    @Inject
    RoccoDriftNetsConfig config;

    @Provides
    public RoccoDriftNetsConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(RoccoDriftNetsConfig.class);
    }

    @Inject
    private Client client;
    private Random random = new Random();
    private long randomDelay;


    private int lastAnimation;

    @Subscribe
    public void onGameTick(GameTick event) {
        int tick_count = client.getTickCount();
        Optional<NPC> fishSpot = NPCs.search().withAction("Lure").nearestToPlayer();

        if (tick_count % 3 == 0 ) {
            tickManip();
            dropFish();
        }
        if (tick_count % 3 == 1 ) {
            MousePackets.queueClickPacket();
            NPCPackets.queueNPCAction(fishSpot.get(), "Lure");
        }

    }


    private void tickManip() {
        Optional<Widget> mahoganyLogs = Inventory.search().withId(ItemID.TEAK_LOGS).first();
        Optional<Widget> knife = Inventory.search().withId(ItemID.KNIFE).first();
        if (mahoganyLogs.isEmpty() || knife.isEmpty()) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Missing mahogany logs or knife", null);
            EthanApiPlugin.stopPlugin(this);
            return;
        }
        MousePackets.queueClickPacket();
        MousePackets.queueClickPacket();
        WidgetPackets.queueWidgetOnWidget(mahoganyLogs.get(), knife.get());
    }

    private void dropFish() {
        List<Widget> logs = Inventory.search().nameContains("Raw").result();
        for(int i = 0; i < Math.min(logs.size(), 3); i++) {
            InventoryInteraction.useItem(logs.get(i), "Drop");
        }
    }


    private boolean checkIdleLogout() {
        int idleClientTicks = this.client.getKeyboardIdleTicks();
        if (this.client.getMouseIdleTicks() < idleClientTicks) {
            idleClientTicks = this.client.getMouseIdleTicks();
        }
        return (long)idleClientTicks >= this.randomDelay;
    }


    private long randomDelay() {
        return (long)clamp(Math.round(this.random.nextGaussian() * 8000.0));
    }

    private static double clamp(double val) {
        return Math.max(1.0, Math.min(13000.0, val));
    }

    private void pressKey() {
        KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyPress);
        KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyRelease);
        KeyEvent keyTyped = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyTyped);
    }

}
