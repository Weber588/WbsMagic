package wbs.magic.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.magic.WbsMagic;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;

public class ErrorsSubcommand extends WbsSubcommand {
    private WbsMagic plugin;
    public ErrorsSubcommand(WbsMagic plugin) {
        super(plugin, "errors");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> errors = plugin.settings.getErrors();

        if (errors.isEmpty()) {
            sendMessage("&aThere were no errors in the last reload.", sender);
            return true;
        }
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sendMessage("Usage: &h/magic errors [page]", sender);
                return true;
            }
        }
        page--;
        int ENTRIES_PER_PAGE = 5;
        int pages = errors.size() / ENTRIES_PER_PAGE;
        if (errors.size() % ENTRIES_PER_PAGE != 0) {
            pages++;
        }
        sendMessage("Displaying page " + (page+1) + "/" + pages + ":", sender);
        int index = 1;
        for (String error : errors) {
            if (index > page * ENTRIES_PER_PAGE && index <= (page + 1) * (ENTRIES_PER_PAGE)) {
                sendMessage("&6" + index + ") " + error, sender);
            }
            index++;
        }
        return true;
    }
}
