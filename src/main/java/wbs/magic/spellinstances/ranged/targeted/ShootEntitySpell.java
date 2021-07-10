package wbs.magic.spellinstances.ranged.targeted;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.annotations.Spell;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spells.SpellConfig;
import wbs.magic.targeters.SelfTargeter;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMaterials;
import wbs.utils.util.WbsMath;

import java.util.Set;

@Spell(name = "Generic Projectile",
        cost = 5,
        cooldown = 5,
        description = "Fire a minecraft projectile, as configured."
)
@SpellOption(optionName = "projectile", type = SpellOptionType.STRING, defaultString = "ARROW", enumType = EntityType.class)
@SpellOption(optionName = "speed", type = SpellOptionType.DOUBLE, defaultDouble = 1)
@SpellOption(optionName = "amount", type = SpellOptionType.INT)
@SpellOption(optionName = "delay", type = SpellOptionType.DOUBLE, defaultDouble = 0.25)
@SpellOption(optionName = "spread", type = SpellOptionType.DOUBLE, defaultDouble = 0.1)
@SpellOption(optionName = "on-fire", type = SpellOptionType.BOOLEAN, defaultBool = false, aliases = {"burning", "fire", "flame"})
@SpellOption(optionName = "yield", type = SpellOptionType.DOUBLE, defaultDouble = -1, aliases = {"explosion-damage", "explosion-force"})
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "potion", type = SpellOptionType.STRING) // Don't set a default so it'll be null by default
@SpellOption(optionName = "amplifier", type = SpellOptionType.INT, defaultInt = 1)
@SpellOption(optionName = "lingering", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "material", type = SpellOptionType.STRING, defaultString = "OAK_PLANKS", enumType = Material.class)
@SpellOption(optionName = "charged", type = SpellOptionType.BOOLEAN, defaultBool = false)
// Overrides
@SpellOption(optionName = "targeter", type = SpellOptionType.STRING, defaultString = "SELF")
public class ShootEntitySpell extends TargetedSpell {

    public ShootEntitySpell(SpellConfig config, String directory) {
        super(config, directory);

        speed = config.getDouble("speed");
        spread = config.getDouble("spread");
        amount = config.getInt("amount");
        delay = (int) (config.getDouble("delay") * 20);
        onFire = config.getBoolean("on-fire");
        yield = (float) config.getDouble("yield");
        duration = (int) (config.getDouble("duration") * 20);
        charged = config.getBoolean("charged");

        int amplifier = config.getInt("amplifier") - 1;
        PotionEffectType potionType = PotionEffectType.getByName(config.getString("potion"));

        if (potionType != null) {
            potion = potionType.createEffect(duration, amplifier);
        } else {
            potion = null;
        }

        lingering = config.getBoolean("lingering");
        String materialString = config.getString("material");
        material = WbsEnums.getEnumFromString(Material.class, materialString);

        if (material == null) {
            logError("Invalid material: " + materialString, directory);
            material = Material.OAK_PLANKS;
        }

        String projectileString = config.getString("projectile");
        type = WbsEnums.getEnumFromString(EntityType.class, projectileString);

        if (type == null) {
            logError("Invalid projectile: " + projectileString, directory);
            type = EntityType.ARROW;
        }

        switch (type) {
            case UNKNOWN:
            case PLAYER:
            case ENDER_DRAGON:
            case DROPPED_ITEM:
                throw new InvalidConfigurationException("The entity type " + type.name() + " is not allowed.");
            case SPLASH_POTION:
            case AREA_EFFECT_CLOUD:
                if (potion == null) {
                    throw new InvalidConfigurationException(type.name() + " requires a potion type to be set.");
                }
                break;
            case FALLING_BLOCK:
                if (!material.isBlock()) {
                    throw new InvalidConfigurationException("Material must be a block when using " + type.name());
                }
        }

        projectileName = WbsEnums.toPrettyString(type);

        entityClass = type.getEntityClass();
    }

    private final Class<? extends Entity> entityClass;
    private EntityType type;
    private final String projectileName;
    private final double speed;
    private final double spread;
    private final int amount;
    private final int delay;
    private final boolean onFire;
    private final float yield;
    private final int duration;
    @Nullable
    private final PotionEffect potion;
    private final boolean lingering;
    @NotNull
    private Material material;
    private final boolean charged;

    @Override
    protected <T extends LivingEntity> boolean preCast(SpellCaster caster, Set<T> targets) {
        if (disabled) return true;

        if (targets.size() == 1) {
            caster.sendActionBar("Firing &h" + amount + " " + projectileName + (amount != 1 ? "s" : "") + "&r!");
        } else {
            caster.sendActionBar("Firing &h" + amount + " " + projectileName + (amount != 1 ? "s" : "") + "&r at " + targets.size() + " creatures!");
        }

        if (delay <= 0) {
            for (int i = 0; i < amount; i++) {
                for (LivingEntity target : targets) {
                    castOn(caster, target);
                }
            }
        } else {
            new BukkitRunnable() {
                int amountSoFar = 0;

                @Override
                public void run() {
                    for (LivingEntity target : targets) {
                        castOn(caster, target);
                    }

                    amountSoFar++;

                    if (amountSoFar >= amount) {
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0, delay);
        }

        return true;
    }

    private boolean disabled = false;

    @Override
    protected <T extends LivingEntity> void castOn(SpellCaster caster, T target) {
        Entity entity;

        switch (type) {
            case FALLING_BLOCK:
                FallingBlock fallingBlock = caster.getPlayer()
                        .getWorld().spawnFallingBlock(
                                caster.getEyeLocation(),
                                material.createBlockData()
                        );

                fallingBlock.setDropItem(false);

                entity = fallingBlock;
                break;
            default:
                try {
                    entity = target.getWorld().spawn(caster.getEyeLocation(), entityClass);
                } catch (IllegalArgumentException e) {
                    disabled = true;
                    logger.warning(e.getMessage());
                    return;
                }
        }


        if (entity instanceof Projectile) {
            Projectile proj = (Projectile) entity;
            proj.setShooter(caster.getPlayer());

            if (proj instanceof ShulkerBullet) {
                ShulkerBullet bullet = (ShulkerBullet) proj;

                bullet.setTarget(target);
                return;
            }

            if (proj instanceof AbstractArrow) {
                AbstractArrow aArrow = (AbstractArrow) proj;
                aArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                aArrow.setTicksLived(20 * 60 - duration); // 1 minute, minus lifetime ticks

                if (potion != null && aArrow instanceof Arrow) {
                    Arrow arrow = (Arrow) aArrow;
                    arrow.addCustomEffect(potion, true);
                    arrow.setColor(potion.getType().getColor());
                }
            }

            if (proj instanceof ThrownPotion) {
                assert potion != null; // Checked in constructor
                Material material = lingering ? Material.LINGERING_POTION : Material.SPLASH_POTION;
                ItemStack potionItem = new ItemStack(material);
                PotionMeta meta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(material);
                assert meta != null;

                meta.setColor(potion.getType().getColor());
                meta.addCustomEffect(potion, true);

                potionItem.setItemMeta(meta);

                ThrownPotion thrownPotion = (ThrownPotion) proj;
                thrownPotion.setItem(potionItem);
            }
        }

        if (entity instanceof Creeper) {
            ((Creeper) entity).setPowered(charged);
        }

        if (entity instanceof AreaEffectCloud) {
            assert potion != null; // Checked in constructor
            AreaEffectCloud cloud = (AreaEffectCloud) entity;

            cloud.addCustomEffect(potion, true);
        }

        if (entity instanceof Boat) {
            Boat boat = (Boat) entity;
            TreeSpecies species = WbsMaterials.getTreeSpecies(material);

            if (species == null) {
                species = TreeSpecies.GENERIC;
            }

            boat.setWoodType(species);
        }

        if (entity instanceof Explosive) {
            Explosive explosive = (Explosive) entity;

            if (yield != -1) explosive.setYield(yield);
            explosive.setIsIncendiary(onFire && explosive.getYield() > 0);

            if (explosive instanceof TNTPrimed) {
                TNTPrimed tnt = (TNTPrimed) explosive;
                tnt.setFuseTicks(duration);
            }
            if (explosive instanceof WitherSkull) {
                ((WitherSkull) explosive).setCharged(charged);
            }
        }

        Vector velocity;
        if (targeter instanceof SelfTargeter) {
            velocity = caster.getFacingVector();
        } else {
            velocity = target.getEyeLocation()
                    .subtract(caster.getEyeLocation())
                    .toVector();
        }

        velocity.add(WbsMath.randomVector(spread))
                .normalize()
                .multiply(speed);

        entity.setVelocity(velocity);

        if (entity instanceof Fireball) {
            ((Fireball) entity).setDirection(velocity);
        }

        if (onFire) {
            entity.setFireTicks(100000);
        }

    }
}
