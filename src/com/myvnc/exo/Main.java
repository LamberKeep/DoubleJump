package com.myvnc.exo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements TabCompleter, Listener {
	
    HashMap<String, Long> cooldown = new HashMap<>(); // cooldown for jumps
    HashMap<String, Boolean> inAir = new HashMap<>(); // event check
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("doublejump")) {	
			if (!sender.hasPermission("doublejump.admin")) {
				if (!this.getConfig().getString("messages.permission").isEmpty()) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.permission")));
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Usage: /doublejump reload");
			} else {
				if (args[0].equalsIgnoreCase("reload")) {
					this.reloadConfig();
					if (!this.getConfig().getString("messages.reloaded").isEmpty()) sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.reloaded")));
					for(Player p : Bukkit.getOnlinePlayers()){
						if (p.hasPermission("doublejump.jump") && p.getGameMode() == GameMode.SURVIVAL) {
							p.setAllowFlight(false);
							p.setFlying(false);
						}
					}
				}
			}
		}
		return false;
	}
	
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args){
    	if (cmd.getName().equalsIgnoreCase("doublejump")) {
    		List<String> list = new ArrayList<String>();
    		if (args.length == 1 && sender.hasPermission("doublejump.admin")) list.add("reload");
    		return list;
        }
		return null;
    }
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		String n = e.getPlayer().getName();
		Double s = 0.2;
		if (!getConfig().getStringList("enabled-world").contains(p.getLocation().getWorld().getName())) return;
		if (p.hasPermission("doublejump.jump") && p.getGameMode() == GameMode.SURVIVAL) {
			if (p.isOnGround()) {
				if (System.currentTimeMillis() >= cooldown.getOrDefault(n, (long) 0)) p.setAllowFlight(true);
				inAir.put(n,false);
			} else if (this.getConfig().getBoolean("trace.enabled") && inAir.get(n)) for (int i = 0; i < this.getConfig().getInt("trace.count"); i++) p.spawnParticle(Particle.valueOf(this.getConfig().getString("trace.particle")),p.getLocation(), 0, getRandomRange(-s,s), getRandomRange(-s,s), getRandomRange(-s,s)); // in-trace
		}
	}
	
	@EventHandler
	public void onFly(PlayerToggleFlightEvent e) {
		Player p = e.getPlayer();
		String n = e.getPlayer().getName();
		Double s = 0.2;
		if (!getConfig().getStringList("enabled-world").contains(p.getLocation().getWorld().getName())) return;
		if (p.hasPermission("doublejump.jump") && p.getGameMode() == GameMode.SURVIVAL) {
			cooldown.put(n, System.currentTimeMillis() + 1500);
			inAir.put(n,true);
			e.setCancelled(true);
			p.setAllowFlight(false);
	        p.setFlying(false);
			p.setVelocity(p.getLocation().getDirection().multiply(this.getConfig().getDouble("jump-forces.forward")).setY(this.getConfig().getDouble("jump-forces.up")));
	        if (!this.getConfig().getString("sound").isEmpty()) p.playSound(p.getLocation(), Sound.valueOf(this.getConfig().getString("sound")), 1f, 1f);
			if (!this.getConfig().getString("messages.jump").isEmpty()) p.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.jump")));
			if (this.getConfig().getBoolean("start.enabled")) for (int i = 0; i < this.getConfig().getInt("start.count"); i++) p.spawnParticle(Particle.valueOf(this.getConfig().getString("start.particle")),p.getLocation(), 0, getRandomRange(-s,s), getRandomRange(-s,s), getRandomRange(-s,s)); // in-start
		}
	}
	
	public double getRandomRange(double min, double max) {
	    return (double) ((Math.random() * (max - min)) + min);
	}
	
}