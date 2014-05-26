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
package diplomka.WBSlave;

import static diplomka.ExtLStar.WBSlaveLearner.my_telnet;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class WBSlave {

    // simulation parameters and signal names
    private static final String force = "force ";
    private static final String run = "run ";
    private static final String examine = "examine -value ";
    private static final String resultAnchor = "value: ";
    private static final String arch = "t/";

    // input signals
    private static final String WE_I = "WE_I ";
    private static final String CYC_I = "CYC_I ";
    private static final String STB_I = "STB_I ";
    // output signals
    private static final String ACK_O = "ACK_O";
    private static final String NOT_CYC_WRITE = "NOT_CYC_WRITE";
    private static final String NOT_CYC_READ = "NOT_CYC_READ";

    public static final String D_0 = "000";
    public static final String D_1 = "001";
    public static final String D_2 = "010";
    public static final String D_3 = "011";
    public static final String D_4 = "100";
    public static final String D_5 = "101";
    public static final String D_6 = "110";
    public static final String D_7 = "111";

    private static final String clk_half_period = "20 ns";
    private static final String CLK_0 = "CLK 0";
    private static final String CLK_1 = "CLK 1";
    private static final String RST_0 = "RST 0";
    private static final String RST_1 = "RST 1";
    
    public static int exec_symbols_num = 0;

    // set the input signal
    public String execute_symbol(String s) {
        //System.out.println(s + ",");

        StringBuilder sb = new StringBuilder();

        if (s.equals(D_0) || s.equals(D_1) || s.equals(D_2) || s.equals(D_3) || s.equals(D_4) || s.equals(D_5) || s.equals(D_6) || s.equals(D_7)) {
            exec_symbols_num++;
            // send commands over telnet
            my_telnet.sendCommand(force + arch + WE_I + s.toString().charAt(0));
            my_telnet.sendCommand(force + arch + CYC_I + s.toString().charAt(1));
            my_telnet.sendCommand(force + arch + STB_I + s.toString().charAt(2));
            my_telnet.sendCommand(force + arch + CLK_1);
            my_telnet.sendCommand(run + clk_half_period);
            my_telnet.sendCommand(force + arch + CLK_0);
            my_telnet.sendCommand(run + clk_half_period);

            // examine output signals
            my_telnet.sendCommand(examine + arch + ACK_O);
            my_telnet.readUntil(resultAnchor, false);
            sb.append(my_telnet.readResult(false));

            my_telnet.sendCommand(examine + arch + NOT_CYC_WRITE);
            my_telnet.readUntil(resultAnchor, false);
            sb.append(my_telnet.readResult(false));

            my_telnet.sendCommand(examine + arch + NOT_CYC_READ);
            my_telnet.readUntil(resultAnchor, false);
            sb.append(my_telnet.readResult(false));

            String response = sb.toString();

            switch (response) {
                case D_0:
                    return response;                
                case D_1:
                    return response;                
                case D_2:
                    return response;
                case D_3:
                    return response;
                case D_4:
                    return response;
                case D_5:
                    return response;
                case D_6:
                    return response;
                case D_7:
                    return response;
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
