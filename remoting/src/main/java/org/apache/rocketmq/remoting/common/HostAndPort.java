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

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class HostAndPort implements Serializable {
    private static final long serialVersionUID = 6752191230776376443L;

    /** Magic number indicating lack of port. */
    private static final int NO_PORT = -1;

    /** Hostname, IPv4/IPv6 literal, or unvalidated nonsense. */
    private final String host;

    /** Validated port number in the range [0, 65525], or NO_PORT. */
    private final int port;

    /** True if the parsed host has colons, but no surrounding brackets. */
    private final boolean hasBracketlessColons;

    private HostAndPort(String host, int port, boolean hasBracketlessColons) {
        this.host = host;
        this.port = port;
        this.hasBracketlessColons = hasBracketlessColons;
    }

    public String getHost() {
        return host;
    }

    public boolean hasPort() {
        return port >= 0;
    }

    public int getPort() {
        return port;
    }

    public int getPortOrDefault(int defaultPort) {
        return hasPort() ? port : defaultPort;
    }

    public static HostAndPort fromParts(String host, int port) {
        if (!isValidPort(port)) {
            throw new IllegalArgumentException(
                    String.format("Port out of range : %d", port));
        }
        HostAndPort parsedHost = fromString(host);
        if (parsedHost.hasPort()) {
            throw new IllegalArgumentException(
                    String.format("host has a port : %s", host));
        }
        return new HostAndPort(parsedHost.host, port, parsedHost.hasBracketlessColons);
    }

    public static HostAndPort fromHost(String host) {
        HostAndPort parsedHost = fromString(host);
        if (parsedHost.hasPort()) {
            throw new IllegalArgumentException(
                    String.format("host has a port : %s", host));
        }
        return parsedHost;
    }

    public static HostAndPort fromString(String hostPortString) {
        if (StringUtils.isEmpty(hostPortString)) {
            throw new IllegalArgumentException("hostPortString is empty");
        }

        String host;
        String portString = null;
        boolean hasBracketlessColons = false;
        if (hostPortString.startsWith("[")) {
            String[] hostAndPort = getHostAndPortFromBracketedHosts(hostPortString);
            host = hostAndPort[0];
            portString = hostAndPort[1];
        } else {
            int colonIndex = hostPortString.indexOf(':');
            if (colonIndex >= 0 && hostPortString.indexOf(':', colonIndex + 1) == -1) {
                host = hostPortString.substring(0, colonIndex);
                portString = hostPortString.substring(colonIndex + 1);
            } else {
                host = hostPortString;
                hasBracketlessColons = colonIndex >= 0;
            }
        }

        int port = NO_PORT;
        if (!StringUtils.isEmpty(portString)) {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("Unparsed port number : %s", hostPortString));
            }
            if (!isValidPort(port)) {
                throw new IllegalArgumentException(
                        String.format("port number out of range : %s", hostPortString));
            }
        }
        return new HostAndPort(host, port, hasBracketlessColons);
    }

    public static boolean isValidPort(int port) {
        return port >= 0 && port <= 65536;
    }

    public static String[] getHostAndPortFromBracketedHosts(String hostPortString) {
        int colonIndex = 0;
        int closeBracketIndex = 0;
        if (hostPortString.charAt(0) != '[') {
            throw new IllegalArgumentException(
                    String.format("bracketed host-port string must start with a bracket : %s", hostPortString));
        }
        colonIndex = hostPortString.indexOf(':');
        closeBracketIndex = hostPortString.lastIndexOf(']');
        if (colonIndex == -1 || closeBracketIndex < colonIndex) {
            throw new IllegalArgumentException(
                    String.format("Invalid bracketed host/port : %s", hostPortString));
        }
        String host = hostPortString.substring(1, closeBracketIndex);
        if (closeBracketIndex + 1 == hostPortString.length()) {
            return new String[] {host, ""};
        } else {
            if (hostPortString.charAt(closeBracketIndex + 1) != ':') {
                throw new IllegalArgumentException(
                        String.format("only a colon may follow a closed bracket : %s", hostPortString));
            }
            for (int i = closeBracketIndex + 2; i < hostPortString.length(); ++i) {
                if (!Character.isDigit(hostPortString.charAt(i)))  {
                    throw new IllegalArgumentException(
                            String.format("Port number must number : %s", hostPortString));
                }
            }
        }
        return new String[] {host, hostPortString.substring(closeBracketIndex + 2)};
    }

    public HostAndPort requireBracketsForIPv6() {
        if (hasBracketlessColons) {
            throw new IllegalArgumentException(
                    String.format("Possible bracketless IPv6 literal : %s", host));
        }
        return this;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof HostAndPort) {
            HostAndPort that = (HostAndPort) obj;
            return host.equals(that.host) && port == that.port;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(host.length() + 8);
        if (host.indexOf(':') >= 0) {
            builder.append('[').append(host).append(']');
        } else {
            builder.append(host);
        }
        if (hasPort()) {
            builder.append(':').append(port);
        }
        return builder.toString();
    }
}
