package wbs.magic.spellinstances;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.magic.annotations.RequiresPlugin;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.annotations.SpellSettings;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spells.SpellConfig;
import wbs.magic.spells.SpellManager;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsRunnable;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.string.WbsStrings;


@Spell(name = "Hallucination",
        cost = 50,
        cooldown = 15,
        description = "Cause nearby players to hallucinate and see you running away, while being unable to see you."
)
@SpellSettings(canBeConcentration = true)
@RequiresPlugin("LibsDisguises")
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 3)
@SpellOption(optionName = "show-name", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "mob-type", type = SpellOptionType.STRING, defaultString = "OCELOT", enumType = EntityType.class)
@SpellOption(optionName = "mob-speed", type = SpellOptionType.DOUBLE, defaultDouble = 0.25)
@SpellOption(optionName = "glow-duration", type = SpellOptionType.DOUBLE, defaultDouble = 2)
public class Hallucination extends SpellInstance {

    NormalParticleEffect effect = new NormalParticleEffect().setXYZ(0.3);
    private final Particle particle = Particle.SMOKE_NORMAL;

    public Hallucination(SpellConfig config, String directory) {
        super(config, directory);

        duration = config.getDouble("duration") * 20;
        showName = config.getBoolean("show-name");
        mobSpeed = config.getDouble("mob-speed");
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
    private final double glowDuration;
    private EntityType mobType;
    private final PotionEffect glowEffect;

    @Override
    public boolean cast(SpellCaster caster) {

        if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            System.out.println(this.getClass().getName() + " requires LibsDisguises!");
            SpellManager.unregisterSpell(this.getClass());
            return false;
        }

        Player casterPlayer = caster.getPlayer();

        LivingEntity entity = (LivingEntity) casterPlayer.getWorld().spawnEntity(casterPlayer.getLocation(), mobType);

        MagicEntityEffect marker = new MagicEntityEffect(entity, caster, this);
        marker.run();

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

        if (isConcentration()) caster.setConcentration(this);

        long smokeFrequency = 3;

        new WbsRunnable() {
            int age = 0;
            @Override
            public void run() {
                age++;
                if (marker.isExpired() || (isConcentration && !caster.isConcentratingOn(Hallucination.this))
                        || age >= duration / smokeFrequency) {
                    cloneDisguise.stopDisguise();
                    entity.remove();
                    escapeDisguise.stopDisguise();

                    if (caster.isConcentratingOn(Hallucination.this)) {
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
}
