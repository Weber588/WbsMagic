package wbs.magic.spells;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.targeters.RadiusTargeter;
import wbs.magic.SpellCaster;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.particles.DiscParticleEffect;
import wbs.utils.util.pluginhooks.WbsRegionUtils;

import java.util.Set;

@Spell(name = "Conflagration",
        cost = 100,
        cooldown = 30,
        description = "A wave of flame moves out in all directions, burning and damaging creatures in its path."
)
@RestrictWandControls(dontRestrictLineOfSight = true)
@SpellSound(sound = Sound.ENTITY_BLAZE_SHOOT, pitch = 0.5f)
@DamageSpell(defaultDamage = 6, deathFormat = "%victim% was incinerated by %attacker%!")
@SpellOption(optionName = "radius", type = SpellOptionType.DOUBLE, defaultDouble = 5)
@SpellOption(optionName = "push", type = SpellOptionType.DOUBLE, defaultDouble = 0.8)
@SpellOption(optionName = "fire-duration", type = SpellOptionType.DOUBLE, defaultDouble = 5, aliases = {"flame-duration"})
@SpellSound(sound = Sound.ENTITY_BLAZE_SHOOT)
public class Conflagration extends SpellInstance {
    public Conflagration(SpellConfig config, String directory) {
        super(config, directory);

        damage = config.getDouble("damage");
        push = config.getDouble("push");
        fireTicks = (int) (config.getDouble("fire-duration") * 20);

        double radius = config.getDouble("radius");

        radiusTargeter = new RadiusTargeter(radius);

        fireEffect.setRelative(true);
        fireEffect.setSpeed(radius / 15);
        fireEffect.setVariation(0.1);
        fireEffect.setRadius(radius);
        fireEffect.setRandom(true);
        fireEffect.setAmount((int) (radius * radius * Math.PI));

        popEffect.setRadius(radius);
        popEffect.setRandom(true);
        popEffect.setAmount((int) (radius * radius * Math.PI) / 2);

        fireEffect.build();
        popEffect.build();
    }

    private final double damage;
    private final double push;
    private final int fireTicks;
    private final DiscParticleEffect popEffect = new DiscParticleEffect();
    private final DiscParticleEffect fireEffect = new DiscParticleEffect();
    private final RadiusTargeter radiusTargeter;

    @Override
    public boolean cast(SpellCaster caster) {
        fireEffect.play(Particle.FLAME, caster.getLocation().add(0, 0.1, 0));
        popEffect.play(Particle.LAVA, caster.getLocation());

        Set<LivingEntity> hit = radiusTargeter.getTargets(caster);

        for (LivingEntity target : hit) {
            if (WbsRegionUtils.canDealDamage(caster.getPlayer(), target)) {
                WbsEntities.damage(target, damage, caster.getPlayer());
                target.setFireTicks((int) (fireTicks * (1 + (Math.random() * 0.4 - 0.2))));
                target.setVelocity(
                        WbsEntities.getMiddleLocation(target) // Give a slight upwards force
                                .subtract(caster.getLocation())
                                .toVector()
                                .normalize()
                                .multiply(push)
                );
            }
        }

        return true;
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rDamage: &7" + damage;
        asString += "\n&rRadius: &7" + radiusTargeter.getRange();
        asString += "\n&rPush force: &7" + push;
        asString += "\n&rBurn duration: &7" + fireTicks / 20;

        return asString;
    }
}
