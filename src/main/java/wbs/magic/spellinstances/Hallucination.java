package wbs.magic.spellinstances;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.enums.SpellType;
import wbs.magic.spells.SpellConfig;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.string.WbsStrings;

import java.util.Set;


@Spell(name = "Hallucination",
        cost = 50,
        cooldown = 15,
        description = "Cause nearby players to hallucinate and see you running away, while being unable to see you."
)
@SpellSettings(canBeConcentration = true)
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 3)
@SpellOption(optionName = "show-name", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "mob-type", type = SpellOptionType.STRING, defaultString = "OCELOT")
@SpellOption(optionName = "mob-speed", type = SpellOptionType.DOUBLE, defaultDouble = 0.25)
@SpellOption(optionName = "glow-duration", type = SpellOptionType.DOUBLE, defaultDouble = 2)
public class Hallucination extends SpellInstance {

    NormalParticleEffect effect = new NormalParticleEffect().setXYZ(0.3);
    private final Particle particle = Particle.SMOKE_NORMAL;

    public Hallucination(SpellConfig config, String directory) {
        super(config, directory);

        duration = config.getDouble("duration", duration) * 20;
        showName = config.getBoolean("show-name", showName);
        mobSpeed = config.getDouble("mob-speed", mobSpeed);
        glowDuration = config.getDouble("glow-duration", glowDuration);

        String mobTypeString = config.getString("mob-type", "OCELOT");

        mobType = WbsEnums.getEnumFromString(EntityType.class, mobTypeString);
        if (mobType == null) {
            mobType = EntityType.OCELOT;
            logError("Invalid entity type: " + mobTypeString + ". Defaulting to Ocelot.", directory);
        }

        if (!LivingEntity.class.isAssignableFrom(mobType.getEntityClass())) {
            mobType = EntityType.OCELOT;
            logError("Entity must be a living entity: " + mobTypeString + ". Defaulting to Ocelot.", directory);
        }

        glowEffect = new PotionEffect(PotionEffectType.GLOWING, (int) (glowDuration * 20), 0, false, false, false);
    }

    private double duration = 10;
    private boolean showName = false;
    private double mobSpeed = 0.25;
    private double glowDuration = 2;
    private EntityType mobType;
    private PotionEffect glowEffect;

    @Override
    public boolean cast(SpellCaster caster) {

        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            System.out.println(this.getClass().getName() + " requires LibsDisguises!");
            return false;
        } // TODO: Make this unload this spell class

        Player casterPlayer = caster.getPlayer();

        Ocelot entity = (Ocelot) casterPlayer.getWorld().spawnEntity(casterPlayer.getLocation(), EntityType.OCELOT);

        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                .setBaseValue(mobSpeed);

        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0);

        MobDisguise escapeDisguise = new MobDisguise(DisguiseType.SILVERFISH);
        escapeDisguise.setEntity(casterPlayer);
        escapeDisguise.startDisguise();

        escapeDisguise.getWatcher().setInvisible(true);
        escapeDisguise.setViewSelfDisguise(false);

        PlayerDisguise cloneDisguise = new PlayerDisguise(casterPlayer.getName());
        cloneDisguise.setHidePlayer(false);

        cloneDisguise.getWatcher().setArmor(casterPlayer.getEquipment().getArmorContents());
        cloneDisguise.getWatcher().setNameVisible(showName);
        cloneDisguise.setName(WbsStrings.colourise("&f" + casterPlayer.getName()));

        cloneDisguise.setEntity(entity);
        cloneDisguise.startDisguise();

        caster.setConcentration(getType());

        long smokeFrequency = 3;

        new WbsRunnable() {
            int age = 0;
            @Override
            public void run() {
                age++;
                if ((isConcentration && !caster.isConcentratingOn(getType()))
                        || age >= duration / smokeFrequency) {
                    cloneDisguise.stopDisguise();
                    entity.remove();
                    escapeDisguise.stopDisguise();

                    if (caster.isConcentratingOn(getType())) {
                        caster.stopConcentration();
                    }

                    if (glowDuration > 0) {
                        casterPlayer.addPotionEffect(glowEffect, true);
                    }

                    cancel();
                }

                effect.play(particle, casterPlayer.getLocation());
            }

            @Override
            public void finish() {
            }
        }.runTaskTimer(plugin, 0, smokeFrequency);
        return true;
    }

    @Override
    public SpellType getType() {
        return SpellType.HALLUCINATION;
    }
}
