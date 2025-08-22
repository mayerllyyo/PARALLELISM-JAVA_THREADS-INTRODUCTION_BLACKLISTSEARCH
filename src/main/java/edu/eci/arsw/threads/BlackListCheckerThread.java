package edu.eci.arsw.threads;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.eci.arsw.blacklistvalidator.*;
<<<<<<< HEAD
import edu.eci.arsw.spamkeywordsdatasource.*;
=======
import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
>>>>>>> dfea5ecb3dd65d519c608d985ea5f2cd3dfcc33f

public class BlackListCheckerThread implements Runnable {
    private int start;
    private int end;
    private String ipAddress;
    private List<Integer> sharedOccurrences;
    private AtomicInteger totalOcurrences;
    private HostBlacklistsDataSourceFacade skds;
<<<<<<< HEAD
    private AtomicBoolean stopFlag;



=======
>>>>>>> dfea5ecb3dd65d519c608d985ea5f2cd3dfcc33f

    public BlackListCheckerThread(int start, int end, String ipAddress,
        List<Integer> sharedOccurrences, AtomicInteger totalOcurrences, AtomicBoolean stopFlag) {
        this.start = start;
        this.end = end;
        this.ipAddress = ipAddress;
        this.sharedOccurrences = sharedOccurrences;
        this.totalOcurrences = totalOcurrences;
        this.skds = HostBlacklistsDataSourceFacade.getInstance();
        this.stopFlag = stopFlag;
    }

    @Override
    public void run() {
        for (int i = start; i < end && !stopFlag.get(); i++) {
            if (skds.isInBlackListServer(i, ipAddress)) {
                sharedOccurrences.add(i);
                int current = totalOcurrences.incrementAndGet();
                if (current >= HostBlackListsValidator.BLACK_LIST_ALARM_COUNT) {
                    stopFlag.set(true);
                    break;
                }
            }
        }
    }
}
