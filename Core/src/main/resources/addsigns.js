/*
 This script will fix all signs in the world.
 */
var plots = PS.getBasePlots();
for (var i = 0; i < plots.size(); i++) {
    var plot = plots.get(i);
    plot.setSign();
    PS.class.static.log('&cSetting sign for: ' + plot);
}
