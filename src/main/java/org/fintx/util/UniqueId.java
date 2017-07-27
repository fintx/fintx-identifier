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

import java.io.Serializable;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * A globally unique identifier for objects.
 * </p>
 *
 * <p>
 * Consists of 12 bytes, divided as follows:
 * </p>
 * <table border="1">
 * <caption>ObjectID layout</caption>
 * <tr>
 * <td>0</td>
 * <td>1</td>
 * <td>2</td>
 * <td>3</td>
 * <td>4</td>
 * <td>5</td>
 * <td>6</td>
 * <td>7</td>
 * <td>8</td>
 * <td>9</td>
 * <td>10</td>
 * <td>11</td>
 * <td>12</td>
 * <td>13</td>
 * <td>14</td>
 * </tr>
 * <tr>
 * <td colspan="4">time</td>
 * <td colspan="6">machine</td>
 * <td colspan="2">pid</td>
 * <td colspan="3">inc</td>
 * </tr>
 * </table>
 *
 * <p>
 * Instances of this class are immutable.
 * </p>
 * <p>
 * Limitations:
 * </p>
 * <p>
 * ProcessId on os could not bigger then 65535. Only in one bundle of same JVM when using OSGI. Generated id number
 * could not more then about 16777215 per second per JVM. Id maybe (hardly) generate the same one every 69 years.
 * </p>
 *
 */
public final class UniqueId implements Comparable<UniqueId>, Serializable {

    private static final long serialVersionUID = 3670079982654483072L;

    private static final int LOW_ORDER_THREE_BYTES = 0x00ffffff;

    private static final long MACHINE_IDENTIFIER;
    private static final short PROCESS_IDENTIFIER;

    private static final AtomicInteger NEXT_COUNTER = new AtomicInteger(new SecureRandom().nextInt());
    // to prevent time change back maybe when use time server to correct the machine time.
    private static volatile AtomicLong LAST_TIMESTAMP = new AtomicLong(0);

    private static final char[] HEX_CHARS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private final int timestamp;
    private final long machineIdentifier;
    private final short processIdentifier;
    private final int counter;

    /**
     * Gets a new object id.
     *
     * @return the new id
     */
    public static UniqueId get() {
        return new UniqueId(dateToTimestampSeconds(new Date()), MACHINE_IDENTIFIER, PROCESS_IDENTIFIER, NEXT_COUNTER.getAndIncrement(), false);
    }

    /**
     * Checks if a string could be an {@code UniqueId}.
     *
     * @param hexString a potential UniqueId as a String.
     * @return whether the string could be an object id
     * @throws IllegalArgumentException if hexString is null
     */
    public static boolean isValid(final String idString) {
        if (idString == null) {
            throw new IllegalArgumentException();
        }

        int len = idString.length();
        if (len == 30) {
            for (int i = 0; i < len; i++) {
                char c = idString.charAt(i);
                if (c >= '0' && c <= '9') {
                    continue;
                }
                if (c >= 'a' && c <= 'f') {
                    continue;
                }
                if (c >= 'A' && c <= 'F') {
                    continue;
                }
                return false;
            }
            return true;
        } else if (len == 20) {
            for (int i = 0; i < len; i++) {
                char c = idString.charAt(i);
                if (c >= '0' && c <= '9') {
                    continue;
                }
                if (c >= 'a' && c <= 'z') {
                    continue;
                }
                if (c >= 'A' && c <= 'Z') {
                    continue;
                }
                if (c == '_' || c == '-') {
                    continue;
                }
                return false;
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     * Gets the generated machine identifier.
     *
     * @return an int representing the machine identifier
     */
    public static long getGeneratedMachineIdentifier() {
        return MACHINE_IDENTIFIER;
    }

    /**
     * Gets the generated process identifier.
     *
     * @return the process id
     */
    public static int getGeneratedProcessIdentifier() {
        return PROCESS_IDENTIFIER;
    }

    /**
     * Gets the current value of the auto-incrementing counter.
     *
     * @return the current counter value.
     */
    public static int getCurrentCounter() {
        return NEXT_COUNTER.get();
    }

    private UniqueId(final int timestamp, final long machineIdentifier, final short processIdentifier, final int counter,
            final boolean checkCounter) {
        long current = LAST_TIMESTAMP.get();

        if (((timestamp & 0xffffffffL) == (current & 0xffffffffL))) {
        // @formatter:off
        // mostly   
        // @formatter:on

            // Do nothing

        } else if (((timestamp & 0xffffffffL) > (current & 0xffffffffL))) {
        // @formatter:off
        // once per second or less
        // @formatter:on    
            synchronized (LAST_TIMESTAMP) {
                if ((timestamp & 0xffffffffL) > (LAST_TIMESTAMP.get() & 0xffffffffL)) {
                    current = LAST_TIMESTAMP.addAndGet((timestamp & 0xffffffffL) - (LAST_TIMESTAMP.get() & 0xffffffffL));
                }
            }

        } else {
        // @formatter:off
        //((timestamp & 0xffffffffL) < (current & 0xffffffffL))
        // hardly
        // @formatter:on    
            synchronized (LAST_TIMESTAMP) {
                if (((LAST_TIMESTAMP.get() & 0xffffffffL) - (timestamp & 0xffffffffL)) == 1L) {
                // @formatter:off
                // LAST_TIMESTAMP increased after timestamp generated
                // @formatter:on  
                    // Do nothing
                } else if (((LAST_TIMESTAMP.get() & 0xffffffffL) - (timestamp & 0xffffffffL)) >= 0x7fffffffL) {
                    // timestamp is in the new round of zero to 0xffffffffL. 0x7fffffffL is half of 0xffffffffL.
                    // A round is about 69 years, so the gap between last timestamp in the last round and new timestamp
                    // in this round will not less then 34 years
                    LAST_TIMESTAMP.set(timestamp & 0xffffffffL);
                    current = (timestamp & 0xffffffffL);
                } else {
                    System.err.println(((LAST_TIMESTAMP.get() & 0xffffffffL) - (timestamp & 0xffffffffL)));
                    System.err.println(timestamp);
                    System.err.println(current);
                    System.err.println(LAST_TIMESTAMP.get());
                    throw new IllegalArgumentException(
                            "The timestamp must not be less then the timestamp last time. (Maybe the machine correct time using time server).");
                }
            }
        }
        if (((machineIdentifier >> 32) & 0xff000000) != 0) {
            throw new IllegalArgumentException("The machine identifier must be between 0 and 28139015110655 (it must fit in six bytes).");
        }
        if (checkCounter && ((counter & 0xff000000) != 0)) {
            throw new IllegalArgumentException("The counter must be between 0 and 16777215 (it must fit in three bytes).");
        }
        this.timestamp = (int) current;
        this.machineIdentifier = machineIdentifier;
        this.processIdentifier = processIdentifier;
        this.counter = counter & LOW_ORDER_THREE_BYTES;
    }

    /**
     * Constructs a new instance from a 30-byte hexadecimal string representation.
     *
     * @param hexString the string to convert
     * @throws IllegalArgumentException if the string is not a valid hex string representation of an UniqueId
     */
    public static UniqueId fromHexString(final String hexString) {
        return new UniqueId(parseHexString(hexString));
    }

    /**
     * Constructs a new instance from a 30-byte hexadecimal string representation.
     *
     * @param hexString the string to convert
     * @throws IllegalArgumentException if the string is not a valid hex string representation of an UniqueId
     */
    public static UniqueId fromBase64String(final String base64String) {
        return new UniqueId(parseBase64String(base64String));
    }

    /**
     * Constructs a new instance from the given byte array
     *
     * @param bytes the byte array
     * @throws IllegalArgumentException if array is null or not of length 12
     */
    private UniqueId(final byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException();
        }
        if (bytes.length != 15) {
            throw new IllegalArgumentException("need 15 bytes");
        }
        timestamp = bytes2int(Arrays.copyOfRange(bytes, 0, 4));
        machineIdentifier = bytes2long(Arrays.copyOfRange(bytes, 4, 10));
        processIdentifier = (short) bytes2int(Arrays.copyOfRange(bytes, 10, 12));
        counter = bytes2int(Arrays.copyOfRange(bytes, 12, 15));
    }

    /**
     * Convert to a byte array. Note that the numbers are stored in big-endian order.
     *
     * @return the byte array
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[15];
        byte[] temp = int2bytes(timestamp);
        bytes[0] = temp[0];
        bytes[1] = temp[1];
        bytes[2] = temp[2];
        bytes[3] = temp[3];
        temp = long2bytes(machineIdentifier);
        bytes[4] = temp[2];
        bytes[5] = temp[3];
        bytes[6] = temp[4];
        bytes[7] = temp[5];
        bytes[8] = temp[6];
        bytes[9] = temp[7];
        temp = int2bytes(processIdentifier);
        bytes[10] = temp[2];
        bytes[11] = temp[3];
        temp = int2bytes(counter);
        bytes[12] = temp[1];
        bytes[13] = temp[2];
        bytes[14] = temp[3];
        return bytes;
    }

    /**
     * Gets the timestamp (number of seconds since the Unix epoch).
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        // To unsigned int
        return timestamp & 0x0ffffffffL;
    }

    /**
     * Gets the machine identifier.
     *
     * @return the machine identifier
     */
    public long getMachineIdentifier() {
        return machineIdentifier;
    }

    /**
     * Gets the process identifier.
     *
     * @return the process identifier
     */
    public int getProcessIdentifier() {
        return processIdentifier & 0x0ffff;
    }

    /**
     * Gets the counter.
     *
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Gets the timestamp as a {@code Date} instance.
     *
     * @return the Date
     */
    public Date getDate() {
        return getDate(new Date().getTime());
    }

    /**
     * Gets the timestamp as a {@code Date} instance.
     * 
     * @param timestamp of now
     * @return the Date
     */
    private Date getDate(long now) {
        // Timestamp is in this round of zero to 0xffffffffL scope.
        if ((timestamp & 0xffffffffL) <= (now / 1000L % 0xffffffffL)) {
            return new Date((((now / 1000L / 0xffffffffL) * 0xffffffffL + (timestamp & 0xffffffffL)) + now / 1000L / 0xffffffffL) * 1000L);
        // @formatter:off
        // Timestamp is in last round of zero to 0xffffffffL scope.
        // @formatter:on
        } else if (((timestamp & 0xffffffffL) + now / 1000L / 0xffffffffL) - (now / 1000L % 0xffffffffL) >= 0x7fffffffL) {
            return new Date((((now / 1000L / 0xffffffffL) - 1) * 0xffffffffL + (timestamp & 0xffffffffL) + (now / 1000L / 0xffffffffL) - 1) * 1000L);
        // @formatter:off
        // Timestamp is in this round of zero to 0xffffffffL scope but bigger then now
        // @formatter:on
        } else {
            throw new IllegalArgumentException(
                    "The timestamp must not be less then the timestamp now. (Maybe the machine correct time using time server).");
        }

    }

    /**
     * Converts this instance into a 30-byte hexadecimal string representation.
     *
     * @return a string representation of the UniqueId in hexadecimal format
     */
    public String toHexString() {
        char[] chars = new char[30];
        int i = 0;
        for (byte b : toByteArray()) {
            chars[i++] = HEX_CHARS[b >> 4 & 0xF];
            chars[i++] = HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }

    /**
     * Converts this instance into a 30-byte hexadecimal string representation.
     *
     * @return a string representation of the UniqueId in hexadecimal format
     */
    private static String toHexString(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        int i = 0;
        for (byte b : bytes) {
            chars[i++] = HEX_CHARS[b >> 4 & 0xF];
            chars[i++] = HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }

    /**
     * Converts this instance into a 30-byte hexadecimal string representation.
     *
     * @return a string representation of the UniqueId in hexadecimal format
     */
    public String toBase64String() {
        return Base64.getUrlEncoder().encodeToString(toByteArray());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UniqueId UniqueId = (UniqueId) o;

        if (counter != UniqueId.counter) {
            return false;
        }
        if (machineIdentifier != UniqueId.machineIdentifier) {
            return false;
        }
        if (processIdentifier != UniqueId.processIdentifier) {
            return false;
        }
        if (timestamp != UniqueId.timestamp) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = timestamp;
        result = 31 * result + (int) machineIdentifier;
        result = 31 * result + (int) processIdentifier;
        result = 31 * result + counter;
        return result;
    }

    @Override
    public int compareTo(final UniqueId other) {
        if (other == null) {
            throw new NullPointerException();
        }

        byte[] byteArray = toByteArray();
        byte[] otherByteArray = other.toByteArray();
        for (int i = 0; i < 12; i++) {
            if (byteArray[i] != otherByteArray[i]) {
                return ((byteArray[i] & 0xff) < (otherByteArray[i] & 0xff)) ? -1 : 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return toHexString();
    }

    static {
        try {
            MACHINE_IDENTIFIER = createMachineIdentifier();
            PROCESS_IDENTIFIER = createProcessIdentifier();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static long createMachineIdentifier() {
        byte[] mac = null;
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                if (!ni.isLoopback()) {
                    mac = ni.getHardwareAddress();
                }
                ;
                // ?? mac[1] != (byte) 0xff it is from http://johannburkard.de/software/uuid/
                if (mac != null && mac.length == 6 && mac[1] != (byte) 0xff) {
                    break;
                } else {
                    continue;
                }
            }
        } catch (Throwable t) {
        }
        if (mac != null && mac.length == 6 && mac[1] != (byte) 0xff) {
            return bytes2long(mac);
        } else {
            throw new RuntimeException("MAC address is not correct:" + toHexString(mac));
        }

    }

    // Creates the process identifier. This does not have to be unique per class loader because NEXT_COUNTER will
    // provide the uniqueness.
    private static short createProcessIdentifier() {
        short processId;
        try {
            String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            if (processName.contains("@")) {
                processId = (short) Integer.parseInt(processName.substring(0, processName.indexOf('@')));
            } else {
                throw new Throwable("Process name:'" + processName + "' is invalid!");
            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return processId;
    }

    private static byte[] parseHexString(final String s) {
        if (!isValid(s)) {
            throw new IllegalArgumentException("invalid hexadecimal representation of an UniqueId: [" + s + "]");
        }

        byte[] b = new byte[15];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }

    private static byte[] parseBase64String(final String s) {
        if (!isValid(s)) {
            throw new IllegalArgumentException("invalid hexadecimal representation of an UniqueId: [" + s + "]");
        }

        return Base64.getUrlDecoder().decode(s);
    }

    private static int dateToTimestampSeconds(final Date time) {
        return (int) ((time.getTime() / 1000L) & 0xffffffffL);
    }

    private static byte[] int2bytes(int num) {
        byte[] byteNum = new byte[4];
        for (int ix = 0; ix < 4; ++ix) {
            int offset = 32 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    private static int bytes2int(byte[] byteNum) {
        if (byteNum.length > 4) {
            throw new RuntimeException("byteNum is too long for a int type:" + byteNum.length);
        }
        int num = 0;
        for (int ix = 0; ix < byteNum.length; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    private static byte[] long2bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    private static long bytes2long(byte[] byteNum) {
        for (long lo = 0; lo < Long.MIN_VALUE; lo++) {

        }
        if (byteNum.length > 8) {
            throw new RuntimeException("byteNum is too long for a long type:" + byteNum.length);
        }
        long num = 0;
        for (int ix = 0; ix < byteNum.length; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);// byte become 64bit since the index 1 with higher bit 1? & 0xff make higher bit
                                        // 0
        }
        return num;
    }

    // public static void main(String[] args) {
    // //Full test!!!!
    // UniqueId uniqueId = null;
    // for (long lo = new Date().getTime(); lo > 0 && lo < Long.MAX_VALUE; lo += 100) {
    // uniqueId =
    // new UniqueId(dateToTimestampSeconds(new Date(lo)), MACHINE_IDENTIFIER, PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // if (!uniqueId.getDate(lo).toString().equals(new Date(lo / 1000L * 1000L).toString())) {
    // System.err.println("XXXXXXXXXXXXXXXXXXXXXXXXXXLo:" + lo);
    // System.err.println(uniqueId.getDate(lo).toString());
    // System.err.println(new Date(lo / 1000L * 1000L).toString());
    // throw new RuntimeException();
    // }
    // if (lo / 100 % 1000000 == 0) {
    // System.err.println(lo);
    // System.err.println(uniqueId.getDate(lo).toString());
    // }
    // }

    // //Function test!!!!!!
    // System.err.println("-------------------------------1");
    // long l = 12345678901223322L;
    // System.err.println(Long.toBinaryString(l));
    // byte[] bytes = long2bytes(l);
    // l = bytes2long(bytes);
    // System.err.println(Long.toBinaryString(l));
    // int i = 1500617485;
    // System.err.println(Integer.toBinaryString(i));
    // bytes = int2bytes(i);
    // l = bytes2int(bytes);
    // System.err.println(Long.toBinaryString(i));

    // //Extreme condition test!!!!!!!
    // System.err.println("-------------------------------2");
    // UniqueId uid = new UniqueId(dateToTimestampSeconds(new Date((Integer.MAX_VALUE - 1) * 1000L)),
    // MACHINE_IDENTIFIER, PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println(uid.getDate((Integer.MAX_VALUE - 1) * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date((Integer.MAX_VALUE - 1) * 1000L).toString());
    // System.err.println("-------------------------------3");
    // uid = new UniqueId(dateToTimestampSeconds(new Date(Integer.MAX_VALUE * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println(uid.getDate(Integer.MAX_VALUE * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date(Integer.MAX_VALUE * 1000L).toString());
    // System.err.println("-------------------------------4");
    // uid = new UniqueId(dateToTimestampSeconds(new Date((Integer.MAX_VALUE + 1L) * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println((Integer.MAX_VALUE + 1L) * 1000L);
    // System.err.println(uid.getDate((Integer.MAX_VALUE + 1L) * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date((Integer.MAX_VALUE + 1L) * 1000L).toString());
    // System.err.println("-------------------------------5");
    // uid = new UniqueId(dateToTimestampSeconds(new Date((Integer.MAX_VALUE + 2L) * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println((Integer.MAX_VALUE + 2L) * 1000L);
    // System.err.println(uid.getDate((Integer.MAX_VALUE + 2L) * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date((Integer.MAX_VALUE + 2L) * 1000L).toString());
    // System.err.println("-------------------------------6");
    // System.err.println((new Date(0xfffffffeL * 1000L).getTime() / 1000L) & 0xffffffffL);
    // System.err.println(dateToTimestampSeconds(new Date(0xfffffffeL * 1000L)));
    // System.err.println(Long.toBinaryString(dateToTimestampSeconds(new Date(0xfffffffeL * 1000L))));
    // System.err.println(Integer.toBinaryString((int) dateToTimestampSeconds(new Date(0xfffffffeL * 1000L))));
    // uid = new UniqueId(dateToTimestampSeconds(new Date(0xfffffffeL * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println(uid.getDate(0xfffffffeL * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date(0xfffffffeL * 1000L).toString());
    // System.err.println("-------------------------------7");
    // System.err.println((new Date(0xfffffffeL * 1000L).getTime() / 1000L) & 0xffffffffL);
    // System.err.println(dateToTimestampSeconds(new Date(0xffffffffL * 1000L)));
    // System.err.println(Long.toBinaryString(dateToTimestampSeconds(new Date(0xffffffffL * 1000L))));
    // System.err.println(Integer.toBinaryString((int) dateToTimestampSeconds(new Date(0xffffffffL * 1000L))));
    // uid = new UniqueId(dateToTimestampSeconds(new Date(0xffffffffL * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println(uid.getDate(0xffffffffL * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date(0xffffffffL * 1000L).toString());
    // System.err.println("-------------------------------8");
    // uid = new UniqueId(dateToTimestampSeconds(new Date(0xffffffffL * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println(uid.getDate(0x100000000L * 1000L).toString());
    // System.err.println(uid.getDate(0x100000001L * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date(0xffffffffL * 1000L).toString());
    // System.err.println("-------------------------------9");
    // uid = new UniqueId(dateToTimestampSeconds(new Date(0x100000000L * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println(0x100000000L * 1000L);
    // System.err.println(uid.getDate(0x100000000L * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date(0x100000000L * 1000L).getTime());
    // System.err.println(new Date(0x100000000L * 1000L).toString());
    // System.err.println("-------------------------------a");
    // uid = new UniqueId(dateToTimestampSeconds(new Date(0x100000001L * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println(0x100000001L * 1000L);
    // System.err.println(uid.getDate(0x100000001L * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date(0x100000001L * 1000L).getTime());
    // System.err.println(new Date(0x100000001L * 1000L).toString());
    // System.err.println("-------------------------------b");
    // uid = new UniqueId(dateToTimestampSeconds(new Date(0x1fffffff1L * 1000L)), MACHINE_IDENTIFIER,
    // PROCESS_IDENTIFIER,
    // NEXT_COUNTER.getAndIncrement(), false);
    // System.err.println(0x100000001L * 1000L);
    // System.err.println(uid.getDate(0x200000000L * 1000L).toString());
    // System.err.println(uid.getTimestamp());
    // System.err.println(new Date(0x1fffffff1L * 1000L).toString());
    // System.err.println(new Date(0x200000000L * 1000L).toString());
    // }
}
