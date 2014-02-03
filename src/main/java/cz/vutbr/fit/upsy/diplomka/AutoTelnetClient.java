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

// ---------------------------------------------------------------------------
//http://stackoverflow.com/questions/1195809/looking-for-java-telnet-emulator
// ---------------------------------------------------------------------------

package cz.vutbr.fit.upsy.diplomka;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.net.telnet.TelnetClient;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */

public class AutomatedTelnetClient {

    private final TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;

    public AutomatedTelnetClient(String server, int port) {
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

    public String readResult(char endChar, boolean verbose) {
        try {
            StringBuilder sb = new StringBuilder();
            char ch = (char) in.read();
            
            while (true) {
                if (verbose) System.out.print(ch);
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

    public String readUntil(String pattern, boolean verbose) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuilder sb = new StringBuilder();

            char ch = (char) in.read();
            while (true) {
                if (verbose) System.out.print(ch);
                sb.append(ch);
                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern)) {
                        return sb.toString();
                    }
                }
                ch = (char) in.read();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String sendCommand(String command) {
        try {
            write(command);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            AutomatedTelnetClient my_telnet = new AutomatedTelnetClient("localhost", 2000);

            my_telnet.sendCommand("force x 1");
            Thread.sleep(1000);

            my_telnet.sendCommand("force y 0");
            Thread.sleep(1000);

            my_telnet.sendCommand("run 1 ms");
            Thread.sleep(1000);

            my_telnet.sendCommand("examine -value /test_tb/z");
            Thread.sleep(1000);
            my_telnet.readUntil("result: ", false);
            
            char resEndChar = ' ';
            String result = my_telnet.readResult(resEndChar, true);
            
            // sending ESCAPE
            byte[] bytes = new byte[]{0x1b};
            String esc = new String(bytes, "UTF-8");
            my_telnet.sendCommand(esc);

            my_telnet.disconnect();
            System.out.println("DONE. Disconnected.");

        } catch (UnsupportedEncodingException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}