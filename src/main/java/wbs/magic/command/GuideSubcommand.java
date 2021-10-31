package wbs.magic.command;

import org.bukkit.command.CommandSender;

import org.jetbrains.annotations.NotNull;

import wbs.magic.wand.SimpleWandControl;
import wbs.magic.spellmanagement.RegisteredSpell;
import wbs.magic.spellmanagement.SpellManager;

import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

public class GuideSubcommand extends WbsSubcommand {
    public GuideSubcommand(WbsPlugin plugin) {
        super(plugin, "guide");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        // Defaults:
        if (args.length == 1) {
            sendMessage("Welcome to WbsMagic!", sender);
            sendMessage("This plugin adds a configurable wands & spells, which enable survival players to cast a variety of spells for fighting, moving, or other useful functions.", sender);
            sendMessage("The wands only use simple and unique combinations of key strokes to avoid players needing to memorize complex spells that use only mouse clicks in certain patterns.", sender);
            sendMessage("Assuming you use default Minecraft controls, you will be using combinations of Left click, Right click, Shift, and Q to interact with wands.", sender);
            sendMessage("For a guide on how to cast spells, do &b/magic guide controls&e.", sender);
            sendMessage("To look up a specific spell, do &b/magic guide spell <SpellName>&e.", sender);
            sendMessage("To see what spells a wand can cast, do &b/magic info [wand name]&e.", sender);
        } else {
            switch (args[1].toUpperCase()) {
                case "SPELL":
                    if (args.length == 2) {
                        String spellString = String.join(", ", SpellManager.getSpellNames());

                        sendMessage("Usage: &b/magic guide spell <SpellName>", sender);
                        sendMessage("Please choose from the following list: &b" + spellString, sender);
                    } else {
                        String spellInput;
                        String[] spellStrings = new String[args.length - 2];
                        System.arraycopy(args, 2, spellStrings, 0, args.length - 2);
                        spellInput = String.join(" ", spellStrings);

                        RegisteredSpell registeredSpell = null;

                        try {
                            registeredSpell = SpellManager.getAliasedSpell(spellInput);
                        } catch (IllegalArgumentException e) {
                            for (RegisteredSpell spell : SpellManager.getSpells().values()) {
                                if (spell.getName().toUpperCase().contains(spellInput.toUpperCase())) {
                                    registeredSpell = spell;
                                    break;
                                }
                            }
                        }

                        if (registeredSpell == null) {
                            sendMessage("Spell not found: " + spellInput, sender);
                            return true;
                        }

                        spellGuide(registeredSpell, sender);
                    }
                    break;
                case "CONTROLS":
                    controlGuide(sender);
                    break;
            }
        }
        return true;
    }




    private void controlGuide(CommandSender sender) {
        final String lineBreak = "&8==========================";
        sendMessage(lineBreak, sender);
        for (SimpleWandControl control : SimpleWandControl.values()) {
            sendMessage("&h" + WbsStrings.capitalizeAll(control.name().replace('_', ' ')) + "&r:", sender);
            sendMessage(control.getDescription(), sender);
        }
        sendMessage(lineBreak, sender);
    }

    private void spellGuide(@NotNull RegisteredSpell spell, CommandSender sender) {
        final String lineBreak = "&8==========================";

        sendMessage(lineBreak, sender);
        sendMessage("&hSpell name: &r" + spell.getName(), sender);
        sendMessage("&bDescription: &e" + spell.getSpell().description(), sender);

        if (spell.getFailableSpell() != null) {
            sendMessage("&bCan fail? &eYes. " + spell.getFailableSpell().value(), sender);
        } else {
            sendMessage("&bCan fail? &eNo.", sender);
        }
        if (spell.getSettings() != null && spell.getSettings().canBeConcentration()) {
            sendMessage("&bConcentration spell? &eYes. This spell cannot be cast when another concentration" +
                    " spell is in use. If the spell combination requires the caster to crouch while casting, they must" +
                    " continue to crouch or the spell will end.", sender);
        } else {
            sendMessage("&bConcentration spell? &eNo. This spell may be cast at any time," +
                    " even if the caster is concentrating on another spell.", sender);
        }
        sendMessage(lineBreak, sender);
    }


    @Override
    public List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        switch (args.length) {
            case 2:
                choices.add("spell");
                choices.add("controls");
                break;
            case 3:
                switch (args[1].toLowerCase()) {
                    case "spell":
                        choices.addAll(SpellManager.getSpellNames());
                        break;
                }
                break;
        }

        return choices;
    }
}
