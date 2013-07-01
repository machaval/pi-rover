/**
 *
 */
package org.mule.test;

import java.util.concurrent.CountDownLatch;

public class Timer
{

    private long start;
    private long end;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    void start()
    {
        start = System.nanoTime();
    }

    void end()
    {
        end = System.nanoTime();
        countDownLatch.countDown();
    }

    long timeNano()
    {
        try
        {
            countDownLatch.await();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Error while measuring time");
        }
        return end - start;
    }

}
