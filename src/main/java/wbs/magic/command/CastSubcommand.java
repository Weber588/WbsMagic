package wbs.magic.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.magic.spellinstances.SpellInstance;
import wbs.magic.spells.RegisteredSpell;
import wbs.magic.spells.SpellConfig;
import wbs.magic.spells.SpellManager;
import wbs.magic.wrappers.SpellCaster;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CastSubcommand extends WbsSubcommand {
    public CastSubcommand(WbsPlugin plugin) {
        super(plugin, "cast");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("Only players may use this command.", sender);
            return true;
        }

        SpellCaster caster = SpellCaster.getCaster((Player) sender);

        if (args.length == 1) {
            sendMessage("Usage: &b/mag cast <spellType> [-option1 <value1> [-option2 <value2>...]]", sender);
            return true;
        }

        String spellName = args[1];
        RegisteredSpell spell;
        try {
            spell = SpellManager.getSpell(spellName);
        } catch (IllegalArgumentException e) {
            sendMessage("Invalid spell: " + spellName, sender);
            return true;
        }

        SpellConfig config = new SpellConfig(spell);

        // Stop 1 before the end, as that should be a value, not a key.
        for (int i = 1; i < args.length - 1; i++) {
            if (args[i].startsWith("-")) {
                boolean valueFound = false;
                for (String key : config.getBoolKeys()) {
                    if (args[i].equalsIgnoreCase("-" + key)) {
                        config.set(key, Boolean.parseBoolean(args[i + 1]));
                        valueFound = true;
                        break;
                    }
                }
                if (valueFound) continue;

                for (String key : config.getIntKeys()) {
                    if (args[i].equalsIgnoreCase("-" + key)) {
                        int foundValue;
                        try {
                            foundValue = Integer.parseInt(args[i + 1]);
                        } catch (NumberFormatException e) {
                            caster.sendMessage("Invalid int " + args[i + 1] + " for key " + args[i]);
                            return true;
                        }
                        config.set(key, foundValue);
                        valueFound = true;
                        break;
                    }
                }
                if (valueFound) continue;

                for (String key : config.getDoubleKeys()) {
                    if (args[i].equalsIgnoreCase("-" + key)) {
                        double foundValue;
                        try {
                            foundValue = Double.parseDouble(args[i + 1]);
                        } catch (NumberFormatException e) {
                            caster.sendMessage("Invalid double " + args[i + 1] + " for key " + args[i]);
                            return true;
                        }
                        config.set(key, foundValue);
                        valueFound = true;
                        break;
                    }
                }
                if (valueFound) continue;

                for (String key : config.getStringKeys()) {
                    if (args[i].equalsIgnoreCase("-" + key)) {
                        config.set(key, args[i + 1]);
                        valueFound = true;
                        break;
                    }
                }

                if (valueFound) {
                    i++; // Skip next section; already checked
                }
            }
        }

        SpellInstance spellInstance = config.buildSpell("Custom");

        spellInstance.cast(caster);

        return true;
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        List<String> choices = new ArrayList<>();

        if (args.length == 2) {
            for (String spellName : SpellManager.getSpellNames()) {
                choices.add(spellName.replace(' ', '_')); // Gets undone in the getSpell call
            }
            return filterCastTabs(choices, args);
        }

        String spellName = args[1];
        RegisteredSpell spell;
        try {
            spell = SpellManager.getSpell(spellName);
        } catch (IllegalArgumentException e) {
            return choices;
        }

        SpellConfig config = spell.getDefaultConfig();

        boolean valueSuggested = false;
        for (String key : config.getBoolKeys()) {
            // If previous arg is a key
            if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
                choices.clear(); // Remove previous as keys may have been added, but this is a value slot
                choices.add("true");
                choices.add("false");
                valueSuggested = true;
                break;
            } else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
                choices.add("-" + key);
                // Don't break; might be multiple matches
            }
        }

        if (!valueSuggested) {
            for (String key : config.getDoubleKeys()) {
                // If previous arg is a key
                if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
                    choices.clear(); // Remove previous as keys may have been added, but this is a value slot
                    choices.add("0.5");
                    choices.add("1.0");
                    choices.add("5.0");
                    choices.add("10.0");
                    valueSuggested = true;
                    break;
                } else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
                    choices.add("-" + key);
                    // Don't break; might be multiple matches
                }
            }
        }

        if (!valueSuggested) {
            for (String key : config.getIntKeys()) {
                // If previous arg is a key
                if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
                    choices.clear(); // Remove previous as keys may have been added, but this is a value slot
                    choices.add("1");
                    choices.add("5");
                    choices.add("10");
                    choices.add("25");
                    valueSuggested = true;
                    break;
                } else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
                    choices.add("-" + key);
                    // Don't break; might be multiple matches
                }
            }
        }
        if (!valueSuggested) {
            for (String key : config.getStringKeys()) {
                // If previous arg is a key
                if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
                    choices.clear(); // Remove previous as keys may have been added, but this is a value slot

                    if (key.equalsIgnoreCase("targetter") ||
                            key.equalsIgnoreCase("targeter") ||
                            key.equalsIgnoreCase("target"))
                    {
                        choices.add("radius");
                        choices.add("line_of_sight");
                        choices.add("random");
                        choices.add("self");
                    } else if (config.getEnumType(key) != null) {
                        choices.addAll(
                                Arrays.stream(config.getEnumType(key).getEnumConstants())
                                        .map(Enum::toString)
                                        .map(String::toLowerCase)
                                        .collect(Collectors.toList())
                        );
                    }
                    break;
                } else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
                    choices.add("-" + key);
                    // Don't break; might be multiple matches
                }
            }
        }

        return filterCastTabs(choices, args);
    }

    private List<String> filterCastTabs(List<String> choices, String[] args) {
        List<String> result = new ArrayList<>();
        String currentArg = args[args.length - 1];
        for (String add : choices) {
            if (add.toLowerCase().startsWith(currentArg.toLowerCase())) {
                if (!add.startsWith("-")) { // If it's not a key, add it
                    result.add(add);
                } else { // If it is a key, check for duplicates & wand-related keys
                    boolean foundMatch = false;
                    for (String arg : args) {
                        if (add.equalsIgnoreCase(arg)) {
                            foundMatch = true;
                            break;
                        }
                    }
                    if (!foundMatch) {
                        // Don't include
                        if (!(add.equalsIgnoreCase("-consume") ||
                                add.equalsIgnoreCase("-concentration") ||
                                add.equalsIgnoreCase("-cooldown") ||
                                add.equalsIgnoreCase("-custom-name") ||
                                add.equalsIgnoreCase("-cost")))
                        {
                            result.add(add);
                        }
                    }
                }
            }
        }

        return result;
    }
}
