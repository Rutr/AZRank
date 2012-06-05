/*
 * Group Manager
 * 
 */
package pl.azpal.azrank.groupManager;


import org.bukkit.entity.Player;
/**
 *
 * @author Rutr
 */
public interface AZPermissionsSysIf {
    
    public abstract AZGroup[] getPlayersGroups(Player player); 
    
    public abstract void setPlayersGroups(Player player, AZGroup[] groups);
    
    //public 
    
}
