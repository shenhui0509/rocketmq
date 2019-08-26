/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.remoting.common;

import org.junit.Test;
import static org.junit.Assert.*;

public class HostAndPortTest {

    @Test
    public void testFromParts() {
        HostAndPort hostAndPort = HostAndPort.fromParts("127.0.0.1", 9876);
        assertEquals(hostAndPort.getHost(), "127.0.0.1");
        assertEquals(hostAndPort.getPort(), 9876);

        hostAndPort = HostAndPort.fromParts("2001:db8::1", 80);
        assertEquals(hostAndPort.getHost(), "2001:db8::1");
        assertEquals(hostAndPort.getPort(), 80);

        hostAndPort = HostAndPort.fromParts("[2001:db8::1]", 123);
        assertEquals(hostAndPort.getHost(), "2001:db8::1");
        assertEquals(hostAndPort.getPort(), 123);

        try {
            HostAndPort.fromParts("org.apache.com:123", 91);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            HostAndPort.fromParts("foo.bar.com", -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testFromHost() {
        HostAndPort hostAndPort = HostAndPort.fromHost("127.0.0.1");
        assertFalse(hostAndPort.hasPort());

        hostAndPort = HostAndPort.fromHost("2001:db8::1");
        assertFalse(hostAndPort.hasPort());
        hostAndPort = HostAndPort.fromHost("[2001:db8::1]");
        assertEquals(hostAndPort.getHost(), "2001:db8::1");
        assertFalse(hostAndPort.hasPort());

        hostAndPort = HostAndPort.fromHost("2001:db9:2:3");
        assertEquals(hostAndPort.getHost(), "2001:db9:2:3");
        assertFalse(hostAndPort.hasPort());

        hostAndPort = HostAndPort.fromHost("localhost");
        assertEquals(hostAndPort.getHost(), "localhost");
        assertFalse(hostAndPort.hasPort());

        try {
            HostAndPort.fromHost("org.apache.com:123");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            HostAndPort.fromHost("[org.apache.com]");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
}

    @Test
    public void testIllegalPort() {
        try {
            HostAndPort.fromParts("2011:ac2:2:1", 123456);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            HostAndPort.fromString("123.233.233.233:-12");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }
    }

    @Test
    public void testFromString() {
        HostAndPort hp = HostAndPort.fromString("127.0.123.133:9121");
        assertEquals(hp.getHost(), "127.0.123.133");
        assertEquals(hp.getPort(), 9121);

        hp = HostAndPort.fromString("[2019:dd1::0]:9139");
        assertEquals(hp.getHost(), "2019:dd1::0");
        assertEquals(hp.getPort(), 9139);

        try {
            HostAndPort.fromString("[foo.bar.com]");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            HostAndPort.fromString("[foo.bar.com]:80");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            HostAndPort.fromString("[]:80");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            HostAndPort.fromString("[foo.bar.com]80");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

    }

    @Test
    public void testEquals() {
        HostAndPort hp1 = HostAndPort.fromString("127.1.2.3:9821");
        HostAndPort hp2 = HostAndPort.fromParts("127.1.2.3", 9821);
        assertEquals(hp1, hp2);

        hp1 = HostAndPort.fromString("[2001:ac:12:1]:212");
        hp2 = HostAndPort.fromParts("2001:ac:12:1", 212);
        HostAndPort hp3 = HostAndPort.fromParts("[2001:ac:12:1]", 212);
        assertEquals(hp1, hp2);
        assertEquals(hp1, hp3);
        assertEquals(hp2, hp3);
    }

    @Test
    public void testToString() {
        HostAndPort hp = HostAndPort.fromParts("2011:ed1:12:3c", 12345);
        assertEquals(hp.toString(), "[2011:ed1:12:3c]:12345");
        assertEquals("foo:101", "" + HostAndPort.fromString("foo:101"));
        assertEquals("[1::2]", HostAndPort.fromString("1::2").toString());
        assertEquals("[::1]:21", HostAndPort.fromString("[::1]:21").toString());
        assertEquals("[1::1]:21", HostAndPort.fromParts("1::1", 21).toString());
    }
}
