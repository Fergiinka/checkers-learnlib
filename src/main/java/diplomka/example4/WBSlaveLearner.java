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
package diplomka.example4;

import diplomka.WBSlave.WBSlaveAlphabet;
import de.learnlib.algorithms.baselinelstar.BaselineLStar;
import de.learnlib.api.EquivalenceOracle.DFAEquivalenceOracle;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import de.learnlib.api.LearningAlgorithm;
import de.learnlib.api.MembershipOracle;
import de.learnlib.eqtests.basic.RandomWordsEQOracle;
import de.learnlib.experiments.Experiment.DFAExperiment;
import diplomka.WBSlave.WBSlaveInput;

import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;

import diplomka.telnet.AutoTelnetClient;
import java.io.FileWriter;
import net.automatalib.automata.fsa.DFA;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class WBSlaveLearner {

    public static AutoTelnetClient my_telnet = new AutoTelnetClient("localhost", 2000);

    public static void main(String[] args) throws NoSuchMethodException, IOException {

        Alphabet<WBSlaveInput> inputs = new SimpleAlphabet<>();
        WBSlaveAlphabet alphabet = WBSlaveAlphabet.getInstance();

        inputs.add(alphabet.getDI_0());
        inputs.add(alphabet.getDI_1());
        inputs.add(alphabet.getDI_2());
        inputs.add(alphabet.getDI_3());
        inputs.add(alphabet.getDI_4());
        inputs.add(alphabet.getDI_5());
        inputs.add(alphabet.getDI_6());
        inputs.add(alphabet.getDI_7());

        //BaselineLStar(Alphabet<I> alphabet, MembershipOracle<I,Boolean> oracle)
        // - an implementation of the L* algorithm by Dana Angluin
        MembershipOracle<WBSlaveInput, Boolean> oracle = new BaselineLStarOracle();
        LearningAlgorithm.DFALearner<WBSlaveInput> dfalearn = new BaselineLStar<>(inputs, oracle);

        DFAEquivalenceOracle<WBSlaveInput> randomWords
                = new RandomWordsEQOracle.DFARandomWordsEQOracle<>(
                        oracle,
                        1,
                        20,
                        10,
                        new Random(46346293) // make results reproducible
                );

        DFAExperiment<WBSlaveInput> experiment
                = new DFAExperiment<>(dfalearn, randomWords, inputs);

        // turn on time profiling
        experiment.setProfile(true);
        // enable logging of models
        experiment.setLogModels(false);

        // run experiment
        experiment.run();

        // get learned model
        DFA<?, WBSlaveInput> result = experiment.getFinalHypothesis();

        // close the connection with ModelSim
        my_telnet.disconnect();

        /*        
         // report results - profiling
         System.out.println(SimpleProfiler.getResults());
         // learning statistics
         System.out.println(experiment.getRounds().getSummary());
         // model statistics
         System.out.println("States: " + result.size());
         System.out.println("Sigma: " + inputs.size());
         // show model
         System.out.println();
         System.out.println("Model: ");
         */
        
        GraphDOT.write(result, inputs, System.out); // may throw IOException!
        try (Writer w = new FileWriter("out_WBSlave_BaselineLStar.dot")) {
            GraphDOT.write(result, inputs, w);
        }
    }
}