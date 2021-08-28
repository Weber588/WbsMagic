package wbs.magic.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.magic.WbsMagic;
import wbs.magic.spells.SpellInstance;
import wbs.magic.spellmanagement.RegisteredSpell;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.SpellManager;
import wbs.magic.SpellCaster;
import wbs.utils.util.commands.WbsSubcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CastSubcommand extends SpellSubcommand {
    public CastSubcommand(WbsMagic plugin) {
        super(plugin, "cast");
    }

    @Override
    protected void useSpell(SpellCaster caster, SpellInstance instance) {
        instance.cast(caster);
    }
}
