package wbs.magic.annotations.generators;

import org.bukkit.entity.Entity;
import wbs.magic.EntityGenerator;

@GeneratorType(EntityGenerator.class)
public @interface EntitySpell {

    Class<? extends Entity> entityClass() default Entity.class;
    String entityAlias() default "entity";

}
