/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.azpal.azrank.permissions;

import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import pl.azpal.azrank.AZRank;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
@Deprecated
public class AZGroupManagerBridge extends AZPermissionsHandler{
    
    private WorldsHolder holder;
    
    public AZGroupManagerBridge(WorldsHolder holder, AZRank origin){
        this.holder=holder;
        plugin=origin;
    }
    
    public String getName() {
        return("GroupManager");
    }
 
    public String[] getPlayersGroups(String playerName){
        final AnjoPermissionsHandler handler = holder.getWorldPermissionsByPlayerName(playerName);
        if (handler == null)
            return null;
        return handler.getGroups(playerName);
    }
    
    public void setPlayersGroups(String playerName, String[] groups){
        final OverloadedWorldHolder handler = holder.getWorldData(plugin.getServer().getPlayer(playerName));
        if (handler == null)
                return;
        handler.getUser(playerName).setGroup(handler.getGroup(groups[0]));
        if(groups.length>1) {
                for(int i=1;i<groups.length;i++){
                        handler.getUser(playerName).addSubGroup(handler.getGroup(groups[i]));
                }
        }
    }
    
}
