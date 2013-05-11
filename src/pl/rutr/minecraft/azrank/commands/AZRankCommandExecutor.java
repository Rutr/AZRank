
package pl.rutr.minecraft.azrank.commands;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */

import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import pl.rutr.minecraft.azrank.AZRank;
import pl.rutr.minecraft.azrank.Util;
import pl.rutr.minecraft.azrank.permissions.AZRankPermissions;



public class AZRankCommandExecutor implements CommandExecutor {
    AZRank plugin;
    
    
    public AZRankCommandExecutor (AZRank plugin)
    {
        this.plugin = plugin;
    }
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args)
    {

        if (cmd.getName().equalsIgnoreCase("azplayer")) {
            if(args.length > 0){
                if(args.length > 1)
                    plugin.sayTooManyArgs(cs,1);
                if(plugin.hasPerm(cs, AZRankPermissions.INFO_NODE))
                    return plugin.infoCMD(cs,args[0]);
                else {
                    plugin.sayNoPerm(cs);
                    return true;
                }
            } else
                plugin.sayTooFewArgs(cs,1);


        }else if (cmd.getName().equalsIgnoreCase("azrankreload")) {
            if (args.length > 0) {
                    plugin.sayTooManyArgs(cs, 0);
            }
            PluginDescriptionFile pdffile = plugin.getDescription();
            if (cs instanceof Player) {
                    Player player = (Player)cs;
                    if (plugin.hasReload(player) || (plugin.cfg.allowOpsChanges && player.isOp())) {
                            if(plugin.dLoad()) {  // jeżeli dobrze przeładowano
                                    cs.sendMessage(ChatColor.GREEN + "[AZRank] " + pdffile.getFullName() + " was succesfully reloaded");

                                    AZRank.log.log(Level.INFO, "[AZRank] {0} was succesfully reloaded", pdffile.getFullName());
                            } else {  //jeżeli błąd podczas przeładowywania
                                    cs.sendMessage(ChatColor.GREEN + "[AZRank] " + pdffile.getFullName() + " - Error when reloading");
                            }
                    } else {
                            cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "You do not have permission to do this!" );
                    }
            } else {
                    if(plugin.dLoad()) {  // jeżeli dobrze przeładowano
                            AZRank.log.log(Level.INFO, "[AZRank] {0} was succesfully reloaded", pdffile.getFullName());
                    } else {  //jeżeli błąd podczas przeładowywania
                            cs.sendMessage(ChatColor.GREEN + "[AZRank] " + pdffile.getFullName() + " - Error when reloading");
                    }
            }
            return true;



        } else if (cmd.getName().equalsIgnoreCase("azsetgroup")) {
            if(args.length < 2) {
                plugin.sayTooFewArgs(cs, 2);
                return false;
            }
            long czas = -1;
            if (args.length > 2) {
                if(args.length > 4)
                    plugin.sayTooManyArgs(cs, 4);
                try {
                    //czas = Util.parseDateDiff(args[args.length-1], true);
                    czas = Util.parseTimeDiffInMillis(args[args.length-1]);
                    }
                catch (Exception e) {
                    cs.sendMessage(ChatColor.RED + "[AZRank] Error - " + e.getMessage());
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    AZRank.log.log(Level.SEVERE, "  [AZRank] Error - {0}", e.getMessage());
                    for(StackTraceElement element : stackTrace)
                    {
                        AZRank.log.log(Level.SEVERE, "  {0}", element.toString());
                    }
                    return false;
                }
            } 
            if(plugin.canSetRank(cs, args[0],args[1])) {
                String world;
                if(cs instanceof Player) {
                    world=((Player)cs).getLocation().getWorld().getName();
                } else {
                    world=plugin.getServer().getWorlds().get(0).getName();
                }

                if(!plugin.SetRank(cs, args[0], new String[]{ args[1]}, czas,args[args.length-2].equalsIgnoreCase("-s"),true, world))
                {
                    cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.RED +"An error occurred when tried to set group!");
                    plugin.debugmsg("Error when tried to set group "+args[0] + " to " + args[1] + " time: "+czas/1000+"seconds");
                }
                else
                    return true;
            } //else 
              //  sayNoPerm(cs);

        }else if (cmd.getName().equalsIgnoreCase("azaddgroup"))
        {
            cs.sendMessage(ChatColor.RED + "[AZRank] Dodawanie... ");
            if(args.length<2)
            {
                plugin.sayTooFewArgs(cs,2);
                return false;
            }
            long czas=-1;
            if(args.length>2)
            {
                if(args.length > 4)
                    plugin.sayTooManyArgs(cs, 4);
                try {
                    czas = Util.parseTimeDiffInMillis(args[args.length-1]);
                    }
                catch (Exception e) {
                    cs.sendMessage(ChatColor.RED + "[AZRank] Error - " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
            if(plugin.canSetRank(cs, args[0],args[1]))
            {
                String world;
                if(cs instanceof Player) {
                    world=((Player)cs).getLocation().getWorld().getName();
                } else {
                    world=plugin.getServer().getWorlds().get(0).getName();
                }

                if(!plugin.playerAddTmpGroup(cs, args[0], new String[]{ args[1]}, czas,args[args.length-2].equalsIgnoreCase("-s"), true, world))
                {
                    cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.RED +"An error occurred when tried to add group!");
                    plugin.debugmsg("Error when tried to add group "+args[0] + " to " + args[1] + " time: "+czas);
                }
                else
                    return true;
            }


        }
        else if (cmd.getName().equalsIgnoreCase("azremovegroup"))
        {
            if(args.length==2){
                if(plugin.canSetRank(cs, args[0], args[1]))
                {
                    String world;
                    if(cs instanceof Player) {
                        world=((Player)cs).getLocation().getWorld().getName();
                    } else {
                        world=plugin.getServer().getWorlds().get(0).getName();
                    }
                    if(plugin.permsSys.playerRemoveGroups(args[0],new String[] {args[1]} , world, true))
                    {
                        if(plugin.database.getConfigurationSection("users."+args[0]+"."+args[1])!=null)
                        {
                            plugin.database.set("users."+args[0]+"."+args[1],null);
                        }
                        cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.AQUA +" Successful removed "+args[0] + " from " + args[1] + "!");
                        plugin.debugmsg("successful removed group "+args[0] + " from " + args[1]);
                        return true;
                    } else
                    {
                        cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.RED +"An error occurred when tried to remove group!");
                        plugin.debugmsg("Error when tried to remove group "+args[0] + " from " + args[1]);
                        return false;
                    }

                }else
                {
                    plugin.sayNoPerm(cs);
                    return false;
                }
            }
            else if(args.length<2){
                plugin.sayTooFewArgs(cs,2);
                return false;
            }
            else if(args.length>2){
                plugin.sayTooManyArgs(cs,2);
                return false;
            }



        }
        else if (cmd.getName().equalsIgnoreCase("azranks"))
        {
            if(cs instanceof Player)
            {
                if(! plugin.hasPerm(cs, AZRankPermissions.LIST_NODE))
                {
                    plugin.sayNoPerm(cs);
                    return false;
                }
                //wypisywanie jezeli gracz 
                if(args.length>0)
                {
                    try {
                        int page = Integer.parseInt(args[0]);
                        if(page<1) throw new NumberFormatException("Number must be positive!");
                        plugin.wypiszGraczy(cs,10,page);
                        return true;
                    } catch(NumberFormatException e) {
                        cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Error! Invalid page number!" + e.getMessage());
                        return false;
                    }
                } else {
                    plugin.wypiszGraczy(cs,10,1);
                    return true;
                }
            } else
            { //wypisywanie jeżeli konsola
                plugin.wypiszGraczy(cs,0,0);
                return true;
            }

        }else if (cmd.getName().equalsIgnoreCase("azextend"))
        {
            //TODO: extension all temp groups time
            if(args.length != 3)
            {
                if(args.length < 3)
                    plugin.sayTooFewArgs(cs, 3);
                else
                    plugin.sayTooManyArgs(cs, 3);
                return false;
            }
            long interval;
            try {
                interval = Util.parseTimeDiffInMillis(args[args.length-1]);
                }
            catch (Exception e) {
                cs.sendMessage(ChatColor.RED + "[AZRank] Invalid time format - " + e.getMessage());
                e.printStackTrace();
                return false;
            }
            if(plugin.canSetRank(cs, args[0], args[1]))
            {
                ConfigurationSection groupSection = plugin.database.getConfigurationSection("users."+args[0]+"."+args[1]);
                if(groupSection==null)
                {
                    cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Player isnt in that group temporary");
                    return false;
                }
                long to=groupSection.getLong("to");
                if(to>0)
                {
                    groupSection.set("to",to + interval);
                    plugin.save(cs);
                    cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.AQUA + "Successful prolonged duration "+args[0]+ " in "+args[1]);
                    plugin.debugmsg("Successful prolonged duration "+args[0]+ " in "+args[1]);
                    return true;
                } else
                {
                    cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Invalid end time for "+args[0]+ " in "+args[1]+"! Deleting!");
                    plugin.database.set("users."+args[0]+"."+args[1], null);
                    plugin.save(cs);
                    plugin.debugmsg("Invalid end time for "+args[0]+ " in "+args[1]+"! Deleted!");
                    return false;
                }


            }
        }
        cs.sendMessage("Plugin Internal Error - please report on dev.bukkit.org/server-mods/azrank");
        return false;
    }
    
    
}
