package wbs.magic.command;

import org.bukkit.command.PluginCommand;
import wbs.magic.WbsMagic;
import wbs.utils.util.commands.WbsCommand;
import wbs.utils.util.commands.WbsSubcommand;

public class MagicCommand extends WbsCommand {
    public MagicCommand(WbsMagic plugin, PluginCommand command) {
        super(plugin, command);

        String permission = command.getPermission();

        String adminPermission = permission + ".admin";
        addSubcommand(new WandSubcommand(plugin), adminPermission + ".wand");
        addSubcommand(new ListWandsSubcommand(plugin), adminPermission + ".listwands");
        addSubcommand(new ReloadSubcommand(plugin), adminPermission + ".reload");
        addSubcommand(new ErrorsSubcommand(plugin), adminPermission + ".reload");
        addSubcommand(new ManaSubcommand(plugin), adminPermission + ".mana");
        addSubcommand(new CastSubcommand(plugin), adminPermission + ".cast");

        addSubcommand(new InfoSubcommand(plugin), permission + ".info");
        addSubcommand(new FullInfoSubcommand(plugin), permission + ".info");
        addSubcommand(new GuideSubcommand(plugin), permission + ".guide");

        WbsSubcommand defaultCommand = new HelpSubcommand(plugin);
        addSubcommand(defaultCommand, permission + ".help");
    //    setDefaultCommand(defaultCommand);
    }
}
