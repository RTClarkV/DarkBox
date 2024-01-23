package core.pluginchaos.darkbox;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

public class BlackBox implements Listener, CommandExecutor {
    private DarkBox plugin;
    private Random ran = new Random();
    private DBManager manager;
    private String name;
    private Location loc1;
    private Location loc2;
    private BoundingBox box;
    private FileConfiguration config;
    private ArrayList<String> insideBox = new ArrayList<>();
    BukkitScheduler scheduler;
    private World world;


    public BlackBox(DarkBox plugin, DBManager manager, String name, FileConfiguration config){
        this.plugin = plugin;
        this.manager = manager;
        this.name = name;
        this.config = plugin.getConfig();
        this.scheduler = plugin.getServer().getScheduler();
        plugin.getCommand("dbEdit").setExecutor(this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.loc1 = config.getLocation("Boxes."+name+".loc1");
        this.loc2 = config.getLocation("Boxes."+name+".loc2");
        this.box = new BoundingBox(loc1.getX(),loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
        this.world = loc1.getWorld();
        runTasks();
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        insideBox.remove(e.getPlayer().getName());
    }
    public void runTasks(){
        scheduler.runTaskTimerAsynchronously(plugin, ()->{
            for(Player p : Bukkit.getOnlinePlayers()){
                if(box.contains(p.getLocation().toVector()) && !insideBox.contains(p.getName())){
                    scheduler.runTask(plugin, ()->{
                        insidePlay(p);
                        insideBox.add(p.getName());
                        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 8, 1);
                    });
                }
            }
        }, 0L, 10L);

        scheduler.runTaskTimer(plugin, ()->{
            int random = getRan(1, 10);
            int random2 = getRan(1, 10);
            if(random == random2 && insideBox.size() > 0){
                int index = getRan(0, insideBox.size()-1);
                Player p = Bukkit.getPlayer(insideBox.get(index));
                Location pLoc = p.getLocation();
                int x = getRan(-10, 10);
                int y = getRan(-10, 10);
                int z = getRan(-10, 10);
                playSounds(new Location(world, x +pLoc.getX() , y + pLoc.getY(), z + pLoc.getZ()), Sound.AMBIENT_CAVE, getRan(5, 15), 1, getRan(60, 200));
            }
        }, 0L, 20L);
    }

    public void insidePlay(Player player){
        if(!player.isOnline()){
            insideBox.remove(player.getName());
            return;
        }
        if(!box.contains(player.getLocation().toVector())){
            player.removePotionEffect(PotionEffectType.DARKNESS);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2, 2);
            insideBox.remove(player.getName());
            return;
        }

        int random = getRan(1, 30);
        int random2 = getRan(1, 30);
        if(random == random2){
            int effect = getRan(1, 7);
            if(effect == 1){
                player.addPotionEffect(PotionEffectType.SLOW.createEffect(getRan(100, 700), getRan(0, 1)));
            }
            if(effect == 2){
                player.addPotionEffect(PotionEffectType.POISON.createEffect(getRan(100, 500), 0));
            }
            if(effect == 3){
                player.addPotionEffect(PotionEffectType.SLOW_DIGGING.createEffect(getRan(100, 700), 0));
            }
            if(effect == 4){
                player.addPotionEffect(PotionEffectType.WITHER.createEffect(getRan(100, 500), 0));
            }
            if(effect == 5){
                player.addPotionEffect(PotionEffectType.HUNGER.createEffect(getRan(100, 500), getRan(0, 1)));
            }
            if(effect == 7){
                //stand.teleport(player.getLocation().multiply(1.1));
                //stand.getLocation().setY(player.getLocation().getY());
                player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(getRan(100, 500), getRan(0, 1)));
            }
            if(effect == 6){
                player.setFreezeTicks(getRan(100, 600));
            }
        }
        player.addPotionEffect(PotionEffectType.DARKNESS.createEffect(50, 0));
        scheduler.runTaskLater(plugin, ()->{
           insidePlay(player);
        }, 20L);
    }
    @EventHandler
    public void onLeave(PlayerKickEvent e){
        insideBox.remove(e.getPlayer().getName());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        insideBox.remove(e.getPlayer().getName());
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player))return true;
        Player p = (Player) sender;
        if(!p.isOp())return true;

        if(command.getName().equalsIgnoreCase("dbEdit")){
            String arg = args[0];
            if(arg.equalsIgnoreCase("set") && args[1].equalsIgnoreCase(name)){
                if(args[2].equalsIgnoreCase("1")){
                    config.set("Boxes."+name+".loc1", p.getLocation());
                    p.sendMessage(ChatColor.GREEN + "Location 1 set for " + name +".");
                    loc1 = p.getLocation();
                    this.box = new BoundingBox(loc1.getX(),loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
                    plugin.saveConfig();
                }
                if(args[2].equalsIgnoreCase("2")){
                    config.set("Boxes."+name+".loc2", p.getLocation());
                    loc2 = p.getLocation();
                    this.box = new BoundingBox(loc1.getX(),loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
                    p.sendMessage(ChatColor.GREEN + "Location 2 set for " + name +".");
                    plugin.saveConfig();
                }
            }
        }
        return true;
    }
    public void playSounds(Location loc, Sound sound, int volume, int pitch, int radius){
        for(Player viewer : loc.getNearbyPlayers(radius)){
            viewer.playSound(loc, sound, volume, pitch);
        }
    }
    public int getRan(int min, int max){
        int a = ran.nextInt(max+1-min);
        a += min;
        return a;
    }
}
