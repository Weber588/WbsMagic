package wbs.magic.spells.ranged.projectile;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.magic.SpellCaster;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.generics.DynamicProjectileObject;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.DamageSpell;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;

import java.util.LinkedList;
import java.util.List;

@Spell(name = "Acid Bomb",
        description = "Throw a green orb that explodes with acid on impact, affecting nearby creatures!")
@DamageSpell(defaultDamage = 6, deathFormat = "%victim% fell victim to %attacker%'s acidic attack!")
@DoubleOption(optionName = "radius", defaultValue = 4)
@DoubleOption(optionName = "acid-duration", defaultValue = 6)
@DoubleOption(optionName = "sick-duration", defaultValue = 4)
// Overrides
@DoubleOption(optionName = "gravity", defaultValue = 5)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
public class AcidBomb extends ProjectileSpell {
    public AcidBomb(SpellConfig config, String directory) {
        super(config, directory);

        damage = config.getDouble("damage");
        radius = config.getDouble("radius");
        int acidDuration = (int) (config.getDouble("acid-duration") * 20);
        int sickDuration = (int) (config.getDouble("sick-duration") * 20);

        explodeEffect = new NormalParticleEffect();
        explodeEffect.setAmount((int) (radius * 15));
        explodeEffect.setSpeed(0.2);

        bombEffect = new NormalParticleEffect();
        bombEffect.setXYZ(getHitbox());
        bombEffect.setAmount((int) (getHitbox() * getHitbox()));
        bombEffect.setOptions(new Particle.DustOptions(Color.fromRGB(156, 222, 98), 1f));

        // TODO: Make potion lists configurable?
        hitEffects.add(new PotionEffect(PotionEffectType.SLOW, acidDuration, 1));
        hitEffects.add(new PotionEffect(PotionEffectType.POISON, acidDuration, 0));

        hitEffects.add(new PotionEffect(PotionEffectType.CONFUSION, sickDuration, 0));

        // Get by name to allow it to be skipped pre-1.19
        PotionEffectType darknessType = PotionEffectType.getByName("DARKNESS");

        if (darknessType != null) {
            hitEffects.add(new PotionEffect(darknessType, sickDuration, 0));
        } else {
            hitEffects.add(new PotionEffect(PotionEffectType.BLINDNESS, sickDuration, 0));
            hitEffects.add(new PotionEffect(PotionEffectType.NIGHT_VISION, sickDuration, 0));
        }
    }

    private final double damage;
    private final double radius;

    private final NormalParticleEffect bombEffect;
    private final NormalParticleEffect explodeEffect;

    private final List<PotionEffect> hitEffects = new LinkedList<>();

    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        AcidBombProjectile projectile = new AcidBombProjectile(caster.getEyeLocation(), caster, this);

        projectile.run();
        return true;
    }

    private class AcidBombProjectile extends DynamicProjectileObject {

        public AcidBombProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
            super(location, caster, castingSpell);
        }

        @Override
        protected boolean afterMove(Location oldLocation, Location newLocation) {
            bombEffect.play(Particle.REDSTONE, newLocation);
            return false;
        }

        @Override
        protected boolean hitBlock(Location hitLocation, Block hitBlock, BlockFace hitFace) {
            return explode(hitLocation);
        }

        @Override
        protected boolean hitEntity(Location hitLocation, LivingEntity hitEntity) {
            return explode(hitLocation);
        }

        private boolean explode(Location hitLocation) {
            explodeEffect.play(Particle.SNEEZE, hitLocation);
            explodeEffect.play(Particle.TOTEM, hitLocation);

            RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class);
            selector.setRange(radius);
            selector.exclude(caster.getPlayer());

            List<LivingEntity> nearby = selector.select(hitLocation);

            for (LivingEntity hit : nearby) {
                caster.damage(hit, damage, castingSpell);
                hit.addPotionEffects(hitEffects);
            }

            return true;
        }
    }
}
