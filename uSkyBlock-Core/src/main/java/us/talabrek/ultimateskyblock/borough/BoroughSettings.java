package us.talabrek.ultimateskyblock.borough;

import us.talabrek.ultimateskyblock.uSkyBlock;

public final class BoroughSettings {
    private BoroughSettings() {}

    public static boolean enabled(uSkyBlock plugin) {
        return plugin.getConfig().getBoolean("skyborough.enabled", false);
    }

    public static BoroughBounds bounds(uSkyBlock plugin) {
        var c = plugin.getConfig();
        if (!c.getBoolean("skyborough.city.registered", false)) return null;
        return new BoroughBounds(c.getInt("skyborough.city.min-x"), c.getInt("skyborough.city.min-z"),
            c.getInt("skyborough.city.max-x"), c.getInt("skyborough.city.max-z"));
    }

    public static boolean ready(uSkyBlock plugin) {
        return bounds(plugin) != null && plugin.getConfig().getBoolean("skyborough.city.ready", false);
    }

}
