package wbs.magic.generators;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.MagicSettings;
import wbs.magic.WbsMagic;
import wbs.magic.spellmanagement.configuration.SpellOption;
import wbs.magic.spellmanagement.configuration.SpellOptionType;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsColours;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMaterials;

@SpellOption(optionName = "on-fire", type = SpellOptionType.BOOLEAN, defaultBool = false, aliases = {"burning", "fire", "flame"})
@SpellOption(optionName = "yield", type = SpellOptionType.DOUBLE, defaultDouble = -1, aliases = {"explosion-damage", "explosion-force"})
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "potion", type = SpellOptionType.STRING) // Don't set a default so it'll be null by default
@SpellOption(optionName = "amplifier", type = SpellOptionType.INT, defaultInt = 1, aliases = {"power, level"})
@SpellOption(optionName = "lingering", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "material", type = SpellOptionType.STRING, defaultString = "OAK_PLANKS", enumType = Material.class)
@SpellOption(optionName = "charged", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "baby", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "colour", type = SpellOptionType.STRING, defaultString = "", aliases = {"colour"}, enumType = DyeColor.class)
public class EntityGenerator extends OptionGenerator {

    private static final Color DEFAULT_COLOUR = Color.RED;

    public EntityGenerator(SpellConfig config, MagicSettings settings, String directory) {
        super(config, settings, directory);
        onFire = config.getBoolean("on-fire");
        yield = (float) config.getDouble("yield");
        duration = (int) (config.getDouble("duration") * 20);
        charged = config.getBoolean("charged");

        amplifier = config.getInt("amplifier");
        PotionEffectType potionType = PotionEffectType.getByName(config.getString("potion"));

        if (potionType != null) {
            potion = potionType.createEffect(duration, amplifier - 1);
        } else {
            potion = null;
        }

        lingering = config.getBoolean("lingering");
        String materialString = config.getString("material");
        material = WbsEnums.getEnumFromString(Material.class, materialString);
        baby = config.getBoolean("baby");

        String colourString = config.getString("colour");
        colour = WbsColours.fromHexOrDyeString(colourString);

        if (material == null) {
            settings.logError("Invalid material: " + materialString, directory);
            material = Material.OAK_PLANKS;
        }

        String projectileString = config.getString("projectile");
        type = WbsEnums.getEnumFromString(EntityType.class, projectileString);

        if (type == null) {
            settings.logError("Invalid projectile: " + projectileString, directory);
            type = EntityType.ARROW;
        }

        entityName = WbsEnums.toPrettyString(type);

        switch (type) {
            case UNKNOWN:
            case PLAYER:
            case ENDER_DRAGON:
            case DROPPED_ITEM:
                throw new InvalidConfigurationException("The entity type " + entityName + " is not allowed.");
            case SPLASH_POTION:
            case AREA_EFFECT_CLOUD:
                if (potion == null) {
                    throw new InvalidConfigurationException(entityName + " requires a potion type to be set.");
                }
                break;
            case FALLING_BLOCK:
                if (!material.isBlock()) {
                    throw new InvalidConfigurationException("Material must be a block when using " + entityName);
                }
        }

        entityClass = type.getEntityClass();
    }

    private final Class<? extends Entity> entityClass;
    private EntityType type;
    private final String entityName;
    private final boolean onFire;
    private final float yield;
    private final int duration;
    private final int amplifier;
    @Nullable
    private final PotionEffect potion;
    private final boolean lingering;
    private Material material;
    private final boolean charged;
    private final boolean baby;
    @Nullable
    private final Color colour;

    public Entity spawn(Location loc) {
        return spawn(loc, null, null);
    }

    public Entity spawn(Location loc, @Nullable LivingEntity target) {
        return spawn(loc, target, null);
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public Entity spawn(Location loc, @Nullable LivingEntity target, @Nullable Player owner) {
        World world = loc.getWorld();
        assert world != null;

        Entity entity;

        switch (type) {
            case FALLING_BLOCK:
                FallingBlock fallingBlock = world.spawnFallingBlock(
                                loc,material.createBlockData());

                fallingBlock.setDropItem(false);

                entity = fallingBlock;
                break;
            default:
                try {
                    entity = world.spawn(loc, entityClass);
                } catch (IllegalArgumentException e) {
                    WbsMagic.getInstance().getLogger().warning(e.getMessage());
                    return null;
                }
        }


        if (entity instanceof Projectile) {
            Projectile proj = (Projectile) entity;
            proj.setShooter(owner);

            if (proj instanceof ShulkerBullet) {
                ShulkerBullet bullet = (ShulkerBullet) proj;

                bullet.setTarget(target);
                return null;
            }

            if (proj instanceof AbstractArrow) {
                AbstractArrow aArrow = (AbstractArrow) proj;
                aArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                aArrow.setTicksLived(20 * 60 - duration); // 1 minute, minus lifetime ticks

                if (potion != null && aArrow instanceof Arrow) {
                    Arrow arrow = (Arrow) aArrow;
                    arrow.addCustomEffect(potion, true);
                    if (colour != null) {
                        arrow.setColor(colour);
                    } else {
                        arrow.setColor(potion.getType().getColor());
                    }
                }
            }

            if (proj instanceof ThrownPotion) {
                assert potion != null; // Checked in constructor
                Material material = lingering ? Material.LINGERING_POTION : Material.SPLASH_POTION;
                ItemStack potionItem = new ItemStack(material);
                PotionMeta meta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(material);
                assert meta != null;

                if (colour != null) {
                    meta.setColor(colour);
                } else {
                    meta.setColor(potion.getType().getColor());
                }
                meta.addCustomEffect(potion, true);

                potionItem.setItemMeta(meta);

                ThrownPotion thrownPotion = (ThrownPotion) proj;
                thrownPotion.setItem(potionItem);
            }

            if (proj instanceof Firework) {
                Firework firework = (Firework) proj;
                firework.setShotAtAngle(true);

                firework.getFireworkMeta().setPower(amplifier);
            }
        }

        if (entity instanceof Creeper) {
            ((Creeper) entity).setPowered(charged);
        }

        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;

            mob.setTarget(target);

            if (mob instanceof Ageable) {
                Ageable ageable = (Ageable) mob;

                if (baby) ageable.setBaby();
            }

            if (mob instanceof Tameable) {
                Tameable tameable = (Tameable) mob;

                tameable.setOwner(owner);

                if (tameable instanceof Wolf) {
                    ((Wolf) tameable).setCollarColor(WbsColours.toDyeColour(colour));
                }
                if (tameable instanceof Cat) {
                    ((Cat) tameable).setCollarColor(WbsColours.toDyeColour(colour));
                }
            }
        }

        if (entity instanceof AreaEffectCloud) {
            assert potion != null; // Checked in constructor
            AreaEffectCloud cloud = (AreaEffectCloud) entity;

            cloud.addCustomEffect(potion, true);
            if (colour != null) cloud.setColor(colour);
        }

        if (entity instanceof Boat) {
            Boat boat = (Boat) entity;
            TreeSpecies species = WbsMaterials.getTreeSpecies(material);

            if (species == null) {
                species = TreeSpecies.GENERIC;
            }

            boat.setWoodType(species);
        }

        if (entity instanceof Colorable) {
            Colorable colorable = (Colorable) entity;

            if (colour != null) {
                colorable.setColor(WbsColours.toDyeColour(colour));
            }
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

        if (onFire) {
            entity.setFireTicks(100000);
        }

        return entity;
    }

    public String getEntityName() {
        return entityName;
    }

    @NotNull
    private Color getColour() {
        return colour != null ? colour : DEFAULT_COLOUR;
    }

    @Override
    public String toString() {
        String asString = "";

        asString += "&rOn fire: &7" + onFire;

        if (Ageable.class.isAssignableFrom(entityClass)) {
            asString += "\n&rBaby: &7" + baby;
        }

        if (Colorable.class.isAssignableFrom(entityClass)) {
            DyeColor dyeColour = WbsColours.toDyeColour(colour);
            asString += "\n&rColour: &7#" + Integer.toHexString(getColour().asRGB()) + " (" + dyeColour + ")";
        }

        if (Explosive.class.isAssignableFrom(entityClass)) {
            asString += "\n&rYield: &7" + yield;
        }

        if (AreaEffectCloud.class.isAssignableFrom(entityClass) ||
                ThrownPotion.class.isAssignableFrom(entityClass) ||
                AbstractArrow.class.isAssignableFrom(entityClass)
        ) {
            assert potion != null;
            asString += "\n&rPotion: &7" + potion.getType().getName();
            asString += "\n&rDuration: &7" + (duration / 20) + " seconds";
            asString += "\n&rAmplifier: &7" + amplifier;
        }

        if (ThrownPotion.class.isAssignableFrom(entityClass)) {
            asString += "\n&rLingering: &7" + lingering;
        }

        if (FallingBlock.class.isAssignableFrom(entityClass)) {
            asString += "\n&rMaterial: &7" + material;
        }

        if (Creeper.class.isAssignableFrom(entityClass) ||
                WitherSkull.class.isAssignableFrom(entityClass)
        ) {
            asString += "\n&rCharged: &7" + lingering;
        }

        return asString;
    }
}
