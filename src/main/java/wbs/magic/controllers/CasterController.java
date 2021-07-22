package wbs.magic.controllers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import wbs.magic.objects.generics.MagicObject;
import wbs.magic.wrappers.SpellCaster;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class CasterController implements Listener {

	@EventHandler
	public void onDisconnect(PlayerQuitEvent event) {
		if (SpellCaster.isRegistered(event.getPlayer())) {
			SpellCaster caster = SpellCaster.getCaster(event.getPlayer());

			caster.forceStopCasting();
			caster.stopConcentration();

			// Wrap collection to avoid concurrent modification
			List<MagicObject> magicObjects = new LinkedList<>(MagicObject.getAllActive(caster));

			magicObjects.forEach(obj -> obj.remove(true));
		}
	}
}
