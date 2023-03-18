package wbs.magic;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.utils.util.WbsMath;

import java.util.Arrays;
import java.util.function.Function;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public enum DamageType {
    ALL(DamageCause.values()),

    HEAT(
            DamageType::getFireModifier,
            DamageType::getFireResModifier,
            DamageCause.FIRE,
            DamageCause.FIRE_TICK,
            DamageCause.HOT_FLOOR,
            DamageCause.LAVA
    ),
    ELECTRIC(DamageCause.LIGHTNING),
    PHYSICAL(
            DamageCause.CONTACT,
            DamageCause.CRAMMING,
            DamageCause.ENTITY_SWEEP_ATTACK,
            DamageCause.FALL,
            DamageCause.FALLING_BLOCK,
            DamageCause.FLY_INTO_WALL,
            DamageCause.PROJECTILE,
            DamageCause.THORNS
    ),
    CHEMICAL(DamageCause.POISON, DamageCause.DRAGON_BREATH),
    COLD(DamageCause.valueOf(Name.FREEZE)),

    CONTACT(DamageCause.CONTACT),
    CRAMMING(DamageCause.CRAMMING),
    DRAGON_BREATH(DamageCause.DRAGON_BREATH),
    DROWNING(DamageCause.DROWNING, DamageCause.DRYOUT),
    ENTITY_ATTACK(
            DamageCause.ENTITY_ATTACK,
            DamageCause.ENTITY_SWEEP_ATTACK
    ),
    EXPLOSION(
            target -> getProtectionFactor(target.getEquipment(), ProtectionType.BLAST),
            DamageCause.BLOCK_EXPLOSION,
            DamageCause.ENTITY_EXPLOSION
    ),
    FALL(
            target -> getProtectionFactor(target.getEquipment(), ProtectionType.FALL),
            DamageCause.FALL
    ),
    FALLING_BLOCK(DamageCause.FALLING_BLOCK),
    FIRE(
            DamageType::getFireModifier,
            DamageType::getFireResModifier,
            DamageCause.FIRE,
            DamageCause.FIRE_TICK,
            DamageCause.HOT_FLOOR
    ),
    FLY_INTO_WALL(DamageCause.FLY_INTO_WALL),
    FREEZE(DamageCause.valueOf(Name.FREEZE)),
    LAVA(
            DamageType::getFireModifier,
            DamageType::getFireResModifier,
            DamageCause.LAVA,
            DamageCause.HOT_FLOOR
    ),
    LIGHTNING(DamageCause.LIGHTNING),
    MAGIC(DamageCause.MAGIC),
    POISON(DamageCause.POISON),
    PROJECTILE(
            target -> getProtectionFactor(target.getEquipment(), ProtectionType.PROJECTILE),
            DamageCause.PROJECTILE
    ),
    SONIC_BOOM(DamageCause.valueOf(Name.SONIC_BOOM)),
    STARVATION(DamageCause.STARVATION),
    SUFFOCATION(DamageCause.SUFFOCATION),
    THORNS(DamageCause.THORNS),
    VOID(DamageCause.VOID, DamageCause.SUICIDE),
    WITHER(DamageCause.WITHER),
    ;

    private static double getFireModifier(LivingEntity target) {
        return getProtectionFactor(target.getEquipment(), ProtectionType.FIRE);
    }

    private static double getFireResModifier(LivingEntity target) {
        PotionEffect effect = target.getPotionEffect(PotionEffectType.FIRE_RESISTANCE);
        if (effect != null) {
            return 1.0;
        }
        return 0.0;
    }

    protected final DamageCause[] bukkitCauses;
    protected final Function<LivingEntity, Double> armourModifier;
    protected final Function<LivingEntity, Double> anyModifier;

    DamageType(DamageCause... bukkitCauses) {
        this(val -> 0.0, bukkitCauses);
    }
    DamageType(Function<LivingEntity, Double> armourModifier, DamageCause... bukkitCauses) {
        this(armourModifier, val -> 0.0, bukkitCauses);
    }

    DamageType(Function<LivingEntity, Double> armourModifier, Function<LivingEntity, Double> anyModifier,  DamageCause... bukkitCauses) {
        this.bukkitCauses = bukkitCauses;
        this.armourModifier = armourModifier;
        this.anyModifier = anyModifier;
    }

    public boolean matches(DamageCause cause) {
        return Arrays.asList(bukkitCauses).contains(cause);
    }

    public boolean matches(EntityDamageEvent event) {
        DamageCause cause = event.getCause();
        if (matches(cause)) {
            return true;
        }

        if (this == MAGIC && event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageEvent = (EntityDamageByEntityEvent) event;

            Entity attacker = entityDamageEvent.getDamager();

            if (attacker instanceof Player) {
                Player attackingPlayer = (Player) attacker;
                if (SpellCaster.isRegistered(attackingPlayer)) {
                    SpellCaster attackingCaster = SpellCaster.getCaster(attackingPlayer);

                    return attackingCaster.isDealingSpellDamage();
                }
            }
        }

        return false;
    }

    public double getModifier(LivingEntity target) {
        @SuppressWarnings("ConstantConditions")
        double armourPoints = target.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        @SuppressWarnings("ConstantConditions")
        double toughness = target.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();

        PotionEffect effect = target.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        double anyModifier = effect == null ? 0 : effect.getAmplifier() * 0.2;
        anyModifier = WbsMath.clamp(0, 1, anyModifier + this.anyModifier.apply(target));
        
        double protectionFactor = getProtectionFactor(target.getEquipment(), ProtectionType.ENVIRONMENTAL);
        protectionFactor = WbsMath.clamp(0, 0.8, protectionFactor + armourModifier.apply(target));

        return calculateModifier(armourPoints, toughness, anyModifier, protectionFactor);
    }

    private static double calculateModifier(double points, double toughness, double anyModifier, double protectionFactor) {
        double damage = 1.0;
        double withArmorAndToughness = damage * (1 - Math.min(20, Math.max(points / 5, points - damage / (2 + toughness / 4))) / 25);
        double withResistance = withArmorAndToughness * (1 - anyModifier);
        double withEnchants = withResistance * (1 - protectionFactor);
        return withEnchants - damage;
    }

    /**
     * Get the protection factor for the given protection type based on armour as a fraction
     * @param inv The inventory to retrieve armour from
     * @param type The type of protection enchantment to use
     * @return The damage reduction as a fraction
     */
    private static double getProtectionFactor(EntityEquipment inv, ProtectionType type) {
        if (inv == null) {
            return 0;
        }

        ItemStack helm = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack legs = inv.getLeggings();
        ItemStack boot = inv.getBoots();

        double levels = (helm != null ? helm.getEnchantmentLevel(type.enchantment) : 0) +
                (chest != null ? chest.getEnchantmentLevel(type.enchantment) : 0) +
                (legs != null ? legs.getEnchantmentLevel(type.enchantment) : 0) +
                (boot != null ? boot.getEnchantmentLevel(type.enchantment) : 0);

        return Math.min(0.8, (levels * type.protectionFactor) / 100);
    }

    private enum ProtectionType {
        ENVIRONMENTAL(4, Enchantment.PROTECTION_ENVIRONMENTAL),
        BLAST(8, Enchantment.PROTECTION_EXPLOSIONS),
        FIRE(8, Enchantment.PROTECTION_FIRE),
        FALL(12, Enchantment.PROTECTION_FALL),
        PROJECTILE(8, Enchantment.PROTECTION_PROJECTILE),
        ;

        // (protectionFactor × level)% for each armor piece
        private final double protectionFactor;
        private final Enchantment enchantment;

        ProtectionType(double protectionFactor, Enchantment enchantment) {
            this.protectionFactor = protectionFactor;
            this.enchantment = enchantment;
        }
    }

    @SuppressWarnings("unused")
    public static class Name {
        public static final String ALL = "ALL";
        public static final String HEAT = "HEAT";
        public static final String ELECTRIC = "ELECTRIC";
        public static final String PHYSICAL = "PHYSICAL";
        public static final String CHEMICAL = "CHEMICAL";
        public static final String COLD = "COLD";
        public static final String CONTACT = "CONTACT";
        public static final String CRAMMING = "CRAMMING";
        public static final String DRAGON_BREATH = "DRAGON_BREATH";
        public static final String DROWNING = "DROWNING";
        public static final String ENTITY_ATTACK = "ENTITY_ATTACK";
        public static final String EXPLOSION = "EXPLOSION";
        public static final String FALL = "FALL";
        public static final String FALLING_BLOCK = "FALLING_BLOCK";
        public static final String FIRE = "FIRE";
        public static final String FLY_INTO_WALL = "FLY_INTO_WALL";
        public static final String FREEZE = "FREEZE";
        public static final String LAVA = "LAVA";
        public static final String LIGHTNING = "LIGHTNING";
        public static final String MAGIC = "MAGIC";
        public static final String POISON = "POISON";
        public static final String PROJECTILE = "PROJECTILE";
        public static final String SONIC_BOOM = "SONIC_BOOM";
        public static final String STARVATION = "STARVATION";
        public static final String SUFFOCATION = "SUFFOCATION";
        public static final String THORNS = "THORNS";
        public static final String VOID = "VOID";
        public static final String WITHER = "WITHER";
    }
}
