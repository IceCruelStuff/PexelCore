// @formatter:off
/*
 * Pexel Project - Minecraft minigame server platform. 
 * Copyright (C) 2014 Matej Kormuth <http://www.matejkormuth.eu>
 * 
 * This file is part of Pexel.
 * 
 * Pexel is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Pexel is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
// @formatter:on
package eu.matejkormuth.pexel.network;

import java.nio.charset.Charset;

public class ByteUtils {
    public static void writeInt(final int i, final byte[] array, final int index) {
        array[index + 0] = (byte) (i >> 24);
        array[index + 1] = (byte) (i >> 16);
        array[index + 2] = (byte) (i >> 8);
        array[index + 3] = (byte) (i >> 0);
    }
    
    public static void writeLong(final long i, final byte[] array, final int index) {
        array[index + 0] = (byte) (i >> 56);
        array[index + 1] = (byte) (i >> 48);
        array[index + 2] = (byte) (i >> 40);
        array[index + 3] = (byte) (i >> 32);
        array[index + 4] = (byte) (i >> 24);
        array[index + 5] = (byte) (i >> 16);
        array[index + 6] = (byte) (i >> 8);
        array[index + 7] = (byte) (i >> 0);
    }
    
    public static byte[] merge(final byte[] array1, final byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
    
    public static byte[] toByteArray(final String string) {
        return string.getBytes(Charset.defaultCharset());
    }
    
    public static String toString(final byte[] array) {
        return new String(array, Charset.defaultCharset());
    }
    
    public static long readLong(final byte[] payload, final int i) {
        long num = 0;
        for (int h = 0; h < 8; h++) {
            num = num | ((payload[i + h] & 0xff) << h * 8);
        }
        return num;
    }
    
    public static int readInt(final byte[] payload, final int i) {
        int num = 0;
        for (int h = 0; h < 4; h++) {
            num = num | ((payload[i + h] & 0xff) << h * 8);
        }
        return num;
    }
}
