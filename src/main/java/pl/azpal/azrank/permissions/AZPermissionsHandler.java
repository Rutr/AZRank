/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.azpal.azrank.permissions;

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
    
}
