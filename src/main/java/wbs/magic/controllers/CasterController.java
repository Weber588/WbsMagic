package wbs.magic.controllers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import wbs.magic.wrappers.SpellCaster;

public class CasterController implements Listener {

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		if (SpellCaster.isRegistered(event.getPlayer())) {
			SpellCaster caster = SpellCaster.getCaster(event.getPlayer());

			caster.forceStopCasting();
			caster.stopConcentration();
		}
	}
}
