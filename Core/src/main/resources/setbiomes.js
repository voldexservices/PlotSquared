/*
 This script will reset all biomes in claimed plots
 /plot debugexec runasync setbiomes.js Forest
 */
var plots = PS.getBasePlots();
for (var i = 0; i < plots.size(); i++) {
    var plot = plots.get(i);
    PS.class.static.log('&cSetting biome for: ' + plot);
    plot.setBiome("%s0", null);
}