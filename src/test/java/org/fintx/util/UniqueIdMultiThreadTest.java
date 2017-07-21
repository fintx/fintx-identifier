package org.fintx.util;

import static org.junit.Assert.*;

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
	public static int count = 1000000;
	public static int threads = 10;

	private List<Set> list = new ArrayList<Set>();

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
		Set set = new HashSet(threads * count);
		for (int i = 0; i < threads; i++) {
			Thread t1 = new Thread(new Runnable() {

				@Override
				public void run() {
					runtest();
				}

			});
			t1.start();
		}
		while (list.size() != threads) {
			System.err.print(list.size());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.err.println("");
		for (Set s : list) {
			set.addAll(s);
		}
		System.err.println("--------------" + (set.size() == threads * count));
	}

	public Set runtest() {
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
		System.out.println(
				"Base64 ID generation total milliseconds:" + (end - begin) + " total seconds:" + (end - begin) / 1000);
		System.out.println("Base64 ID generation QPMS:" + count / ((end - begin)));

		// check encode decode safety
		String uniqueId30 = null;
		UniqueId uniqueId = null;
		for (int i = 0; i < count; i++) {
			uniqueId = UniqueId.get();
			uniqueId30 = uniqueId.toHexString();
			uniqueId20 = uniqueId.toBase64String();
			Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + i + " " + uniqueId30,
					uniqueId30.equals(UniqueId.fromBase64String(uniqueId20).toHexString()));
		}
		Set set = new HashSet(count);
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
