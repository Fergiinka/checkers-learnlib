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
package diplomka.example2;

import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.api.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.SUL;
import de.learnlib.cache.Caches;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment.MealyExperiment;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SULOracle;
import de.learnlib.statistics.SimpleProfiler;
import de.learnlib.statistics.StatisticSUL;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

import diplomka.telnet.AutoTelnetClient;
import java.io.FileWriter;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class WBSlaveLearner {

    public static AutoTelnetClient my_telnet = new AutoTelnetClient("localhost", 2000);

    public static void main(String[] args) throws NoSuchMethodException, IOException {

        // create learning alphabet
        WBSlaveInput DI_0 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_0});
        WBSlaveInput DI_1 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_1});
        WBSlaveInput DI_2 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_2});
        WBSlaveInput DI_3 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_3});
        WBSlaveInput DI_4 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_4});
        WBSlaveInput DI_5 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_5});
        WBSlaveInput DI_6 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_6});
        WBSlaveInput DI_7 = new WBSlaveInput(WBSlave.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{WBSlave.D_7});

        Alphabet<WBSlaveInput> inputs = new SimpleAlphabet<>();
        inputs.add(DI_0);
        inputs.add(DI_1);
        inputs.add(DI_2);
        inputs.add(DI_3);
        inputs.add(DI_4);
        inputs.add(DI_5);
        inputs.add(DI_6);
        inputs.add(DI_7);

        // create an oracle that can answer membership queries
        // using the WBSlaveAdapter
        SUL<WBSlaveInput, String> sul = new WBSlaveAdapter();

        // oracle for counting queries wraps sul
        StatisticSUL<WBSlaveInput, String> statisticSul = new ResetCounterSUL<>("membership queries", sul);

        SUL<WBSlaveInput, String> effectiveSul = statisticSul;
        // use caching in order to avoid duplicate queries
        effectiveSul = Caches.createSULCache(inputs, effectiveSul);

        SULOracle<WBSlaveInput, String> mqOracle = new SULOracle<>(effectiveSul);  //effectiveSul buggy

        // create initial set of suffixes
        List<Word<WBSlaveInput>> suffixes = new ArrayList<>();
        suffixes.add(Word.fromSymbols(DI_0));
        suffixes.add(Word.fromSymbols(DI_1));
        suffixes.add(Word.fromSymbols(DI_2));
        suffixes.add(Word.fromSymbols(DI_3));
        suffixes.add(Word.fromSymbols(DI_4));
        suffixes.add(Word.fromSymbols(DI_5));
        suffixes.add(Word.fromSymbols(DI_6));
        suffixes.add(Word.fromSymbols(DI_7));

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table 
        // instead of single outputs.        
        MealyLearner<WBSlaveInput, String> lstar
                = new ExtensibleLStarMealy<>(
                        inputs, // input alphabet
                        mqOracle, // mq oracle
                        suffixes, // initial suffixes
                        ObservationTableCEXHandlers.CLASSIC_LSTAR, // handling of counterexamples
                        ClosingStrategies.CLOSE_FIRST // always choose first unclosedness found 
                );

        // create random walks equivalence test
        MealyEquivalenceOracle<WBSlaveInput, String> randomWalks
                = new RandomWalkEQOracle<>(
                        0.05, // reset SUL w/ this probability before a step 
                        20, // max steps (overall)
                        false, // reset step count after counterexample 
                        new Random(46346293), // make results reproducible 
                        sul // system under learning
                );

        // construct a learning experiment from
        // the learning algorithm and the random walks test.
        // The experiment will execute the main loop of
        // active learning
        MealyExperiment<WBSlaveInput, String> experiment
                = new MealyExperiment<>(lstar, randomWalks, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, WBSlaveInput, ?, String> result = experiment.getFinalHypothesis();

        // close the connection with ModelSim by sending ESCAPE character
        my_telnet.disconnect();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(statisticSul.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // show model
        System.out.println();
        System.out.println("Model: ");

        GraphDOT.write(result, inputs, System.out); // may throw IOException!
        //Writer w = DOT.createDotWriter(true);
        //GraphDOT.write(result, inputs, w);
        //w.close();        
        GraphDOT.write(result, inputs, System.out); // may throw IOException!
        try (Writer w = new FileWriter("out_WBSlave.dot")) {
            GraphDOT.write(result, inputs, w);
        }

        System.out.println("-------------------------------------------------------");

    }

}
