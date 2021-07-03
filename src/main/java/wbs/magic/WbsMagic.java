package wbs.magic;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import wbs.magic.command.MagicCommand;
import wbs.magic.controllers.CasterController;
import wbs.magic.controllers.PassivesController;
import wbs.magic.controllers.SpellController;
import wbs.magic.controllers.WandController;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.wrappers.SpellCaster;

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
		pm.registerEvents(new WandController(this), this);
		pm.registerEvents(new PassivesController(this), this);
		pm.registerEvents(new SpellController(this), this);
		pm.registerEvents(new CasterController(), this);

	//	SpellCaster.loadSpellCasters();
		
	//	getCommand("magic").setExecutor(new MagicCommand(this));
		new MagicCommand(this, getCommand("magic"));
	}
	
	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			SpellCaster caster = SpellCaster.getCaster(p);
			caster.stopConcentration();
			caster.forceStopCasting();
		}
	//	SpellCaster.saveSpellCasters();
	}
}
