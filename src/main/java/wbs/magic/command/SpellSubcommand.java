package wbs.magic.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.magic.SpellCaster;
import wbs.magic.WbsMagic;
import wbs.magic.spellmanagement.RegisteredSpell;
import wbs.magic.spellmanagement.SpellConfig;
import wbs.magic.spellmanagement.SpellManager;
import wbs.magic.spellmanagement.configuration.options.OptionParameter;
import wbs.magic.spells.SpellInstance;
import wbs.utils.util.commands.WbsSubcommand;

import java.util.*;

public abstract class SpellSubcommand extends WbsSubcommand {
    protected final WbsMagic plugin;
    public SpellSubcommand(@NotNull WbsMagic plugin, @NotNull String label) {
        super(plugin, label);
        this.plugin = plugin;
    }

    protected final Set<String> ignoreOptions = new HashSet<>(
            Arrays.asList(
                    "alignment",
                    "consume",
                    "concentration",
                    "cooldown",
                    "custom-name",
                    "durability",
                    "cost",
                    "send-messages",
                    "send-errors"
            )
    );

    protected abstract void useSpell(SpellCaster caster, SpellInstance instance);

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sendMessage("Only players may use this command.", sender);
            return true;
        }

        SpellCaster caster = SpellCaster.getCaster((Player) sender);

        if (args.length == 1) {
            sendMessage("Usage: &b/mag " + getLabel() + " <spellType> [-option1 <value1> [-option2 <value2>...]]", sender);
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

        config.set("cost", 0, Integer.class);
        config.set("cooldown", 0d, Double.class);

        if (args.length > 2) {
            configure(config, args, null, "",2, new LinkedList<>());
        }

        List<String> errors = plugin.settings.getErrors();

        int existingErrorLength = errors.size();

        String directory = sender.getName() + ": " + spell.getName();
        SpellInstance spellInstance = config.buildSpell(directory);

        int postErrorLength = errors.size();

        if (postErrorLength - existingErrorLength == 1) {
            sendMessage("Error: &w"
                    + errors.get(existingErrorLength), sender);
        } else if (postErrorLength != existingErrorLength){
            sendMessage("Errors:", sender);
            for (int i = existingErrorLength; i < postErrorLength; i++) {
                // Don't use i, as the list changes length with each removal
                sendMessage("&w"
                        + errors.get(existingErrorLength), sender);
                errors.remove(existingErrorLength);
            }
        }

        if (spellInstance != null) {
            useSpell(caster, spellInstance);
        }

        return true;
    }

    private List<String> configure(SpellConfig config, String[] args, String optionName, String valueSoFar, int index, List<String> errors) {
        String arg = args[index];

        // Detect keys && don't accept 2 keys in a row - this also fixes negative number fields
        if (arg.startsWith("-") && !args[index - 1].startsWith("-")) {
            if (optionName != null) {
                String error = config.set(optionName, valueSoFar.trim());
                errors.add(error);
                valueSoFar = "";
            }

            optionName = arg.substring(1);
        } else {
            valueSoFar += arg + " ";
        }

        if (index < args.length - 1) {
            return configure(config, args, optionName, valueSoFar, index + 1, errors);
        } else {
            if (optionName != null) {
                String error = config.set(optionName, valueSoFar.trim());
                errors.add(error);
            }

            return errors;
        }
    }

    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {

        List<String> choices = new ArrayList<>();

        if (args.length == 2) {
            for (String spellName : SpellManager.getSpellNames()) {
                choices.add(spellName.replace(' ', '_')); // Gets undone in the getSpell call
            }
            return filterSpellTabs(choices, args);
        }

        String spellName = args[1];
        RegisteredSpell spell;
        try {
            spell = SpellManager.getSpell(spellName);
        } catch (IllegalArgumentException e) {
            return choices;
        }

        SpellConfig config = spell.getDefaultConfig();

        for (String key : config.getOptionKeys()) {
            // If previous arg is a key
            if (("-" + key).equalsIgnoreCase(args[args.length - 2])) {
                choices.clear(); // Remove previous as keys may have been added, but this is a value slot

                for (OptionParameter param : config.getOptions(key)) {
                    if (param.getOptionName().equalsIgnoreCase(key)) {
                        choices.addAll(param.getSuggestions());
                    }
                }

                break;
            } else if (("-" + key).startsWith(args[args.length - 1])) { // If this is a new key
                choices.add("-" + key);
                // Don't break; might be multiple matches
            }
        }

        return filterSpellTabs(choices, args);
    }

    private List<String> filterSpellTabs(List<String> choices, String[] args) {
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
                        boolean ignored = false;
                        for (String toIgnore : ignoreOptions) {
                            if (add.equalsIgnoreCase("-" + toIgnore)) {
                                ignored = true;
                                break;
                            }
                        }

                        if (!ignored) {
                            result.add(add);
                        }
                    }
                }
            }
        }

        return result;
    }
}
