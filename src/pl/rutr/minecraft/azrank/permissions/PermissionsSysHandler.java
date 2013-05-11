
package pl.rutr.minecraft.azrank.permissions;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import pl.rutr.minecraft.azrank.AZRank;



public abstract class PermissionsSysHandler {
 
    protected AZRank azrank;
    
    
    public abstract String getName();
 
    
    public abstract String[] getPlayersGroups(String playerName, String worldName, boolean globaly);
    
 
    public abstract boolean setPlayersGroups(String playerName, String[] groups, String worldName, boolean globaly);
    

    
    public synchronized boolean playerAddGroups(String userName, String[] groups, String worldName, boolean globaly) {
        List<String> crGroups = new ArrayList(Arrays.asList(getPlayersGroups(userName, worldName, true)));
        List<String> addGroups= Arrays.asList(groups);
        crGroups.addAll(addGroups);
        return setPlayersGroups(userName, crGroups.toArray(new String[]{}), worldName, true);
    }

    
    public synchronized boolean playerRemoveGroups(String userName, String[] groups, String worldName, boolean globaly) {
        List<String> crGroups = new ArrayList(Arrays.asList(getPlayersGroups(userName, worldName, true)));
        List<String> remGroups= Arrays.asList(groups);
        Iterator<String> iter = crGroups.iterator();
        String crGroup;
        while(iter.hasNext()){
            crGroup=iter.next();
            for(String remGroup:remGroups){
                if(crGroup.equalsIgnoreCase(remGroup)){
                    iter.remove();
                    break;
                }
            }
        }
        return setPlayersGroups(userName, crGroups.toArray(new String[]{}), worldName, true);
    }
    
 
    

}
