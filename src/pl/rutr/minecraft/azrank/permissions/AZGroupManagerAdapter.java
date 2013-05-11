
package pl.rutr.minecraft.azrank.permissions;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
 

import java.util.Iterator;
import java.util.List;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import pl.rutr.minecraft.azrank.AZRank;



public class AZGroupManagerAdapter extends PermissionsSysHandler{
    
    private WorldsHolder holder;
    
    public AZGroupManagerAdapter(AZRank origin, WorldsHolder holder){
        this.holder=holder;
        azrank=origin;
    }
    
    @Override
    public String getName() {
        return("GroupManager");
    }
 
    @Override
    public String[] getPlayersGroups(String playerName, String worldName, boolean globaly){
        AnjoPermissionsHandler handler = holder.getWorldPermissionsByPlayerName(playerName);
        
        if (handler == null)
            handler = holder.getWorldPermissions(worldName);
        if (handler == null)
            return null;
        
        return handler.getGroups(playerName);
    }
    
    @Override
    public boolean setPlayersGroups(String playerName, String[] groups, String worldName, boolean globaly){
        OverloadedWorldHolder handler = holder.getWorldDataByPlayerName(playerName);
        if (handler == null)
            handler = holder.getWorldData(worldName);
        if (handler == null)
                return false;
        
        handler.getUser(playerName).setGroup(handler.getGroup(groups[0]));
        if(groups.length>1) {
                for(int i=1;i<groups.length;i++){
                        handler.getUser(playerName).addSubGroup(handler.getGroup(groups[i]));
                }
        }
        return true;
    }
    
    @Override
    public boolean playerAddGroups(String playerName, String[] groups, String worldName, boolean globaly){
        OverloadedWorldHolder handler = holder.getWorldDataByPlayerName(playerName);
        if (handler == null)
            handler = holder.getWorldData(worldName);
        if (handler == null)
                return false;
        
        for(int i=0;i<groups.length;i++){
                handler.getUser(playerName).addSubGroup(handler.getGroup(groups[i]));
        }
        
        return true;
    }
    
    @Override
    public boolean playerRemoveGroups(String playerName, String[] groups, String worldName, boolean globaly){
        OverloadedWorldHolder handler = holder.getWorldDataByPlayerName(playerName);
        if (handler == null)
            handler = holder.getWorldData(worldName);
        if (handler == null) {
            azrank.debugmsg("Failed to remove groups from player: " + playerName);
            return false;
        }
        azrank.debugmsg("getName: "+ handler.getName());
        User user = handler.getUser(playerName);
        String obecna = user.getGroupName();
        boolean czy=false;
        
        for(int i=0;i<groups.length;i++){
            if(obecna.equalsIgnoreCase(groups[i])){
                czy=true;
                break;
            }       
        }
        if(czy) {//jeżeli jest w grupie która jest do usunięcia 
             if(user.isSubGroupsEmpty())
                 user.setGroup(handler.getDefaultGroup());
             else {
                 List<Group> oldGroups = user.subGroupListCopy();
                 
                 Iterator iter = oldGroups.iterator();
                 Group oldGroup;
                 while(iter.hasNext()){//dla każdej starej grupy sprawdz czy nie ma jej w grupach do usunięcia
                     oldGroup=(Group)iter.next();
                     for(int i=0;i<groups.length;i++){
                         if(oldGroup.getName().equalsIgnoreCase(groups[i]))
                             iter.remove();
                     }
                 }
                 
                 if(oldGroups.size()<1)
                     user.setGroup(handler.getDefaultGroup());
                 else{
                     user.setGroup(oldGroups.get(0));
                     if(oldGroups.size()>1) {
                         for(Group newGroup:oldGroups)
                             user.addSubGroup(newGroup);
                     }
                 }
             }
                 
        } else {
            for(int i=0;i<groups.length;i++){
                if(!user.removeSubGroup(handler.getGroup(groups[i]))){
                    azrank.debugmsg("Failed to remove group: "+ groups[i] + " from player: "+playerName);
                    return false;
                }
            }
        }
        
        return true;
    }

}
