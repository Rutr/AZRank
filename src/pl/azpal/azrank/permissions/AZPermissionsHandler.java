/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.azpal.azrank.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.Player;
import pl.azpal.azrank.AZRank;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
public abstract class AZPermissionsHandler {
    
    AZRank plugin;
    
    public abstract String getName();
 
    
    public abstract String[] getPlayersGroups(String playerName, boolean globaly, String worldName);
    
    
 
    public abstract boolean setPlayersGroups(String playerName, String[] groups, boolean globaly, String worldName);
    

    
    public synchronized boolean playerAddGroups(String userName, String[] groups, boolean globaly, String worldName) {
        List<String> crGroups = new ArrayList(Arrays.asList(getPlayersGroups(userName, true, worldName)));
        List<String> addGroups= Arrays.asList(groups);
        crGroups.addAll(addGroups);
        return setPlayersGroups(userName, crGroups.toArray(new String[]{}), true, worldName);
    }

    
    public synchronized boolean playerRemoveGroups(String userName, String[] groups, boolean globaly, String worldName) {
        List<String> crGroups = new ArrayList(Arrays.asList(getPlayersGroups(userName, true, worldName)));
        List<String> remGroups= Arrays.asList(groups);
        Iterator<String> iter = crGroups.iterator();
        String crGroup;
        while(iter.hasNext()){
            crGroup=iter.next();
            for(String remGroup:remGroups){
                if(crGroup.equalsIgnoreCase(remGroup)){
                    iter.remove();
                    break;
                }
            }
        }
        return setPlayersGroups(userName, crGroups.toArray(new String[]{}), true, worldName);
    }
    
 
    
}
