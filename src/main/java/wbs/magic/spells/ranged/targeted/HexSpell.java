package wbs.magic.spells.ranged.targeted;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import wbs.magic.DamageType;
import wbs.magic.objects.AlignmentType;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;

import java.util.Collection;
import java.util.List;

@Spell(name = "Hex",
		cost = 20,
		cooldown = 60,
		description = "Place a curse on the target creature. You deal more damage to that mob for a set amount of time."
)
@EnumOption(optionName = "alignment", defaultValue = AlignmentType.Name.BAD, enumType = AlignmentType.class)
@EnumOption(optionName = "damage-type", defaultValue = "ALL", enumType = DamageType.class)
@DoubleOption(optionName = "multiplier", defaultValue = 1.5)
public class HexSpell extends StatusSpell {
	private static final Listener LISTENER = new HexListener();

	public HexSpell(SpellConfig config, String directory) {
		super(config, directory);

		multiplier = config.getDouble("multiplier");
        damageTypes = config.getEnumList("damage-type", DamageType.class);

		tryRegisterListener(LISTENER);
	}

	private final double multiplier;
	private final List<DamageType> damageTypes;

	@Override
	public String toString() {
		String asString = super.toString();

		asString += "\n&rMultiplier: &7x" + multiplier;

		return asString;
	}

	@SuppressWarnings("unused")
	private static final class HexListener implements Listener {

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void onDamage(EntityDamageByEntityEvent event) {
			Entity victim = event.getEntity();
			Entity attacker = event.getDamager();

			Collection<MagicEntityEffect> hexes = MagicEntityEffect.getEffects(victim, HexSpell.class);
			if (!hexes.isEmpty()) {
				double damage = event.getDamage();
				for (MagicEntityEffect hex : hexes) {
				    HexSpell spell = (HexSpell) hex.getSpell();

					if (spell.damageTypes.stream().anyMatch(cause -> cause.matches(event))) {
                        Player casterPlayer = hex.getCaster().getPlayer();
                        boolean damageCausedByCaster = casterPlayer.equals(attacker);

                        if (!damageCausedByCaster && attacker instanceof Projectile) {
                            damageCausedByCaster = casterPlayer.equals(((Projectile) attacker).getShooter());
                        }

                        if (damageCausedByCaster) {
                            damage *= ((HexSpell) hex.getSpell()).multiplier;
                        }
                    }
				}

				event.setDamage(damage);
			}
		}
	}

}
