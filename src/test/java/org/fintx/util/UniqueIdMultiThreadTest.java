package org.fintx.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class UniqueIdMultiThreadTest {
    public int count = 1000000;
    public int threads = 8;
    public boolean error = false;

    private List<Set<String>> list = new ArrayList<Set<String>>();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {

    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        Set<String> set = new HashSet<String>(threads * count);
        for (int i = 0; i < threads; i++) {
            Thread t1 = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        runtest();
                    } catch (Throwable t) {
                        t.printStackTrace();
                        error = true;
                    }

                }

            });
            t1.start();
        }
        while ((list.size() != threads) && !error) {
            System.err.print(list.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        System.err.println("");
        for (int i = 0; i < threads; i++) {
            set.addAll(list.get(0));
            list.remove(0);
        }
        System.err.println("The id number sum compare result:" + (set.size() == threads * count));
    }

    public Set<String> runtest() {
        // check length
        String uniqueId20 = null;

        for (int i = 0; i < count; i++) {
            uniqueId20 = UniqueId.get().toBase64String();
            Assert.assertTrue("not 20 character id:" + uniqueId20, 20 == uniqueId20.length());
        }

        // check performance single thread
        long begin = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            uniqueId20 = UniqueId.get().toBase64String();
        }
        long end = System.currentTimeMillis();
        System.out.println("Base64 ID generation total milliseconds:" + (end - begin) + " total seconds:" + (end - begin) / 1000);
        System.out.println("Base64 ID generation QPS:" + count * 1000L / ((end - begin + 1)));

        // check encode decode safety
        String uniqueId30 = null;
        UniqueId uniqueId = null;
        for (int i = 0; i < count; i++) {
            uniqueId = UniqueId.get();
            uniqueId30 = uniqueId.toHexString();
            uniqueId20 = uniqueId.toBase64String();
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    uniqueId30.equals(UniqueId.fromBase64String(uniqueId20).toHexString()));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getTimestamp() == UniqueId.fromBase64String(uniqueId20).getTimestamp());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getMachineIdentifier() == UniqueId.fromBase64String(uniqueId20).getMachineIdentifier());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getProcessIdentifier() == UniqueId.fromBase64String(uniqueId20).getProcessIdentifier());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getCounter() == UniqueId.fromBase64String(uniqueId20).getCounter());
        }
        Set<String> set = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {

            uniqueId20 = UniqueId.get().toBase64String();
            set.add(uniqueId20);
        }
        int size = set.size();
        // set.clear();
        Assert.assertTrue("Duplicated key found in originalId set." + uniqueId20, size == count);
        synchronized (list) {
            list.add(set);
        }

        return set;
    }
}
