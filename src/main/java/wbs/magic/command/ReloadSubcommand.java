package wbs.magic.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.magic.WbsMagic;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;

public class ReloadSubcommand extends WbsSubcommand {
    private final WbsMagic plugin;
    public ReloadSubcommand(WbsMagic plugin) {
        super(plugin, "reload");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        plugin.settings.reload();

        List<String> errors = plugin.settings.getErrors();
        if (errors.isEmpty()) {
            sendMessage("&aReload successful!", sender);
            return true;
        } else {
            sendMessage("&wThere were " + errors.size() + " config errors. Do &h/magic errors&w to see them.", sender);
        }

        return true;
    }
}
