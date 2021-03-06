/*
 * This file is part of AmpLib.
 *
 * Copyright (c) 2014-2017 <http://github.com/ampayne2/>
 *
 * AmpLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AmpLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with AmpLib.  If not, see <http://www.gnu.org/licenses/>.
 */
package ninja.amp.amplib.command;

import ninja.amp.amplib.AmpJavaPlugin;
import ninja.amp.amplib.messenger.DefaultMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A command that only contains child commands.
 */
public class CommandGroup {

    private final AmpJavaPlugin plugin;
    private final String name;
    private Permission permission;
    private int minArgsLength = 0;
    private int maxArgsLength = -1;
    private boolean playerOnly = true;
    private final Map<String, CommandGroup> children = new LinkedHashMap<>();

    /**
     * Creates a new CommandGroup.
     *
     * @param plugin The {@link ninja.amp.amplib.AmpJavaPlugin} instance.
     * @param name   The name of the command.
     */
    public CommandGroup(AmpJavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name.toLowerCase();
    }

    /**
     * Gets the command's name.
     *
     * @return The command's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the command's permission.
     *
     * @return The command's permission.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Sets the command's permission.
     *
     * @param permission The CommandGroup's permission.
     */
    public void setPermission(Permission permission) {
        this.permission = permission;
        if (plugin.getServer().getPluginManager().getPermission(permission.getName()) == null) {
            plugin.getServer().getPluginManager().addPermission(permission);
        }
    }

    /**
     * Gets the minimum required args length of the command.
     *
     * @return The minimum required args length.
     */
    public int getMinArgsLength() {
        return minArgsLength;
    }

    /**
     * Gets the maximum required args length of the command.
     *
     * @return The maximum required args length.
     */
    public int getMaxArgsLength() {
        return maxArgsLength;
    }

    /**
     * Sets the argument range of the command.
     *
     * @param minArgsLength The minimum required args length.
     * @param maxArgsLength The maximum required args length. -1 for no max.
     */
    public void setArgumentRange(int minArgsLength, int maxArgsLength) {
        this.minArgsLength = minArgsLength;
        this.maxArgsLength = maxArgsLength;
    }

    /**
     * Checks to see if the command can only be run by a player.
     *
     * @return True if the command is player only, else false.
     */
    public boolean isPlayerOnly() {
        return playerOnly;
    }

    /**
     * Sets if the command can only be run by a player.
     *
     * @param playerOnly If the command can only be run by a player.
     */
    public void setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    /**
     * Checks to see if the command has the child command.
     *
     * @param name The name of the child command.
     * @return True if the command has the child command, else false.
     */
    public boolean hasChildCommand(String name) {
        return children.containsKey(name.toLowerCase());
    }

    /**
     * Gets a child command of the command.
     *
     * @param name The name of the child command.
     * @return The child command.
     */
    public CommandGroup getChildCommand(String name) {
        return children.get(name.toLowerCase());
    }

    /**
     * Adds a child command to the command.
     *
     * @param command The child command.
     * @return The command the child command was added to.
     */
    public CommandGroup addChildCommand(CommandGroup command) {
        children.put(command.getName().toLowerCase(), command);
        if (permission != null && command.getPermission() != null) {
            command.getPermission().addParent(permission, true);
        }
        return this;
    }

    /**
     * Gets the command's children.
     *
     * @param deep If the method should return all children, or only the command's immediate children.
     * @return The command's children.
     */
    public List<CommandGroup> getChildren(boolean deep) {
        if (deep) {
            List<CommandGroup> deepChildren = new ArrayList<>();
            for (CommandGroup child : children.values()) {
                if (child instanceof Command) {
                    deepChildren.add(child);
                }
                deepChildren.addAll(child.getChildren(true));
            }
            return deepChildren;
        } else {
            return new ArrayList<>(children.values());
        }
    }

    /**
     * Gets the tab completion list of the command.
     *
     * @param args The args already entered.
     * @return The tab completion list of the command.
     */
    public List<String> getTabCompleteList(String[] args) {
        return new ArrayList<>(children.keySet());
    }

    /**
     * The command executor
     *
     * @param command The command label.
     * @param sender  The sender of the command.
     * @param args    The arguments sent with the command.
     */
    public void execute(String command, CommandSender sender, String[] args) {
        CommandGroup entry = children.get(command.toLowerCase());
        if (entry instanceof Command) {
            if ((entry.getMinArgsLength() <= args.length || entry.getMinArgsLength() == -1) && (entry.getMaxArgsLength() >= args.length || entry.getMaxArgsLength() == -1)) {
                if (entry.getPermission() == null || sender.hasPermission(entry.getPermission())) {
                    if (sender instanceof Player || !entry.isPlayerOnly()) {
                        entry.execute(command, sender, args);
                    } else {
                        plugin.getMessenger().sendMessage(sender, DefaultMessage.COMMAND_NOTAPLAYER);
                    }
                } else {
                    plugin.getMessenger().sendMessage(sender, DefaultMessage.COMMAND_NOPERMISSION, command);
                }
            } else {
                plugin.getMessenger().sendMessage(sender, DefaultMessage.COMMAND_USAGE, ((Command) entry).getCommandUsage());
            }
        } else {
            String subCommand = args.length == 0 ? "" : args[0];
            if (entry.hasChildCommand(subCommand)) {
                String[] newArgs;
                if (args.length == 0) {
                    newArgs = args;
                } else {
                    newArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                }
                entry.execute(subCommand, sender, newArgs);
            } else {
                plugin.getMessenger().sendMessage(sender, DefaultMessage.COMMAND_INVALID, "\"" + subCommand + "\"", "\"" + command + "\"");
            }
        }
    }

}
