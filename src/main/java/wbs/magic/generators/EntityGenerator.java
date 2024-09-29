package wbs.magic.generators;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.loot.Lootable;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.magic.WbsMagic;
import wbs.magic.spellmanagement.configuration.options.EntityOptions.EntityOption;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.VersionUtil;
import wbs.utils.util.WbsColours;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMaterials;

import java.util.Objects;
import java.util.function.Consumer;

public class EntityGenerator extends OptionGenerator {
    private static final Color DEFAULT_COLOUR = Color.RED;

    // Non-configurable restriction on which type may be chosen. Provided by annotation by default.
    private final Class<? extends Entity> classRestriction;

    private Class<? extends Entity> entityClass;
    private EntityType type;
    private String displayName;
    private String prettyTypeName;
    private boolean baby;
    private boolean charged;

    @Nullable
    private Color colour;
    private boolean doDrops;
    private int fireTicks;
    private int fuseDuration;

    @Nullable
    private Material holdingItem;
    private boolean lingering;
    private Material material;

    @Nullable
    private PotionEffect potion;
    private int potionAmplifier;
    private int potionDuration;
    private PotionEffectType potionType;

    private float yield;

    public EntityGenerator(EntityOption annotation) {
        fireTicks = (int) (annotation.fireDuration() * 20);
        yield = annotation.yield();
        fuseDuration = (int) (annotation.fuseDuration() * 20);
        charged = annotation.charged();

        if (!annotation.potionType().isEmpty()) {
            potionType = PotionEffectType.getByName(annotation.potionType());
        }
        potionDuration = (int) (annotation.potionDuration() * 20);
        potionAmplifier = annotation.potionAmplifier();

        populatePotion();

        lingering = annotation.lingering();

        baby = annotation.baby();
        doDrops = annotation.doDrops();

        String colourString = annotation.colour();
        colour = WbsColours.fromHexOrDyeString(colourString);

        String materialString = annotation.material();
        material = WbsEnums.getEnumFromString(Material.class, materialString);

        String holdingItemString = annotation.holdingItem();
        holdingItem = WbsEnums.getEnumFromString(Material.class, holdingItemString);

        if (material == null) {
            settings.logError("Invalid material: " + materialString, "Internal");
            material = Material.OAK_PLANKS;
        }

        classRestriction = annotation.classRestriction();
        String error = setTypeFromString(annotation.entityType());
        if (error != null) {
            settings.logError(error, "Internal");
            setTypeFromString(EntityType.ARROW.name());
        }

        switch (type) {
            case UNKNOWN:
            case PLAYER:
            case ENDER_DRAGON:
            case ITEM:
                throw new InvalidConfigurationException("The entity type " + prettyTypeName + " is not allowed.");
            case POTION:
            case AREA_EFFECT_CLOUD:
                if (potion == null) {
                    throw new InvalidConfigurationException(prettyTypeName + " requires a potion type to be set.");
                }
                break;
            case FALLING_BLOCK:
                if (!material.isBlock()) {
                    throw new InvalidConfigurationException("Material must be a block when using " + prettyTypeName);
                }
        }
    }

    public String setTypeFromString(String entityTypeString) {
        type = WbsEnums.getEnumFromString(EntityType.class, entityTypeString);

        if (type == null) {
            return "Invalid entity type: " + entityTypeString;
        }

        entityClass = type.getEntityClass();

        if (entityClass == null || !classRestriction.isAssignableFrom(entityClass)) {
            return "Invalid entity type: " + entityTypeString + ". " +
                    "Only entities that are considered a " + classRestriction.getSimpleName() + " may be used.";
        }

        prettyTypeName = WbsEnums.toPrettyString(type);
        return null;
    }

    public Entity spawn(Location loc, @Nullable LivingEntity target, @Nullable Player owner, @Nullable Consumer<Entity> preSpawn) {
        World world = Objects.requireNonNull(loc.getWorld());

        Entity entity;

        if (type == EntityType.FALLING_BLOCK) {
            FallingBlock fallingBlock = world.spawnFallingBlock(
                    loc, material.createBlockData());

            fallingBlock.setDropItem(doDrops);

            entity = fallingBlock;

            if (preSpawn != null) {
                preSpawn.accept(entity);
            }
            configureEntity(entity, target, owner);
        } else {
            try {
                entity = world.spawn(loc, entityClass, spawnedEntity -> {
                    if (preSpawn != null) {
                        preSpawn.accept(spawnedEntity);
                    }

                    configureEntity(spawnedEntity, target, owner);
                });

            } catch (IllegalArgumentException e) {
                WbsMagic.getInstance().getLogger().warning(e.getMessage());
                return null;
            }
        }

        return entity;
    }

    private void configureEntity(Entity entity, @Nullable LivingEntity target, @Nullable Player owner) {
        entity.setCustomName(displayName);

        if (entity instanceof Projectile) {
            Projectile proj = (Projectile) entity;
            proj.setShooter(owner);

            if (proj instanceof ShulkerBullet) {
                ShulkerBullet bullet = (ShulkerBullet) proj;

                bullet.setTarget(target);
            }

            if (proj instanceof AbstractArrow) {
                AbstractArrow aArrow = (AbstractArrow) proj;
                aArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
                aArrow.setTicksLived(20 * 60 - potionDuration); // 1 minute, minus lifetime ticks

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

                // "Each level of power is half a second of flight time"
                int halfSeconds = fuseDuration / 10;
                firework.getFireworkMeta().setPower(halfSeconds);
            }
        }

        if (entity instanceof Lootable) {
            if (!doDrops) {
                ((Lootable) entity).setLootTable(null);
            }
        }

        if (entity instanceof Creeper) {
            ((Creeper) entity).setPowered(charged);
        }

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;

            if (holdingItem != null && holdingItem.isItem()) {
                EntityEquipment equipment = livingEntity.getEquipment();
                if (equipment != null) {
                    equipment.setItemInMainHand(new ItemStack(holdingItem));
                }
            }

            if (livingEntity instanceof Mob) {
                Mob mob = (Mob) livingEntity;

                mob.setTarget(target);

                if (mob instanceof Ageable) {
                    Ageable ageable = (Ageable) mob;

                    if (baby) {
                        ageable.setBaby();
                    } else {
                        ageable.setAdult();
                    }
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
            explosive.setIsIncendiary(fireTicks > 0 && explosive.getYield() > 0);

            if (explosive instanceof TNTPrimed) {
                TNTPrimed tnt = (TNTPrimed) explosive;
                tnt.setFuseTicks(potionDuration);
            }
            if (explosive instanceof WitherSkull) {
                ((WitherSkull) explosive).setCharged(charged);
            }
        }

        if (fireTicks > 0) {
            entity.setFireTicks(fireTicks);
        }
    }

    public String getEntityName() {
        return prettyTypeName;
    }

    @NotNull
    public Color getColour() {
        return colour != null ? colour : DEFAULT_COLOUR;
    }

    public void setColour(@Nullable Color colour) {
        this.colour = colour;
    }

    @Override
    public String toString() {
        String asString = "";

        asString += "&rOn fire: &7" + fireTicks;

        if (Ageable.class.isAssignableFrom(entityClass)) {
            asString += "\n&rBaby: &7" + baby;
        }

        if (Colorable.class.isAssignableFrom(entityClass)) {
            DyeColor dyeColour = WbsColours.toDyeColour(getColour());
            String hexString = Integer.toHexString(getColour().asRGB());
            if (VersionUtil.getVersion() >= 16) {
                asString += "\n&rColour: &7#" + hexString + " &#" + hexString + "(" + WbsEnums.toPrettyString(dyeColour) + ")";
            } else {
                asString += "\n&rColour: &7#" + hexString + " (" + WbsEnums.toPrettyString(dyeColour) + ")";
            }
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
            asString += "\n&rDuration: &7" + (potionDuration / 20) + " seconds";
            asString += "\n&rAmplifier: &7" + potionAmplifier;
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

    private void populatePotion() {
        if (potionType != null) {
            potion = potionType.createEffect(potionDuration, potionAmplifier - 1);
        } else {
            potion = null;
        }
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    public float getYield() {
        return yield;
    }

    public void setYield(float yield) {
        this.yield = yield;
    }

    public int getPotionDuration() {
        return potionDuration;
    }

    public void setPotionDuration(int potionDuration) {
        this.potionDuration = potionDuration;
        populatePotion();
    }

    public int getPotionAmplifier() {
        return potionAmplifier;
    }

    public void setPotionAmplifier(int potionAmplifier) {
        this.potionAmplifier = potionAmplifier;
        populatePotion();
    }

    public PotionEffectType getPotionType() {
        return potionType;
    }

    public void setPotionType(PotionEffectType potionType) {
        this.potionType = potionType;
        populatePotion();
    }

    public int getFuseDuration() {
        return fuseDuration;
    }

    public void setFuseDuration(int fuseDuration) {
        this.fuseDuration = fuseDuration;
    }

    public boolean isLingering() {
        return lingering;
    }

    public void setLingering(boolean lingering) {
        this.lingering = lingering;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public boolean isCharged() {
        return charged;
    }

    public void setCharged(boolean charged) {
        this.charged = charged;
    }

    public boolean isBaby() {
        return baby;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    public boolean doDrops() {
        return doDrops;
    }

    public void setDoDrops(boolean doDrops) {
        this.doDrops = doDrops;
    }

    public @Nullable Material getHoldingItem() {
        return holdingItem;
    }

    public void setHoldingItem(@Nullable Material holdingItem) {
        this.holdingItem = holdingItem;
    }
}
