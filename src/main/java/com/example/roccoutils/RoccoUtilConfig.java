package com.example.roccoutils;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("RoccoUtilConfig")
public interface RoccoUtilConfig extends Config {
    @ConfigItem(
            keyName = "RoccoUtilConfig",
            name = "Disable afk log",
            description = ""
    )
    default boolean afkLoggerPreventer() {
        return false;
    }

}
