import static org.junit.Assert.*;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import org.fintx.org.bson.types.ObjectId;
import org.fintx.util.ObjectIdUtil;
import org.junit.Assert;
import org.junit.Test;

public class ObjectIdUtilTest {

	@Test
	public void test() {
		// check length
		String objectId16 = null;
		int count = 1000000000;
		for (int i = 0; i < count; i++) {
			objectId16 = ObjectIdUtil.objectId16();
			Assert.assertTrue("not 16 character id:" + objectId16, 16 == objectId16.length());
		}


		// check performance single thread
		long begin = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			objectId16 = ObjectIdUtil.objectId16();
		}
		long end = System.currentTimeMillis();
		System.out.println(
				"Base64 ID generation total milliseconds:" + (end - begin) + " total seconds:" + (end - begin) / 1000);
		System.out.println("Base64 ID generation QPMS:" + count / ((end - begin)));

		// check encode decode safety
		String objectId24 = null;
		ObjectId objectId = null;
		for (int i = 0; i < count; i++) {
			objectId = ObjectId.get();
			objectId24 = objectId.toHexString();
			objectId16 = Base64.getUrlEncoder().encodeToString(objectId.toByteArray());
			Assert.assertTrue("Unsafe Base64 encode and decode, original id:" + objectId24,
					objectId24.equals(new ObjectId(Base64.getUrlDecoder().decode(objectId16)).toHexString()));
		}

		// check unique safety
		count = 10000000;
		Set set = new HashSet(count + 2);
		for (int i = 0; i < count; i++) {

			objectId16 = ObjectIdUtil.objectId16();
			set.add(objectId16);
		}
		int size = set.size();
		set.clear();
		Assert.assertTrue("Duplicated key found in originalId set." + objectId16, size == count);

	}

}
