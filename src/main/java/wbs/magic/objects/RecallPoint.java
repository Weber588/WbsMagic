package wbs.magic.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import wbs.magic.objects.generics.MagicObject;
import wbs.magic.spells.ranged.Recall;
import wbs.magic.spells.SpellInstance;
import wbs.magic.SpellCaster;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.RingParticleEffect;

public class RecallPoint extends MagicObject {
    public RecallPoint(Location location, SpellCaster caster, SpellInstance castingSpell, int duration) {
        super(location, caster, castingSpell);
        this.duration = duration;

        effect = new NormalParticleEffect().setXYZ(0.05);
        effect.setAmount(2);

        ringEffect = new RingParticleEffect();
        ringEffect.setRadius(0.5).setAmount(3);
        ringEffect.setOptions(Bukkit.createBlockData(Material.PURPLE_WOOL));
    }

    private int age = 0;
    private int duration;
    private boolean autoRecall;

    private NormalParticleEffect effect;
    private RingParticleEffect ringEffect;

    @Override
    protected void onRemove() {
        if (autoRecall) ((Recall) castingSpell).recall(caster, this);
    }

    @Override
    protected boolean tick() {
        age++;
        if (age > duration) {
            caster.sendActionBar("Your recall point fizzles away...");
            return true;
        }
        if (castingSpell.isConcentration() && !caster.isConcentratingOn(castingSpell)) {
            return true;
        }
        effect.play(Particle.SPELL_WITCH, getLocation());
        ringEffect.setRotation(age * 6);
        ringEffect.buildAndPlay(Particle.FALLING_DUST, getLocation().add(0, 1, 0));
        return false;
    }

    public void setAutoRecall(boolean autoRecall) {
        this.autoRecall = autoRecall;
    }
}
