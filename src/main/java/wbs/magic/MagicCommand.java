package wbs.magic;

import java.time.Duration;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import org.jetbrains.annotations.NotNull;
import wbs.magic.enums.WandControl;
import wbs.magic.passives.PassiveEffect;
import wbs.magic.passives.PassiveEffectType;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.spells.RegisteredSpell;
import wbs.magic.spells.SpellConfig;
import wbs.magic.spells.SpellManager;
import wbs.magic.wrappers.MagicWand;
import wbs.magic.wrappers.SpellCaster;

import wbs.utils.util.WbsEnums;
import wbs.utils.util.plugin.WbsMessenger;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

public class MagicCommand extends WbsMessenger implements CommandExecutor, TabCompleter {
	
	private final MagicSettings settings;
	protected MagicCommand(WbsMagic plugin) {
		super(plugin);

		settings = plugin.settings;
	}

	private boolean checkPermission(SpellCaster caster, String permission) {
		Player player = caster.getPlayer();
		if (player.isOp()) {
			return true;
		}
		if (!player.hasPermission(permission)) {
			caster.sendMessage("&cYou are lacking the permission node: &7" + permission);
			return false;
		}
		return true;
	}
	private boolean checkAllPermissions(CommandSender sender, String permissionStartsWith) {
		for (PermissionAttachmentInfo perm : sender.getEffectivePermissions()) {
			if (perm.getPermission().startsWith(permissionStartsWith)) {
				return true;
			}
		}
		return sender.isOp();
	}
	
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
    	 if (!(sender instanceof  Player)) {
      		switch (args[0].toUpperCase()) {
      		case "GIVEWAND":
      		case "GIVE":
      		case "WAND":
      			Player target;
      			MagicWand wand;
      			if (args.length < 3) {
      				sender.sendMessage(plugin.prefix + "Usage: /magic wand <wandName> <player>");
      				return true;
      			}
  				if (MagicWand.wandExists(args[1])) {
  					wand = MagicWand.getWand(args[1]);
  				} else {
  					sender.sendMessage(plugin.prefix + "Invalid wand name.");
  					return true;
  				}
      				
  				target = Bukkit.getPlayer(args[2]);
  				if (target == null) {
  					sender.sendMessage(plugin.prefix + "Player not found.");
 					return true;
  				}
      				
  				sender.sendMessage(plugin.prefix + "Giving " + target.getName() + " a " + wand.getDisplay());
      			ItemStack wandItem = wand.buildNewWand();
      			target.getInventory().addItem(wandItem);
      			return true;

      		case "MANA":
      		case "SETMANA":
  			
      			// Defaults:
      			int amount;
      			// argument builder
      			if (args.length < 3) {
      				return true;
      			}
      			
  				try {
  					amount = Integer.parseInt(args[1]);
 				} catch (NumberFormatException e) {
 					sender.sendMessage(plugin.prefix + "Usage: /magic " + args[0] + " <integer> [player]");
 					return true;
 	         	}

				target = Bukkit.getPlayer(args[2]);

  				if (target == null) {
  					sender.sendMessage(plugin.prefix + "The player \"" + args[2] + "\" was not found. Please use the player's full username.");
  	     			return true;
  				}

      			SpellCaster targetCaster = SpellCaster.getCaster(target);
      			targetCaster.setMana(amount);
      			plugin.sendMessage(plugin.prefix + "Set " + target.getName() + "'s mana to &h" + amount + ".", sender);
      			return true;
      		default:
      			plugin.sendMessage(plugin.prefix + "This command is not defined for console.", sender);
      			return true;
      		}
    	 } else {
         	Player player = (Player) sender;
         	SpellCaster caster = SpellCaster.getCaster(player);
        	if (args.length == 0) {
        		usage(caster);
        		return true;
        	}
         	final int length = args.length;
 			int level;
 			MagicWand wand;
 			ItemStack wandItem;
 			Player target;
 			if (checkAllPermissions(sender, "wbsmagic.admin")) {
 				
 				ArrayList<String> errors;
 				int page = 1;
 				int index = 1;
 				int pages;
 				
	     		switch (args[0].toUpperCase()) {
	     		case "LISTWANDS":
	     		case "WANDS":
	     			if (checkPermission(caster, "wbsmagic.admin.listwands")) {
		     			Set<String> nameSet = MagicWand.getWandNames();
		     			String list = "";
		     			for (String name : nameSet) {
		     				list += name + ", ";
		     			}
		     			list = list.substring(0, list.length() - 2);
		     			caster.sendMessage("All wand types: &b" + list);
	     			}
	     			return true;
	     		case "GIVEWAND":
	     		case "GIVE":
	     		case "WAND":
	     			if (checkPermission(caster, "wbsmagic.admin.givewand")) {
		     			target = player;
		     			wand = null;
		     			switch (length) {
		     			case 1:
		     				caster.sendMessage("Usage: &b/magic wand [wandName]");
		     				return true;
		     			case 2:
		     				if (MagicWand.wandExists(args[1])) {
		     					wand = MagicWand.getWand(args[1]);
		     				} else {
		     					caster.sendMessage("Invalid wand name; do &b/magic wands&r for a list.");
		     					return true;
		     				}
		     				
		     				target = player;
		     				break;
		     			case 3:
		     				if (MagicWand.wandExists(args[1])) {
		     					wand = MagicWand.getWand(args[1]);
		     				} else {
		     					caster.sendMessage("Invalid wand name; do &b/magic wands&r for a list.");
		     					return true;
		     				}
		     				
		     				target = Bukkit.getPlayer(args[2]);
		     				if (target == null) {
		 						caster.sendMessage("Player not found.");
		 						return true;
		     				}
		     				break;
		     			}
	     			
		     			caster.sendMessage("Giving &b" + target.getName() + " &ra &b" + wand.getDisplay() + "&r wand");
		     			wandItem = wand.buildNewWand();
		     			target.getInventory().addItem(wandItem);
		     			
	     			}
	     			return true;
	     		case "RESET":
	     			if (checkPermission(caster, "wbsmagic.admin.reset")) {
		     			// Defaults:
		     			target = player;
		     			// argument builder
		     			if (length >= 2) {
							target = Bukkit.getPlayerExact(args[1]);

		     				if (target == null) {
		     	     			caster.sendMessage("The player &b\"" + args[1] + "\"&r was not found. Please use the player's full username.");
		     	     			return true;
		     				}
		     			}
		     			caster.sendMessage("Resetting &b" + target.getName() + " &eto default caster.");
		     			SpellCaster.resetCaster(target);
	     			}

	     			return true;
	     		case "RELOAD":
	     			if (checkPermission(caster, "wbsmagic.admin.reload")) {
	     				settings.reload();

	     				errors = settings.getErrors();
	     				if (errors.isEmpty()) {
	     					caster.sendMessage("&aReload successful!");
	     					return true;
	     				} else {
	     					caster.sendMessage("&wThere were " + errors.size() + " config errors. Do &h/magic errors&w to see them.");
	     				}
	     				
	     			}
	     			return true;
	     		case "ERROR":
	     		case "ERRORS":
	     			if (checkPermission(caster, "wbsmagic.admin.errors")) {
	     				errors = settings.getErrors();
	     				if (errors.isEmpty()) {
	     					caster.sendMessage("&aThere were no errors in the last reload.");
	     					return true;
	     				}
	     				if (args.length > 1) {
	     					try {
	     						page = Integer.parseInt(args[1]);
	     					} catch (Exception e) {
	     						caster.sendMessage("Usage: &b/magic errors [page]");
	     						return true;
	     					}
	     				}
	     				page--;
						int ENTRIES_PER_PAGE = 5;
						pages = errors.size() / ENTRIES_PER_PAGE;
	     				if (errors.size() % ENTRIES_PER_PAGE != 0) {
	     					pages++;
	     				}
	     				caster.sendMessage("Displaying page " + (page+1) + "/" + pages + ":");
	     				for (String error : errors) {
	     					if (index > page* ENTRIES_PER_PAGE && index <= (page+1)*(ENTRIES_PER_PAGE)) {
	     						caster.sendMessage("&6" + index + ") " + error);
	     					}
	     					index++;
	     				}
	     			}

	     			return true;
	     		case "MANA":
	     		case "SETMANA":
	     			if (checkPermission(caster, "wbsmagic.admin.setmana")) {
		     			// Defaults:
		     			int amount = 0;
		     			target = player;
		     			// argument builder
		     			if (length >= 2) {
		     				try {
		     					amount = Integer.parseInt(args[1]);
		    				} catch (NumberFormatException e) {
		    					caster.sendMessage("Usage: &b/magic " + args[0] + " <integer> [player]");
		    					e.printStackTrace();
		    					return false;
		    	         	}
		     			}
		     			if (length >= 3) {
							target = Bukkit.getPlayer(args[2]);

		     				if (target == null) {
		     	     			caster.sendMessage("The player &b\"" + args[2] + "\"&r was not found. Please use the player's full username.");
		     	     			return true;
		     				}
		     			}
		     			SpellCaster targetCaster = SpellCaster.getCaster(target);
		     			targetCaster.setMana(amount);
		     			caster.sendMessage("Set &b" + target.getName() + "&r's mana to &b" + amount + "&r.");
			     	}
	     			return true;
				case "CAST":
					if (checkPermission(caster, "wbsmagic.admin.cast")) {
						if (length == 1) {
							caster.sendMessage("Usage: &b/mag cast <spellType> [-option1 <value1> [-option2 <value2>...]] ");
							return true;
						}

						String spellName = args[1];
						RegisteredSpell spell;
						try {
							spell = SpellManager.getSpell(spellName);
						} catch (IllegalArgumentException e) {
							caster.sendMessage("Invalid spell: " + spellName);
							return true;
						}

						SpellConfig config = new SpellConfig(spell);

						// Stop 1 before the end, as that should be a value, not a key.
						for (int i = 1; i < args.length - 1; i++) {
							if (args[i].startsWith("-")) {
								boolean valueFound = false;
								for (String key : config.getBoolKeys()) {
									if (args[i].equalsIgnoreCase("-" + key)) {
										config.set(key, Boolean.parseBoolean(args[i + 1]));
										valueFound = true;
										break;
									}
								}
								if (valueFound) continue;

								for (String key : config.getIntKeys()) {
									if (args[i].equalsIgnoreCase("-" + key)) {
										int foundValue;
										try {
											foundValue = Integer.parseInt(args[i + 1]);
										} catch (NumberFormatException e) {
											caster.sendMessage("Invalid int " + args[i + 1] + " for key " + args[i]);
											return true;
										}
										config.set(key, foundValue);
										valueFound = true;
										break;
									}
								}
								if (valueFound) continue;

								for (String key : config.getDoubleKeys()) {
									if (args[i].equalsIgnoreCase("-" + key)) {
										double foundValue;
										try {
											foundValue = Double.parseDouble(args[i + 1]);
										} catch (NumberFormatException e) {
											caster.sendMessage("Invalid double " + args[i + 1] + " for key " + args[i]);
											return true;
										}
										config.set(key, foundValue);
										valueFound = true;
										break;
									}
								}
								if (valueFound) continue;

								for (String key : config.getStringKeys()) {
									if (args[i].equalsIgnoreCase("-" + key)) {
										config.set(key, args[i + 1]);
										valueFound = true;
										break;
									}
								}

								if (valueFound) {
									i++; // Skip next section; already checked
								}
							}
						}

						SpellInstance spellInstance = config.buildSpell("Custom");

						spellInstance.cast(caster);
					}
					return true;
	     		}
	    	}
 		// Normal user commands
 	 		switch (args[0].toUpperCase()) {
 	 		case "HELP":
				if (length == 1) {
					help(sender, 1);
				} else {
					try {
						help(sender, Integer.parseInt(args[1]));
					} catch (NumberFormatException e) {
						caster.sendMessage("Usage: &b/mag help [page number]");
						return true;
					}
				}
 				
 	 			break;
 	 		case "FULLINFO":
     			if (checkPermission(caster, "wbsmagic.fullinfo")) {
     				int infoTier = 1;
     				
     				if (length == 1) {
	 	     			ItemStack item = player.getInventory().getItemInMainHand();
	 	    			wand = MagicWand.getWand(item);
	 	    			
	 	    			if (wand == null) {
 	    					caster.sendMessage("Usage: &h/magic fullinfo [wand name] [tier]&r. Alternatively, hold a wand and do &h/magic fullinfo [tier]");
						} else {
	 	    				if (wand.getMaxTier() != 1) {
	 	    					caster.sendMessage("Usage: &h/magic fullinfo [tier]");
							} else {
	 	    					showInfo(wand, caster, true, 1);
							}
						}
						return true;
					}
	 	 			if (length == 2) {
	 	     			ItemStack item = player.getInventory().getItemInMainHand();
	 	    			wand = MagicWand.getWand(item);
	 	    			try {
	 	    				infoTier = Integer.parseInt(args[1]);
	 	    				
		 	    			if (wand == null) {
	 	    					caster.sendMessage("You are not holding a valid wand; if you wish to look up info about a specific wand, use &b/magic fullinfo <wand name> [tier]"); 
		 	    				return true;
		 	    			}
	 	    			} catch (NumberFormatException e) { // User is trying to use a wand name
		 	 				if (MagicWand.wandExists(args[1])) {
		 	 					wand = MagicWand.getWand(args[1]);
		 	 					
		 	 					if (wand.getMaxTier() != 1) {
			 	 					caster.sendMessage("That wand has multiple tiers; Do &h/magic fullinfo " + args[1] + " <tier>");
			 	 					return true;
		 	 					}
		 	 				} else {
		 	 					caster.sendMessage("Invalid wand name; do &b/magic wands&r for a list. (Or hold a wand)");
		 	 					return true;
		 	 				}
	 	    			}
	 	    			
	 	 			} else {
	 	 				if (MagicWand.wandExists(args[1])) {
	 	 					wand = MagicWand.getWand(args[1]);
	 	 				} else {
	 	 					caster.sendMessage("Invalid wand name; do &b/magic wands&r for a list. (Or hold a wand)");
	 	 					return true;
	 	 				}

	 	 				if (wand.getMaxTier() != 1) {
		 	 				try {
		 	    				infoTier = Integer.parseInt(args[1]);
		 	 				} catch (NumberFormatException e) {
	 	    					caster.sendMessage("Usage: &h/magic fullinfo " + args[1] + " [tier]"); 
		 	    				return true;
		 	 				}
	 	 				}
	 	 			}
	 	 			
	 	 			if (infoTier > wand.getMaxTier()) {
 	 					caster.sendMessage("That tier is too high. This wand's maximum tier is " + wand.getMaxTier());
 	 					return true;
	 	 			}
	
 					showInfo(wand, caster, true, infoTier);
     			}
 	 			break;
 	 		case "INFO":
     			if (checkPermission(caster, "wbsmagic.info")) {
	 	 			if (length == 1) {
	 	     			ItemStack item = player.getInventory().getItemInMainHand();
	 	    			wand = MagicWand.getWand(item);
	 	    			
	 	    			if (wand == null) {
 	    					caster.sendMessage("You are not holding a valid wand; if you wish to look up info about a specific wand, use &b/magic info [wand name]"); 
	 	    				return true;
	 	    			}
	 	 			} else {
	 	 				if (MagicWand.wandExists(args[1])) {
	 	 					wand = MagicWand.getWand(args[1]);
	 	 				} else {
	 	 					caster.sendMessage("Invalid wand name; do &b/magic wands&r for a list. (Or hold a wand)");
	 	 					return true;
	 	 				}
	 	 			}
	 	 			
 					showInfo(wand, caster, false, 1);
     			}
 	 			break;
 	 		case "GUIDE":
     			if (checkPermission(caster, "wbsmagic.guide")) {
	 	 			// Defaults:
	 	 			if (length == 1) {
	 	 				caster.sendMessage("Welcome to WbsMagic!");
	 	 				caster.sendMessage("This plugin adds a configurable wands & spells, which enable survival players to cast a variety of spells for fighting, moving, or other useful functions.");
	 	 				caster.sendMessage("The wands only use simple and unique combinations of key strokes to avoid players needing to memorize complex spells that use only mouse clicks in certain patterns.");
	 	 				caster.sendMessage("Assuming you use default Minecraft controls, you will be using combinations of Left click, Right click, Shift, and Q to interact with wands.");
	 	 				caster.sendMessage("For a guide on how to cast spells, do &b/magic guide controls&e.");
	 	 				caster.sendMessage("To look up a specific spell, do &b/magic guide spell <SpellName>&e.");
	 	 				caster.sendMessage("To see what spells a wand can cast, do &b/magic info [wand name]&e.");
	 	     		} else {
	 	     			switch (args[1].toUpperCase()) {
	 	     			case "SPELL":
	 	     				if (length == 2) {
	 	     					String spellString = null;

								spellString = String.join(", ", SpellManager.getSpellNames());
	 	     					caster.sendMessage("Usage: &b/magic guide spell <SpellName>");
	 	     					caster.sendMessage("Please choose from the following list: &b" + spellString);
	 	     				} else {
								String spellInput;
								String[] spellStrings = new String[length - 2];
								System.arraycopy(args, 2, spellStrings, 0, length - 2);
								spellInput = String.join(" ", spellStrings).toUpperCase();

								RegisteredSpell registeredSpell;

								try {
									registeredSpell = SpellManager.getSpell(spellInput);
									spellGuide(registeredSpell, caster);
								} catch (IllegalArgumentException e) {
									for (String spellName : SpellManager.getSpellNames()) {
										if (spellName.toUpperCase().contains(spellInput.toUpperCase())) {
											spellGuide(SpellManager.getSpell(spellName), caster);
											return true;
										}
									}
									caster.sendMessage("Spell not found.");
								}
							}
	 	     				break;
	 	     			case "CONTROLS":
	 	     				controlGuide(sender);
							break;
						}
	 	     		}
     			}
 	 			break;
 	 		default:
 	 			usage(caster);
 	 		}
    	 }
 		
    	 return true;
    }
    	 
    private void showInfo(MagicWand wand, SpellCaster caster, boolean full, int infoTier) {
    	CommandSender sender = caster.getPlayer();
    	if (full) {
			Map<Integer, Map<WandControl, SpellInstance>> bindings = wand.bindingMap();
			caster.sendMessage("&m   &r== " + wand.getDisplay() + "&r ==&m   ");

			Map<WandControl, SpellInstance> tiersBindings = bindings.get(infoTier);
			if (tiersBindings != null) {
				caster.sendMessage("&5&m  &r Tier " + infoTier + " &5&m  ");
				
				for (WandControl control : tiersBindings.keySet()) {
					sendMessageNoPrefix(WbsEnums.toPrettyString(control) + ": &h" + tiersBindings.get(control).toString().replaceAll("\n", "\n    "), sender);
				}
			}

			Map<PassiveEffectType, PassiveEffect> passives = wand.passivesMap();
			if (passives != null && !passives.isEmpty()) {
				caster.sendMessage("&5&m  &r Passives &5&m  ");
				
				for (PassiveEffectType effectType : passives.keySet()) {
					sendMessageNoPrefix(passives.get(effectType).toString().replaceAll("\n", "\n&r  - &h"), sender);
 				}
			}
    	} else {
			Map<Integer, Map<WandControl, SpellInstance>> bindings = wand.bindingMap();
			caster.sendMessage("&m   &r== " + wand.getDisplay() + "&r ==&m   ");
			Set<Integer> tierSet = bindings.keySet();
			boolean showTierInfo = tierSet.size() > 1;
			if (showTierInfo) {
			caster.sendMessage("To change tier, drop your wand &owithout&r shifting!");
			}
			for (int tier : tierSet) {
				Map<WandControl, SpellInstance> tiersBindings = bindings.get(tier);
				if (showTierInfo) {
				caster.sendMessage("&5&m  &r Tier " + tier + " &5&m  ");
				}
				for (WandControl control : tiersBindings.keySet()) {
					sendMessageNoPrefix(WbsEnums.toPrettyString(control) + ": &h" + tiersBindings.get(control).simpleString(), sender);
				}
			}
			
			Map<PassiveEffectType, PassiveEffect> passives = wand.passivesMap();
			if (passives != null && !passives.isEmpty()) {
				caster.sendMessage("&5&m  &r Passives &5&m  ");
				
				for (PassiveEffect effect : passives.values()) {
					sendMessageNoPrefix(effect.toString().replaceAll("\n", "\n    "), sender);
 				}
			}
    	}
    }
    

	private void help(CommandSender sender, int page) {
		sendMessage("&m     &0 (&5Magic&0) &r&m     ", sender);
		switch (page) {
		case 1:
			sendMessage("&h/magic&r:", sender);
			sendMessage("View your mana and the command for this menu.", sender);
			
			sendMessage("&h/magic help [page]&r:", sender);
			sendMessage("Display this and other help screens.", sender);
			
			sendMessage("&h/magic givewand <wand name> [player]&r:", sender);
			sendMessage("Give yourself or another player a specific wand.", sender);
			
			sendMessage("&h/magic guide [spell|controls]&r:", sender);
			sendMessage("Display this and other help screens.", sender);
			
			sendMessage("&h/magic info [wand name]&r:", sender);
			sendMessage("View what spells a wand can cast, and how to cast them", sender);
			break;
		default:
			help(sender, 1);
		}
	}
    
    private void usage(SpellCaster caster) {
    	caster.sendMessage("You currently have &b" + caster.getMana() + " &emana!");
    	caster.sendMessage("For help with commands, use &b/magic help&e. For help with spells or controls, use &b/magic guide&e.");
    }
    
    private void controlGuide(CommandSender sender) {
    	final String lineBreak = "&8==========================";
    	sendMessage(lineBreak, sender);
    	for (WandControl control : WandControl.values()) {
        	sendMessage("&h" + WbsStrings.capitalizeAll(control.name().replace('_', ' ')) + "&r:", sender);
        	sendMessage(control.getDescription(), sender);
    	}
    	sendMessage(lineBreak, sender);
    	/*
	PUNCH, SHIFT_PUNCH,
	RIGHT_CLICK, SHIFT_RIGHT_CLICK,
	PUNCH_ENTITY, RIGHT_CLICK_ENTITY,
	SHIFT_DROP;
    	 */
    }
    
    private void spellGuide(RegisteredSpell spell, SpellCaster caster) {
    	final String lineBreak = "&8==========================";
    	if (spell != null) {
    		caster.sendMessage(lineBreak);
    		caster.sendMessage("&hSpell name: &r" + spell.getName());
    		caster.sendMessage("&bDescription: &e" + spell.getSpell().description());

    		if (spell.getFailableSpell() != null) {
				caster.sendMessage("&bCan fail? &eYes. " + spell.getFailableSpell().value());
    		} else {
    			caster.sendMessage("&bCan fail? &eNo.");
    		}
			if (spell.getSettings() != null && spell.getSettings().canBeConcentration()) {
    			caster.sendMessage("&bConcentration spell? &eYes. This spell cannot be cast when another concentration spell is in use. If the spell combination requires the caster to crouch while casting, they must continue to crouch or the spell will end.");
    		} else {
    			caster.sendMessage("&bConcentration spell? &eNo. This spell may be cast at any time, even if the caster is concentrating on another spell.");
    		}
    		caster.sendMessage(lineBreak);
    	}
    }
    
    @Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		List<String> choices = new ArrayList<>();

		if (args.length > 1) {
			if (args[0].equalsIgnoreCase("cast")) {
				if (sender.hasPermission("wbsmagic.admin.cast")) {
					return handleCastTab(args);
				}
			}
		}

    	switch (args.length) {
    	case 1:
    		String[] adminChoices = {"errors", "reload", "reset", "setlevel", "setmana", "givewand", "listwands", "cast"};
    		for (String option : adminChoices) {
    			if (sender.hasPermission("wbsmagic.admin." + option)) {
        			choices.add(option);
    			}
    		}
    		
        	String[] defaultChoices = {"guide", "help", "info"};

			choices.addAll(Arrays.asList(defaultChoices));
    		
    		break;
    	case 2:
    		switch (args[0].toUpperCase()) {
    		case "GIVEWAND":
    		case "WAND":
    		case "INFO":
    			if (sender.hasPermission("wbsmagic.admin.listwands")) {
					choices.addAll(MagicWand.getWandNames());
    			}
    			break;
    		case "GUIDE":
    			if (sender.hasPermission("wbsmagic.guide.controls")) {
    				choices.add("controls");
    			}
    			if (sender.hasPermission("wbsmagic.guide.spell")) {
    				choices.add("spell");
    			}
    			break;
    		case "RESET":
    			if (sender.hasPermission("wbsmagic.admin.reset")) {
    				for (Player player : Bukkit.getOnlinePlayers()) {
    					choices.add(player.getName());
    				}
    			}
    			break;
    		}
    		break;
    	case 3:
    		switch (args[0].toUpperCase()) {
    		case "GIVEWAND":
    		case "WAND":
    		case "INFO":
    			if (sender.hasPermission("wbsmagic.admin.givewand")) {
    				for (Player player : Bukkit.getOnlinePlayers()) {
    					choices.add(player.getName());
    				}
    			}
    			break;
    		case "GUIDE":
    			switch (args[1].toUpperCase()) {
    			case "SPELL":
					choices.addAll(SpellManager.getSpellNames());
    				break;
         		case "LEVEL":
         		case "SETLEVEL":
        			if (sender.hasPermission("wbsmagic.admin.setlevel")) {
        				for (Player player : Bukkit.getOnlinePlayers()) {
        					choices.add(player.getName());
        				}
        			}
         			
         			break;
         		case "MANA":
         		case "SETMANA":
        			if (sender.hasPermission("wbsmagic.admin.setmana")) {
        				for (Player player : Bukkit.getOnlinePlayers()) {
        					choices.add(player.getName());
        				}
        			}
         			
         			break;
    			}
    			break;
    		}
    		break;
    	}
    	
    	List<String> result = new ArrayList<>();
		for (String add : choices) {
    		if (add.toLowerCase().startsWith(args[args.length-1].toLowerCase())) {
    			result.add(add);
    		}
		}
    	
    	return result;
	}


	private List<String> handleCastTab(String[] args) {
		List<String> choices = new ArrayList<>();

		if (args.length == 2) {
			for (String spellName : SpellManager.getSpellNames()) {
				choices.add(spellName.replace(' ', '_')); // Gets undone in the getSpell call
			}
			return filterCastTabs(choices, args);
		}

		String spellName = args[1];
		RegisteredSpell spell;
		try {
			spell = SpellManager.getSpell(spellName);
		} catch (IllegalArgumentException e) {
			return choices;
		}

		SpellConfig config = spell.getDefaultConfig();

		boolean valueSuggested = false;
		for (String key : config.getBoolKeys()) {
			// If previous arg is a key
			if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
				choices.clear(); // Remove previous as keys may have been added, but this is a value slot
				choices.add("true");
				choices.add("false");
				valueSuggested = true;
				break;
			} else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
				choices.add("-" + key);
				// Don't break; might be multiple matches
			}
		}

		if (!valueSuggested) {
			for (String key : config.getDoubleKeys()) {
				// If previous arg is a key
				if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
					choices.clear(); // Remove previous as keys may have been added, but this is a value slot
					choices.add("0.5");
					choices.add("1.0");
					choices.add("5.0");
					choices.add("10.0");
					valueSuggested = true;
					break;
				} else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
					choices.add("-" + key);
					// Don't break; might be multiple matches
				}
			}
		}

		if (!valueSuggested) {
			for (String key : config.getIntKeys()) {
				// If previous arg is a key
				if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
					choices.clear(); // Remove previous as keys may have been added, but this is a value slot
					choices.add("1");
					choices.add("5");
					choices.add("10");
					choices.add("25");
					valueSuggested = true;
					break;
				} else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
					choices.add("-" + key);
					// Don't break; might be multiple matches
				}
			}
		}
		if (!valueSuggested) {
			for (String key : config.getStringKeys()) {
				// If previous arg is a key
				if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
					choices.clear(); // Remove previous as keys may have been added, but this is a value slot

					if (key.equalsIgnoreCase("targetter") ||
						key.equalsIgnoreCase("targeter") ||
						key.equalsIgnoreCase("target"))
					{
						choices.add("radius");
						choices.add("line_of_sight");
						choices.add("random");
						choices.add("self");
					}
					break;
				} else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
					choices.add("-" + key);
					// Don't break; might be multiple matches
				}
			}
		}

		return filterCastTabs(choices, args);
	}

	private List<String> filterCastTabs(List<String> choices, String[] args) {
		List<String> result = new ArrayList<>();
		String currentArg = args[args.length - 1];
		for (String add : choices) {
			if (add.toLowerCase().startsWith(currentArg.toLowerCase())) {
				if (!add.startsWith("-")) { // If it's not a key, add it
					result.add(add);
				} else { // If it is a key, check for duplicates & wand-related keys
					boolean foundMatch = false;
					for (String arg : args) {
						if (add.equalsIgnoreCase(arg)) {
							foundMatch = true;
							break;
						}
					}
					if (!foundMatch) {
						// Don't include
						if (!(add.equalsIgnoreCase("-consume") ||
								add.equalsIgnoreCase("-concentration") ||
								add.equalsIgnoreCase("-cooldown") ||
								add.equalsIgnoreCase("-custom-name") ||
								add.equalsIgnoreCase("-cost")))
						{
							result.add(add);
						}
					}
				}
			}
		}

		return result;
	}
}