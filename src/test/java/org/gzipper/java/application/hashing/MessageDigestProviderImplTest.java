/*
 * Copyright (C) 2018 Matthias Fussenegger
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gzipper.java.application.hashing;

import java.util.EnumMap;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Matthias Fussenegger
 */
public class MessageDigestProviderImplTest {

    /**
     * The test value of which to compute the hash values.
     */
    private static final String TEST_VALUE = "gzipper";

    private final EnumMap<MessageDigestAlgorithm, String> _resultMap;

    public MessageDigestProviderImplTest() {
        _resultMap = new EnumMap<>(MessageDigestAlgorithm.class);
        // expected results for string "gzipper"
        _resultMap.put(MessageDigestAlgorithm.MD5,
                "de0f5d52f214223991ad720af4c19bb0");
        _resultMap.put(MessageDigestAlgorithm.SHA_1,
                "8fd5c761ea51947cf14ce83059b7a73e4244ed6c");
        _resultMap.put(MessageDigestAlgorithm.SHA_256,
                "31685001b8f052a6aa701be092231e8"
                + "f4404bdff1aeb2e0a6214440c16476a5f");
        _resultMap.put(MessageDigestAlgorithm.SHA_384,
                "e6b03411f9ad267f50e841009ab72"
                + "684a0064441a8f3893f3ae90"
                + "fba14d5bdebf0b8ce14d58b9"
                + "48550db8b067dc15c76");
        _resultMap.put(MessageDigestAlgorithm.SHA_512,
                "cfc351af41d6704cf935071d3b5e3ae9d3337"
                + "e353764416537f3febf45f9183ddb2e9"
                + "5e4006dc0fc69d3b59e3570f3201fa14"
                + "3928adc2560e44eaf11ad25d0b7");
    }

    /**
     * Test of computeHash method, of class MessageDigestProviderImpl.
     */
    @Test
    public void testComputeHash() {
        System.out.println("computeHash");

        final byte[] bytes = TEST_VALUE.getBytes();
        for (MessageDigestAlgorithm algorithm : MessageDigestAlgorithm.values()) {
            System.out.println(algorithm.name() + " start");
            // compute hash value
            String result = _resultMap.get(algorithm);
            MessageDigestResult expResult = MessageDigestProvider.computeHash(bytes, algorithm);
            // only compare hex value since it is calculated from byte array
            assertEquals(result.toUpperCase(), expResult.toString());
            System.out.println(algorithm.name() + " end");
        }
        System.out.println("All tests successful.");
    }
}
