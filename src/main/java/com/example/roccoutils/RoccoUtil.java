package com.example.roccoutils;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.Collections.query.ItemQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.EthanApiPlugin.Utility.DelayedActionExecutor;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.*;
import com.example.roccodriftnet.RoccoDriftNetsConfig;
import com.example.rocconaugas.RoccoNaugas;
import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
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

    private Widget cureMe;
    private Widget hunterKit;

    int[][] rockPos = new int[][]{{1470, 10098}, {1471, 10098}, {1472, 10098}, {1473, 10097}};
    int[][] walkPos = new int[][]{{1470, 10097}, {1471, 10097}, {1472, 10097}, {1472, 10097}};
    int timeout = 0;
    int rockIndex = 0;


    DelayedActionExecutor executor;

    @javax.inject.Inject
    private EventBus eventBus;

    @Override
    protected void startUp() throws Exception {
        executor = new DelayedActionExecutor(eventBus);
        cureMe = client.getWidget((218 << 16) | 113);
        hunterKit = client.getWidget((218 << 16) | 114);
        timeout = 2;
        rockIndex = 0;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }


        Inventory.search().withName("Infernal shale").first().ifPresent(item -> {
            MousePackets.queueClickPacket();
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(item,Inventory.search().withName("Chisel").first().get() );
        });

        if (client.getTickCount() % 3 == 0) {
            Optional<Widget> cloth = Inventory.search().nameContains("wet cloth").withAction("Wipe").first();
            MousePackets.queueClickPacket();
            InventoryInteraction.useItem(cloth.get(), "Wipe");
        }
        if (client.getTickCount() % 3 == 1) {
            MousePackets.queueClickPacket();
            MovementPackets.queueMovement(walkPos[rockIndex][0], walkPos[rockIndex][1], false);
        }
        if (client.getTickCount() % 3 == 2) {
            MousePackets.queueClickPacket();

            int rockId = rockIndex >= 2 ? 56359 : 56360;

            ObjectPackets.queueObjectAction(1, rockId, rockPos[rockIndex][0], rockPos[rockIndex][1], false);
            rockIndex++;
            rockIndex = rockIndex >= walkPos.length ? 0 : rockIndex;
        }





        /*
        if (client.getTickCount() % 4 == 0) {
            Inventory.search().nameContains("wet cloth").withAction("Wipe").first().ifPresent(cloth -> {
                Optional<TileObject> rock = TileObjects.search().withAction("Mine").nameContains("ocks").nearestToPlayer();
                InventoryInteraction.useItem(cloth, "Wipe");
                ObjectPackets.queueWidgetOnTileObject(Inventory.search().first().get(), rock.get());
                executor.schedule( () -> {
                    MousePackets.queueClickPacket();
                    TileObjectInteraction.interact(rock.get(), "Mine");
                }, 2);
            });
        }

         */





        /*
        int tick_count = client.getTickCount();

        if (client.getLocalPlayer().getAnimation() != -1) {
            return;
        }

        if(isBankOpen()) {
            Widget depositInventory = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
            if (depositInventory != null) {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(depositInventory, "Deposit inventory");
            }
            Bank.search().withName("Battlestaff").first().ifPresentOrElse(item -> {
                MousePackets.queueClickPacket();
                BankInteraction.useItem(item, "Withdraw-14");
            }, () -> {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "no raw karambwans", null);
                EthanApiPlugin.stopPlugin(this);
            });
            Bank.search().withName("Air orb").first().ifPresentOrElse(item -> {
                MousePackets.queueClickPacket();
                BankInteraction.useItem(item, "Withdraw-14");
            }, () -> {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "no raw karambwans", null);
                EthanApiPlugin.stopPlugin(this);
            });

            MovementPackets.queueMovement(client.getLocalPlayer().getWorldLocation());
            return;
        }

        Optional<Widget> earthOrb = Inventory.search().withName("Air orb").first();
        Optional<Widget> battlestaff = Inventory.search().withName("Battlestaff").first();


        if(battlestaff.isPresent() && earthOrb.isPresent()) {
            MousePackets.queueClickPacket();
            MousePackets.queueClickPacket();
            WidgetPackets.queueWidgetOnWidget(battlestaff.get(), earthOrb.get());
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePause((270 << 16) | 14, 14);
        } else {
            findBank();

        }

         */




    }




    private boolean isBankOpen() {
        Widget bank = client.getWidget(WidgetInfo.BANK_CONTAINER);
        return bank != null && !bank.isHidden();
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

    private void findBank(){
        Optional<NPC> banker = NPCs.search().withAction("Bank").nearestToPlayer();
        if (banker.isPresent()){
            NPCInteraction.interact(banker.get(), "Bank");
            return;
        } else {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "couldn't find bank or banker", null);
            EthanApiPlugin.stopPlugin(this);
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



    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        // Only add our menu entry to objects
        if (event.getType() == MenuAction.EXAMINE_OBJECT.getId())
        {
            // Get current menu entries
            MenuEntry[] menuEntries = client.getMenuEntries();
            // Create new menu entry
            MenuEntry customEntry = client.createMenuEntry(-1);
            customEntry.setOption("Seed walk");
            customEntry.setTarget(event.getTarget());
            customEntry.setType(MenuAction.RUNELITE);
            customEntry.setIdentifier(event.getIdentifier());

            MenuEntry[] newEntries = new MenuEntry[menuEntries.length + 1];
            System.arraycopy(menuEntries, 0, newEntries, 0, menuEntries.length);
            newEntries[menuEntries.length] = customEntry;

            client.setMenuEntries(newEntries);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (event.getMenuOption().equals("Seed walk") &&
                event.getMenuAction() == MenuAction.RUNELITE)
        {

            int objectId = event.getId();
            client.addChatMessage(
                    net.runelite.api.ChatMessageType.GAMEMESSAGE,
                    "",
                    "Seedwalking to " + event.getMenuTarget() + " (Object ID: " + objectId + ")",
                    null
            );

            Widget firstItem = Inventory.search().first().get();
            TileObject tileObject = TileObjects.search().withId(objectId).nearestToPlayer().get();
            ObjectPackets.queueWidgetOnTileObject(firstItem, tileObject);

            Inventory.search().nameContains("seeds").first().ifPresent(item -> {
                MousePackets.queueClickPacket();
                WidgetPackets.queueWidgetAction(item, "Plant");
            });
        }
    }



}
