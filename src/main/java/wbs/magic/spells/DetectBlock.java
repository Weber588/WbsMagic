package wbs.magic.spells;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import wbs.magic.SpellCaster;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spellmanagement.configuration.options.StringOptions;
import wbs.magic.spellmanagement.configuration.options.StringOptions.StringOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.WbsEnums;

import java.util.Objects;

@Spell(name = "Detect Block", description = "Highlights nearby blocks of a specific type!")
@EnumOption(optionName = "colour", defaultValue = "red", enumType = ChatColor.class)
@EnumOption(optionName = "material", defaultValue = "gold_ore", enumType = Material.class)
@DoubleOption(optionName = "duration", defaultValue = 10)
@DoubleOption(optionName = "radius", defaultValue = 5)
@DoubleOption(optionName = "unhighlight-radius", defaultValue = 2)
@StringOption(optionName = "success-message", defaultValue = "Detected %amount% &h%material%&r!")
@StringOption(optionName = "fail-message", defaultValue = "Nothing was found...")
public class DetectBlock extends SpellInstance {

    private static final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 9999999, 0, false, false);

    public DetectBlock(SpellConfig config, String directory) {
        super(config, directory);

        ChatColor colour = config.getEnum("colour", ChatColor.class);

        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
        String colourTeamName = plugin.getName() + "_col_" + colour.getChar();
        Team checkTeam = scoreboard.getTeam(colourTeamName);
        if (checkTeam != null) {
            colourTeam = checkTeam;
        } else {
            colourTeam = scoreboard.registerNewTeam(colourTeamName);
        }

        colourTeam.setColor(colour);

        duration = (int) (config.getDouble("duration") * 20);
        unhighlightRadius = config.getDouble("unhighlight-radius");
        radius = config.getDouble("radius");
        radiusSquared = radius * radius;

        material = config.getEnum("material", Material.class);

        successMessage = config.getString("success-message");
        failMessage = config.getString("fail-message");
    }

    private final Team colourTeam;
    private final Material material;
    private final int duration;
    private final double unhighlightRadius;
    private final double radius;
    private final double radiusSquared;
    private final String successMessage;
    private final String failMessage;

    @Override
    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        World world = caster.getPlayer().getWorld();
        Location centerLoc = caster.getEyeLocation();
        int centerX = centerLoc.getBlockX();
        int centerY = centerLoc.getBlockY();
        int centerZ = centerLoc.getBlockZ();

        int localRadius = (int) Math.ceil(radius);

        int highlighted = 0;

        for (int x = -localRadius; x < localRadius; x++) {
            for (int y = -localRadius; y < localRadius; y++) {
                for (int z = -localRadius; z < localRadius; z++) {
                    if (x * x + y * y + z * z < radiusSquared) {
                        int currentX = x + centerX;
                        int currentY = y + centerY;
                        int currentZ = z + centerZ;
                        Material check = world.getBlockAt(currentX, currentY, currentZ).getType();
                        if (check == material) {
                            Location loc = new Location(world, currentX, currentY, currentZ);
                            createTempHighlight(caster, loc);
                            highlighted++;
                        }
                    }
                }
            }
        }

        String message;
        if (highlighted > 0) {
            message = successMessage;
        } else {
            message = failMessage;
        }

        message = message.replaceAll("%amount%", highlighted + "")
                .replaceAll("%material%", WbsEnums.toPrettyString(material));

        caster.sendActionBar(message);

        return true;
    }

    private void createTempHighlight(SpellCaster caster, Location loc) {
        World world = Objects.requireNonNull(loc.getWorld());

        Slime entity = world.spawn(loc.add(0.5, 0, 0.5), Slime.class, slime -> {
            slime.setInvulnerable(true);
            slime.setGlowing(true);
            slime.setGravity(false);
            slime.setSilent(true);
            slime.setLootTable(null);
            slime.setAI(false);
            slime.setSize(2);
            slime.setCollidable(false);
        });

        entity.addPotionEffect(INVISIBILITY);

        String uuid = entity.getUniqueId().toString();
        colourTeam.addEntry(uuid);

        MagicEntityEffect effect = new MagicEntityEffect(entity, caster, this) {

            @Override
            protected boolean tick() {
                boolean remove = super.tick();
                if (remove) return true;

                Material type = getEntity().getLocation().getBlock().getType();
                if (type != material) {
                    return true;
                }

                if (getAge() > 20 && getLocation().distanceSquared(caster.getLocation()) < unhighlightRadius) {
                    return true;
                }

                if (getAge() % 100 == 0) {
                    entity.setTicksLived(1);
                }

                return false;
            }

            @Override
            protected void onRemove() {
                super.onRemove();
                colourTeam.removeEntry(uuid);
            }
        };

        effect.setMaxAge(duration);
        effect.setRemoveOnExpire(true);
        effect.setExpireOnDeath(true);

        effect.run();
    }
}
