package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;

public class HostSearchThread extends Thread {

    private final String ipaddress;
    private final int startIndex;
    private final int endIndex;

    private final List<Integer> blackListOcurrences = new LinkedList<>();

    private int checkedListsCount = 0;

    public HostSearchThread(String ipaddress, int startIndex, int endIndex) {
        this.ipaddress = ipaddress;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public void run() {

        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();

        for (int i = startIndex; i < endIndex; i++) {
            checkedListsCount++;
            if (skds.isInBlackListServer(i, ipaddress)) {
                blackListOcurrences.add(i);
            }
        }

    }

    public int getOcurrencesCount() {
        return blackListOcurrences.size();
    }

    public List<Integer> getBlackListOcurrences() {
        return blackListOcurrences;
    }

    /**
     * @return numero de listas negras que este hilo alcanzo a revisar.
     */
    public int getCheckedListsCount() {
        return checkedListsCount;
    }

}
