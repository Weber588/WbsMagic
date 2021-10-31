package wbs.magic.spells;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.SpellCaster;

import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.string.WbsStrings;

@Spell(name = "Shield",
		cost = 3,
		cooldown = 10,
		description = "The caster creates a block shield 3 blocks in front of them, that moves as the caster turns their head. If the shield is set to bubble mode, a dome is formed instead."
)
@SpellSettings(isContinuousCast = true)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 300, aliases = {"max-duration"})
@SpellOption(optionName = "radius", type = SpellOptionType.INT, defaultInt = 4)
@SpellOption(optionName = "bubble", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "material", type = SpellOptionType.STRING, defaultString = "PURPLE_STAINED_GLASS", aliases = {"block"}, enumType = Material.class)
public class Shield extends SpellInstance {

	private final static Material DEFAULT_MATERIAL = Material.PURPLE_STAINED_GLASS;

	public Shield(SpellConfig config, String directory) {
		super(config, directory);
		
		maxDuration = config.getDouble("duration");
		radius = config.getInt("radius");

		bubble = config.getBoolean("bubble");

		String materialString = config.getString("material");
		Material checkMaterial = WbsEnums.getEnumFromString(Material.class, materialString);
		if (checkMaterial == null) {
			logError("Invalid material: " + materialString, directory + "/material");
			checkMaterial = DEFAULT_MATERIAL;
		}

		if (!checkMaterial.isBlock()) {
			logError("Material must be a block: " + materialString, directory + "/material");
			checkMaterial = DEFAULT_MATERIAL;
		}

		blockType = checkMaterial;
	}
	
	private final double maxDuration;
	private final boolean bubble;
	private final Material blockType;
	private final int radius;
	
	private final Particle aura = Particle.END_ROD;

	@Override
	public boolean cast(CastingContext context) {
		SpellCaster caster = context.caster;
		caster.stopCastingLater((int) maxDuration*20);
		
		boolean success;
		if (bubble) {
			success = bubble(caster);
		} else {
			success = segment(caster);
		}
		
		return success;
	}

	public boolean segment(SpellCaster caster) {
		Set<Location> initial = preShield(radius, bubble, caster, blockType);
		if (initial.isEmpty()) {
			return false;
		}

		fillBlocks(blockType, initial);
		
		WbsRunnable runnable = new WbsRunnable() {
			final Player player = caster.getPlayer();
			
			Set<Location> remove, add, step;
			Set<Location> current = new HashSet<>(initial);
			int spent = 0;
			
			Location playerLoc = player.getLocation();
			final World world = playerLoc.getWorld();
			@Override
            public void run() {
				playerLoc = player.getLocation();
				world.spawnParticle(aura, playerLoc, 15, 0.5, 0.5, 0.5, 0);
				
				if (caster.getCasting() != Shield.this || !player.isSneaking()) {
					cancel();
				} else if (current.isEmpty()) {
					caster.sendActionBar("&wNo valid spots!");
					cancel();
				} else if (!caster.spendMana(cost)) {
					caster.sendActionBar("&wOut of " + caster.manaName() + "!");
					cancel();
				} else {
					caster.sendActionBar("-" + caster.manaDisplay(spent));
				}
				
				if (isCancelled()) {
					caster.stopCasting();
					fillBlocks(Material.AIR, current);
					return;
				}
				
				// Get the elements in the new shield
				step = preShield(radius, bubble, caster, blockType);
				
				// Set "add" to all the blocks in the new shield that aren't in the old
				add = new HashSet<>(step);
				add.removeAll(current);
				
				// Set "remove" to all the blocks in the current shield that aren't in the new
				remove = new HashSet<>(current);
				remove.removeAll(step);

				fillBlocks(Material.AIR, remove);
				fillBlocks(blockType, add);
				
				current = step;
				
				spent += cost;
            }
			
			@Override
			protected void finish() {
				fillBlocks(Material.AIR, current);
			}
        };

		caster.setCasting(this, runnable);
		
		runnable.runTaskTimer(plugin, 5L, 5L);
		
        return true;
	}
	
	public boolean bubble(SpellCaster caster) {
		Set<Location> initial = preShield(radius, bubble, caster, blockType);
		if (initial.isEmpty()) {
			return false;
		}

		WbsRunnable runnable = new WbsRunnable() {
			final Player player = caster.getPlayer();
			
			int spent = 0;
			Location playerLoc = player.getLocation();
			final World world = playerLoc.getWorld();
			
			@Override
            public void run() {
				playerLoc = caster.getLocation();
				world.spawnParticle(aura, playerLoc, 15, 0.5, 0.5, 0.5, 0);
				
				if (caster.getCasting() != Shield.this) {
					cancel();
				} else if (!caster.spendMana(cost) || !player.isSneaking()) {
					cancel();
				} else {
					caster.sendActionBar("-" + caster.manaDisplay(spent));
				}
				
				if (isCancelled()) {
					caster.stopCasting();
					
				} else {
					fillBlocks(blockType, initial);
				}
				
				spent+=cost;
            }

			@Override
			protected void finish() {
				fillBlocks(Material.AIR, initial);
			}
        };

		runnable.runTaskTimer(plugin, 5L, 5L);

		caster.setCasting(this, runnable);

        return true;
	}
	

	private void fillBlocks(Material newBlock, Set<Location> blockSet) {
		for (Location loc : blockSet) {
			Block block = loc.getBlock();
			if (block.getType() != newBlock) {
				loc.getBlock().setType(newBlock);
			}
		}
	}

	private Set<Location> preShield(int radius, boolean bubble, SpellCaster caster, Material blockType) {
		Player player = caster.getPlayer();
		Location center = player.getLocation();
		Set<Location> blocksInRadius = new HashSet<>();
		Location loopLoc;
		for (int x = -radius; x <=radius; x++) {
			for (int y = -radius; y <=radius; y++) {
				for (int z = -radius; z <=radius; z++) {
					double distance = (new Vector(x, y, z)).length();
					// If distance is within 0.5 of radius (ensures 1 block thick)
					if (distance < radius + 0.5 && distance > radius - 0.5) {
						loopLoc = center.clone().add(x, y, z);
						blocksInRadius.add(loopLoc);
					}
				}
			}
		}
		if (bubble) {
			Set<Location> shieldBlocks = new HashSet<>();
			for (Location test : blocksInRadius) {
				Material testType = test.getBlock().getType();
				// Don't check for purple glass on a static bubble
				if (testType == Material.AIR || testType == Material.CAVE_AIR) {
					shieldBlocks.add(test);
				}
			}
			return shieldBlocks;
		} else {
			Location shieldCenter = center.clone().add(caster.getFacingVector(radius));
			Set<Location> shieldBlocks = new HashSet<>();
			for (Location test : blocksInRadius) {
				double distance = shieldCenter.distance(test);
				if (distance < radius - 1) {
					Material testType = test.getBlock().getType();
					if (testType == blockType || testType == Material.AIR || testType == Material.CAVE_AIR) {
						shieldBlocks.add(test);
					}
				}
			}
			return shieldBlocks;
		}
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rBlock: &7" + WbsStrings.capitalizeAll(WbsStrings.capitalizeAll(blockType.toString().replace('_', ' ')));
		asString += "\n&rMax duration: &7" + maxDuration + " seconds";
		asString += "\n&rRadius: &7" + radius;
		asString += "\n&rBubble? &7" + bubble;

		return asString;
	}
}
