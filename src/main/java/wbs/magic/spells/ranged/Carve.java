package wbs.magic.spells.ranged;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.SpellCaster;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.VersionUtil;
import wbs.utils.util.WbsColours;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsSound;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.pluginhooks.WbsRegionUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Spell(name = "Carve",
        cost = 5,
        cooldown = 0,
        description = "Shoots a beam that breaks blocks it hits."
)
@FailableSpell("If there are no breakable blocks in range, the spell will fail.")
@SpellOption(optionName = "energy", type = SpellOptionType.INT, defaultInt = 8, aliases = {"max-blocks"})
@SpellOption(optionName = "stop-at-air", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "whitelist", type = SpellOptionType.STRING, defaultString = "")
@SpellOption(optionName = "blacklist", type = SpellOptionType.STRING, defaultString = "")
@SpellOption(optionName = "colour", type = SpellOptionType.STRING, defaultString = "purple", aliases = {"color"}, enumType = DyeColor.class)

@SpellOption(optionName = "do-drops", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "scale-energy", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "do-break-particles", type = SpellOptionType.BOOLEAN, defaultBool = true)
@SpellOption(optionName = "do-sounds", type = SpellOptionType.BOOLEAN, defaultBool = true)
// Overrides
@SpellOption(optionName = "range", type = SpellOptionType.DOUBLE, defaultDouble = 5)
public class Carve extends RangedSpell {
    private final static List<Material> FORCE_BLACKLIST = new LinkedList<>();

    static {
        Tag<Material> materialTag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft("wither_immune"), Material.class);
        if (materialTag == null) {
            FORCE_BLACKLIST.addAll(Arrays.asList(
                    Material.BEDROCK,
                    Material.BARRIER
            ));
        } else {
            FORCE_BLACKLIST.addAll(materialTag.getValues());
        }
    }

    public Carve(SpellConfig config, String directory) {
        super(config, directory);

        energy = config.getInt("energy");
        stopAtAir = config.getBoolean("stop-at-air");
        doDrops = config.getBoolean("do-drops");
        doBreakParticles = config.getBoolean("do-break-particles");
        doSounds = config.getBoolean("do-sounds");
        scaleEnergy = config.getBoolean("scale-energy");

        String whitelistString = config.getString("whitelist");
        usingWhitelist = whitelistString != null && !whitelistString.equalsIgnoreCase("");
        whitelist = parseMaterialListString(whitelistString, directory + "/whitelist");
        whitelistTags = parseTagListString(whitelistString, directory + "/whitelist");

        String blacklistString = config.getString("blacklist");
        blacklist = parseMaterialListString(blacklistString, directory + "/blacklist");
        blacklistTags = parseTagListString(blacklistString, directory + "/blacklist");

        String colourString = config.getString("colour");
        colour = WbsColours.fromHexOrDyeString(colourString, Color.fromRGB(0xaa00ee));

        lineEffect.setScaleAmount(true);
        lineEffect.setAmount(6);

        Particle.DustOptions options = new Particle.DustOptions(colour, 0.7f);
        lineEffect.setOptions(options);

        breakEffect.setAmount(50);
        breakEffect.setSpeed(0);
        breakEffect.setXYZ(0.3);
    }

    private final int energy;
    private final Color colour;
    private final boolean stopAtAir;
    private final boolean usingWhitelist;
    private final boolean doDrops;
    private final boolean doBreakParticles;
    private final boolean doSounds;
    private final boolean scaleEnergy;
    private final List<Material> whitelist;
    private final List<Tag<Material>> whitelistTags;
    private final List<Material> blacklist;
    private final List<Tag<Material>> blacklistTags;
    private final LineParticleEffect lineEffect = new LineParticleEffect();
    private final NormalParticleEffect breakEffect = new NormalParticleEffect();

    private List<Material> parseMaterialListString(@Nullable String from, String directory) {
        List<Material> materialList = new LinkedList<>();
        if (from == null || from.equalsIgnoreCase("")) return materialList;
        for (String materialString : from.replaceAll("\\s", "").split(",")) {
            if (materialString.startsWith("#")) {
                continue;
            }

            Material toAdd = WbsEnums.materialFromString(materialString);
            if (toAdd != null) {
                materialList.add(toAdd);
            } else {
                logError("Material not recognized: " + materialString, directory);
            }
        }

        return materialList;
    }

    private List<Tag<Material>> parseTagListString(String from, String directory) {
        List<Tag<Material>> tagList = new LinkedList<>();
        if (from == null || from.equalsIgnoreCase("")) return tagList;

        for (String tagString : from.replaceAll("\\s", "").split(",")) {
            if (!tagString.startsWith("#")) {
                continue;
            }

            tagString = tagString.substring(1);

            NamespacedKey checkKey = NamespacedKey.minecraft(tagString);
            Tag<Material> materialTag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, checkKey, Material.class);

            if (materialTag != null) {
                tagList.add(materialTag);
            } else {
                logError("Tag not recognized: " + tagString, directory);
            }
        }

        return tagList;
    }


    @Override
    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        Location start = caster.getEyeLocation();
        Vector direction = caster.getFacingVector();

        World world = caster.getPlayer().getWorld();
        RayTraceResult lookingTrace = world.rayTraceBlocks(start, direction, range, FluidCollisionMode.NEVER);

        if (lookingTrace == null) return false;
        lineEffect.play(Particle.REDSTONE, caster.getEyeLocation(), lookingTrace.getHitPosition().toLocation(world));

        CarveResult result = breakLine(caster, start, direction, range, 0, null);

        return result != null;
    }

    @Nullable
    private CarveResult breakLine(SpellCaster caster, Location start, Vector direction, double maxRange, float energyUsed, @Nullable CarveResult carveResult) {
        World world = start.getWorld();
        assert world != null;
        RayTraceResult result = world.rayTraceBlocks(start, direction, maxRange, FluidCollisionMode.NEVER);

        if (result == null) return carveResult;

        Block hitBlock = result.getHitBlock();
        assert hitBlock != null;

        // If any blocks have been broken, stop unless the hit block is adjacent.
        // If not adjacent, then the ray must've gone through air.
        if (stopAtAir && energyUsed >= 1) {
            assert carveResult != null;
            if (!isAdjacentBlockLoc(carveResult.hitBlock, hitBlock)) {
                return carveResult;
            }
        }

        Material blockType = hitBlock.getType();

        double hardness = blockType.getHardness();
        if (scaleEnergy && energyUsed + hardness > energy) {
            return carveResult;
        }

        if (!canBreak(blockType)) return carveResult;

        // Check if they can build somewhere before actually doing it. Even though
        // the event would cancel it later, do this to prevent spamming with error messages
        // from other plugins
        if (!WbsRegionUtils.canBuildAt(hitBlock.getLocation(), caster.getPlayer())) {
            return carveResult;
        }

        Location hitPoint = result.getHitPosition().toLocation(world);

        BlockBreakEvent event = new BlockBreakEvent(hitBlock, caster.getPlayer());

        event.setDropItems(doDrops);

        caster.setIsBreaking(true);
        Bukkit.getPluginManager().callEvent(event);
        caster.setIsBreaking(false);

        if (event.isCancelled()) {
            return carveResult;
        }

        // Break confirmed

        boolean localDoDrops = event.isDropItems();

        CarveResult newResult = new CarveResult(hitPoint, hitBlock);

        if (doBreakParticles) {
            breakEffect.setOptions(hitBlock.getBlockData());
            breakEffect.play(Particle.BLOCK_CRACK, hitBlock.getLocation().add(0.5, 0.5, 0.5));
        }
        if (doSounds) {
            Sound sound = WbsEnums.getEnumFromString(Sound.class, "BLOCK_" + blockType + "_BREAK");

            if (sound == null) sound = Sound.BLOCK_STONE_BREAK;

            WbsSound wbsSound = new WbsSound(sound);
            wbsSound.play(hitPoint);
        }

        if (scaleEnergy) {
            energyUsed += hardness;
        } else {
            energyUsed++;
        }

        if (localDoDrops) {
            hitBlock.breakNaturally();
        } else {
            hitBlock.setType(Material.AIR);
        }

        if (energyUsed >= energy) {
            return newResult;
        }

        double distanceTravelled = hitPoint.distance(start);

        return breakLine(caster, hitPoint, direction, maxRange - distanceTravelled, energyUsed, newResult);
    }

    private boolean canBreak(Material material) {
        if (FORCE_BLACKLIST.contains(material)) return false;
        if (blacklist.contains(material)) return false;
        for (Tag<Material> blacklisted : blacklistTags) {
            if (blacklisted.isTagged(material)) return false;
        }

        if (usingWhitelist) {
            if (!whitelist.contains(material)) {
                boolean whitelistedByTag = false;
                for (Tag<Material> whitelisted : whitelistTags) {
                    if (whitelisted.isTagged(material)) {
                        whitelistedByTag = true;
                        break;
                    }
                }

                if (!whitelistedByTag) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isAdjacentBlockLoc(@Nullable Block block, Block otherBlock) {
        if (block == null) return true;
        return block.getLocation().distanceSquared(otherBlock.getLocation()) == 1;
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rEnergy: &7" + energy;
        asString += "\n&rScale energy? &7" + scaleEnergy;
        asString += "\n&rDrops blocks? &7" + doDrops;
        asString += "\n&rStop at air? &7" + stopAtAir;

        String hexString = Integer.toHexString(colour.asRGB());
        if (VersionUtil.getVersion() >= 16) {
            asString += "\n&rColour: &7#" + hexString + " &#" + hexString + "(" + WbsEnums.toPrettyString(WbsColours.toDyeColour(colour)) + ")";
        } else {
            asString += "\n&rColour: &7#" + hexString + " (" + WbsEnums.toPrettyString(WbsColours.toDyeColour(colour)) + ")";
        }

        if (usingWhitelist) {
            String materialString = whitelist.stream()
                    .map(WbsEnums::toPrettyString)
                    .collect(Collectors.joining(", "));
            String tagString = whitelistTags.stream()
                    .map(Tag::getKey)
                    .map(NamespacedKey::getKey)
                    .map(key -> "#" + key)
                    .collect(Collectors.joining(", "));

            asString += "\n&rWhitelist: &7" + String.join(", ", materialString, tagString);
        }
        if (!blacklist.isEmpty() || !blacklistTags.isEmpty()) {
            String materialString = blacklist.stream()
                    .map(WbsEnums::toPrettyString)
                    .collect(Collectors.joining(", "));
            String tagString = blacklistTags.stream()
                    .map(Keyed::getKey)
                    .map(NamespacedKey::getKey)
                    .map(key -> "#" + key)
                    .collect(Collectors.joining(", "));

            asString += "\n&rBlacklist: &7" + String.join(", ", materialString, tagString);
        }

        return asString;
    }

    private static class CarveResult {
        Location hitLocation;
        Block hitBlock;

        public CarveResult(Location hitLocation, Block hitBlock) {
            this.hitLocation = hitLocation;
            this.hitBlock = hitBlock;
        }
    }
}
