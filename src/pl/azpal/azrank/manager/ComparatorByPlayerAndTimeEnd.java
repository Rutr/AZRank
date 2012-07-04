package pl.azpal.azrank.manager;

import java.util.Comparator;
/**
 *
 * @author Rutr <artuczapl at gmail.com>
 */
public class ComparatorByPlayerAndTimeEnd implements Comparator<AZPlayersGroup>{
    
    public int compare(AZPlayersGroup o1, AZPlayersGroup o2) {
        int wynik = o1.playerName.compareTo(o2.playerName);
        if(wynik==0)
            return o1.to.compareTo(o2.to);
        else return wynik;
    }
    
}
