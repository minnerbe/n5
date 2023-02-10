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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Encodes arrays of variable length strings that are null-terminated when written on disk.
 */
public class VlStringArraySerializer {
    private final String[] data;
    private final Charset encoding;
    private static final String NULLCHAR = "\0";
    public static String dataTypeTag = "String(-1)";

    VlStringArraySerializer(String[] data) {
       this(data, StandardCharsets.UTF_8);
    }

    VlStringArraySerializer(String[] data, Charset encoding) {
        this.data = data;
        this.encoding = encoding;
    }

    public String[] getData() {
        return data;
    }

    public byte[] toByteArray() {
        final byte[] nullCharSequence = NULLCHAR.getBytes(encoding);
        final int charSize = nullCharSequence.length;

        final int numTotalChars = Arrays.stream(data).map(String::length).reduce(0, Integer::sum);
        final int numNullChars = data.length;

        final ByteBuffer buffer = ByteBuffer.allocate((numTotalChars + numNullChars) * charSize);
        for (String str : data)
            buffer.put(str.getBytes(encoding)).put(nullCharSequence);
        return buffer.array();
    }

    public static VlStringArraySerializer fromByteArray(byte[] byteArray) {
        final String rawChars = new String(byteArray);
        final String[] data = rawChars.split(NULLCHAR);
        return new VlStringArraySerializer(data);
    }
}