package core.pluginchaos.darkbox;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

public class DBManager implements CommandExecutor, Listener {

    private DarkBox plugin;
    private FileConfiguration config;
    BukkitScheduler scheduler;

    public DBManager(DarkBox plugin, FileConfiguration config){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        this.config = config;
        plugin.getCommand("dbCreate").setExecutor(this);
        plugin.getCommand("dbList").setExecutor(this);
        loadBoxes();
    }
    public void loadBoxes(){
        if(Bukkit.getOnlineMode()){
            if(config.getConfigurationSection("Boxes") != null){
                for(String name : config.getConfigurationSection("Boxes").getKeys(false)){
                    BlackBox blackBox = new BlackBox(plugin, this, name, config);
                }
            }
        }
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player))return true;
        Player p = (Player) sender;
        if(!p.isOp())return true;
        if(command.getName().equalsIgnoreCase("dbCreate")){
            String name = args[0];
            if(!config.isConfigurationSection("Boxes")){
                config.createSection("Boxes");
            }
            if(config.isConfigurationSection("Boxes."+name)){
                p.sendMessage(ChatColor.RED + "A Dark Box is named "+ name +" already exists. Please use a different name.");
                return true;
            }
            String pathName = "Boxes."+name;
            config.createSection(pathName);
            config.createSection(pathName+".loc1");
            config.createSection(pathName+".loc2");
            config.set(pathName+".loc1", p.getLocation());
            config.set(pathName+".loc2", p.getLocation());
            plugin.saveConfig();
            p.sendMessage(ChatColor.GREEN + "Added the box "+ name +".");
            p.sendMessage(ChatColor.GRAY + "Use these commands to finish creating the box: ");
            p.sendMessage(ChatColor.GRAY + "to set Location 1: /dbEdit set " + name +" 1");
            p.sendMessage(ChatColor.GRAY + "to set Location 2: /dbEdit set " + name +" 2");
            BlackBox blackBox = new  BlackBox(plugin, this, name, config);
        }
        if(command.getName().equalsIgnoreCase("dbList")){
            for(String name : config.getConfigurationSection("Boxes").getKeys(false)){
                p.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + name);
            }
        }
        return true;
    }
}
