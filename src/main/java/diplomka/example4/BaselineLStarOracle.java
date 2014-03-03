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
package diplomka.example4;

import diplomka.WBSlave.WBSlaveAlphabet;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.api.Query;
import diplomka.WBSlave.WBSlave;
import diplomka.WBSlave.WBSlaveInput;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.automatalib.words.Word;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class BaselineLStarOracle implements DFAMembershipOracle<WBSlaveInput> {

    private final WBSlave testDriver;

    public BaselineLStarOracle() {
        testDriver = new WBSlave();
    }

    @Override
    public void processQueries(Collection<? extends Query<WBSlaveInput, Boolean>> queries) {
        //  Collection trace = new Query<WBSlaveInput, String>() {};
        testDriver.reset();

        WBSlaveAlphabet a;
        try {
            a = WBSlaveAlphabet.getInstance();

            for (Query<WBSlaveInput, Boolean> query : queries) {
                Word<WBSlaveInput> wi = query.getInput();
                WBSlaveInput input = wi.firstSymbol();

                System.out.println("input.getData() = " + input.getData());

                String answer = testDriver.execute_symbol(input.getData());

                if (answer != null) {
                    query.answer(true);
                } else {
                    query.answer(false);
                }

            }

        } catch (NoSuchMethodException ex) {
            Logger.getLogger(BaselineLStarOracle.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
