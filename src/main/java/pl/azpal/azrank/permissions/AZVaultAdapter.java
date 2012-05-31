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
    public String[] getPlayersGroups(Player player){
        return pp.getPlayerGroups(player);
               
    }
    
    public void setPlayersGroups(Player player, String[] groups){
        String[] oldGroups=pp.getPlayerGroups(player);
        for(String group : oldGroups)
        {
            pp.playerRemoveGroup(player, group);
        }
        for(String group : groups)
        {
            pp.playerAddGroup(player, group);
        }
        
    }
    
}
