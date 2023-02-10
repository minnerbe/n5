/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.janelia.saalfeldlab.n5;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

public class VlStringArraySerializer {
    private final String[] data;
    public static String dataTypeTag = "String(-1)";

    VlStringArraySerializer(String[] data) {
        this.data = data;
    }

    public String[] getData() {
        return data;
    }

    public byte[] toByteArray() {
        final byte[] nullChar = "\0".getBytes(StandardCharsets.UTF_8);
        final int charSize = nullChar.length;

        final int numTotalChars = Arrays.stream(data).map(String::length).reduce(0, Integer::sum);
        final int numNullChars = data.length;

        final ByteBuffer buffer = ByteBuffer.allocate((numTotalChars + numNullChars) * charSize);
        for (String str : data)
            buffer.put(str.getBytes(StandardCharsets.UTF_8)).put(nullChar);
        return buffer.array();
    }

    public static VlStringArraySerializer fromByteArray(byte[] byteArray) {
        final String rawChars = new String(byteArray);
        final byte[] nullChar = "\0".getBytes(StandardCharsets.UTF_8);
        final String[] data = rawChars.split("\0");
        return new VlStringArraySerializer(data);
    }
}