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

import de.learnlib.api.SUL;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class WBSlaveAdapter implements SUL<WBSlaveInput, String> {

    // system under learning
    private final WBSlave sul = new WBSlave();

    // reset the SUL
    @Override
    public void post() {
        sul.reset();
    }

    // execute one input on the SUL
    @Override
    public String step(WBSlaveInput in) {
        try {
            // invoke the method wrapped by in
            Object ret = in.action.invoke(sul, in.data);
            // make sure that we return a string
            return ret == null ? "" : (String) ret;
        } catch (IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException e) {
                // This should never happen. In a real experiment
            // this would be the point when we want to issue
            // a warning or stop the learning.
            return "err";
        }
    }

    @Override
    public void pre() {
        ;
    }
}
