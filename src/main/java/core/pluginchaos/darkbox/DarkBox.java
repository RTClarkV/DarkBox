package core.pluginchaos.darkbox;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;


public final class DarkBox extends JavaPlugin implements Listener {
    FileConfiguration config;
    @Override
    public void onEnable() {
        // Plugin startup logic
        config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        new DBManager(this, config);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
