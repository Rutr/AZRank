package pl.azpal.azrank;

import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;




class TimeRankChecker implements Runnable {
	private AZRank plugin;

	public TimeRankChecker(AZRank origin) {
		this.plugin = origin;
	}
	@Override
	public void run() {
		try {
			plugin.debugmsg("Checking Time Ranks");
			long now = Calendar.getInstance().getTimeInMillis();
			//GregorianCalendar
			ConfigurationSection usersSection = plugin.database.getConfigurationSection("users");
			
			if (usersSection != null) {
                                Set<String> usersNames = usersSection.getKeys(false);
                                //plugin.tempranks=usersNames.size();
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
                                            for(String group:groups)
                                            {
                                                tempranks++;
                                                to = plugin.database.getLong("users." + userName + "."+group+".to");
                                                if(to < now)
                                                {
                                                    List<String> restoreGroups = userSection.getStringList(group+".restoreGroups");
                                                    if(restoreGroups!=null)
                                                    {
                                                        plugin.permBridge.playerAddGroups(userName,restoreGroups.toArray(new String[]{}));
                                                        for(String rGroup:restoreGroups)
                                                        {
                                                            userSection.set(rGroup,null);
                                                        }
                                                    }
                                                    plugin.permBridge.playerRemoveGroups(userName,new String[]{group});
                                                    plugin.database.set("users." + userName + "."+group,null);
                                                    plugin.save();
                                                }
                                            }
                                        }
                                        else {//capability with 1.2.5 and older versions
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
                                                                    if(plugin.setGroups(userName, groups)){
                                                                        plugin.log.info("[AZRank] unranked user " + userName + " to group(s) " + oldGroups);
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
                                                        String[] crGroups = plugin.permBridge.getPlayersGroups(userName);
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
                                plugin.tempranks=tempranks;
                                plugin.debugmsg("temporary ranks: "+tempranks);
			}
			
		} catch(Exception e) {
			plugin.log.info("[AZRank]ERROR - " + e.getMessage());
                        e.printStackTrace();
		}
	}

}
