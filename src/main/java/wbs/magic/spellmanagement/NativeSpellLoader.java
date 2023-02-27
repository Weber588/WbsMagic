package wbs.magic.spellmanagement;

import wbs.magic.spells.ArcaneSurge;

import java.util.*;

import wbs.magic.spells.*;
import wbs.magic.spells.ranged.*;
import wbs.magic.spells.ranged.projectile.*;
import wbs.magic.spells.ranged.targeted.*;
import wbs.magic.spells.ranged.targeted.missile.*;

public class NativeSpellLoader implements SpellLoader {

    private static final List<SpellRegistrationEntry<?>> classes = new LinkedList<>();

    static {
        Collections.addAll(classes,
                new SpellRegistrationEntry<>(PsychedelicGlimmer.class, PsychedelicGlimmer::new),
                new SpellRegistrationEntry<>(AcidBomb.class, AcidBomb::new),

                // Uncategorized
                new SpellRegistrationEntry<>(AntiMagicShell.class, AntiMagicShell::new),
                new SpellRegistrationEntry<>(ArcaneSurge.class, ArcaneSurge::new),
                new SpellRegistrationEntry<>(Blink.class, Blink::new),
                new SpellRegistrationEntry<>(ConjureBridge.class, ConjureBridge::new),
                new SpellRegistrationEntry<>(ConeOfCold.class, ConeOfCold::new),
                new SpellRegistrationEntry<>(Conflagration.class, Conflagration::new),
                new SpellRegistrationEntry<>(ChangeTier.class, ChangeTier::new),
                new SpellRegistrationEntry<>(CheckMana.class, CheckMana::new),
                new SpellRegistrationEntry<>(FlySpell.class, FlySpell::new),
                new SpellRegistrationEntry<>(Hallucination.class, Hallucination::new),
                new SpellRegistrationEntry<>(Leap.class, Leap::new),
                new SpellRegistrationEntry<>(RegenerateMana.class, RegenerateMana::new),
                new SpellRegistrationEntry<>(Shield.class, Shield::new),
                new SpellRegistrationEntry<>(ShimmerWall.class, ShimmerWall::new),
                new SpellRegistrationEntry<>(ThrowBlock.class, ThrowBlock::new),
                new SpellRegistrationEntry<>(VoidStep.class, VoidStep::new),
                new SpellRegistrationEntry<>(WaterWalkSpell.class, WaterWalkSpell::new),

                // Ranged, non-targeted non-projectile
                new SpellRegistrationEntry<>(Carve.class, Carve::new),
                new SpellRegistrationEntry<>(GravityWellSpell.class, GravityWellSpell::new),
                new SpellRegistrationEntry<>(HomingProjectile.class, HomingProjectile::new),
                new SpellRegistrationEntry<>(NegateMagic.class, NegateMagic::new),
                new SpellRegistrationEntry<>(PrismaticRay.class, PrismaticRay::new),
                new SpellRegistrationEntry<>(Tornado.class, Tornado::new),
                new SpellRegistrationEntry<>(Recall.class, Recall::new),

                // Projectile
                new SpellRegistrationEntry<>(BlizzardSpell.class, BlizzardSpell::new),
                new SpellRegistrationEntry<>(DepthSurgeSpell.class, DepthSurgeSpell::new),
                new SpellRegistrationEntry<>(EldritchBlast.class, EldritchBlast::new),
                new SpellRegistrationEntry<>(EnergyBurst.class, EnergyBurst::new),
                new SpellRegistrationEntry<>(FaerieFireSpell.class, FaerieFireSpell::new),
                //    FireballSpell.class,
                new SpellRegistrationEntry<>(Firebolt.class, Firebolt::new),
                new SpellRegistrationEntry<>(FrostShards.class, FrostShards::new),
                new SpellRegistrationEntry<>(Warp.class, Warp::new),

                // Targeted
                new SpellRegistrationEntry<>(ChainLightning.class, ChainLightning::new),
                new SpellRegistrationEntry<>(CleanseSpell.class, CleanseSpell::new),
                new SpellRegistrationEntry<>(Confuse.class, Confuse::new),
                new SpellRegistrationEntry<>(CounterSpell.class, CounterSpell::new),
                new SpellRegistrationEntry<>(Disarm.class, Disarm::new),
                new SpellRegistrationEntry<>(Displace.class, Displace::new),
                new SpellRegistrationEntry<>(DominateMonster.class, DominateMonster::new),
                new SpellRegistrationEntry<>(DrainLife.class, DrainLife::new),
                //    HexSpell.class,
                new SpellRegistrationEntry<>(Hold.class, Hold::new),
                new SpellRegistrationEntry<>(ImbueCreature.class, ImbueCreature::new),
                new SpellRegistrationEntry<>(InflictWounds.class, InflictWounds::new),
                new SpellRegistrationEntry<>(MassBreed.class, MassBreed::new),
                new SpellRegistrationEntry<>(ShootEntitySpell.class, ShootEntitySpell::new),
                new SpellRegistrationEntry<>(Polymorph.class, Polymorph::new),
                new SpellRegistrationEntry<>(Push.class, Push::new),
                new SpellRegistrationEntry<>(Regenerate.class, Regenerate::new),

                // Missile
                new SpellRegistrationEntry<>(MagicMissiles.class, MagicMissiles::new)
        );
    }

    @Override
    public List<SpellRegistrationEntry<?>> getSpells() {
        return classes;
    }

    @Override
    public int getSpellCount() {
        return classes.size();
    }
}
