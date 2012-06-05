/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.azpal.azrank.permissions;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import pl.azpal.azrank.AZRank;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
public class AZVaultAdapter extends AZPermissionsHandler{
    
    public static Permission pp;
    
    public AZVaultAdapter(AZRank origin, Permission pp){
        this.plugin=origin;
        this.pp = pp;
    }
    
    public String getName() {
        return(pp.getName());
    }
 
    @Override
    public String[] getPlayersGroups(String player){
        return pp.getPlayerGroups((String) null, player);
               
    }
    
    public boolean setPlayersGroups(String player, String[] groups){
        String[] oldGroups=pp.getPlayerGroups((String) null,player);
        int i=0;
        for(String group : oldGroups)
        {
            if(!pp.playerRemoveGroup((String) null,player, group))
                plugin.debugmsg("i cant remove group: "+ group + " from player: "+player);
            else
                i++;
        }
        int j=0;
        for(String group : groups)
        {
            if(!pp.playerAddGroup((String) null,player, group))
                plugin.debugmsg("i cant add group: "+ group + " to player: "+player);
            else
                j++;
        }
        plugin.debugmsg("removed: "+i+"/"+oldGroups.length+ " added: "+j+"/"+groups.length);
        if(i==0 || j==0){
            return false;
        }
        return true;
    }
    
}
