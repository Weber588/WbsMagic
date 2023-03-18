package wbs.magic.spells.ranged.targeted;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import org.jetbrains.annotations.NotNull;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellSettings;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.SpellCaster;

import wbs.magic.spellmanagement.configuration.options.DoubleOptions;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.EnumOptions;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.utils.util.WbsSound;
import wbs.utils.util.particles.RingParticleEffect;

@Spell(name = "Hold",
		cost = 15,
		cooldown = 15,
		description = "The creature that the caster is looking at is held in place, unable to move for the duration of the spell. If the target is in the air, they will be pulled down to the ground and prevented from taking off again."
)
@SpellSettings(canBeConcentration = true)
@TargeterOption(optionName = "targeter", defaultRange = 100)
@DoubleOption(optionName = "duration", defaultValue = 4)
@SpellOption(optionName = "glow", type = SpellOptionType.BOOLEAN, defaultBool = true, aliases = {"glowing"})
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.NEGATIVE, enumType = AlignmentType.class)
public class Hold extends StatusSpell {
	private static final Vector HOLD_VEC = new Vector(0, -10, 0);

	public Hold(SpellConfig config, String directory) {
		super(config, directory);

		glowing = config.getBoolean("glow");

		effect = new RingParticleEffect();
		effect.setRadius(0.5);
		effect.setOptions(Material.GRAVEL.createBlockData());
	}

	private final boolean glowing;
	
	private final PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, 2, 100, false, false);
	private final PotionEffect glow = new PotionEffect(PotionEffectType.GLOWING, 2, 0, false, false);
	private final Particle display = Particle.FALLING_DUST;
	
	private final RingParticleEffect effect;
	private final WbsSound sound = new WbsSound(Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 2, 2);

	@Override
	public boolean preCastEntity(CastingContext context, Collection<LivingEntity> targets) {
		if (isConcentration) {
			context.caster.setConcentration(this);
		}
		return false;
	}

	@Override
	protected @NotNull MagicEntityEffect generateEffect(LivingEntity target, @NotNull SpellCaster caster) {
		RingParticleEffect localEffect = effect.clone(); // Don't change the orientation for other

		localEffect.setAmount((int) (target.getWidth()*10));
		localEffect.setRadius(target.getWidth());

		return new MagicEntityEffect(target, caster, this) {
			@Override
			protected void onRun() {
				if (target instanceof Player) {
					SpellCaster.getCaster((Player) target).sendActionBar("&h" + caster.getName() + "&r cast &hHold&r on you!");
				}
				sound.play(target.getLocation());
			}

			@Override
			protected boolean onTick(Entity entity) {
				localEffect.setRotation(getAge());
				localEffect.buildAndPlay(display, getLocation().add(0, entity.getHeight() + 0.5, 0));

				target.removePotionEffect(PotionEffectType.SLOW);
				target.addPotionEffect(slow);

				if (glowing) {
					target.removePotionEffect(PotionEffectType.GLOWING);
					target.addPotionEffect(glow);
				}

				if (target.getType() == EntityType.ENDER_DRAGON) {
					target.teleport(getSpawnLocation());
				} else {
					target.setVelocity(HOLD_VEC);
				}

				return false;
			}

			@Override
			protected void onRemove() {
				if (isConcentration) {
					boolean isHolding = MagicObject.getAllActive(caster).stream()
							.anyMatch(obj -> obj.getSpell().equals(Hold.this));

					// If this was the last hold effect from this spell
					if (!isHolding) {
						caster.stopConcentration();
					}
				}
			}

			@Override
			protected void onMaxAgeHit() {
				if (isConcentration) {
					caster.stopConcentration();
				}
			}
		};
	}

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rGlow: &7" + glow;

		return asString;
	}
}
