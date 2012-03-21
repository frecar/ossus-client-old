package backupclient.commons;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CrossProcessLockTest {


                                           
    private CrossProcessLock lock = CrossProcessLock.instance;


    // just a simple simulation
    @Test
    public void testBasicTryLock() {
        boolean process1_attempt = lock.tryLock(0);
        assertTrue(process1_attempt);

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean process2_attempt = lock.tryLock(0);
                assertFalse(process2_attempt);            }
        }).start();

        try { Thread.sleep(50); }
        catch (InterruptedException e) { }
        lock.releaseLock();
        assertTrue(lock.tryLock(0));

    }

    @Test
    public void testTimeout() {
        boolean p1_attempt = lock.tryLock(0);
        assertTrue(p1_attempt);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try { Thread.sleep(500); }
                catch (InterruptedException e) { }
                lock.releaseLock();
                System.out.println("lock released");
            }
        }).start();

        boolean p2_attempt = lock.tryLock(10);
        assertTrue(p2_attempt);

    }

    @After
    public void teardown() {
        lock.releaseLock();
    }

}
