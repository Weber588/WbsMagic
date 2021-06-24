package wbs.magic.spellinstances.ranged.targeted;

import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import wbs.magic.spells.SpellConfig;
import wbs.magic.annotations.Spell;
import wbs.magic.statuseffects.generics.StatusEffect;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.wrappers.SpellCaster;

@Spell(name = "Hex",
		cost = 20,
		cooldown = 60,
		description = "Place a curse on the target creature. You deal more damage to that mob for a set amount of time."
)
public class HexSpell extends TargetedSpell {
	public HexSpell(SpellConfig config, String directory) {
		super(config, directory);
	}

	private StatusEffect status = null;

	@Override
	public <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
		caster.sendActionBar("In progress");
	//	status = new StatusEffect(StatusEffectType., caster, 20 * (int) duration);
		return true;
	}
	
	@Override
	protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {
		// Safe cast ensured by targetedClass declared in constructor
		Player playerTarget = (Player) target;
		if (SpellCaster.isRegistered(playerTarget)) {
			SpellCaster otherCaster = SpellCaster.getCaster(playerTarget);
			
			otherCaster.addStatusEffect(status);
		}
	}
}
