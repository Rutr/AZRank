package pl.rutr.minecraft.azrank;

import java.util.logging.Level;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;




public class TimeRankChecker implements Runnable {
    private AZRank plugin;
    public boolean working=false;
    private boolean toReload=false;

    private CommandSender cs;

    public TimeRankChecker(AZRank origin) {
            this.plugin = origin;
    }
    @Override
    public void run()
    {
        if(!working)
        {
            working=true;
        
            try {
                plugin.debugmsg("Checking Time Ranks");
                long now = Calendar.getInstance().getTimeInMillis();
                //GregorianCalendar
                ConfigurationSection usersSection = plugin.database.getConfigurationSection("users");

                if (usersSection != null) {
                    Set<String> usersNames = usersSection.getKeys(false);

                    int tempranks=0;
                    for (String userName : usersNames)
                    {
                        plugin.debugmsg("Checking user: " + userName);
                        
                        long to = plugin.database.getLong("users." + userName + ".to");
                        if(to == 0)
                        { //version >= 1.3.0
                            //TODO: usuwanie jak nie ma .to
                            ConfigurationSection userSection = plugin.database.getConfigurationSection("users."+userName);
                            Set<String> groups = userSection.getKeys(false);

                            if(groups.size()>0){
                                for(String group:groups)
                                {
                                    tempranks++;
                                    to = plugin.database.getLong("users." + userName + "."+group+".to");
                                    if(to < now)
                                    {
                                        List<String> restoreGroups = userSection.getStringList(group+".restoreGroups");
                                        String world = userSection.getString(group+".world");
                                        if((world == null) || (world.equalsIgnoreCase("")))
                                        {
                                            world = plugin.getServer().getWorlds().get(0).getName();
                                        }

                                        if(restoreGroups!=null)
                                        {
                                            plugin.permsSys.playerAddGroups(userName,restoreGroups.toArray(new String[]{}), world, true);
                                            for(String rGroup:restoreGroups)//usuwanie danych o grupach które mają zostać przywrócone.
                                            {
                                                userSection.set(rGroup,null);
                                            }
                                        }
                                        plugin.permsSys.playerRemoveGroups(userName,new String[]{group}, world, true);
                                        plugin.database.set("users." + userName + "."+group,null);
                                        plugin.save();
                                    }
                                }
                            } else {

                                plugin.debugmsg(userName + " dont have any groups - deleting!");

                                usersSection.set(userName, null);
                            }

                        }
                        else {//capability with 1.2.5 and older versions
                            String world = plugin.getServer().getWorlds().get(0).getName();
                            if(to < now) {
                                List<String> oldGroups = plugin.database.getStringList("users." + userName + ".oldRanks");

                                plugin.debugmsg("groups count: " + oldGroups.size());

                                if(oldGroups.size() > 0) {
                                    String[] groups= new String[oldGroups.size()];
                                    for(int i=0;i<oldGroups.size();i++) {
                                        groups[i] = oldGroups.get(i);

                                        plugin.debugmsg("Was in group: " + oldGroups.get(i) );

                                    }
                                    try{
                                        if(plugin.setGroups(userName, groups, true, world)){
                                            AZRank.log.log(Level.INFO, "[AZRank] unranked user {0} to group(s) {1}", new Object[]{userName, oldGroups});
                                            plugin.database.set("users." + userName, null);
                                            plugin.save();
                                        } else {
                                            String oldGroupsS="[";
                                            if (oldGroups.size() > 0) {
                                                oldGroupsS += oldGroups.get(0);    // start with the first element
                                                for (int i=1; i<oldGroups.size(); i++) {
                                                    oldGroupsS += ", " + oldGroups.get(i);
                                                }
                                            }
                                            oldGroupsS+="]";
                                            plugin.log.severe("[AZRank][ERROR]F " + "Failed to restore group for "+userName+" to "+oldGroupsS+".\nYou should manualy retore player groups in permissions manager, and later in database.yml");
                                        }
                                    } catch (Exception e) {
                                        plugin.log.severe("[AZRank][ERROR]E " + e.getMessage());
                                        e.printStackTrace();
                                    }

                                } else {
                                    plugin.log.severe("[AZRank] Failed to unrank user " + userName + "! He haven't 'oldGroups'");
                                    plugin.database.set("users." + userName, null);
                                    plugin.save();
                                }
                            } else {
                                plugin.debugmsg("Checked " + userName + " - converting to new system...");
                                String[] crGroups = plugin.permsSys.getPlayersGroups(userName, world, true);
                                ConfigurationSection userSection = plugin.database.getConfigurationSection("users."+userName);
                                for(String crGroup:crGroups)
                                {
                                    userSection.set(crGroup+".from",userSection.getLong("from"));
                                    userSection.set(crGroup+".to",to);
                                    userSection.set(crGroup+".restoreGroups",userSection.getStringList("oldRanks"));
                                    userSection.set("from",null);
                                    userSection.set("to",null);
                                    userSection.set("oldRankss",null);
                                    plugin.debugmsg("Is in: " + crGroup);
                                }
                                plugin.save();
                                plugin.debugmsg("Converted!");
                            }
                        }
                    }
                    if(usersNames.size()<=0)
                    {
                        plugin.debugmsg("no users!");
                    }
                    else
                    {
                        plugin.debugmsg("checked user: " + usersNames.size() + "!");
                    }
                    plugin.tempranks=tempranks;

                    plugin.debugmsg("temporary ranks: "+tempranks);


                }

            } catch(Exception e) {
                    plugin.log.severe("[AZRank]ERROR - " + e.getMessage());
                    e.printStackTrace();

            }
            if(toReload)
            {
                if(plugin.dataReload(cs))
                {
                    toReload=false;
                }
            }
            working=false;
            
        }
        else
        {
            plugin.debugmsg("[TmpRankChecker] Still checking! new check canceled!");
        }

    }
       
    public void setToDataReload(CommandSender cs)
    {
        this.toReload=true;
        this.cs=cs;            
    }
    
    public boolean isToDataReload()
    {
        return toReload;
    }

}
