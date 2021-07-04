package wbs.magic.spells;

import wbs.magic.spellinstances.ArcaneSurge;
import wbs.magic.spellinstances.SpellInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import wbs.magic.spellinstances.*;
import wbs.magic.spellinstances.ranged.*;
import wbs.magic.spellinstances.ranged.projectile.*;
import wbs.magic.spellinstances.ranged.targeted.*;
import wbs.magic.spellinstances.ranged.targeted.missile.*;

public class NativeSpellLoader implements SpellLoader {

    private static final Set<Class<? extends SpellInstance>> classes = new HashSet<>();

    static {
        Collections.addAll(classes,
                // Uncategorized
                AntiMagicShell.class,
                ArcaneSurge.class,
                Blink.class,
                ConeOfCold.class,
                CheckMana.class,
                FlySpell.class,
                Hallucination.class,
                Leap.class,
                RegenerateMana.class,
                Shield.class,
                VoidStep.class,
                WaterWalkSpell.class,
                Conflagration.class,

                // Ranged, non-targeted non-projectile
                GravityWellSpell.class,
                NegateMagic.class,
                PrismaticRay.class,
                Tornado.class,
                Recall.class,

                // Projectile
                BlizzardSpell.class,
                DepthSurgeSpell.class,
                EldritchBlast.class,
                EnergyBurst.class,
                FaerieFireSpell.class,
                FireballSpell.class,
                Firebolt.class,
                FrostShards.class,
                Warp.class,

                // Targeted
                ChainLightning.class,
                Confuse.class,
                CounterSpell.class,
                Disarm.class,
                Displace.class,
                DominateMonster.class,
                DrainLife.class,
                HexSpell.class,
                Hold.class,
                ImbueCreature.class,
                InflictWounds.class,
                MinecraftProjectileSpell.class,
                Polymorph.class,
                Push.class,
                Regenerate.class,

                // Missile
                MagicMissiles.class
        );
    }

    @Override
    public Collection<Class<? extends SpellInstance>> getSpells() {
        return classes;
    }

    @Override
    public int getSpellCount() {
        return classes.size();
    }
}
