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
 
    public abstract String[] getPlayersGroups(String playerName);
    
    
    public String[] getPlayersGroups(Player player){
        return getPlayersGroups(player.getName());
    }
    
    public abstract boolean setPlayersGroups(String playerName, String[] groups);

    
    public boolean setPlayersGroups(Player player, String[] groups)
    {
        return setPlayersGroups(player.getName(),groups);
    }
    
    @Deprecated
    public String getPlayersGroupsAsString(String playerName) {
        try {
            String[] groupsArray = getPlayersGroups(playerName);
            String groups="[";
            if (groupsArray.length > 0) {
                groups = groups+ groupsArray[0];    // start with the first element
                for (int i=1; i<groupsArray.length; i++) {
                    groups = groups + ", " + groupsArray[i];
                }
            }
            groups = groups +"]";
            return groups;
        }catch(NullPointerException e){
            return "[]";
        }
    }
 
    @Deprecated
    public String getPlayersGroupsAsString(Player player) {
        String[] groupsArray = getPlayersGroups(player);
        String groups="[";
        if (groupsArray.length > 0) {
            groups = groups+ groupsArray[0];    // start with the first element
            for (int i=1; i<groupsArray.length; i++) {
                groups = groups + ", " + groupsArray[i];
            }
        }
        groups = groups +"]";
        return groups;
    }

    public boolean playerAddGroups(String userName, String[] groups) {
        List<String> crGroups = new ArrayList(Arrays.asList(getPlayersGroups(userName)));
        List<String> addGroups= Arrays.asList(groups);
        crGroups.addAll(addGroups);
        return setPlayersGroups(userName, crGroups.toArray(new String[]{}));
    }

    public synchronized boolean playerRemoveGroups(String userName, String[] groups) {
        List<String> crGroups = new ArrayList(Arrays.asList(getPlayersGroups(userName)));
        List<String> remGroups= Arrays.asList(groups);
        //crGroups.removeAll(remGroups);
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
        return setPlayersGroups(userName, crGroups.toArray(new String[]{}));
    }
    
}
