package wbs.magic.spells.ranged.targeted;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import wbs.magic.SpellCaster;
import wbs.magic.generators.EntityGenerator;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.ConfiguredEntityOption;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.EntityOptions.EntityOption;
import wbs.magic.spellmanagement.configuration.options.IntOptions.IntOption;
import wbs.magic.spellmanagement.configuration.options.StringOptions.StringOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Spell(name = "Summon Ally", description = "Summon a monster to fight by your side!")
@EntityOption(optionName = "entity", entityType = "skeleton", classRestriction = Monster.class)
@IntOption(optionName = "amount", defaultValue = 1)
@StringOption(optionName = "success-message", defaultValue = "Summoned %amount% %entity% allies!")
@TargeterOption(optionName = "targeter", defaultRange = 40)
@DoubleOption(optionName = "lifetime", defaultValue = -1)
public class SummonAlly extends SpellInstance implements LivingEntitySpell {
    // 5 seconds, if repeating damage doesn't kill first
    private static final int DELAY_BEFORE_DEATH = 100;

    public SummonAlly(SpellConfig config, String directory) {
        super(config, directory);

        entityGenerator = config.get("entity", ConfiguredEntityOption.class);
        amount = config.getInt("amount");
        lifetime = (int) (config.getDouble("lifetime") * 20);
        successMessage = config.getString("success-message");

        targeter = config.getTargeter("targeter");
    }

    private final EntityGenerator entityGenerator;
    private final int amount;
    private final int lifetime;
    private final String successMessage;
    private final GenericTargeter targeter;

    private final NormalParticleEffect allKilledEffect = new NormalParticleEffect();

    @Override
    public boolean preCastEntity(CastingContext context, final Collection<LivingEntity> targets) {
        SpellCaster caster = context.caster;

        // Create a copy of the targets to ensure that, when first spawned, no two allies target the same mob
        // (unless amount > targets.size())
        List<LivingEntity> distributionTargets = new LinkedList<>(targets);

        for (int i = 0; i < amount; i++) {
            Entity entity = entityGenerator.spawn(caster.getLocation(), null, caster.getPlayer(), spawned ->
                    {
                        if (spawned instanceof Monster) {
                            Monster monster = (Monster) spawned;

                            if (distributionTargets.isEmpty()) {
                                distributionTargets.addAll(targets);
                            }

                            LivingEntity target = WbsCollectionUtil.getRandom(distributionTargets);
                            distributionTargets.remove(target);

                            monster.setTarget(target);
                        }
                    });

            MagicEntityEffect controller = new MagicEntityEffect(entity, caster, this) {

                // The time at which all targets had been killed, to allow the mobs to remain for a short time after
                // killing the last target
                int deathTime = -1;

                @Override
                protected boolean onTick(Entity entity) {
                    Monster ally = (Monster) entity;

                    if (caster.getPlayer().equals(ally.getTarget())) {
                        ally.setTarget(null);
                    }

                    if (deathTime != -1) {
                        if (getAge() > deathTime) {
                            ally.setHealth(0);
                            return true;
                        } else {
                            if (chance(5)) {
                                allKilledEffect.setXYZ(ally.getWidth());
                                allKilledEffect.setY(ally.getHeight());
                                allKilledEffect.play(Particle.DAMAGE_INDICATOR, WbsEntityUtil.getMiddleLocation(ally));
                                ally.damage(2);
                            }
                        }
                    } else {
                        LivingEntity target = ally.getTarget();
                        if (target == null || target.isDead() || !target.isValid() || !targets.contains(target)) {
                            targets.remove(target);

                            if (!targets.isEmpty()) {
                                ally.setTarget(WbsCollectionUtil.getRandom(targets));
                            }
                        }

                        if (targets.isEmpty()) {
                            deathTime = getAge() + DELAY_BEFORE_DEATH;
                        }
                    }

                    return false;
                }
            };

            controller.setExpireOnDeath(true);
            controller.setRemoveOnExpire(true);

            controller.run();
        }

        String message = successMessage.replaceAll("%amount%", amount + "")
                .replaceAll("%entity%", entityGenerator.getEntityName());

        caster.sendActionBar(message);

        return true;
    }

    @Override
    public void castOn(CastingContext context, LivingEntity target) {

    }

    @Override
    public GenericTargeter getTargeter() {
        return targeter;
    }
}
