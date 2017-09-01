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

import org.junit.Assert;
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
    public int count = 2000000;
    public int threads = 4;
    public boolean error = false;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSingleThread() {

        for (int i = 0; i < threads; i++) {
            doTest().clear();
        }

    }

    @Test
    public void testMultiThread() {
        Set<String> totalSet = new HashSet<String>(threads * count);
        List<Set<String>> list = new ArrayList<Set<String>>();
        for (int i = 0; i < threads; i++) {
            Thread t1 = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Set<String> set = doTest();
                        synchronized (list) {
                            list.add(set);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        error = true;
                    }

                }

            });
            t1.start();
        }
        System.err.println("");
        while ((list.size() != threads) && !error) {
            System.err.print(list.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        System.err.println(list.size());
        for (int i = 0; i < threads; i++) {
            totalSet.addAll(list.get(0));
            list.get(0).clear();
            list.remove(0);
        }
        Assert.assertTrue(totalSet.size() == threads * count);
        // System.err.println("The id number sum compare result:" + (set.size() == threads * count));
    }

    public Set<String> doTest() {
        UniqueId uniqueId = UniqueId.get();
        String uniqueId20 = null;
        String uniqueId30 = null;
        // check length
        for (int i = 0; i < count; i++) {
            uniqueId20 = UniqueId.get().toBase64String();
            Assert.assertTrue("not 20 character id:" + uniqueId20, 20 == uniqueId20.length());
        }

        for (int i = 0; i < count; i++) {
            uniqueId30 = UniqueId.get().toHexString();
            Assert.assertTrue("not 30 character id:" + uniqueId30, 30 == uniqueId30.length());
        }

        // check performance
        long begin = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            uniqueId20 = UniqueId.get().toBase64String();
        }
        long end = System.currentTimeMillis();
        System.out.println("Base64 ID generation total count:" + count + " total milliseconds:" + (end - begin) + " total seconds:" + (end - begin) / 1000);
        System.out.println("Base64 ID generation QPS:" + count * 1000L / ((end - begin)));

        // check encode decode safety

        for (int i = 0; i < count; i++) {
            UniqueId temp = null;
            temp = UniqueId.get();
            uniqueId30 = temp.toHexString();
            uniqueId20 = temp.toBase64String();
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.isValid(UUID.randomUUID().toString().substring(0, 30)));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId20,
                    UniqueId.isValid(UUID.randomUUID().toString().substring(0, 20)));
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.isValid(UUID.randomUUID().toString().toUpperCase().substring(0, 30)));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId20,
                    UniqueId.isValid(UUID.randomUUID().toString().toUpperCase().substring(0, 20)));
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, UniqueId.isValid(UUID.randomUUID().toString()));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, UniqueId.isValid(
                    uniqueId30));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, UniqueId.isValid(uniqueId20));
            
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    temp.equals(UniqueId.fromHexString(uniqueId30)));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    temp.equals(UniqueId.fromBase64String(uniqueId20)));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    temp.equals(UniqueId.fromByteArray(temp.toByteArray())));

            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    uniqueId30.equals(UniqueId.fromBase64String(uniqueId20).toHexString()));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    uniqueId30.equals(UniqueId.fromByteArray(temp.toByteArray()).toHexString()));

            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    uniqueId20.equals(UniqueId.fromHexString(uniqueId30).toBase64String()));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    uniqueId20.equals(UniqueId.fromByteArray(temp.toByteArray()).toBase64String()));
            
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
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, temp.equals(temp));
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, temp.equals(null));
            Assert.assertFalse("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30, temp.equals("1243543246"));
            Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
                    UniqueId.fromHexString(uniqueId30).hashCode() == UniqueId.fromBase64String(uniqueId20).hashCode());
            UniqueId newTemp=UniqueId.get();
            Assert.assertFalse(UniqueId.fromHexString(uniqueId30).equals(newTemp));
            Assert.assertFalse(newTemp.equals(temp));
            Assert.assertTrue(newTemp.compareTo(temp)!=0);
            UniqueId.getCurrentCounter();
            UniqueId.getCurrentTimeStamp();
            UniqueId.getGeneratedMachineIdentifier();
            UniqueId.getGeneratedProcessIdentifier();
        }
        Assert.assertFalse(UniqueId.get().equals(uniqueId));
        Assert.assertTrue(UniqueId.get().compareTo(uniqueId)!=0);
        Set<String> set = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {

            uniqueId20 = UniqueId.get().toBase64String();
            set.add(uniqueId20);
        }
        int size = set.size();
        // set.clear();
        Assert.assertTrue("Duplicated key found in originalId set." + uniqueId20, size == count);

        Assert.assertTrue(uniqueId.compareTo(UniqueId.get()) != 0);
        return set;
    }

    @Test
    public void testException1() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument should not be null!");
        UniqueId.fromBase64String(null);

    }

    @Test
    public void testException2() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument should not be null!");
        UniqueId.fromHexString(null);

    }

    @Test
    public void testException3() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument should not be null!");
        UniqueId.fromByteArray(null);

    }

    @Test
    public void testException4() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("invalid hexadecimal representation of an UniqueId");
        UniqueId.fromBase64String(UUID.randomUUID().toString());

    }

    @Test
    public void testException5() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("invalid hexadecimal representation of an UniqueId");
        UniqueId.fromHexString(UUID.randomUUID().toString());

    }

    @Test
    public void testException6() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Argument need 15 bytes");
        UniqueId.fromByteArray(UUID.randomUUID().toString().getBytes());

    }

    @Test
    public void testException7() {
        thrown.expect(NullPointerException.class);
        UniqueId.get().compareTo(null);
    }

}
