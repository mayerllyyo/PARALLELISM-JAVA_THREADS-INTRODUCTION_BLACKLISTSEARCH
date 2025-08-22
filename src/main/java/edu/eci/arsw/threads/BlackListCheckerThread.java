package edu.eci.arsw.threads;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import edu.eci.arsw.blacklistvalidator.*;
import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class BlackListCheckerThread implements Runnable {
    private int start;
    private int end;
    private String ipAddress;
    private List<Integer> sharedOccurrences;
    private AtomicInteger totalOcurrences;
    private HostBlacklistsDataSourceFacade skds;

    public BlackListCheckerThread(int start, int end, String ipAddress,
                                List<Integer> sharedOccurrences, AtomicInteger totalOcurrences) {
        this.start = start;
        this.end = end;
        this.ipAddress = ipAddress;
        this.sharedOccurrences = sharedOccurrences;
        this.totalOcurrences = totalOcurrences;
        this.skds = HostBlacklistsDataSourceFacade.getInstance();
    }

    @Override
    public void run() {
        for (int i = start; i < end && totalOcurrences.get() < HostBlackListsValidator.BLACK_LIST_ALARM_COUNT; i++) {
            if (skds.isInBlackListServer(i, ipAddress)) {
                sharedOccurrences.add(i);
                totalOcurrences.incrementAndGet();
            }
        }
    }
}
