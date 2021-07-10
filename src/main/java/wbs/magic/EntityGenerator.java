package wbs.magic;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.annotations.SpellOption;
import wbs.magic.enums.SpellOptionType;
import wbs.magic.spells.SpellConfig;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMaterials;

@SpellOption(optionName = "on-fire", type = SpellOptionType.BOOLEAN, defaultBool = false, aliases = {"burning", "fire", "flame"})
@SpellOption(optionName = "yield", type = SpellOptionType.DOUBLE, defaultDouble = -1, aliases = {"explosion-damage", "explosion-force"})
@SpellOption(optionName = "duration", type = SpellOptionType.DOUBLE, defaultDouble = 10)
@SpellOption(optionName = "potion", type = SpellOptionType.STRING) // Don't set a default so it'll be null by default
@SpellOption(optionName = "amplifier", type = SpellOptionType.INT, defaultInt = 1)
@SpellOption(optionName = "lingering", type = SpellOptionType.BOOLEAN, defaultBool = false)
@SpellOption(optionName = "material", type = SpellOptionType.STRING, defaultString = "OAK_PLANKS", enumType = Material.class)
@SpellOption(optionName = "charged", type = SpellOptionType.BOOLEAN, defaultBool = false)
public class EntityGenerator {

    public EntityGenerator(SpellConfig config, MagicSettings settings, String directory) {
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
            settings.logError("Invalid material: " + materialString, directory);
            material = Material.OAK_PLANKS;
        }

        String projectileString = config.getString("projectile");
        type = WbsEnums.getEnumFromString(EntityType.class, projectileString);

        if (type == null) {
            settings.logError("Invalid projectile: " + projectileString, directory);
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

        entityName = WbsEnums.toPrettyString(type);

        entityClass = type.getEntityClass();
    }

    private final Class<? extends Entity> entityClass;
    private EntityType type;
    private final String entityName;
    private final boolean onFire;
    private final float yield;
    private final int duration;
    @Nullable
    private final PotionEffect potion;
    private final boolean lingering;
    @NotNull
    private Material material;
    private final boolean charged;

    public Entity spawn(Location loc) {
        return spawn(loc, null, null);
    }

    public Entity spawn(Location loc, @Nullable LivingEntity target) {
        return spawn(loc, target, null);
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public Entity spawn(Location loc, @Nullable LivingEntity target, @Nullable Player shooter) {
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
            proj.setShooter(shooter);

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

        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;

            mob.setTarget(target);
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

        if (onFire) {
            entity.setFireTicks(100000);
        }

        return entity;
    }

    public String getEntityName() {
        return entityName;
    }
}
