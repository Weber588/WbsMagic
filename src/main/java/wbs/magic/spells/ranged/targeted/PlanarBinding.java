package wbs.magic.spells.ranged.targeted;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import wbs.magic.objects.MagicEntityEffect;
import wbs.magic.objects.MagicEntityMarker;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.Spell;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.TargeterOptions.TargeterOption;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.framework.LivingEntitySpell;
import wbs.magic.targeters.GenericTargeter;
import wbs.magic.targeters.RadiusTargeter;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.*;
import wbs.utils.util.providers.NumProvider;
import wbs.utils.util.providers.generator.num.CycleGenerator;

import java.util.Objects;

@Spell(name = "Planar Binding", description = "Binds a creature to this plane, preventing it from teleporting!")
@DoubleOption(optionName = "duration", defaultValue = 15)
@TargeterOption(optionName = "targeter", defaultType = RadiusTargeter.class, defaultRange = 15)
public class PlanarBinding extends SpellInstance implements LivingEntitySpell {
    public PlanarBinding(SpellConfig config, String directory) {
        super(config, directory);

        duration = (int) (config.getDouble("duration") * 20);
        targeter = config.getTargeter("targeter");

        effect = new SpiralParticleEffect();
        effect.setRadius(0.5)
                .setAbout(new Vector(0, 1, 0))
                .setSpeed(0.2)
                .setAmount(1);
    }

    private final GenericTargeter targeter;
    private final int duration;
    private final SpiralParticleEffect effect;

    @Override
    public void castOn(CastingContext context, LivingEntity target) {
        CircleParticleEffect clonedEffect = effect.clone()
                .setRotation(new NumProvider(new CycleGenerator(0, 360, 30, 0)));

        clonedEffect.setRadius(target.getWidth() * 2 / 3);

        FallingBlock test = target.getWorld().spawnFallingBlock(target.getEyeLocation(), Material.BARRIER.createBlockData());

        test.setDropItem(false);
        test.setGravity(false);
        test.setGlowing(true);

        MagicEntityMarker marker = new MagicEntityMarker(target, context.caster, this) {
            @Override
            protected void playParticles() {
                clonedEffect.buildAndPlay(Particle.SPELL_WITCH, getLocation());
            }
        };

        marker.setMaxAge(duration);

        marker.run();
    }

    @Override
    public GenericTargeter getTargeter() {
        return targeter;
    }

}
