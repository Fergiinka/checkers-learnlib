/*
 * Copyright (C) 2014 Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
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
package diplomka.example;

import static diplomka.example.EasyLearner.my_telnet;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class EasyFSM {

    // simulation parameters and signal names
    private static final String force = "force ";
    private static final String run = "run ";
    private static final String examine = "examine -value ";
    private static final String resultAnchor = "value: ";
    private static final String arch = "t/";

    private static final String DI = "DI ";
    private static final String DO = "DO ";

    private static final String D_0 = "0";
    private static final String D_1 = "1";

    private static final String clk_half_period = "20 ns";
    private static final String CLK_0 = "CLK 0";
    private static final String CLK_1 = "CLK 1";
    private static final String RST_0 = "RST 0";
    private static final String RST_1 = "RST 1";

    // set the input signal
    public String execute_symbol(String s) {
        //System.out.println("execute_symbol DI " + s);

        StringBuilder sb = new StringBuilder();

        if (s.equals(D_0) || s.equals(D_1)) {
            // send commands over telnet
            my_telnet.sendCommand(force + arch + DI + s.toString().charAt(0));

            my_telnet.sendCommand(force + arch + CLK_1);
            my_telnet.sendCommand(run + clk_half_period);
            my_telnet.sendCommand(force + arch + CLK_0);
            my_telnet.sendCommand(run + clk_half_period);

            my_telnet.sendCommand(examine + arch + DO);
            my_telnet.readUntil(resultAnchor, false);
            sb.append(my_telnet.readResult(false));

            String response = sb.toString();
            //System.out.println("DO " + s);

            switch (response) {
                case D_0:
                    return D_0;
                case D_1:
                    return D_1;
            }
        }
        return null;

    }

    /*
     * Set and unset RST signal
     */
    public void reset() {
        my_telnet.sendCommand(force + arch + RST_1);
        my_telnet.sendCommand(force + arch + CLK_1);
        my_telnet.sendCommand(run + clk_half_period);

        my_telnet.sendCommand(force + arch + RST_0);
        my_telnet.sendCommand(force + arch + CLK_0);
    }

}
