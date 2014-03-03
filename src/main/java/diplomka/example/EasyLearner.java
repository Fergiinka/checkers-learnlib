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
package diplomka.example;


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
import diplomka.telnet.AutoTelnetClient;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.commons.util.mappings.Mapping;
import net.automatalib.graphs.dot.DOTPlottableGraph;
import net.automatalib.graphs.dot.DefaultDOTHelper;
import net.automatalib.graphs.dot.GraphDOTHelper;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;

/**
 *
 * @author Lucie Matusova <xmatus21@stud.fit.vutbr.cz>
 */
public class EasyLearner {

    public static AutoTelnetClient my_telnet = new AutoTelnetClient("localhost", 2000);

    public static void main(String[] args) throws NoSuchMethodException, IOException {

        // create learning alphabet
        // offer(a)
        EasyInput DI_0 = new EasyInput(EasyFSM.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{"0"});

        // offer(b)
        EasyInput DI_1 = new EasyInput(EasyFSM.class.getMethod(
                "execute_symbol", new Class<?>[]{String.class}), new Object[]{"1"});

        Alphabet<EasyInput> inputs = new SimpleAlphabet<>();
        inputs.add(DI_0);
        inputs.add(DI_1);

        // create an oracle that can answer membership queries
        // using the EasyAdapter
        SUL<EasyInput, String> sul = new EasyAdapter();

        // oracle for counting queries wraps sul
        StatisticSUL<EasyInput, String> statisticSul = new ResetCounterSUL<>("membership queries", sul);

        SUL<EasyInput, String> effectiveSul = statisticSul;
        // use caching in order to avoid duplicate queries
        effectiveSul = Caches.createSULCache(inputs, effectiveSul);

        SULOracle<EasyInput, String> mqOracle = new SULOracle<>(effectiveSul);  //effectiveSul buggy

        // create initial set of suffixes
        List<Word<EasyInput>> suffixes = new ArrayList<>();
        suffixes.add(Word.fromSymbols(DI_0));
        suffixes.add(Word.fromSymbols(DI_1));

        /* 
        Interface LearningAlgorithm<M,I,O>
        All Known Subinterfaces:
          LearningAlgorithm.DFALearner<I>, LearningAlgorithm.MealyLearner<I,O>
        All Known Implementing Classes:
          AbstractAutomatonLStar, AbstractLStar, BaselineLStar, ClassicLStarMealy, ExtensibleAutomatonLStar, ExtensibleLStarDFA, ExtensibleLStarMealy, MealyDHC
        
         - Interface LearningAlgorithm.DFALearner<I>:
            BaselineLStar, ExtensibleLStarDFA
         - Interface LearningAlgorithm.MealyLearner<I,O>:
            ExtensibleLStarMealy, MealyDHC
        */

        // construct L* instance (almost classic Mealy version)
        // almost: we use words (Word<String>) in cells of the table 
        // instead of single outputs.        
        MealyLearner<EasyInput, String> lstar
                = new ExtensibleLStarMealy<>(
                        inputs, // input alphabet
                        mqOracle, // mq oracle
                        suffixes, // initial suffixes
                        ObservationTableCEXHandlers.CLASSIC_LSTAR, // handling of counterexamples
                        ClosingStrategies.CLOSE_FIRST // always choose first unclosedness found 
                );
        
        // create random walks equivalence test
        MealyEquivalenceOracle<EasyInput, String> randomWalks
                = new RandomWalkEQOracle<>(
                        0.05, // reset SUL w/ this probability before a step 
                        1, // max steps (overall)
                        false, // reset step count after counterexample 
                        new Random(46346293), // make results reproducible 
                        sul // system under learning
                );

        // construct a learning experiment from
        // the learning algorithm and the random walks test.
        // The experiment will execute the main loop of
        // active learning
        MealyExperiment<EasyInput, String> experiment
                = new MealyExperiment<>(lstar, randomWalks, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        MealyMachine<?, EasyInput, ?, String> result = experiment.getFinalHypothesis();
        
        // close the connection with ModelSim by sending ESCAPE character
        byte[] bytes = new byte[]{0x1b};
        String esc = new String(bytes, "UTF-8");
        my_telnet.sendCommand(esc, false);
        my_telnet.disconnect();
        
        // report results
        /*System.out.println("-------------------------------------------------------");

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
        
        //System.out.println(result);
        
        GraphDOT.write(result, inputs, System.out); // may throw IOException!
        try (Writer w = new FileWriter("learned_design.dot")) {
            GraphDOT.write(result, inputs, w);
        }
        //System.out.println("-------------------------------------------------------");
        

    }

}