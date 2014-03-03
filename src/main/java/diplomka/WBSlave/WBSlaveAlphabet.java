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

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class WBSlaveAlphabet {

    private static WBSlaveAlphabet alphabet;

    WBSlaveInput DI_0;
    WBSlaveInput DI_1;
    WBSlaveInput DI_2;
    WBSlaveInput DI_3;
    WBSlaveInput DI_4;
    WBSlaveInput DI_5;
    WBSlaveInput DI_6;
    WBSlaveInput DI_7;

    // private constructor
    private WBSlaveAlphabet() throws NoSuchMethodException {
        // create learning alphabet
        DI_0 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_0});
        DI_1 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_1});
        DI_2 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_2});
        DI_3 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_3});
        DI_4 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_4});
        DI_5 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_5});
        DI_6 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_6});
        DI_7 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_7});
    }

    // create (singleton) alphabet
    public static WBSlaveAlphabet getInstance() throws NoSuchMethodException {
        if (alphabet == null) {
            alphabet = new WBSlaveAlphabet();
        }

        return alphabet;
    }

    public WBSlaveInput getDI_0() {
        return DI_0;
    }

    public WBSlaveInput getDI_1() {
        return DI_1;
    }

    public WBSlaveInput getDI_2() {
        return DI_2;
    }

    public WBSlaveInput getDI_3() {
        return DI_3;
    }

    public WBSlaveInput getDI_4() {
        return DI_4;
    }

    public WBSlaveInput getDI_5() {
        return DI_5;
    }

    public WBSlaveInput getDI_6() {
        return DI_6;
    }

    public WBSlaveInput getDI_7() {
        return DI_7;
    }

}
