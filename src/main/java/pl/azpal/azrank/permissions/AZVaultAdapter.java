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
        return pp.getPlayerGroups(plugin.getServer().getWorlds().get(0).getName(), player);
               
    }
    
    public boolean setPlayersGroups(String player, String[] groups){
        String world=plugin.getServer().getWorlds().get(0).getName();
        String[] oldGroups=pp.getPlayerGroups(world,player);
        int i=0;
        for(String group : oldGroups)
        {
            if(!pp.playerRemoveGroup(world,player, group))
                plugin.debugmsg("i cant remove group: "+ group + " from player: "+player+" in world: "+ world);
            else
                i++;
        }
        int j=0;
        for(String group : groups)
        {
            if(!pp.playerAddGroup(world,player, group))
                plugin.debugmsg("i cant add group: "+ group + " to player: "+player+" in world: "+ world);
            else
                j++;
        }
        if(i==0 || j==0){
            plugin.debugmsg("removed: "+i+"/"+oldGroups.length+ " added: "+j+"/"+groups.length);
            return false;
        }
        plugin.debugmsg("removed: "+i+"/"+oldGroups.length+ " added: "+j+"/"+groups.length);
        return true;
    }
    
}
