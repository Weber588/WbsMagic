package wbs.magic.spells;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.magic.spellmanagement.configuration.RequiresPlugin;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.SpellManager;
import wbs.magic.targeters.RadiusTargeter;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.WbsSound;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.string.WbsStrings;


@Spell(name = "Hallucination",
        cost = 50,
        cooldown = 15,
        description = "Cause nearby players to hallucinate and see you running away, while being unable to see you."
)
@SpellSettings(canBeConcentration = true)
@RequiresPlugin("LibsDisguises")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "show-name", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "mob-type", type = SpellOptionType.STRING, defaultString = "OCELOT", enumType = EntityType.class)
@SpellOption(optionName = "mob-speed", type = SpellOptionType.DOUBLE, defaultDouble = 0.25)
@SpellOption(optionName = "mob-health", type = SpellOptionType.DOUBLE, defaultDouble = -1)
@SpellOption(optionName = "glow-duration", type = SpellOptionType.DOUBLE, defaultDouble = 2)
public class Hallucination extends SpellInstance {

    NormalParticleEffect effect = new NormalParticleEffect().setXYZ(0.3);
    private final Particle particle = Particle.SMOKE_NORMAL;

    public Hallucination(SpellConfig config, String directory) {
        super(config, directory);

        duration = config.getDouble("duration") * 20;
        showName = config.getBoolean("show-name");
        mobSpeed = config.getDouble("mob-speed");
        mobHealth = config.getDouble("mob-health");
        glowDuration = config.getDouble("glow-duration");

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

    private final double duration;
    private final boolean showName ;
    private final double mobSpeed;
    private final double mobHealth;
    private final double glowDuration;
    private EntityType mobType;
    private final PotionEffect glowEffect;

    private final RadiusTargeter radiusTargeter = new RadiusTargeter(30);
    private final WbsSound dissipateSound = new WbsSound(Sound.ENTITY_ENDER_EYE_DEATH, 1, 1);

    @Override
    public boolean cast(SpellCaster caster) {

        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            System.out.println(this.getClass().getName() + " requires LibsDisguises!");
            SpellManager.unregisterSpell(this.getClass());
            return false;
        }

        Player casterPlayer = caster.getPlayer();

        LivingEntity entity = (LivingEntity) casterPlayer.getWorld().spawnEntity(casterPlayer.getLocation(), mobType);

        MagicEntityEffect entityMarker = new MagicEntityEffect(entity, caster, this);
        entityMarker.setExpireOnDeath(true);
        entityMarker.run();

        MagicEntityEffect playerMarker = new MagicEntityEffect(casterPlayer, caster, this);
        playerMarker.setExpireOnDeath(true);
        playerMarker.run();

        entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                .setBaseValue(mobSpeed);

        entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0);

        if (mobHealth != -1) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mobHealth);
            entity.setHealth(mobHealth);
        }

        MobDisguise escapeDisguise = new MobDisguise(DisguiseType.SILVERFISH);
        escapeDisguise.setEntity(casterPlayer);
        escapeDisguise.startDisguise();

        escapeDisguise.getWatcher().setInvisible(true);
        escapeDisguise.setViewSelfDisguise(false);
        escapeDisguise.setMobsIgnoreDisguise(true);

        PlayerDisguise cloneDisguise = new PlayerDisguise(casterPlayer.getName());
        cloneDisguise.setHidePlayer(false);

        cloneDisguise.getWatcher().setArmor(casterPlayer.getEquipment().getArmorContents());
        cloneDisguise.getWatcher().setItemInMainHand(casterPlayer.getInventory().getItemInMainHand());
        cloneDisguise.getWatcher().setItemInOffHand(casterPlayer.getInventory().getItemInOffHand());
        cloneDisguise.getWatcher().setNameVisible(showName);
        cloneDisguise.setName(WbsStrings.colourise("&f" + casterPlayer.getName()));

        cloneDisguise.setEntity(entity);
        cloneDisguise.startDisguise();

        if (isConcentration()) caster.setConcentration(this);

        long smokeFrequency = 3;

        for (Mob mob : radiusTargeter.getTargets(caster, Mob.class)) {
            LivingEntity mobTarget = mob.getTarget();
            if (mobTarget != null && mobTarget.equals(casterPlayer)) {
                mob.setTarget(entity);
            }
        }

        new WbsRunnable() {
            int age = 0;
            @Override
            public void run() {
                age++;
                if (entityMarker.isExpired() ||
                        playerMarker.isExpired() ||
                        (isConcentration && !caster.isConcentratingOn(Hallucination.this)) ||
                        age >= duration / smokeFrequency) {
                    cloneDisguise.stopDisguise();
                    entity.remove();
                    escapeDisguise.stopDisguise();

                    if (caster.isConcentratingOn(Hallucination.this)) {
                        caster.stopConcentration();
                    }

                    if (glowDuration > 0) {
                        casterPlayer.addPotionEffect(glowEffect, true);
                    }

                    dissipateSound.play(entity.getLocation());
                    dissipateSound.play(casterPlayer.getLocation());

                    cancel();
                }

                effect.play(particle, casterPlayer.getLocation());
                cloneDisguise.getWatcher().setItemInMainHand(casterPlayer.getInventory().getItemInMainHand());
                cloneDisguise.getWatcher().setItemInOffHand(casterPlayer.getInventory().getItemInOffHand());
            }

            @Override
            public void finish() {
            }
        }.runTaskTimer(plugin, 0, smokeFrequency);
        return true;
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDuration: &7" + duration / 20 + " seconds";
        asString += "\n&rMob type: &7" + WbsEnums.toPrettyString(mobType);
        asString += "\n&Clone speed: &7" + mobSpeed;
        asString += "\n&rClone health: &7" + mobHealth;
        asString += "\n&rGlow duration: &7" + glowDuration / 20 + " seconds";

        return asString;
    }
}
