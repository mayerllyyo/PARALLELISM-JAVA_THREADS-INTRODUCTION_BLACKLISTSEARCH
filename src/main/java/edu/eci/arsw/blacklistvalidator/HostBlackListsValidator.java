/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import edu.eci.arsw.threads.BlackListCheckerThread;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    public static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int numThread) {
    
        List<Integer> blackListOccurrences = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();
        
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int totalServers = skds.getRegisteredServersCount();
        AtomicInteger totalOcurrences = new AtomicInteger(0);
    
        int serverPerThread = totalServers / numThread;
        int threadReminder = totalServers % numThread;
    
        int start = 0;
        for (int i = 0; i < numThread; i++) {
            int end = start + serverPerThread;
            if (i < threadReminder) {
                end++;
            }
    
            int threadStart = start;
            int threadEnd = end;
<<<<<<< HEAD

            AtomicBoolean stopFlag = new AtomicBoolean(false);  
            Runnable task = new BlackListCheckerThread(threadStart, threadEnd, ipaddress,
                               blackListOccurrences, totalOcurrences, stopFlag);

            Thread thread = new Thread(task);

            threads.add(thread);
            thread.start();
            start = end;
        }
    
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
        if (totalOcurrences.get() >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }
    
        LOG.log(Level.INFO, "Checked Black Lists: {0} of {1}",
                new Object[]{totalOcurrences.get(), skds.getRegisteredServersCount()});
    
        return blackListOccurrences;
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
}
