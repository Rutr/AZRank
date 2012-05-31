/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.azpal.azrank.permissions;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;
import pl.azpal.azrank.AZRank;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
@Deprecated
public class AZbPermissionsBridge extends AZPermissionsHandler {
    
    
    public AZbPermissionsBridge(AZRank origin){
        plugin=origin;
    }
    
    public String getName() {
        return("bPermissions");
    }
 
    public String[] getPlayersGroups(String playerName){
        return ApiLayer.getGroups(plugin.getServer().getPlayer(playerName).getWorld().getName(), CalculableType.USER, playerName);
    }
    
    public void setPlayersGroups(String playerName, String[] groups){
        ApiLayer.setGroup(plugin.getServer().getPlayer(playerName).getWorld().getName() , CalculableType.USER,playerName, groups[0]);
        //TO DO: dodanie wiecej grup
        if(groups.length>1){
            for(int i=1;i<groups.length;i++){
                ApiLayer.addGroup(plugin.getServer().getPlayer(playerName).getWorld().getName() , CalculableType.USER,playerName, groups[i]);
            }
        }
    }
    
}
