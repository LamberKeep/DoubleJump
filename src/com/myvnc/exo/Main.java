package com.myvnc.exo;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
	}

	//Events
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("doublejump")) {	
			if (!sender.hasPermission("doublejump.admin")) {
				sender.sendMessage(ChatColor.RED+"You dont have permission to do that!");
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "Usage: /doublejump reload");
			} else {
				if (args[0].equalsIgnoreCase("reload")) {
					this.reloadConfig();
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("reload-message")));
				}
			}
		}
		return false;
	}
	
	@EventHandler
	public void onJump(PlayerMoveEvent e) {
		Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE) return;
        if (p.isFlying()) return;
        if (p.getLocation().subtract(0.0, 1.0D, 0.0D).getBlock().getType() != Material.AIR) {
        	p.setAllowFlight(true);
        }
	}

	@EventHandler
	public void onFly(PlayerToggleFlightEvent e) {
		Player p = e.getPlayer();
		if (p.hasPermission("doublejump.jump") && p.getGameMode () == GameMode.ADVENTURE) {				
	        	e.setCancelled(true);
				p.setAllowFlight(false);
		        p.setFlying(false);
		        p.spawnParticle(Particle.valueOf(this.getConfig().getString("type-particles")), p.getLocation(), this.getConfig().getInt("number-particles"));
		        p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 1f, 1f);
				p.setVelocity(p.getLocation().getDirection().multiply(1.6d).setY(1));
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("jump-message")));
		}
	}

}