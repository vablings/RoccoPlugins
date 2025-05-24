package com.example.rocconaugas;

import com.google.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

public class RoccoNaugasOverlay extends OverlayPanel {

    private final RoccoNaugas plugin;

    @Inject
    private RoccoNaugasOverlay(RoccoNaugas plugin) {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setPreferredSize(new Dimension(160, 160));
    }


    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(265, 320));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Roccos Naugas Killer")
                .color(new Color(255, 157, 249))
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("State: ")
                .leftColor(new Color(255, 157, 249))
                .right("" + plugin.state)
                .rightColor(Color.WHITE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Ticks since state change: ")
                .leftColor(new Color(255, 157, 249))
                .right("" + plugin.ticksSinceLastStateChange)
                .rightColor(Color.WHITE)
                .build());

        int moonlightPotionCount = plugin.moonlightPotions != null
                && plugin.moonlightPotions.result() != null ?
                plugin.moonlightPotions.result().size() : 0;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Moonlight Potions: ")
                .leftColor(new Color(255, 157, 249))
                .right("" + moonlightPotionCount)
                .rightColor(Color.WHITE)
                .build());





        return super.render(graphics);
    }
}