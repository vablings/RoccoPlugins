package com.example.roccowildyboner;
import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.*;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import com.example.roccowildyboner.RoccoBonerConfig;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "RoccoWildyBoner", description = "", enabledByDefault = false, tags = {"Testing"})
public class RoccoBoner extends Plugin {
    @Inject
    private Client client;
    @Inject
    private RoccoBonerConfig config;
    @Inject
    private KeyManager keyManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ClientThread clientThread;
    private boolean started = false;
    public int timeout = 0;

    @Provides
    private RoccoBonerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(RoccoBonerConfig.class);
    }
    @Override
    protected void startUp() throws Exception {
        keyManager.registerKeyListener(toggle);
        timeout = 0;
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(toggle);

        timeout = 0;
        started = false;
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
            return;
        }

        if (client.getGameState() != GameState.LOGGED_IN || !started) {
            return;
        }
        Inventory.search().onlyUnnoted().nameContains(config.boneName()).first().ifPresent(bone -> {
            TileObjects.search().nameContains(config.altarName()).first().ifPresent(altar -> {
                MousePackets.queueClickPacket();
                MousePackets.queueClickPacket();
                ObjectPackets.queueWidgetOnTileObject(bone, altar);
            });
        });
    }

    private final HotkeyListener toggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };


    public void toggle() {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        started = !started;
        System.out.println("Toggled to: " + started);
        System.out.println("Toggled to: " + started);

    }
}
