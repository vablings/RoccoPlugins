package com.example.roccodriftnet;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.NPCs;
import com.example.EthanApiPlugin.Collections.TileObjects;
import com.example.EthanApiPlugin.Collections.Widgets;
import com.example.EthanApiPlugin.Collections.query.TileObjectQuery;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.InventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.NPCPackets;
import com.example.Packets.WidgetPackets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.*;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;


@PluginDescriptor(name = "RoccoDriftNets", description = "", enabledByDefault = false, tags = {"Testing"})
public class RoccoDriftNets extends Plugin {
    @Inject
    RoccoDriftNetsConfig config;

    @Provides
    public RoccoDriftNetsConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(RoccoDriftNetsConfig.class);
    }

    @Inject
    private Client client;
    @Getter
    private final Set<NPC> fish = new HashSet<>();
    @Getter
    private final Map<NPC, Integer> taggedFish = new HashMap<>();
    @Getter
    private final List<DriftNet> NETS = ImmutableList.of(
            new DriftNet(ObjectID.FOSSIL_DRIFT_NET1_MULTI, VarbitID.FOSSIL_DRIFT_NET1, VarbitID.FOSSIL_DRIFT_NET1_CATCH, ImmutableSet.of(
                    new WorldPoint(3746, 10297, 1),
                    new WorldPoint(3747, 10297, 1),
                    new WorldPoint(3748, 10297, 1),
                    new WorldPoint(3749, 10297, 1)
            )),
            new DriftNet(ObjectID.FOSSIL_DRIFT_NET2_MULTI, VarbitID.FOSSIL_DRIFT_NET2, VarbitID.FOSSIL_DRIFT_NET2_CATCH, ImmutableSet.of(
                    new WorldPoint(3742, 10288, 1),
                    new WorldPoint(3742, 10289, 1),
                    new WorldPoint(3742, 10290, 1),
                    new WorldPoint(3742, 10291, 1),
                    new WorldPoint(3742, 10292, 1)
            )));

    @Getter
    private boolean driftNetsInInventory;
    private static final String CHAT_PRODDING_FISH  = "You prod at the shoal of fish to scare it.";
    private boolean armInteraction;

    @Override
    protected void shutDown()
    {
        reset();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // First priority: Check if nets need handling
        // 1. If nets are full, harvest them
        stamIfRequired();

        if (client.getEnergy() < 1000 ) {
            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Ran out of stamina pots", null);
            EthanApiPlugin.stopPlugin(this);
        }

        for (DriftNet net : NETS) {
            if (net.getStatus() == DriftNetStatus.FULL) {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to harvest full net", null);
                MousePackets.queueClickPacket();
                TileObjectInteraction.interact(net.getObjectId(), "Harvest");
                return;
            }
        }

        // 2. If we have nets in inventory and there are empty frames (UNSET nets), set them up
        if (driftNetsInInventory) {
            Optional<DriftNet> emptyNet = NETS.stream()
                    .filter(n -> n.getStatus() == DriftNetStatus.UNSET)
                    .findFirst();

            if (emptyNet.isPresent()) {
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Attempting to reset empty net", null);
                MousePackets.queueClickPacket();
                TileObjectInteraction.interact(emptyNet.get().getObjectId(), "Set up");
                return;
            }
        }

        // 3. If we don't have nets, get them from Annette
        if (!driftNetsInInventory) {
            Optional<TileObject> annette = TileObjects.search().withId(ObjectID.FOSSIL_MERMAID_DRIFTNETS).withAction("Nets").first();
            if (annette.isPresent()) {
                MousePackets.queueClickPacket();
                TileObjectInteraction.interact(annette.get(), "Nets");
                return;
            }
        }

        // Handle tagged fish tracking
        List<DriftNet> closedNets = NETS.stream()
                .filter(DriftNet::isNotAcceptingFish)
                .collect(Collectors.toList());

        taggedFish.entrySet().removeIf(entry ->
                isTagExpired(entry.getValue()) ||
                        isFishNextToNet(entry.getKey(), closedNets)
        );

        // Update the previous tick status for all nets
        NETS.forEach(net -> net.setPrevTickStatus(net.getStatus()));

        // Only chase fish if we have active nets that can accept fish
        boolean anyActiveNets = NETS.stream().anyMatch(net ->
                net.getStatus() == DriftNetStatus.SET ||
                        net.getStatus() == DriftNetStatus.CATCH);

        if (anyActiveNets) {
            NPC bestFish = getBestFishToTag();
            if (bestFish != null) {
                MousePackets.queueClickPacket();
                NPCInteraction.interact(bestFish, "Chase");
                return;
            }
        }

        // If we reach here, we're not doing any action, so reset the armInteraction flag
        armInteraction = false;
    }

    public NPC getBestFishToTag() {
        List<NPC> sortedFish = getSortedUntaggedFish();
        return sortedFish.isEmpty() ? null : sortedFish.get(0);
    }

    public List<NPC> getSortedUntaggedFish() {
        Set<NPC> untaggedFish = new HashSet<>(fish);
        untaggedFish.removeAll(taggedFish.keySet());

        final Player player = client.getLocalPlayer();
        if (player == null) {
            return new ArrayList<>(untaggedFish);
        }

        final WorldPoint playerLocation = player.getWorldLocation();
        final int CLUSTER_RADIUS = 4; // tiles
        List<NPC> sortedFish = new ArrayList<>(untaggedFish);

        // Compute cluster size for each fish
        Map<NPC, Integer> nearbyFishCount = new HashMap<>();
        for (NPC fish1 : sortedFish) {
            WorldPoint loc1 = fish1.getWorldLocation();
            int count = 0;
            for (NPC fish2 : sortedFish) {
                if (fish1 == fish2) continue;
                if (loc1.distanceTo(fish2.getWorldLocation()) <= CLUSTER_RADIUS) {
                    count++;
                }
            }
            nearbyFishCount.put(fish1, count);
        }

        // Sort by: (1) cluster size (desc), (2) south first, (3) east next, (4) distance to player
        sortedFish.sort((fish1, fish2) -> {
            int cluster1 = nearbyFishCount.get(fish1);
            int cluster2 = nearbyFishCount.get(fish2);

            if (cluster1 != cluster2) {
                return Integer.compare(cluster2, cluster1); // more neighbors first
            }

            WorldPoint loc1 = fish1.getWorldLocation();
            WorldPoint loc2 = fish2.getWorldLocation();

            if (loc1.getY() != loc2.getY()) {
                return Integer.compare(loc1.getY(), loc2.getY()); // more south first
            }

            if (loc1.getX() != loc2.getX()) {
                return Integer.compare(loc2.getX(), loc1.getX()); // more east first
            }

            int dist1 = playerLocation.distanceTo(loc1);
            int dist2 = playerLocation.distanceTo(loc2);
            return Integer.compare(dist1, dist2); // closest to player
        });

        return sortedFish;
    }


    private boolean isFishNextToNet(NPC fish, Collection<DriftNet> nets)
    {
        final WorldPoint fishTile = WorldPoint.fromLocalInstance(client, fish.getLocalLocation());
        return nets.stream().anyMatch(net -> net.getAdjacentTiles().contains(fishTile));
    }

    private void stamIfRequired() {
        if (client.getEnergy() > 5000 || (client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 1 )) {
            return;
        }
        Widget stam = Inventory.search().nameContains("Stamina").result().get(0);
        MousePackets.queueClickPacket();
        InventoryInteraction.useItem(stam, "Drink");
    }




    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == 309) {
            MousePackets.queueClickPacket();
            Optional<Widget> driftNet = Widgets.search().withAction("Withdraw-5").first();
            WidgetPackets.queueWidgetAction(driftNet.get(), "Withdraw-5");
            pressEsc();
        }
    }

    private void pressEsc() {
        KeyEvent keyPress = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyPress);
        KeyEvent keyRelease = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyRelease);
        KeyEvent keyTyped = new KeyEvent(this.client.getCanvas(), KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
        this.client.getCanvas().dispatchEvent(keyTyped);
    }


    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        updateDriftNetVarbits();
    }

    private void updateDriftNetVarbits() {
        for (DriftNet net : NETS) {
            int statusVarbitValue = client.getVarbitValue(net.getStatusVarbit());
            int countVarbitValue = client.getVarbitValue(net.getCountVarbit());

            DriftNetStatus status = DriftNetStatus.of(statusVarbitValue);

            client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
                    "Net ID: " + net.getObjectId() +
                            " - Varbit value: " + statusVarbitValue +
                            " - Status: " + status +
                            " - Count: " + countVarbitValue, null);

            net.setStatus(status);
            net.setCount(countVarbitValue);
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        final NPC npc = event.getNpc();
        if (npc.getId() == NpcID.FOSSIL_FISH_SHOAL)
        {
            fish.add(npc);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        final NPC npc = event.getNpc();
        fish.remove(npc);
        taggedFish.remove(npc);
    }

    @Subscribe
    public void onItemContainerChanged(final ItemContainerChanged event)
    {
        final ItemContainer itemContainer = event.getItemContainer();
        if (itemContainer != client.getItemContainer(InventoryID.INV))
        {
            return;
        }

        driftNetsInInventory = itemContainer.contains(ItemID.FOSSIL_DRIFT_NET);
    }

    @Subscribe
    public void onInteractingChanged(InteractingChanged event)
    {
        if (armInteraction
                && event.getSource() == client.getLocalPlayer()
                && event.getTarget() instanceof NPC
                && ((NPC) event.getTarget()).getId() == NpcID.FOSSIL_FISH_SHOAL)
        {
            tagFish(event.getTarget());
            armInteraction = false;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {

        if (event.getType() == ChatMessageType.SPAM && event.getMessage().equals(CHAT_PRODDING_FISH))
        {
            Actor target = client.getLocalPlayer().getInteracting();

            if (target instanceof NPC && ((NPC) target).getId() == NpcID.FOSSIL_FISH_SHOAL)
            {
                tagFish(target);
            }
            else
            {
                // If the fish is on an adjacent tile, the interaction change happens after
                // the chat message is sent, so we arm it
                armInteraction = true;
            }
        }
    }
    private boolean isTagExpired(Integer tick) {
        // Remove fish from tagged list after 20 ticks (adjust as needed)
        return tick + 22 < client.getTickCount();
    }

    private void tagFish(Actor fish)
    {
        NPC fishTarget = (NPC) fish;
        taggedFish.put(fishTarget, client.getTickCount());
    }

    private void reset()
    {
        fish.clear();
        taggedFish.clear();
        armInteraction = false;
    }


}