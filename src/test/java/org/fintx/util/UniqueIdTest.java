/**
 *  Copyright 2017 FinTx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fintx.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author bluecreator(qiang.x.wang@gmail.com)
 *
 */
public class UniqueIdTest {
    public int count = 1000000;
    public int threads = 4;
    public boolean error = false;
    private List<Set<String>> list = new ArrayList<Set<String>>();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSingleThread() {
        // check null safety
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument should not be null!");
        UniqueId.fromBase64String(null);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument should not be null!");
        UniqueId.fromHexString(null);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument should not be null!");
        UniqueId.fromByteArray(null);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("invalid hexadecimal representation of an UniqueId");
        UniqueId.fromBase64String(UUID.randomUUID().toString());
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("invalid hexadecimal representation of an UniqueId");
        UniqueId.fromHexString(UUID.randomUUID().toString());
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument need 15 bytes");
        UniqueId.fromByteArray(UUID.randomUUID().toString().getBytes());
        thrown.expect(NullPointerException.class);
        UniqueId uniqueId=UniqueId.get();
        uniqueId.compareTo(UniqueId.get());
        uniqueId.compareTo(null);
        uniqueId.equals(null);
       try {
        Thread.sleep(1001);
    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
       uniqueId.compareTo(UniqueId.get());
        runtest();
    }

    @Test
    public void testMultiThread() {
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
        Assert.assertTrue(set.size() == threads * count);
        // System.err.println("The id number sum compare result:" + (set.size() == threads * count));
    }

    public Set<String> runtest() {
        // check length
        String uniqueId20 = null;

        for (int i = 0; i < count; i++) {
            uniqueId20 = UniqueId.get().toBase64String();
            Assert.assertTrue("not 20 character id:" + uniqueId20, 20 == uniqueId20.length());
        }

        // check performance
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
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.isValid(UUID.randomUUID().toString().substring(0, 30)));
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, UniqueId.isValid(UUID.randomUUID().toString()));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, UniqueId.isValid(uniqueId30));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, UniqueId.isValid(uniqueId20));

            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    uniqueId30.equals(UniqueId.fromBase64String(uniqueId20).toHexString()));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    uniqueId30.equals(UniqueId.fromByteArray(uniqueId.toByteArray()).toHexString()));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getTimestamp() == UniqueId.fromBase64String(uniqueId20).getTimestamp());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.getCurrentTimeStamp() >= UniqueId.fromBase64String(uniqueId20).getTimestamp());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getMachineIdentifier() == UniqueId.fromBase64String(uniqueId20).getMachineIdentifier());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.getGeneratedMachineIdentifier() == UniqueId.fromBase64String(uniqueId20).getMachineIdentifier());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getProcessIdentifier() == UniqueId.fromBase64String(uniqueId20).getProcessIdentifier());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.getGeneratedProcessIdentifier() == UniqueId.fromBase64String(uniqueId20).getProcessIdentifier());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getCounter() == UniqueId.fromBase64String(uniqueId20).getCounter());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).getDate().getTime() == UniqueId.fromBase64String(uniqueId20).getDate().getTime());
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).equals(UniqueId.fromBase64String(uniqueId20)));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).compareTo(UniqueId.fromBase64String(uniqueId20)) == 0);
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).toString().equals(UniqueId.fromBase64String(uniqueId20).toBase64String()));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, uniqueId.equals(uniqueId));
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, uniqueId.equals(null));
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, uniqueId.equals("1243543246"));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).hashCode() == UniqueId.fromBase64String(uniqueId20).hashCode());
            Assert.assertFalse(UniqueId.fromHexString(uniqueId30).equals(UniqueId.get()));
            
            UniqueId.getCurrentCounter();
            UniqueId.getCurrentTimeStamp();
            UniqueId.getGeneratedMachineIdentifier();
            UniqueId.getGeneratedProcessIdentifier();
            
            uniqueId.getCurrentCounter();
            uniqueId.getCurrentTimeStamp();
            uniqueId.getGeneratedMachineIdentifier();
            uniqueId.getGeneratedProcessIdentifier();
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
