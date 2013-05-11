
package pl.azpal.azrank.manager;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */



public class AZPlayersGroup {
    public String playerName;
    public String groupName;
    public Long from;
    public Long to;
    public String world=null;
    public String[] restoreGroups = new String[]{};
    
    public AZPlayersGroup(String playerName,String groupName,long from, long to)
    {
        this.playerName = playerName;
        this.groupName = groupName;
        this.from = from;
        this.to = to;
    }
    public AZPlayersGroup(String playerName,String groupName,long from, long to,String[] restoreGroups)
    {
        this.playerName = playerName;
        this.groupName = groupName;
        this.from = from;
        this.to = to;
        if(restoreGroups!=null)
            this.restoreGroups = restoreGroups;
    }
    public AZPlayersGroup(String playerName,String groupName,long from, long to,String world)
    {
        this.playerName = playerName;
        this.groupName = groupName;
        this.from = from;
        this.to = to;
        this.world = world;
    }
    public AZPlayersGroup(String playerName,String groupName,long from, long to,String world,String[] restoreGroups)
    {
        this.playerName = playerName;
        this.groupName = groupName;
        this.from = from;
        this.to = to;
        this.world = world;
        if(restoreGroups!=null)
            this.restoreGroups = restoreGroups;
    }
}
