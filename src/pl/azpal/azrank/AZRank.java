package pl.azpal.azrank;
/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */

import pl.azpal.azrank.permissions.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class AZRank extends JavaPlugin{
	protected final YamlConfiguration database = new YamlConfiguration();
	protected Cfg cfg = new Cfg(this);
        
        //public static String permPlg;
        //public static byte permtype=0;
        public static AZPermissionsHandler permBridge=null;
        
	public static final Logger log = Logger.getLogger("Minecraft");
	public final File dir = new File("plugins/AZRank");
	public final File yml = new File(dir, "config.yml");
	public final File yamlDataBaseFile = new File(dir, "database.yml");
	public boolean cancelE;
	
	protected int taskID;
	private TimeRankChecker checker;
	private int checkDelay=10*20;
        
        public int tempranks=0;
	//private int checkInterval=10*20;
	private String INFO_NODE = "azrank.info";
	private String RESTORE_NODE = "azrank.restore";
	private String LIST_NODE = "azrank.list";
        private String ALL_NODE = "azrank.*";
        Metrics metrics;
    /**
     * @param args the command line arguments
     */
        @Override
	public void onEnable() {
               
		setupPermissions();
		if (cancelE == true) {
			return;
		}
		if (!yml.exists() || !yamlDataBaseFile.exists()) {
			firstRunSettings();
		}
		dLoad();
		
		checker = new TimeRankChecker(this);
		taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, checker, checkDelay, cfg.checkInterval);
		
		PluginDescriptionFile pdffile = this.getDescription();

		log.info("[AZRank] "+ pdffile.getFullName() + " is now enabled.");
                try {
                    metrics = new Metrics(this);
                    Metrics.Graph pp = metrics.createGraph("Permissions plugins");
                    pp.addPlotter(new Metrics.Plotter(permBridge.getName()){
                        public int getValue(){
                            return 1;
                        }
                    });
                    Metrics.Graph ranks = metrics.createGraph("Temporiary ranks count");
                    ranks.addPlotter(new Metrics.Plotter(){
                        public int getValue(){
                            return tempranks;
                        }
                    });
                    Metrics.Graph versions = metrics.createGraph("AZRank version");
                    versions.addPlotter(new Metrics.Plotter(getDescription().getVersion()){
                        public int getValue(){
                            return 1;
                        }
                    });
                    metrics.start();
                } catch (IOException e) {
                    // Failed to submit the stats :-(
                    e.printStackTrace();
                    log.severe("[AZRank] unable to start metrics!");
                }
	}
	
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(taskID);
		PluginDescriptionFile pdffile = this.getDescription();
		log.info("[AZRank] " + pdffile.getName() + " is now disabled.");
	}
	
        
	private void setupPermissions() {
            RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null)
            {
                permBridge = new AZVaultAdapter(this, permissionProvider.getProvider());
                log.info("[AZRank] Found Vault and " + permBridge.getName() + " and is good to go");
            } else{
                log.severe("[AZRank] Don't found Vault and/or Permission Plugin - Disabling!");
                this.setEnabled(false);
                cancelE = true;
                return;
            }
            
	}
	
	private void firstRunSettings() {
            try {
                    if (!dir.exists()) {
                            dir.mkdir();
                    }
                    if (!yml.exists()) {
                            cfg.defaultConfig();
                    }
                    if (!yamlDataBaseFile.exists()) {
                            FileWriter fstream = new FileWriter(yamlDataBaseFile);
                            BufferedWriter out = new BufferedWriter(fstream);
                            out.write("{}");
                            out.close();
                    }
            }
            catch (Exception e) {
                   log.severe("[AZRank] Failed to create config file!");
            }
	}
	
    @Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("azrank")) {
                    if(args.length == 1){
                        if(hasPerm(cs, INFO_NODE))
                            return infoCMD(cs,args[0]);
                        else {
                            sayNoPerm(cs);
                            return true;
                        }
                    } else if(args.length>1 && args.length<4) {
                        long czas = -1;
			if (args.length == 3) {
                            try {
                                czas = Util.parseDateDiff(args[2], true);
                            }
                            catch (Exception e) {
                                cs.sendMessage(ChatColor.RED + "[AZRank] Error - " + e.getMessage());
                            }
			}
                        
                        if(hasSetRank(cs, args[1])) {
                            SetRank(cs, args[0], args[1], czas);  
                        } else 
                            sayNoPerm(cs);
                        return true;

                    }//end dla argumentnów 2 lub 3
                    //jeżeli zla ilosc argumentow:
                    else {
                        sayBadArgs(cs);
                        return false;
                    }
                    
                    
			/* else if (args.length != 2) {
				return false;
			}*/
			
		}else if (cmd.getName().equalsIgnoreCase("azrankreload")) {
			if (args.length > 0) {
				return false;
			}
			PluginDescriptionFile pdffile = this.getDescription();
			if (cs instanceof Player) {
				Player player = (Player)cs;
				if (hasReload(player) || (cfg.allowOpsChanges && player.isOp())) {
					if(dLoad()) {  // jeżeli dobrze przeładowano
						cs.sendMessage(ChatColor.GREEN + "[AZRank] " + pdffile.getFullName() + " was succesfully reloaded");
                                                getServer().getScheduler().cancelTask(taskID);
                                                taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, checker, checkDelay, cfg.checkInterval);
						log.info("[AZRank] " + pdffile.getFullName() + " was succesfully reloaded");
					} else {  //jeżeli błąd podczas przeładowywania
						cs.sendMessage(ChatColor.GREEN + "[AZRank] " + pdffile.getFullName() + " - Error when reloading");
					}
				} else {
					cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "You do not have permission to do this!" );
				}
			} else {
				if(dLoad()) {  // jeżeli dobrze przeładowano
                                        getServer().getScheduler().cancelTask(taskID);
                                        taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, checker, checkDelay, cfg.checkInterval);
					log.info("[AZRank] " + pdffile.getFullName() + " was succesfully reloaded");
				} else {  //jeżeli błąd podczas przeładowywania
					cs.sendMessage(ChatColor.GREEN + "[AZRank] " + pdffile.getFullName() + " - Error when reloading");
				}
			}
			return true;
		
		} else if (cmd.getName().equalsIgnoreCase("restoregroup")) {
                    if(cs instanceof Player) {
                        if(!((Player)cs).hasPermission(RESTORE_NODE)) {
                            sayNoPerm(cs);
                            return true;
                        }
                    }
                    if(args.length!=1)
                        return false;
                    ConfigurationSection userSection= database.getConfigurationSection("users."+args[0]);
                    if(userSection==null) {
                        cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Error! User arent having temporiary group!" );
                        return true;
                    } else {
                        List<String> oldGroups = database.getStringList("users." + args[0] + ".oldRanks");
                        debugmsg("groups count: " + oldGroups.size());
                        if(oldGroups.size() > 0) {
                                String[] groups= new String[oldGroups.size()];
                                for(int i=0;i<oldGroups.size();i++) {
                                        groups[i] = oldGroups.get(i);
                                        debugmsg("Was in group: " + oldGroups.get(i) );
                                }
                                try{
                                        setGroups(args[0], groups);
                                } catch (Exception e) {
                                        log.severe("[AZRank][ERROR]" + e.getMessage());
                                }
                                log.info("[AZRank] unranked user " + args[0] + " to group(s): " + oldGroups);
                                cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.AQUA + "Unranked user " + args[0] + " to group(s): " + oldGroups);
                                database.set("users." + args[0], null);
                                save();
                        } else {
                                log.severe("[AZRank] Failed to unrank user " + args[0] + "! He haven't 'oldGroups'");
                                cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.AQUA + "Failed to unrank user " + args[0] + "! He haven't 'oldGroups'");
                                database.set("users." + args[0], null);
                                save();
                        }
                    }
                    
                    return true;
		} else if (cmd.getName().equalsIgnoreCase("azranks")) {
                    if(cs instanceof Player) {
                        if(! ((Player)cs).hasPermission(LIST_NODE) && !((Player)cs).hasPermission("azrank.*")) {
                            sayNoPerm(cs);
                            return true;
                        }
                        //wypisywanie jezlie gracz
                        if(args.length>0) {
                            try {
                                int page = Integer.parseInt(args[0]);
                                wypiszGraczy(cs,10,page);
                            } catch(NumberFormatException e) {
                                cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Error! Invalid page number!");
                                return false;
                            }
                        } else {
                            wypiszGraczy(cs,10,1);
                        }
                    } else {
                        //wypisywanie jeżeli konsola
                        ConfigurationSection usersSection = database.getConfigurationSection("users");
                        if(usersSection!=null){
                            Iterator<String> users = usersSection.getKeys(false).iterator();
                            cs.sendMessage("Temporiary ranks: <user>[<currentGroup>] to: <to>, next will by: <group>");
                         
                            for(int i=0;users.hasNext();i++){
                                String user = users.next();
                                String groups=permBridge.getPlayersGroupsAsString(user);
                                long to = database.getLong("users." + user + ".to");
                                SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                java.util.Date toDate = new java.util.Date(to);
                                List<String> oldGroups = database.getStringList("users." + user + ".oldRanks");
                                cs.sendMessage(user + groups + " to: " + dateformat.format(toDate) + ", next will by: " + oldGroups);
                                
                            }
                        } else {
                            cs.sendMessage("no users" );
                            
                        }
                        
                    }
                    return true;
		} else if (cmd.getName().equalsIgnoreCase("azranks")) {
                    cs.sendMessage("This command is not jet implemented!");
                }
		return false;
	}
	
	public void sayNoPerm(CommandSender cs){
		cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "You do not have permission to do this!" );
	}
	public void sayBadArgs(CommandSender cs){
		cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Bad amount of args!" );
	}
	public void sayBadArgs(CommandSender cs,int a){
		cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Bad amount of args! Expected "+ a );
	}
	
        
	public void SetRank(CommandSender cs, String Name, String Group, long time) {
            ConfigurationSection usersSection = database.getConfigurationSection("users."+Name);
            if(time <= 0L) {
                    if(usersSection!=null){
                        database.set("users." + Name, null);
                    }
            } else {
    		java.util.Date now = new java.util.Date();
                if(usersSection==null)
                    database.set("users." + Name + ".oldRanks", getGroups(Name));
    		database.set("users." + Name + ".from", now.getTime());
    		database.set("users." + Name + ".to", time );
    		save(cs);
			
            }
            if(setGroup(Name,Group)){
                String message = cfg.message;
                message = message.replace("+player", Name);
                message = message.replace("+group", Group);
                if(time>0) {
                    message = message.replace("+time", cfg.aWhile);
                } else {
                    message = message.replace("+time", cfg.ever);
                }
                message = message.replace("&", "�");
                if (cfg.broadcastRankChange) {
                    getServer().broadcastMessage(ChatColor.YELLOW + "[AZRank] " + ChatColor.BLUE + message);
                } else {
                    cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.BLUE + message);
                }
            } else {
                cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Error when setting group!");
            }
        }

	public void save() {
		try {
			database.save(yamlDataBaseFile);
			database.load(yamlDataBaseFile);
		} catch (IOException e) {
			log.info(ChatColor.RED + "[AZRank] I/O ERROR - unable to save database");
			e.printStackTrace();
		} catch (Exception e) {
			log.info(ChatColor.RED + "[AZRank] OTHER ERROR - unable to save database");
			e.printStackTrace();
		}
	}
	public void save(CommandSender cs) {
		try {
			database.save(yamlDataBaseFile);
			database.load(yamlDataBaseFile);
		} catch (IOException e) {
			log.info(ChatColor.RED + "[AZRank] I/O ERROR - unable to save database");
			cs.sendMessage(ChatColor.RED + "[AZRank] I/O ERROR - unable to save database");
			e.printStackTrace();
		} catch (Exception e) {
			log.info(ChatColor.RED + "[AZRank] OTHER ERROR - unable to save database");
			e.printStackTrace();
		}
	}
	      
        
	public boolean hasSetRank(CommandSender cs, String group) {
		try {
                    if(cs instanceof Player){
                        Player player = (Player)cs;
                        if (cfg.allowOpsChanges && player.isOp()) {
                            debugmsg("Asking for setRank perm for user: "+player.getName()+" - allowed - he is Op ");
                            return true;
                        } else if(player.hasPermission("azrank.setrank.*") || player.hasPermission("azrank.*")) {
                            debugmsg("Asking for -azrank.setrank." + group.toLowerCase() + "  perm for user: "+player.getName()+"  and he has permissions for all groups! - true");
                            return true;
                        } else {
                            String node = "azrank.setrank." + group.toLowerCase();
                            boolean czy = player.hasPermission(node);
                            debugmsg("Asking for setRank perm for specyfic group, user: "+player.getName()+" - "+ czy);
                            return czy;
                        }
                        
                    } else return true;		
		} catch(Exception e) {
			log.severe("[AZRank][ERROR]" + e.getMessage());
			return false;
		}	
	}

	public boolean hasReload(Player player) {
		try {
                    String node = "azrank.reload";
                    return player.hasPermission(node);

		} catch(Exception e) {
			log.severe("[AZRank][ERROR]" + e.getMessage());
			return false;
		}	
		
	}
	
	@Deprecated
	public boolean setGroups(String name, String[] oldGroups) {
		try {
                    return permBridge.setPlayersGroups(name, oldGroups);
		} catch(Exception e) {
                    log.severe("[AZRank][Exception]when setting group|" + e.getMessage());
                    e.printStackTrace();
                    return false;
		}
	}
	@Deprecated
	public boolean setGroup(String name, String group) {
            String[] groups ={group};
            return setGroups(name,groups);
	}
        
	@Deprecated
	public String[] getGroups(String name){
		try {
                    return permBridge.getPlayersGroups(name);
		} catch(Exception e) {
			log.severe("[AZRank][ERROR]" + e.getMessage());
			return null;
		}
	}
	
	public boolean dLoad() {
		try {
			cfg.checkConfig();
	        cfg.loadConfig();
			database.load(yamlDataBaseFile);
			return true;
		} catch (Exception e) {
			log.info(ChatColor.RED + "[AZRank][ERROR]" + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	public void debugmsg(String msg){
		if(cfg.logEverything)
			log.info(ChatColor.RED + "[AZRank][DEBUG]" + msg);
	}


    public void wypiszGraczy(CommandSender cs, int count, int page) {
        ConfigurationSection usersSection = database.getConfigurationSection("users");
        if(usersSection!=null){
            String[] users = usersSection.getKeys(false).toArray(new String[0]);
            int pages = users.length/count;
            if(pages<1 || users.length % count >0) pages++;
            cs.sendMessage(ChatColor.RED + "===Temporiary ranks: == PAGE: " + page +"/"+pages+"====");
            for(int i=0+10*(page-1);i<10*page && i<users.length;i++){
                cs.sendMessage("" + (i+1) + ". " + wypiszGracza(users[i]));
            }
        } else {
            cs.sendMessage(ChatColor.RED + "No users!");
        }
        
        
    }

    private String wypiszGracza(String user) {
        String groups=permBridge.getPlayersGroupsAsString(user);
        long to = database.getLong("users." + user + ".to");
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        java.util.Date toDate = new java.util.Date(to);
        List<String> oldGroups = database.getStringList("users." + user + ".oldRanks");
        return user + ChatColor.RED +groups + ChatColor.WHITE + " to: " + ChatColor.YELLOW + dateformat.format(toDate) + ChatColor.WHITE + ", next " + ChatColor.BLUE + oldGroups;
    }

    private boolean hasPerm(CommandSender cs, String node) {
        if(cs instanceof Player){
            Player player = (Player)cs;
            if(player.hasPermission(node) || player.hasPermission(ALL_NODE))
                return true;
            else return false;
        } else
            return true;
    }

    public boolean infoCMD(CommandSender cs, String playername) {
        ConfigurationSection userSection = database.getConfigurationSection("users."+playername);
        if(userSection==null) {
            String groups=permBridge.getPlayersGroupsAsString(playername);
            cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.AQUA + "User "+playername+ " is in " + groups + " forever");
        } else {
            long to = database.getLong("users." + playername + ".to");
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date toDate = new java.util.Date(to);
            List<String> oldGroups = database.getStringList("users." + playername + ".oldRanks");
            if(to>0){
                    String groups=permBridge.getPlayersGroupsAsString(playername);
                    cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.AQUA + "User "+playername+ " is '" + groups + "' to " + dateformat.format(toDate) + " and later will be in: "+oldGroups);
            } else{
                    cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Error! wrong data in database!" );
            }
        }
        return true;
    }

}


