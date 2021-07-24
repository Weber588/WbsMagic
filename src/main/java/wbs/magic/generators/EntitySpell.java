package wbs.magic.generators;

import org.bukkit.entity.Entity;

@GeneratorType(EntityGenerator.class)
public @interface EntitySpell {

    Class<? extends Entity> entityClass() default Entity.class;
    String entityAlias() default "entity";

}
