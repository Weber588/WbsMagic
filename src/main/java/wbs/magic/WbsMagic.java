package wbs.magic;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import wbs.magic.command.MagicCommand;
import wbs.magic.listeners.*;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spells.SpellInstance;

import wbs.utils.util.plugin.WbsPlugin;

public class WbsMagic extends WbsPlugin {

	private final Logger logger = this.getLogger();
	
	private static WbsMagic instance;
	
	public static WbsMagic getInstance() {
		return instance;
	}
	
	public MagicSettings settings = new MagicSettings(this);
	
	@Override
	public void onEnable() {
		instance = this;
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		SpellInstance.setPlugin(this);
		MagicObject.setPlugin(this);
		SpellCaster.setPlugin(this);
		
		settings.reload();

		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(new WandListener(this), this);
		pm.registerEvents(new PassivesListener(this), this);
		pm.registerEvents(new SpellListener(this), this);
		pm.registerEvents(new CasterListener(), this);
		pm.registerEvents(new HelperEventListener(), this);

	//	SpellCaster.loadSpellCasters();
		
	//	getCommand("magic").setExecutor(new MagicCommand(this));
		MagicCommand command = new MagicCommand(this, getCommand("magic"));
	}
	
	@Override
	public void onDisable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (SpellCaster.isRegistered(player)) {
				SpellCaster caster = SpellCaster.getCaster(player);

				caster.forceStopCasting();
				caster.stopConcentration();
			}
		}

		// Wrap collection to avoid concurrent modification
		List<MagicObject> magicObjects = new LinkedList<>(MagicObject.getAllActive());

		magicObjects.forEach(obj -> obj.remove(true));
	}
}
