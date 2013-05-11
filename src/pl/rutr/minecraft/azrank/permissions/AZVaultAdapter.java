/*
 * TODO: globaly or not option.
 */
package pl.rutr.minecraft.azrank.permissions;

import net.milkbowl.vault.permission.Permission;
import pl.rutr.minecraft.azrank.AZRank;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
public class AZVaultAdapter extends PermissionsSysHandler{
    
    private Permission pp;
    
    public AZVaultAdapter(AZRank origin, Permission pp){
        this.azrank=origin;
        this.pp = pp;
    }
    
    @Override
    public String getName() {
        return(pp.getName());
    }
 
    @Override
    public String[] getPlayersGroups(String playerName, String worldName, boolean globaly){
        String[] groups=null;
        //String defaultWorld=plugin.getServer().getWorlds().get(0).getName();
        try {
            groups=pp.getPlayerGroups((String) null,playerName);
            if(groups == null)
            {
                return pp.getPlayerGroups(worldName, playerName);
            } else
            return groups;
        } catch (NullPointerException e){
            groups=pp.getPlayerGroups(worldName,playerName);
            return groups;
        }        
    }
    
    @Override
    public boolean setPlayersGroups(String player, String[] groups, String worldName, boolean globaly){
        String[] oldGroups=null;
        //String defaultWorld=plugin.getServer().getWorlds().get(0).getName();
        int i=0;
        
        try {
            oldGroups=pp.getPlayerGroups((String) null,player);
            if(oldGroups==null)
            { //to GroupManager compatybility 
                oldGroups=pp.getPlayerGroups(worldName,player);
            }
        } catch (NullPointerException e){
            oldGroups=pp.getPlayerGroups(worldName,player);
        }
        
        if(oldGroups==null)
        { //to GroupManager compatybility 
            oldGroups=pp.getPlayerGroups(worldName,player);
        }
        for(String group : oldGroups)
        {
            if(!pp.playerRemoveGroup((String) null, player, group))
            {
                if(!pp.playerRemoveGroup(worldName, player, group))
                    azrank.debugmsg("Failed to remove group: "+ group + " from player: "+player);
                else{
                    azrank.debugmsg("Globaly failed, Removed localy group: "+ group + " from player: "+player+" w:"+worldName);
                    i++;
                }
            }
            else
                i++;
        }

        int j=0;
        for(String group : groups)
        {
            if(!pp.playerAddGroup((String) null,player, group))
            {
                if(!pp.playerAddGroup(worldName,player, group))
                    azrank.debugmsg("Failed to add group: "+ group + " to player: "+player);
                else
                {
                    azrank.debugmsg("Globaly failed, Added localy group: "+ group + " from player: "+player+" w:"+worldName);
                    j++;
                }
            }
            else
                j++;
        }
        azrank.debugmsg("removed: "+i+"/"+oldGroups.length+ " added: "+j+"/"+groups.length);
        if(i==0 || j==0){
            return false;
        }
        return true;
    }
    
}
