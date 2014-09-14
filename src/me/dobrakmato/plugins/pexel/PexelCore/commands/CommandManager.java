package me.dobrakmato.plugins.pexel.PexelCore.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import me.dobrakmato.plugins.pexel.PexelCore.chat.ChatManager;
import me.dobrakmato.plugins.pexel.PexelCore.core.Log;
import me.dobrakmato.util.AnnotationNotPresentException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Class that is used for dynamic command registration.
 */
public class CommandManager {
    /**
     * Map of subcommands.
     */
    private final Map<String, Map<String, Method>> subcommands = new HashMap<String, Map<String, Method>>();
    /**
     * Map of commands.
     */
    private final Map<String, Object>              commands    = new HashMap<String, Object>();
    /**
     * Map of command aliases.
     */
    private final Map<String, String>              aliases     = new HashMap<String, String>();
    
    public CommandManager() {
        
    }
    
    /**
     * Tries to register specified object as command handler.
     * 
     * @param command
     *            command handler
     */
    public void registerCommands(final Object command) {
        Log.info("Register command on object: " + command.getClass().getSimpleName()
                + "#" + command.hashCode());
        Class<?> clazz = command.getClass();
        if (clazz.isAnnotationPresent(CommandHandler.class)) {
            this.registerCommand(command);
            this.registerAliases(clazz);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SubCommand.class)) {
                    this.registerSubcommand(command, method);
                }
            }
        }
        else {
            throw new AnnotationNotPresentException("Annotation: Command; Class: clazz");
        }
    }
    
    /**
     * Parses command from string and tries to execute it as specified player.
     * 
     * @param sender
     *            executor
     * @param command
     *            command
     */
    public void parseCommand(final Player sender, final String command) {
        String parts[] = command.trim().split("\\s+");
        String baseCommand = parts[0];
        
        if (this.commands.containsKey(baseCommand.toLowerCase())) {
            if (this.hasPermission(sender, baseCommand)) {
                //If no subcommand
                if (parts.length == 1) {
                    this.invoke(this.commands.get(baseCommand),
                            this.subcommands.get(baseCommand).get("main"), sender);
                }
                else {
                    String subCommand = parts[1];
                    if (this.subcommands.get(baseCommand).containsKey(subCommand)) {
                        if (this.hasPermission(sender, baseCommand + "." + subCommand)) {
                            //Executing subcommand
                            if (parts.length == 2) {
                                this.invoke(
                                        this.commands.get(baseCommand),
                                        this.subcommands.get(baseCommand).get(subCommand),
                                        sender);
                            }
                            else {
                                Object[] args = new String[parts.length - 2];
                                System.arraycopy(parts, 2, args, 0, parts.length - 2);
                                this.invoke(
                                        this.commands.get(baseCommand),
                                        this.subcommands.get(baseCommand).get(subCommand),
                                        sender, args);
                            }
                        }
                        else {
                            sender.sendMessage(ChatManager.error("You don't have permission!"));
                        }
                    }
                    else {
                        if (subCommand.equalsIgnoreCase("help")) {
                            sender.sendMessage(ChatColor.YELLOW + "Command help: "
                                    + baseCommand);
                            for (Method m : this.subcommands.get(baseCommand).values()) {
                                SubCommand annotation = m.getAnnotation(SubCommand.class);
                                
                                String scArgs = "";
                                for (Class<?> param : m.getParameterTypes())
                                    scArgs += "<[" + param.getSimpleName() + "]> ";
                                
                                String scName = m.getName();
                                if (annotation.name() != "")
                                    scName = annotation.name();
                                
                                sender.sendMessage(ChatColor.BLUE + "/" + baseCommand
                                        + ChatColor.RED + " " + scName + ChatColor.GOLD
                                        + scArgs + ChatColor.GREEN + "- "
                                        + annotation.description());
                            }
                        }
                        else {
                            if (this.hasPermission(sender, baseCommand + "."
                                    + subCommand)) {
                                Object[] args = new String[parts.length - 1];
                                System.arraycopy(parts, 1, args, 0, parts.length - 1);
                                this.invoke(this.commands.get(baseCommand),
                                        this.subcommands.get(baseCommand).get("main"),
                                        sender, args);
                            }
                            else {
                                sender.sendMessage(ChatManager.error("You don't have permission!"));
                            }
                        }
                    }
                }
            }
            else {
                sender.sendMessage(ChatManager.error("You don't have permission!"));
            }
        }
        else {
            sender.sendMessage(ChatManager.error("Unknown command!"));
        }
    }
    
    /**
     * Returns whether specified player has permission to specififed command.
     * 
     * @param sender
     *            player
     * @param baseCommand
     *            command
     * @return true or false
     */
    private boolean hasPermission(final Player sender, final String node) {
        return true;
    }
    
    /**
     * Invokes specified subcommand of command on specififed player weith specified arguments.
     * 
     * @param command
     *            command
     * @param subcommand
     *            sub command
     * @param invoker
     *            player
     * @param args
     *            args
     */
    private void invoke(final Object command, final Method subcommand,
            final Player invoker, final Object... args) {
        try {
            String argsString = "[";
            if (args != null)
                for (Object o : args)
                    argsString += o.toString() + ",";
            argsString = argsString.substring(0, argsString.length() - 1);
            
            String name = command.getClass().getAnnotation(CommandHandler.class).name();
            
            Log.info("Invoking command '" + name + "("
                    + command.getClass().getSimpleName() + ") -> "
                    + subcommand.getAnnotation(SubCommand.class).name() + " ("
                    + subcommand.getName() + ")' on player '" + invoker.getName()
                    + "' with args: " + argsString + "]");
            
            if (!Arrays.asList(command.getClass().getDeclaredMethods()).contains(
                    subcommand))
                Log.warn("Subcommand is not method of command class.");
            
            if (args.length == 0)
                subcommand.invoke(command, invoker);
            else {
                if (this.validParams(subcommand, args))
                    subcommand.invoke(command, invoker, args);
                else
                    invoker.sendMessage(ChatManager.error("Unknown command: invalid params"));
            }
            
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            invoker.sendMessage(ChatManager.error("Unknown command: " + e.getMessage()));
            if (invoker.isOp())
                invoker.sendMessage(ChatManager.error(e.toString()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            invoker.sendMessage(ChatManager.error("Internal server error occured while attempting to execute this command!"));
            throw new RuntimeException(e);
        }
    }
    
    private boolean validParams(final Method subcommand, final Object[] args) {
        Class<?>[] parameterTypes = subcommand.getParameterTypes();
        if (!parameterTypes[0].getClass().equals(Player.class))
            return false;
        for (int i = 1; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (!args[i - 1].getClass().equals(parameterType))
                return false;
        }
        return true;
    }
    
    private void registerSubcommand(final Object command, final Method method) {
        String baseCommand = command.getClass().getAnnotation(CommandHandler.class).name().toLowerCase();
        String subCommand = method.getName().toLowerCase();
        
        if (!method.getAnnotation(SubCommand.class).name().equalsIgnoreCase(""))
            subCommand = method.getAnnotation(SubCommand.class).name().toLowerCase();
        
        Log.info("  Register subcommand: " + baseCommand + " -> " + subCommand + "#"
                + method.hashCode());
        
        if (!method.isAccessible())
            method.setAccessible(true);
        
        this.subcommands.get(baseCommand).put(subCommand, method);
    }
    
    private void registerCommand(final Object object) {
        String baseCommand = object.getClass().getAnnotation(CommandHandler.class).name().toLowerCase();
        
        Log.info(" Register command: " + baseCommand);
        this.commands.put(baseCommand, object);
        this.subcommands.put(baseCommand, new HashMap<String, Method>());
    }
    
    private void registerAliases(final Class<?> clazz) {
        String baseName = clazz.getAnnotation(CommandHandler.class).name();
        for (String alias : clazz.getAnnotation(CommandHandler.class).aliases()) {
            this.aliases.put(baseName, alias);
        }
    }
}
