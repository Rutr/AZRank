/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.azpal.azrank.permissions;

import ru.tehkode.permissions.PermissionManager;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
public class AZPEXBridge extends AZPermissionsHandler {
    
    public static PermissionManager pex;
    
    public AZPEXBridge(PermissionManager pex){
        AZPEXBridge.pex=pex;
    }
    
    public String getName() {
        return("PermissionsEx");
    }
 
    public String[] getPlayersGroups(String playerName){
        return pex.getUser(playerName).getGroupsNames();
    }
    
    public void setPlayersGroups(String playerName, String[] groups){
        pex.getUser(playerName).setGroups(groups);
    }
    
    
    
}
