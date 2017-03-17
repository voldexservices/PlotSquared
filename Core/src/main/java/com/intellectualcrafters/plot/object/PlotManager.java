package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.commands.Template;
import com.intellectualcrafters.plot.config.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public interface PlotManager {

    /*
     * Plot locations (methods with Abs in them will not need to consider mega
     * plots).
     */
    PlotId getPlotIdAbs(PlotArea plotArea, int x, int y, int z);

    PlotId getPlotId(PlotArea plotArea, int x, int y, int z);

    // If you have a circular plot, just return the corner if it were a square
    Location getPlotBottomLocAbs(PlotArea plotArea, PlotId plotId);

    // the same applies here
    Location getPlotTopLocAbs(PlotArea plotArea, PlotId plotId);

    /*
     * Plot clearing (return false if you do not support some method)
     */
    boolean clearPlot(PlotArea plotArea, Plot plot, Runnable whenDone);

    boolean claimPlot(PlotArea plotArea, Plot plot);

    boolean unclaimPlot(PlotArea plotArea, Plot plot, Runnable whenDone);

    Location getSignLoc(PlotArea plotArea, Plot plot);

    /*
     * Plot set functions (return false if you do not support the specific set
     * method).
     */
    String[] getPlotComponents(PlotArea plotArea, PlotId plotId);

    boolean setComponent(PlotArea plotArea, PlotId plotId, String component, PlotBlock[] blocks);

    /*
     * PLOT MERGING (return false if your generator does not support plot
     * merging).
     */
    boolean createRoadEast(PlotArea plotArea, Plot plot);

    boolean createRoadSouth(PlotArea plotArea, Plot plot);

    boolean createRoadSouthEast(PlotArea plotArea, Plot plot);

    boolean removeRoadEast(PlotArea plotArea, Plot plot);

    boolean removeRoadSouth(PlotArea plotArea, Plot plot);

    boolean removeRoadSouthEast(PlotArea plotArea, Plot plot);

    boolean startPlotMerge(PlotArea plotArea, ArrayList<PlotId> plotIds);

    boolean startPlotUnlink(PlotArea plotArea, ArrayList<PlotId> plotIds);

    boolean finishPlotMerge(PlotArea plotArea, ArrayList<PlotId> plotIds);

    boolean finishPlotUnlink(PlotArea plotArea, ArrayList<PlotId> plotIds);

    default void exportTemplate(PlotArea plotArea) throws IOException {
        HashSet<FileBytes> files = new HashSet<>(
                Collections.singletonList(new FileBytes(Settings.Paths.TEMPLATES + "/tmp-data.yml", Template.getBytes(plotArea))));
        Template.zipAll(plotArea.worldname, files);
    }

    default int getWorldHeight() {
        return 255;
    }

}
