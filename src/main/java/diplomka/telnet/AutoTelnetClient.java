/*
 * Copyright (C) 2013 Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// Inspired by
//http://stackoverflow.com/questions/1195809/looking-for-java-telnet-emulator
package diplomka.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.net.telnet.TelnetClient;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class AutoTelnetClient {

    private final TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;

    private final char endChar = ' ';
    private final String ACK = "ACK";

    public AutoTelnetClient(String server, int port) {
        try {
            // Connect to the specified server
            telnet.connect(server, port);
            in = telnet.getInputStream();
            //in = new BufferedReader(new InputStreamReader(telnet.getInputStream()));
            out = new PrintStream(telnet.getOutputStream());

        } catch (IOException ex) {
            System.err.println("AutomatedTelnetClient could not connect to the server " + server);
        }
    }

    public void write(String value) {
        try {
            out.println(value);
            out.flush();
            // System.out.println(value);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String readResult(boolean verbose) {
        try {
            StringBuilder sb = new StringBuilder();
            char ch = (char) in.read();

            while (true) {
                if (verbose) {
                    System.out.print(ch);
                }
                if (ch == endChar) {
                    return sb.toString();
                }
                sb.append(ch);
                ch = (char) in.read();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Boolean readUntil(String pattern, boolean verbose) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuilder sb = new StringBuilder();

            char ch = (char) in.read();
            while (true) {
                if (verbose) {
                    System.out.print(ch);
                }
                sb.append(ch);
                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern)) {
                        return true; // sb.toString();
                    }
                }
                ch = (char) in.read();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    /*
     * Sends a given command over telnet and waits for acknowledge.
     */
    public void sendCommand(String command) {
        sendCommand(command, true);
    }

    public void sendCommand(String command, boolean wait) {
        try {
            write(command);
            if (wait) {
                waitForAck();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void waitForAck() {
        while (!readUntil(ACK, false)) {
            ;
        }
    }

    public void disconnect() throws UnsupportedEncodingException {
        byte[] bytes = new byte[]{0x1b};
        String esc = new String(bytes, "UTF-8");
        sendCommand(esc, false);
        try {
            telnet.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
