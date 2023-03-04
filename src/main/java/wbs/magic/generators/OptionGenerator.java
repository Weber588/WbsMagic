package wbs.magic.generators;

import wbs.magic.MagicSettings;
import wbs.magic.spellmanagement.SpellConfig;

public abstract class OptionGenerator {

    protected final MagicSettings settings = MagicSettings.getInstance();

    public OptionGenerator() {

    }

    public OptionGenerator(SpellConfig config, String directory) {

    }
}
