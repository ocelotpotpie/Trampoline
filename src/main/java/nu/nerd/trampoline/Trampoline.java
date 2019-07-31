package nu.nerd.trampoline;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

// ----------------------------------------------------------------------------
/**
 * Main plugin class.
 */
public class Trampoline extends JavaPlugin implements Listener {
    /**
     * This plugin as a singleton.
     */
    public static Trampoline PLUGIN;

    /**
     * Configuration as singleton.
     */
    public static Configuration CONFIG = new Configuration();

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        PLUGIN = this;
        saveDefaultConfig();
        CONFIG.reload(false);

        Bukkit.getPluginManager().registerEvents(this, this);
        _movementTask.schedule();
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(getName())) {
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
                return false;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                CONFIG.reload(true);
                sender.sendMessage(ChatColor.DARK_AQUA + getName() + " configuration reloaded.");
                return true;
            }

            sender.sendMessage(ChatColor.RED + "Invalid arguments. Try /trampoline help.");
        }
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * Bukkit scheduler task for handling player movement.
     */
    protected MovementTask _movementTask = new MovementTask();

} // class Trampoline