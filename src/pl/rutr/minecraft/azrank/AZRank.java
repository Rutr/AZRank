
package pl.rutr.minecraft.azrank;

/** AZRank
 *
 * @author Rutr <artuczapl at gmail.com>
 */

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.milkbowl.vault.permission.Permission;
import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.scheduler.BukkitTask;
import pl.azpal.azrank.manager.*;
import pl.rutr.minecraft.azrank.commands.*;
import pl.rutr.minecraft.azrank.permissions.*;



public class AZRank extends JavaPlugin {
	public final YamlConfiguration database = new YamlConfiguration();
	public final Cfg cfg = new Cfg(this);
        
        //public static String permPlg;
        //public static byte permtype=0;
        public /*static*/ PermissionsSysHandler permsSys;
        private AZRankCommandExecutor commandExecutor = new AZRankCommandExecutor(this);
        
	public final static  Logger log = Logger.getLogger("Minecraft");
	public final File dir = new File("plugins/AZRank");//location of files of plugin
	public final File cfgFile = new File(dir, "config.yml"); //config file
	public final File yamlDataBaseFile = new File(dir, "database.yml"); //database file
        public final File updateFile = new File(dir, "update.jar");
        
	public boolean cancelE;
	
       
	//public int checkerTaskID=0;
        private BukkitTask checkerTask;
	public TimeRankChecker checker=new TimeRankChecker(this);
        private BukkitTask updaterTask;
        UpdateChecker updater;
	//public int checkDelay=10*20;
       // protected Thread checkerThread = new Thread(checker);
        
        //protected CheckerCaller checkerCaller = new CheckerCaller(this);
        
        public int tempranks=0;
	//private int checkInterval=10*20;
        

        public Metrics metrics;

        public List<String> dependies = new ArrayList<String>();
        public boolean dloaded = false;
        
        
        @Override
	public void onEnable() {
            if(updater==null)
            {
                updater=new UpdateChecker(this);
            }
            try {
                getCommand("azrank").setExecutor(commandExecutor);
                getCommand("azplayer").setExecutor(commandExecutor);
                getCommand("azsetgroup").setExecutor(commandExecutor);
                getCommand("azaddgroup").setExecutor(commandExecutor);
                getCommand("azremovegroup").setExecutor(commandExecutor);
                getCommand("azrankreload").setExecutor(commandExecutor);
                getCommand("azranks").setExecutor(commandExecutor);
                getCommand("azextend").setExecutor(commandExecutor);
                
            } catch(NullPointerException e)
            {
                log.log(Level.SEVERE, "[AZRank] Error when enabling! Some commands may not work.");
            }
            

            setupPermissions();
            if (cancelE == true) {
                    return;
            }
            if (!cfgFile.exists() || !yamlDataBaseFile.exists()) {
                    firstRunSettings();
            }
            dLoad(null);

            startCheckerTask(false);
            startUpdaterTask(false);

            PluginDescriptionFile pdffile = this.getDescription();

            log.log(Level.INFO, "[AZRank] {0} is now enabled.", pdffile.getFullName());
            try {
                metrics = new Metrics(this);
                Metrics.Graph pp = metrics.createGraph("Permissions plugins");
                pp.addPlotter( new Metrics.Plotter(permsSys.getName()) {
                    @Override
                    public int getValue(){
                        return 1;
                    }
                });
                Metrics.Graph ranks = metrics.createGraph("Temporiary ranks count");
                ranks.addPlotter(new Metrics.Plotter() {
                    @Override
                    public int getValue(){
                        return tempranks;
                    }
                });
                Metrics.Graph versions = metrics.createGraph("AZRank version");
                versions.addPlotter(new Metrics.Plotter(getDescription().getVersion()) {
                    @Override
                    public int getValue(){
                        return 1;
                    }
                });
                metrics.start();
            } catch (IOException e) {
                log.log(Level.WARNING, "[AZRank] unable to start metrics!");
                StackTraceElement[] stackTrace = e.getStackTrace();
                for(StackTraceElement element : stackTrace)
                {
                    log.log(Level.SEVERE, "  {0}", element.toString());
                }
            }
	}
	
	@Override
	public void onDisable() {
		stopCheckerTask();
		PluginDescriptionFile pdffile = this.getDescription();
		log.log(Level.INFO, "[AZRank] {0} is now disabled.", pdffile.getName());
	}
	
        
	private void setupPermissions() {
            RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            
            final PluginManager pluginManager = getServer().getPluginManager();
            final Plugin GMplugin = pluginManager.getPlugin("GroupManager");
            
            if(GMplugin != null && GMplugin.isEnabled()) {
                permsSys = new AZGroupManagerAdapter(this, ((GroupManager)GMplugin).getWorldsHolder());
                log.log(Level.INFO, "[AZRank] Found {0} (directly) and is good to go", ((GroupManager)GMplugin).getDescription().getFullName());
            }
            else if (permissionProvider != null)
            {
                permsSys = new AZVaultAdapter(this, permissionProvider.getProvider());
                log.log(Level.INFO, "[AZRank] Found Vault and {0} and is good to go", permsSys.getName());
                
            }
            else
            {
                log.log(Level.INFO,"[AZRank] Don't found Vault and/or Permission Plugin - Disabling!");
                this.setEnabled(false);
                cancelE = true;
                return;
            }
            dloaded=true;
            
	}
	
	private void firstRunSettings() {
            try {
                    if (!dir.exists()) {
                            dir.mkdir();
                    }
                    if (!cfgFile.exists()) {
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
                   log.log(Level.SEVERE, "[AZRank] Failed to create config file!");
            }
	}
	
    
    /**
     * Say "You do not have permission to do this!"
     * @param cs CommandSender to which is send message
     */
    public void sayNoPerm(CommandSender cs){
            cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "You do not have permission to do this!" );
    }
    @Deprecated
    public void sayBadArgs(CommandSender cs){
            cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Bad amount of args!" );
    }
    @Deprecated
    public void sayBadArgs(CommandSender cs,int a){
            cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Bad amount of args! Expected "+ a );
    }
    /**
     * Say "Too many arguments!"
     * @param cs CommandSender to which is send message
     */
    public void sayTooManyArgs(CommandSender cs){
        cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Too many arguments!");
    }
    /**
     * Say "Too many arguments! Expected maximum <tt>a</tt>"
     * @param cs CommandSender to which is send message
     * @param a maximum amount of parameters that expected
     */
    public void sayTooManyArgs(CommandSender cs,int a){
        cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Too many arguments! Expected maximum " + a );
    }
     /**
     * Say "Too few arguments!"
     * @param cs CommandSender to which is send message
     */
    public void sayTooFewArgs(CommandSender cs){
        cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Too few arguments!");
    }
    /**
     * Say "Too few arguments! Expected minimum <tt>a</tt>"
     * @param cs CommandSender to which is send message
     * @param a minimum amount of parameters that expected
     */
    public void sayTooFewArgs(CommandSender cs,int a){
        cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.RED + "Too few arguments! Expected minimum " + a );
        
    }
        
        //TODO: przenieść do API / move to API
    /**
     * Set rank. If player already is in only given groups and set is false, time will be prolonged.
     * @param cs Messages output
     * @param Player player to change his groups
     * @param groups new groups to set to the player
     * @param timeDiff the duration of give groups in milliseconds
     * @param set if true, time will be set, else if already player is in only give groups, time will be prolonged
     * @param globaly if true, plugin try set rank globaly
     * @param worldName the world name, in witch rank have to be seted if not globaly, when Permissions plugin not support globaly changes or <s>in config globaly changes are disabled. 
     * @return  true if rank has been seted
     * @deprecated this will be moved to API
     */
    @Deprecated
    public boolean SetRank(CommandSender cs, String Player, String[] groups, long timeDiff ,boolean set, boolean globaly, String worldName){
        ConfigurationSection usersSection = database.getConfigurationSection("users."+Player);
        if(timeDiff <= 0L) {
            if(usersSection!=null){
                debugmsg("Clearing database for user "+Player+". Setting group to "+ Util.tableToString(groups)+" permanently ...");
                database.set("users." + Player, null);
                save(cs);

            }
            if(permsSys.setPlayersGroups(Player, groups, worldName, globaly))
            {
                debugmsg("Seted group "+ Player + " to "+Util.tableToString(groups) + " permanently.");
                cs.sendMessage(ChatColor.AQUA + "[AZRank]"+ChatColor.AQUA + " Successful moved "+ Player + " to " + Util.tableToString(groups) + " forever!");
                return true;
            }
            debugmsg("Permissions plugin dont Set group "+ Player + " to "+Util.tableToString(groups) + " (permanently)");
            return false;
        }
        else //temporary
        {
            String[] oldGroups = permsSys.getPlayersGroups(Player, worldName, globaly);
            if(Arrays.equals(groups, oldGroups)) //jeżeli jest już w tych grupach
            {
                //TODO: multi check
                // debugmsg
                int i=0;
                java.util.Date now = new java.util.Date();
                ConfigurationSection groupSection = database.getConfigurationSection("users."+Player+"."+groups[i]);
                
                long timeTo = (groupSection!=null ? groupSection.getLong("to") : 0);
                if(groupSection!=null && !set && timeTo>0) { //has temporiary and without -s flag (adding time)
                    //TODO: add adding time
                    timeTo += timeDiff;
                    cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.AQUA + " Adding some time for "+ Player + " to be in " + Util.tableToString(groups) );
                } else
                {
                    cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.AQUA + " Setting new time for "+ Player + " to be in " + Util.tableToString(groups) );
                    timeTo=now.getTime()+timeDiff;
                }

                {//updateing database
                    database.set("users." + Player + "."+groups[i] + ".from", now.getTime());
                    database.set("users." + Player + "."+groups[i] + ".to", timeTo );
                    database.set("users." + Player + "."+groups[i] + ".world", worldName );
                    save(cs);
                    debugmsg("Successful seted new time for "+Player + " to be in "+ Util.tableToString(groups) );
                }
                return true;
            }
            else //inne grupy niż ma
            {
                //TODO: debug msgs to this:
                List<String> restoreGroups = new LinkedList<String>();
                for(String oldGroup : oldGroups)
                {
                    if(database.getConfigurationSection("users."+Player+"."+oldGroup)!=null)//jeżeli są dane grupy(to tymczasowa)
                    {
                        List<String> rg=(List<String>)database.getList("users."+Player+"."+oldGroup+".restoreGroups");
                        if(rg!=null) //jeżeli jakieś są to dodaj je do nowej listy
                        {
                            Iterator<String> it = rg.iterator();
                            String next;
                            while(it.hasNext()){
                                next =it.next();
                                if(!restoreGroups.contains(next))
                                    restoreGroups.add(next);
                            }
                        }
                    }
                    else
                        restoreGroups.add(oldGroup);
                }
                debugmsg("Generated list of groups to restore: "+ Util.tableToString(restoreGroups.toArray(new String[]{})));
                if(permsSys.setPlayersGroups(Player, groups, worldName, globaly))
                { //jeżeli pomyślnie ustawiono nowe grupy
                    //TODO: obsługe wyjątków
                    database.set("users." + Player , null);
                    java.util.Date now = new java.util.Date();
                    for(String group:groups)
                    {
                        database.set("users."+Player+"."+group+".restoreGroups",restoreGroups);
                        database.set("users."+Player+"."+group+".from", now.getTime());
                        database.set("users."+Player+"."+group+".to",now.getTime() + timeDiff);
                        database.set("users."+Player+"."+group+".world",worldName);
                    }
                    cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.AQUA + " Moved "+ Player + " to " + Util.tableToString(groups) + " to " + now.getTime() + timeDiff);
                    debugmsg("Seted group of "+Player +" to "+Util.tableToString(groups)+" to "+ now.getTime() + timeDiff);
                    save(cs);
                    return true;
                }
                else
                { //błąd
                    cs.sendMessage(ChatColor.RED+"Błąd! Nie udało się zmienić grupy!");
                    debugmsg("Permissions adapter return false for: ("+Player +","+Util.tableToString(groups)+")");
                    return false;
                }  
            }  
        }        

    }
	
    public boolean playerAddTmpGroup(CommandSender cs, String Player, String[] groups, long timeDiff, boolean set, boolean globaly, String worldName) {
        ConfigurationSection usersSection = database.getConfigurationSection("users."+Player);
        if(timeDiff <= 0L) {
            for(String group:groups)
            {
                usersSection.set(group,null);
            }
            save(cs);
            if(permsSys.playerAddGroups(Player, groups, worldName, globaly))
            {
                debugmsg("Added "+ Player + " to "+Util.tableToString(groups) + " permanently.");
                cs.sendMessage(ChatColor.AQUA + "[AZRank]"+ChatColor.AQUA + " Successful add "+ Player + " to " + Util.tableToString(groups) + " forever!");
                return true;
            }
            debugmsg("Permissions plugin dont added "+ Player + " to "+Util.tableToString(groups) + " (permanently)");
            return false;
        }
        else //tymczasowa
        {
            String[] oldGroups = permsSys.getPlayersGroups(Player, worldName, globaly);
            boolean czy=false;
            boolean znaleziono;
            if(groups.length>0)
                czy=true;
            for(String group:groups)
            {
                znaleziono=false;
                for(String cg: oldGroups){
                    if(cg.equalsIgnoreCase(group)){
                        znaleziono=true;
                        break;
                    }
                }
                if(!znaleziono){
                    czy=false;
                    break;
                }
            }
            if(czy) //jeżeli jest już w tych grupach
            {
                //TODO: multi check
                // debugmsg
                int i=0;
                ConfigurationSection groupSection = database.getConfigurationSection("users."+Player+"."+groups[i]);
                java.util.Date now = new java.util.Date();
                long timeTo=groupSection.getLong("to");


                if(!set && timeTo>0) { //has temporiary and without -s flag (adding time)
                    //TODO: add adding time
                    timeTo += timeDiff;
                    //time = Util parse time + database.get
                    cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.AQUA + " Adding some time for "+ Player + " to be in " + Util.tableToString(groups) );
                } else
                {
                    timeTo=timeDiff+now.getTime();
                    cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.AQUA + " Setting new time for "+ Player + " to be in " + Util.tableToString(groups) );
                }
                
                {//updateing database
                    //database.set("users." + Player + "."+groups[i] + ".from", now.getTime());
                    database.set("users." + Player + "."+groups[i] + ".to", timeTo );
                    save(cs);
                    debugmsg("Successful seted new time for "+Player + " to be in "+ Util.tableToString(groups) );
                }
                return true;
            }
            else //inne grupy niż ma
            {
                //TODO: debug msgs to this:
                //TODO: dla każdej grupy osobno sprawdzanie czy w niej już jest!
                if(permsSys.playerAddGroups(Player, groups, worldName, globaly))
                { //jeżeli pomyślnie ustawiono nowe grupy
                    //TODO: obsługe wyjątków
                    java.util.Date now = new java.util.Date();
                    for(String group:groups)
                    {
                        //TODo sprawdzanie czy już jest i nie dodawanie '.from'
                        database.set("users."+Player+"."+group+".from", now.getTime());
                        database.set("users."+Player+"."+group+".to",now.getTime()+timeDiff);
                        database.set("users."+Player+"."+group+".world",worldName);  
                    }
                    cs.sendMessage(ChatColor.GREEN + "[AZRank]"+ChatColor.AQUA + " Added "+ Player + " to " + Util.tableToString(groups) + " to " + now.getTime()+timeDiff);
                    debugmsg("Added "+Player +" to "+Util.tableToString(groups)+" to "+ now.getTime()+timeDiff);
                    save(cs);
                    return true;
                }
                else
                { //błąd
                    cs.sendMessage(ChatColor.RED+"Błąd! Nie udało się zmienić grupy!");
                    debugmsg("Permissions adapter return false for: add ("+Player +","+Util.tableToString(groups)+")");
                    return false;
                }  
            }  
        }        
    }

	public void save() {
		try {
			database.save(yamlDataBaseFile);
			database.load(yamlDataBaseFile);
		} catch (IOException e) {
			log.log(Level.SEVERE, "[AZRank] I/O ERROR - unable to save database");
			StackTraceElement[] stackTrace = e.getStackTrace();
                        for(StackTraceElement element : stackTrace)
                        {
                            log.log(Level.SEVERE, "  {0}", element.toString());
                        }
		} catch (Exception e) {
			log.log(Level.SEVERE, "[AZRank] OTHER ERROR - unable to save database");
			StackTraceElement[] stackTrace = e.getStackTrace();
                        for(StackTraceElement element : stackTrace)
                        {
                            log.log(Level.SEVERE, "  {0}", element.toString());
                        }
		}
	}
	public void save(CommandSender cs) {
		try {
			database.save(yamlDataBaseFile);
			database.load(yamlDataBaseFile);
		} catch (IOException e) {
			log.log(Level.SEVERE, "[AZRank] I/O ERROR - unable to save database");
			cs.sendMessage(ChatColor.RED + "[AZRank] I/O ERROR - unable to save database");
			StackTraceElement[] stackTrace = e.getStackTrace();
                        for(StackTraceElement element : stackTrace)
                        {
                            log.log(Level.SEVERE, "  {0}", element.toString());
                        }
		} catch (Exception e) {
			log.log(Level.SEVERE, "[AZRank] OTHER ERROR - unable to save database");
			StackTraceElement[] stackTrace = e.getStackTrace();
                        for(StackTraceElement element : stackTrace)
                        {
                            log.log(Level.SEVERE, "  {0}", element.toString());
                        }
		}
	}
	      
        public boolean canSetRank(CommandSender cs, String player, String group) {
            //TODO: this, powiadomienia że nie ma permissions do komendy lub do grupy lub do gracza danego!
            return hasSetRank(cs, group);
        }
        @Deprecated
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
			log.log(Level.SEVERE, "[AZRank][ERROR]{0}", e.getMessage());
			return false;
		}	
	}

	public boolean hasReload(Player player) {
		try {
                    String node = "azrank.reload";
                    return player.hasPermission(node);

		} catch(Exception e) {
			log.log(Level.SEVERE, "[AZRank][ERROR]{0}", e.getMessage());
			return false;
		}	
		
	}
	
	@Deprecated
	public boolean setGroups(String name, String[] oldGroups, boolean globaly, String worldName) {
		try {
                    return permsSys.setPlayersGroups(name, oldGroups, worldName, globaly);
		} catch(Exception e) {
                    log.log(Level.SEVERE, "[AZRank][Exception]when setting group|{0}", e.getMessage());
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for(StackTraceElement element : stackTrace)
                    {
                        log.log(Level.SEVERE, "  {0}", element.toString());
                    }
                    return false;
		}
	}
	@Deprecated
	public boolean setGroup(String name, String group, boolean globaly, String worldName) {
            String[] groups ={group};
            return setGroups(name,groups, globaly, worldName);
	}
        
	@Deprecated
	public String[] getGroups(String name, boolean globaly, String worldName){
		try {
                    return permsSys.getPlayersGroups(name, worldName, globaly);
		} catch(Exception e) {
			log.log(Level.SEVERE, "[AZRank][ERROR]{0}", e.getMessage());
			return null;
		}
	}
	
        @Deprecated
	public boolean dLoad() {
		try {
                    cfg.checkConfig();
                    cfg.loadConfig();
                   /* if(checker.working)
                    {
                        checker.setToDataReload(null);
                        return true;
                    }*/
                    if(checkerTask!=null && getServer().getScheduler().isCurrentlyRunning(checkerTask.getTaskId()))
                    {
                        checker.setToDataReload(null);
                        return true;
                    }
                    else
                    {
                        database.load(yamlDataBaseFile);
                        return true;
                    }
		}
                catch (Exception e)
                {
                    log.log(Level.SEVERE, "[AZRank][ERROR]{0}", new Object[]{ e.getMessage()});
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for(StackTraceElement element : stackTrace)
                    {
                        log.log(Level.SEVERE, "  {0}", element.toString());
                    }
                    return false;
		}
	}
        
        public boolean dLoad(CommandSender cs) {
		try {
                    cfg.checkConfig();
                    cfg.loadConfig();
                    /*if(checker.working)
                    {
                        checker.setToDataReload(cs);
                        return true;
                    }*/
                    if(checkerTask!=null && getServer().getScheduler().isCurrentlyRunning(checkerTask.getTaskId()))
                    {
                        checker.setToDataReload(null);
                        return true;
                    }
                    else
                    {
                        database.load(yamlDataBaseFile);
                        return true;
                    }
		} catch (Exception e) {
                    log.log(Level.SEVERE, "[AZRank][ERROR]{0}", new Object[]{ e.getMessage()});
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for(StackTraceElement element : stackTrace)
                    {
                        log.log(Level.SEVERE, "  {0}", element.toString());
                    }
                    return false;
		}
	}
        public boolean dataReload(CommandSender cs) {
		try {
                    database.load(yamlDataBaseFile);
                    if(cs!=null)
                    {
                        cs.sendMessage("[AZRank] Successful reloaded data!");
                    }
                    return true;
		} catch (Exception e) {
                    log.log(Level.SEVERE, "[AZRank][ERROR]{0}", new Object[]{e.getMessage()});
                    StackTraceElement[] stackTrace = e.getStackTrace();
                    for(StackTraceElement element : stackTrace)
                    {
                        log.log(Level.SEVERE, "  {0}", element.toString());
                    }
                    return false;
		}
	}
	
	public void debugmsg(String msg){
            if(cfg.logEverything)
            {
		log.log(Level.INFO, "[AZRank][DEBUG]{0}", new Object[]{ msg});
            }
	}


    public void wypiszGraczy(CommandSender cs, int count, int page) {
        try
        {
            List<AZPlayersGroup> tempGroups = new LinkedList();
            ConfigurationSection usersSection=database.getConfigurationSection("users");
            ConfigurationSection userSection;
            Set<String> players=usersSection.getKeys(false);
            AZPlayersGroup group;
            List<String> restoreGroups;
            for(String playerName:players)
            {
                userSection=usersSection.getConfigurationSection(playerName);
                for(String groupName:userSection.getKeys(false))
                {
                    restoreGroups=userSection.getStringList(groupName+".restoreGroups");
                    if(restoreGroups.size()>0)
                        group = new AZPlayersGroup(playerName,groupName,userSection.getLong(groupName+".from"),userSection.getLong(groupName+".to"), restoreGroups.toArray(new String[]{}) );
                    else
                        group = new AZPlayersGroup(playerName,groupName,userSection.getLong(groupName+".from"),userSection.getLong(groupName+".to"));
                    
                    tempGroups.add(group);
                }
            }
            Collections.sort(tempGroups,new ComparatorByPlayerAndTimeEnd());
            
            int pages;
            int min,max;
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm zzz");
            java.util.Date toDate = new java.util.Date();
            dateformat.setTimeZone(cfg.timeZone);
            
            if(count>0)
            {
                if(tempGroups.size()>0)
                    pages = 1 + tempGroups.size() / count;
                else
                    pages=0;
                min = (page-1)*10;
                max = page*10;
                if(max>=tempGroups.size())
                    max=tempGroups.size()-1;
                cs.sendMessage(ChatColor.RED + "===Temporiary ranks: == PAGE: " + page +"/"+pages+"====");
            } else
            {
                min=0;
                max=tempGroups.size();
                cs.sendMessage(ChatColor.RED + "===Temporiary ranks: == " + max+ " ====");
            }
            
            
            for(int i=min;i<max;i++)
            {
                group=tempGroups.get(i);
                toDate.setTime(group.to);
                String rg="";
                if(group.restoreGroups.length>0)
                    rg=", later in " + Util.tableToString(group.restoreGroups);
                cs.sendMessage("" + (i+1) + ". " + group.playerName + " in: " + group.groupName + " to: " +dateformat.format(toDate)+rg);
            }
        } catch(OutOfMemoryError e){
            cs.sendMessage(ChatColor.RED + "Out of memory error! send ticket on dev.bukkit.org/server-mods/azrank");
        }
        
        
        
        
        /*ConfigurationSection usersSection = database.getConfigurationSection("users");
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
        }*/
        
        
    }

    /*private String wypiszGracza(String user) {
        String groups=permBridge.getPlayersGroupsAsString(user);
        long to = database.getLong("users." + user + ".to");
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        java.util.Date toDate = new java.util.Date(to);
        List<String> oldGroups = database.getStringList("users." + user + ".oldRanks");
        return user + ChatColor.RED +groups + ChatColor.WHITE + " to: " + ChatColor.YELLOW + dateformat.format(toDate) + ChatColor.WHITE + ", next " + ChatColor.BLUE + oldGroups;
    }*/

    public boolean hasPerm(CommandSender cs, String node) {
        if(cs instanceof Player){
            Player player = (Player)cs;
            if(player.hasPermission(node) || player.hasPermission(AZRankPermissions.ALL_NODE) || (player.isOp() && cfg.allowOpsChanges))
                return true;
            else return false;
        } else
            return true;
    }

    public boolean infoCMD(CommandSender cs, String playername) {
        String world;
        if(cs instanceof Player) {
            world=((Player)cs).getLocation().getWorld().getName();
        } else {
            world=getServer().getWorlds().get(0).getName();
        }
        
        ConfigurationSection userSection = database.getConfigurationSection("users."+playername);                
        String[] groups = permsSys.getPlayersGroups(playername, world, true);
        if(userSection==null) {
            cs.sendMessage(ChatColor.GREEN + "[AZRank] " + ChatColor.AQUA + "User "+playername+ " is in " + Util.tableToString(groups) + " forever");
        } else {
            String msg="";
            long to;
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date toDate;
            List<String> crGroups = new LinkedList<String>(Arrays.asList(groups));
            Set<String> keys=userSection.getKeys(false);
            if(keys.size()>0)
            {
                msg+=ChatColor.YELLOW+" temporiary"+ChatColor.AQUA+":\n";
            }
            String dgroup;
            for(String group:keys)//dla każdej grupy w bazie danych
            {
                dgroup=group;
                for(String crGroup : crGroups) 
                    if(crGroup.equalsIgnoreCase(group)){
                        dgroup=crGroup;               
                        crGroups.remove(crGroup);
                        break;
                    }
                to = database.getLong("users." + playername + "."+group+".to");
                toDate = new java.util.Date(to);
                List<String> oldGroups = database.getStringList("users." + playername + "." + group + ".restoreGroups");
                String rg ="";
                if(oldGroups!=null && oldGroups.size()>0)
                    rg=ChatColor.AQUA+" later in: "+ChatColor.DARK_AQUA+oldGroups;
                msg+=ChatColor.DARK_AQUA+ dgroup + ChatColor.AQUA+" to " + ChatColor.DARK_AQUA+dateformat.format(toDate) + rg + "\n";
                
            }
            String pg="";
            if(crGroups.size()>0)
            {
                pg=ChatColor.YELLOW+" permanently"+ChatColor.AQUA+": "+ChatColor.DARK_AQUA+crGroups.get(0);
                crGroups.remove(0);
                for(String cg:crGroups)
                {
                    pg+=ChatColor.AQUA+", "+ChatColor.DARK_AQUA+cg;
                }
                pg+=ChatColor.AQUA+";";
            }
            msg=ChatColor.GREEN + "[AZRank] " + ChatColor.AQUA + "User "+playername+ " is in: "+pg+msg;
            cs.sendMessage(msg);
            
        }
        return true;
    }
    /**
     * Run task repeatively to check rank.
     * @return true if task has'n be yet runed, but now.
     */
    private boolean startCheckerTask(boolean delayed) {
        if(checkerTask==null || !getServer().getScheduler().isQueued(checkerTask.getTaskId()))
        {
            checkerTask = getServer().getScheduler().runTaskTimerAsynchronously(this, checker, delayed ? cfg.checkInterval : 0, cfg.checkInterval);
            return true;
        }
        return false;
    }
    /**
     * Stop task checking rank.
     * @return true if task has be already runed and stoped now.
     */
    private boolean stopCheckerTask() {
        if(checkerTask!=null && getServer().getScheduler().isQueued(checkerTask.getTaskId()))
        {
            checkerTask.cancel();
            return true;
        }
        return false;
    }

    private boolean startUpdaterTask(boolean delayed) {
        if(updaterTask==null || !getServer().getScheduler().isQueued(updaterTask.getTaskId()))
        {
            updaterTask = getServer().getScheduler().runTaskTimerAsynchronously(this, updater, delayed ? cfg.updateInterval : 0, cfg.updateInterval);
            return true;
        }
        return false;
    }
    
    private boolean stopUpdaterTask() {
        if(updaterTask!=null && getServer().getScheduler().isQueued(updaterTask.getTaskId()))
        {
            updaterTask.cancel();
            return true;
        }
        return false;
    }

}


