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

import java.util.Base64;

import org.fintx.org.bson.types.ObjectId;

/**
 * @author bluecreator(qiang.x.wang@gmail.com)
 *
 *         Compress 24 character ObjectId to 16 character using Base64 encoding.
 */
public class ObjectIdUtil {

	private ObjectIdUtil() {
		throw new AssertionError("No ObjectIdUtil instances for you!");
	}

	public static String objectId16() {
		ObjectId objectId = ObjectId.get();
		return Base64.getUrlEncoder().encodeToString(objectId.toByteArray());
	}

	public static long getDate(String objectId16) {
		byte[] byObjectId = Base64.getUrlDecoder().decode(objectId16);
		ObjectId objectId = new ObjectId(byObjectId);
		return objectId.getTimestamp() * 1000L;
	}

	public static int getMachineId(String objectId16) {
		byte[] byObjectId = Base64.getUrlDecoder().decode(objectId16);
		ObjectId objectId = new ObjectId(byObjectId);
		return objectId.getMachineIdentifier();
	}

	public static int getProcessId(String objectId16) {
		byte[] byObjectId = Base64.getUrlDecoder().decode(objectId16);
		ObjectId objectId = new ObjectId(byObjectId);
		return objectId.getProcessIdentifier();
	}

	public static int getCounter(String objectId16) {
		byte[] byObjectId = Base64.getUrlDecoder().decode(objectId16);
		ObjectId objectId = new ObjectId(byObjectId);
		return objectId.getCounter();
	}

}