package wbs.magic.spells.ranged.projectile;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.magic.SpellCaster;
import wbs.magic.objects.generics.DynamicProjectileObject;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.configuration.*;
import wbs.magic.spellmanagement.configuration.options.DoubleOptions.DoubleOption;
import wbs.magic.spellmanagement.configuration.options.EnumOptions.EnumOption;
import wbs.magic.spellmanagement.configuration.options.IntOptions.IntOption;
import wbs.magic.spellmanagement.configuration.options.StringOptions.StringOption;
import wbs.magic.spells.framework.CastingContext;
import wbs.magic.spells.ranged.projectile.PsychedelicGlimmer.CycleType;
import wbs.utils.util.WbsColours;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsSound;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Spell(name = "Psychedelic Glimmer",
        cost = 0,
        description = "A ray of light that does... something.")
@SpellSound(sound = Sound.BLOCK_BEACON_ACTIVATE, pitch = 2F)
@RestrictWandControls(dontRestrictLineOfSight = true)
@DoubleOption(optionName = "size", defaultValue = 0.1)
@IntOption(optionName = "colour-period", defaultValue = 1)
@StringOption(optionName = "colours",
        defaultValue = "ff9aa2, ffb7b2, ffdac1, e2f0cb, b5ead7, c7ceea",
        suggestions = {"5bcefa, f5a9b8, ffffff",
                "000000, a3a3a3, ffffff, 800080",
                "fcf434, ffffff, 9c59d1, 2c2c2c"
        }
)
@StringOption(optionName = "message", defaultValue = "")
@IntOption(optionName = "message-count", defaultValue = 1)
@EnumOption(optionName = "cycle-type", defaultValue = "cycle", enumType = CycleType.class)
// Override parent class defaults for these
@DoubleOption(optionName = "speed", defaultValue = 120)
@DoubleOption(optionName = "hitbox-size", defaultValue = 0.8)
public class PsychedelicGlimmer extends ProjectileSpell {
    public PsychedelicGlimmer(SpellConfig config, String directory) {
        super(config, directory);

        size = config.getDouble("size");
        period = config.getInt("colour-period");
        periodSquared = period * period;
        coloursString = config.getString("colours");

        List<String> invalidColourStrings = new LinkedList<>();

        colours.addAll(getColours(coloursString, invalidColourStrings));

        if (!invalidColourStrings.isEmpty()) {
            logError("Invalid colours (" + String.join(", ", invalidColourStrings) + ") in colours string \"" + coloursString + "\".", directory + "/colours");
        }

        if (colours.isEmpty()) {
            coloursString = config.getDefaultString("colours");
            colours.addAll(getColours(coloursString, invalidColourStrings));
        }

        cycleType = config.getEnum("cycle-type", CycleType.class);

        int particleAmount = (int) (size * 25);
        effect.setAmount(particleAmount);
        effect.setXYZ(size);
        effect.setOptions(new Particle.DustOptions(Color.fromRGB(91, 206, 250), 1.5f));

        Particle particle = Particle.REDSTONE;
        effects.addEffect(effect, particle);

        message = config.getString("message");
        messageCount = config.getInt("message-count");
    }

    private List<Color> getColours(String coloursString, List<String> invalidColourStrings) {
        coloursString = coloursString.replaceAll("\\s", "");

        List<Color> colours = new LinkedList<>();
        String[] colourStrings = coloursString.split("[,;]");

        for (String colourString : colourStrings) {
            Color colour = WbsColours.fromHexOrDyeString(colourString);

            if (colour != null) {
                colours.add(colour);
            } else {
                invalidColourStrings.add(colourString);
            }
        }

        return colours;
    }

    private final double size;
    private final int period;
    private final int periodSquared;
    private final List<Color> colours = new LinkedList<>();
    private final CycleType cycleType;
    private final String message;
    private final int messageCount;

    private String coloursString;

    private final WbsParticleGroup effects = new WbsParticleGroup();
    private static final NormalParticleEffect effect = new NormalParticleEffect();

    public boolean cast(CastingContext context) {
        SpellCaster caster = context.caster;
        PsychedelicGlimmerProjectile projectile = new PsychedelicGlimmerProjectile(caster.getEyeLocation(), caster, this);

        projectile.setParticle(effects);

        projectile.run();
        return true;
    }

    @Override
    public String toString() {
        String asString = super.toString();

        asString += "\n&rSize: &7" + size;
        asString += "\n&rColour period: &7" + period;
        asString += "\n&rColours: &7" + coloursString;
        asString += "\n&rCycle Type: &7" + WbsEnums.toPrettyString(cycleType);
        if (!message.isEmpty()) {
            asString += "\n&rMessage: \"&7" + message + "&r\"";
            asString += "\n&rMessage Count: &7" + messageCount;
        }

        return asString;
    }

    private final Random random = new Random();

    private class PsychedelicGlimmerProjectile extends DynamicProjectileObject {

        public PsychedelicGlimmerProjectile(Location location, SpellCaster caster, ProjectileSpell castingSpell) {
            super(location, caster, castingSpell);

            hitSound.addSound(new WbsSound(Sound.BLOCK_BEACON_POWER_SELECT, 1.5f, 1f));
        }

        private int changes = 0;
        private int index = 0;
        private double distanceSinceChange = 0;

        @Override
        protected boolean afterMove(Location oldLocation, Location newLocation) {
            double distanceMoved = oldLocation.distanceSquared(newLocation);

            distanceSinceChange += distanceMoved;

            int colourCount = colours.size();

            switch (cycleType) {
                case CYCLE:
                    index = changes % colourCount;
                    break;
                case PING_PONG:
                    index = changes % (colourCount * 2);
                    if (index >= colourCount) {
                        index = (colourCount * 2) - 1 - index;
                    }
                    break;
                case RANDOM:
                    if (distanceSinceChange > periodSquared) {
                        index = random.nextInt(colourCount);
                    }
                    break;
                case MIX:
                    index = random.nextInt(colourCount);
                    break;

            }

            if (distanceSinceChange > periodSquared) {
                changes++;
                distanceSinceChange = 0;
            }

            Color color = colours.get(index);
            effect.setOptions(new Particle.DustOptions(color, 1.5f));

            if (effects != null) {
                effects.play(location);
            }


            return super.afterMove(oldLocation, newLocation);
        }

        private final List<Player> hitPlayers = new LinkedList<>();

        @Override
        protected boolean hitEntity(Location hitLocation, LivingEntity hitEntity) {
            if (hitEntity instanceof Player) {
                Player player = (Player) hitEntity;
                if (!hitPlayers.contains(player)) {
                    for (int i = 0; i < messageCount; i++) {
                        plugin.sendMessage(message, hitEntity);
                    }
                    hitPlayers.add(player);
                }
            }

            return false;
        }
    }

    public enum CycleType {
        CYCLE,
        PING_PONG,
        RANDOM,
        MIX,
    }
}
