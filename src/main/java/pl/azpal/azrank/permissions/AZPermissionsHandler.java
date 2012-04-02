/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.azpal.azrank.permissions;

import java.util.List;
import pl.azpal.azrank.AZRank;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
public abstract class AZPermissionsHandler {
    
    AZRank plugin;
    
    public abstract String getName();
 
    public abstract String[] getPlayersGroups(String playerName);
    
    public abstract void setPlayersGroups(String playerName, String[] groups);
    
    public String getPlayersGroupsAsString(String playerName) {
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
    }
    
    
}
