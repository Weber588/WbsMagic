package wbs.magic.spellmanagement;

import wbs.magic.spells.ArcaneSurge;
import wbs.magic.spells.SpellInstance;

import java.util.*;

import wbs.magic.spells.*;
import wbs.magic.spells.ranged.*;
import wbs.magic.spells.ranged.projectile.*;
import wbs.magic.spells.ranged.targeted.*;
import wbs.magic.spells.ranged.targeted.missile.*;

public class NativeSpellLoader implements SpellLoader {

    private static final List<Class<? extends SpellInstance>> classes = new LinkedList<>();

    static {
        Collections.addAll(classes,
                // Uncategorized
                AntiMagicShell.class,
                ArcaneSurge.class,
                Blink.class,
                BridgeSpell.class,
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
                Carve.class,
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
            //    FireballSpell.class,
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
            //    HexSpell.class,
                Hold.class,
                ImbueCreature.class,
                InflictWounds.class,
                ShootEntitySpell.class,
                Polymorph.class,
                Push.class,
                Regenerate.class,

                // Missile
                MagicMissiles.class
        );
    }

    @Override
    public List<Class<? extends SpellInstance>> getSpells() {
        return classes;
    }

    @Override
    public int getSpellCount() {
        return classes.size();
    }
}
