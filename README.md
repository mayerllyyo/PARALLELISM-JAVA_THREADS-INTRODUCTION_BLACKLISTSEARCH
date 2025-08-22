# **Introduction to Parallelism Exercise - Threads - BlackListSearch Case**

## **Description**

This exercise contains an introduction to threaded programming in Java, as well as its application to a specific case.

## **Authors**

- **Santiago Hurtado Martínez** [SantiagoHM20](https://github.com/SantiagoHM20)

- **Mayerlly Suárez Correa** [mayerllyyo](https://github.com/mayerllyyo)


#**Part I - Introduction to Threads in Java**

1. CountThread Class

    Complete the CountThread class so that it defines the life cycle of a thread that prints numbers between A and B.

    ```java
    public class CountThread extends Thread {
        private int startNum, endNum;

        public CountThread(int startNum, int endNum) {
            this.startNum = startNum;
            this.endNum = endNum;
        }

        @Override
        public void run() {
            for (int i = startNum; i <= endNum; i++) {
                System.out.printf("Thread %s: %d%n", 
                    Thread.currentThread().getName(), i);
            }
        }
    }
    ```

2. CountMainThreads Class
    1. Create three threads of type CountThread, assigning the first the range [0..99], the second [99..199], and the third [200..299].
    2. Start all three threads with 'start()'.
    3. Run them and review the output on the screen.
    4. Change the start with 'start()' to 'run()'. How does the output change? Why?

    **Implementation with start() method:**

    ```java
    public static void main(String a[]){
        CountThread ct1 = new CountThread(0, 99);
        CountThread ct2 = new CountThread(99, 199);
        CountThread ct3 = new CountThread(199, 299);

        ct1.start();
        ct2.start(); 
        ct3.start();    
        } 
    ```
    Output analysis using start() method:
    - Behavior: The threads execute concurrently (in parallel)
    - Output: Numbers from different threads are interleaved randomly
    - Execution: Each thread runs in its own execution context
    - Example output:
    ![](img/output_with_start.png)


    **Implementation with run() method:**

    ```java
    public static void main(String a[]){
        CountThread ct1 = new CountThread(0, 99);
        CountThread ct2 = new CountThread(99, 199);
        CountThread ct3 = new CountThread(199, 299);

        ct1.run();
        ct2.run(); 
        ct3.run();    
        } 
    ```
    Output analysis using run() method:
    - Behavior: The methods execute sequentially (one after another)
    - Output: Numbers are printed in order: first 0-99, then 99-199, then 200-299
    - Execution: All code runs in the main thread
    - Example output:
    ![](img/output_with_run.png)

    **Key differences**
    | Method | Execution | Thread Context | Performance | Use Case |
    |--------|-----------|----------------|-------------|----------|
    | start() | Concurrent | Separate threads | Potentially faster | True multithreading |
    | run() | Sequential | Main thread only | Single-threaded | Method call only |

    **Why this happens**
    - **start()**: Creates a new thread and calls run() in that new thread context
    - **run()**: Simply calls the method directly in the current thread (main thread)

    The start() method is the proper way to begin thread execution, while calling run() directly defeats the purpose of multithreading

#**Part II - Black List Search Exercise**

For automatic security software, a component is being developed to validate IP addresses against several thousand known blacklists (of malicious hosts) and report those that exist in at least five of these lists. This component is designed according to the following diagram, where: HostBlackListsDataSourceFacade is a class that provides a 'facade' for querying any of the N registered blacklists (method 'isInBlacklistServer'), and also allows reporting to a local database when an IP address is considered hazardous. This class is NOT MODIFIABLE, but it is known to be 'Thread-Safe'.

HostBlackListsValidator is a class that provides the 'checkHost' method, which, through the 'HostBlackListDataSourceFacade' class, validates a specific host against each of the blacklists. This method considers the policy that if a HOST is found on at least five blacklists, it will be recorded as 'untrustworthy', or 'trustworthy' otherwise. Additionally, it will return the list of the numbers of the 'blacklists' where the HOST was found.

![image](img/Model.png)

When using the module, the evidence that the registration was made as 'trustworthy' or 'not trustworthy' is given by the LOG messages: 

INFO: HOST 205.24.34.55 Reported as trustworthy 

INFO: HOST 205.24.34.55 Reported as NOT trustworthy 

The provided test program (Main) takes only a few seconds to analyze and report the provided address (200.24.34.55), as it is registered more than five times in the first servers, which means it doesn’t need to check all of them. However, searching in cases where there are NO reports, or where they are scattered across thousands of blacklists, takes quite a bit of time. 

This, like any search method, can be viewed as a embarrassingly parallel problem, as there are no dependencies between one partition of the problem and another. To 'refactor' this code and take advantage of the multi-core capability of the CPU of the machine, do the following:

Create a class of type Thread that represents the life cycle of a thread that searches for a segment of the available server set. Add a method to that class that allows you to 'ask' the instances of it (the threads) how many occurrences of malicious servers it has found or has found.

```java

package edu.eci.arsw.threads;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import edu.eci.arsw.blacklistvalidator.*;
import edu.eci.arsw.spamkeywordsdatasource.*;

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
```

Add an integer parameter N to the 'checkHost' method, corresponding to the number of threads among which the search will be performed (remember to take into account whether N is even or odd!). Modify the code of this method so that it divides the search space into the indicated N parts, and parallelizes the search through N threads. Ensure that this function waits until all N threads finish solving their respective sub-problems, adds the occurrences found by each thread to the list returned by the method, and then calculates (by summing the total occurrences found by each thread) whether the number of occurrences is greater than or equal to BLACK_LIST_ALARM_COUNT. If this is the case, the host MUST be reported as reliable or unreliable at the end, and show the list with the numbers of the respective blacklists. To achieve this 'wait' behavior, check the join method of the Java concurrency API. Also, consider:

Within the method checkHost, the LOG must maintain information, before returning the result, about the number of blacklists checked vs. the total number of blacklists (line 60). It must be ensured that this information is accurate under the new proposed parallel processing scheme.

It is known that HOST 202.24.34.55 is reported in blacklists in a more scattered manner, and that host 212.24.24.55 is NOT on any blacklist.

```java
package edu.eci.arsw.blacklistvalidator;


public class HostBlackListsValidator {

    public static final int BLACK_LIST_ALARM_COUNT=5;
    
   
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
    
            Runnable task = new BlackListCheckerThread(threadStart, threadEnd, ipaddress, blackListOccurrences, totalOcurrences);
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
```


