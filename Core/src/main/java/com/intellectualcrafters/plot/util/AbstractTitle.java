package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class AbstractTitle {
    public static AbstractTitle TITLE_CLASS;

    public static void sendTitle(PlotPlayer player, String head, String sub) {
        if (player instanceof ConsolePlayer) {
            return;
        }
        if (TITLE_CLASS != null && !player.getAttribute("disabletitles")) {
            TITLE_CLASS.sendTitle(player, head, sub, Settings.Titles.FADE_IN, Settings.Titles.STAY, Settings.Titles.FADE_OUT);
        }
    }

    public abstract void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out);
}
