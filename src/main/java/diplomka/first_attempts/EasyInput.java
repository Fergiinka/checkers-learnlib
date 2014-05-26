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
package diplomka.first_attempts;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>

 We use the EasyInput class to wrap concrete method invocations and use these as
 alphabet symbols for the learning algorithm
 */
public class EasyInput {

    // method to invoke
    public final Method action;

    // method parameter values
    public final Object[] data;

    public EasyInput(Method action, Object[] data) {
        this.action = action;
        this.data = data;
    }

        // this will be used for printing when 
    // logging or exporting automata
    @Override
    public String toString() {
        return action.getName() + Arrays.toString(data);
    }
}
