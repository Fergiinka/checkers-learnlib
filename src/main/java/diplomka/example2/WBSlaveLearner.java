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

import diplomka.WBSlave.WBSlaveAdapter;
import diplomka.WBSlave.WBSlaveInput;
import diplomka.WBSlave.WBSlave;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.mealy.ExtensibleLStarMealy;
import de.learnlib.api.EquivalenceOracle;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.learnlib.api.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.LearningAlgorithm.MealyLearner;
import de.learnlib.api.SUL;
import de.learnlib.cache.Caches;
import de.learnlib.eqtests.basic.CompleteExplorationEQOracle;
import de.learnlib.eqtests.basic.RandomWordsEQOracle.MealyRandomWordsEQOracle;
import de.learnlib.eqtests.basic.WMethodEQOracle.MealyWMethodEQOracle;
import de.learnlib.eqtests.basic.WpMethodEQOracle.MealyWpMethodEQOracle;
import de.learnlib.eqtests.basic.mealy.RandomWalkEQOracle;
import de.learnlib.experiments.Experiment.MealyExperiment;
import de.learnlib.oracles.ResetCounterSUL;
import de.learnlib.oracles.SULOracle;
import de.learnlib.statistics.SimpleProfiler;
import de.learnlib.statistics.StatisticSUL;
import diplomka.WBSlave.WBSlaveAlphabet;

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

        // create an oracle that can answer membership queries
        // using the WBSlaveAdapter
        SUL<WBSlaveInput, String> sul = new WBSlaveAdapter();

        // oracle for counting queries wraps sul
        StatisticSUL<WBSlaveInput, String> statisticSul = new ResetCounterSUL<>("membership queries", sul);

        SUL<WBSlaveInput, String> effectiveSul = statisticSul;
        // use caching in order to avoid duplicate queries
        effectiveSul = Caches.createSULCache(inputs, effectiveSul);

        SULOracle<WBSlaveInput, String> mqOracle = new SULOracle<>(effectiveSul);

        // create initial set of suffixes
        List<Word<WBSlaveInput>> suffixes = new ArrayList<>();
        suffixes.add(Word.fromSymbols(alphabet.getDI_0()));
        suffixes.add(Word.fromSymbols(alphabet.getDI_1()));
        suffixes.add(Word.fromSymbols(alphabet.getDI_2()));
        suffixes.add(Word.fromSymbols(alphabet.getDI_3()));
        suffixes.add(Word.fromSymbols(alphabet.getDI_4()));
        suffixes.add(Word.fromSymbols(alphabet.getDI_5()));
        suffixes.add(Word.fromSymbols(alphabet.getDI_6()));
        suffixes.add(Word.fromSymbols(alphabet.getDI_7()));

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
                        10000,   // max steps (overall)
                        false, // reset step count after counterexample 
                        new Random(46346293), // make results reproducible 
                        sul // system under learning
                );

        // create random words equivalence test
        MealyEquivalenceOracle randomWords
                = new MealyRandomWordsEQOracle(
                        mqOracle,
                        1,  // minLength
                        20, // maxLength
                        20, // maxTests
                        new Random(46346293)
                );
        
        // create WMethod equivalence test
        // takes longer than randomWalks
        MealyEquivalenceOracle<WBSlaveInput, String> WMethodTest
                = new MealyWMethodEQOracle(
                        4, //  maximum exploration depth
                        mqOracle
                );

        // construct a WpMethod eq. oracle
        EquivalenceOracle WpMethodTest
                = new MealyWpMethodEQOracle(
                        3,  //  maximum exploration depth
                        mqOracle
                );

        // construct a complete exploration eq. oracle
        EquivalenceOracle completeExploration
                = new CompleteExplorationEQOracle(
                        mqOracle,
                        3  // maximum exploration depth
                );
        
        // Construct a learning experiment from
        // the learning algorithm and the equivalence test.
        // The experiment will execute the main loop of
        // active learning
 // ! CHANGE EQ. TEST HERE! //
        MealyExperiment<WBSlaveInput, String> experiment
                = new MealyExperiment<>(lstar, randomWords, inputs);

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

        /*
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
         */
        
        GraphDOT.write(result, inputs, System.out); // may throw IOException!
        //Writer w = DOT.createDotWriter(true);
        //GraphDOT.write(result, inputs, w);
        //w.close();        
        GraphDOT.write(result, inputs, System.out); // may throw IOException!
        try (Writer w = new FileWriter("learned_design.dot")) {
            GraphDOT.write(result, inputs, w);
        }
    }
}