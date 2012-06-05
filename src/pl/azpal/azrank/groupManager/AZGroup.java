/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.azpal.azrank.groupManager;

import java.util.Calendar;

/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
public class AZGroup {
    private String name;
    public long date;
    public long endTime;
    
    public AZGroup(String groupName, long start, long end) {
        name=groupName;
        date=start;
        endTime=end;
    }
    
    public long getDateDiff() {
        return endTime-date;
    }            
     
    public long getRemainingTime(){
        return endTime - Calendar.getInstance().getTimeInMillis();
    }
    
    public String getName() {
        return name;
    }
    
}
