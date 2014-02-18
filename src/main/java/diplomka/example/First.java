package diplomka.example;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.dotutil.DOT;
import net.automatalib.util.graphs.dot.GraphDOT;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.algorithms.lstargeneric.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstargeneric.closing.ClosingStrategies;
import de.learnlib.algorithms.lstargeneric.dfa.ExtensibleLStarDFA;
import de.learnlib.api.MembershipOracle.DFAMembershipOracle;
import de.learnlib.eqtests.basic.WMethodEQOracle.DFAWMethodEQOracle;
import de.learnlib.experiments.Experiment.DFAExperiment;
import de.learnlib.oracles.CounterOracle.DFACounterOracle;
import de.learnlib.oracles.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.statistics.SimpleProfiler;

/**
 * This example shows the usage of a learning algorithm and an equivalence test
 * as part of an experiment in order to learn a simulated SUL (system under
 * learning).
 *
 * @author falkhowar
 */
public class First {

    /**
     * creates example from Angluin's seminal paper.
     * 
     * @return example dfa
     */
    private static CompactDFA<Character> constructSUL() {
        // input alphabet contains characters 'a'..'b'
    	Alphabet<Character> sigma = Alphabets.characters('a', 'b');

        // create states
        CompactDFA<Character> dfa = new CompactDFA<>(sigma);
        int q0 = dfa.addInitialState(true);
        int q1 = dfa.addState(false);
        //int q2 = dfa.addState(false);
        //int q3 = dfa.addState(false);

        // create transitions
        dfa.addTransition(q0, 'a', q1);
        dfa.addTransition(q0, 'b', q1);
        dfa.addTransition(q1, 'a', q0);
        dfa.addTransition(q1, 'b', q1);
        //dfa.addTransition(q1, 'b', q3);
        //dfa.addTransition(q2, 'a', q3);
        //dfa.addTransition(q2, 'b', q0);
        //dfa.addTransition(q2, 'a', q2);
        //dfa.addTransition(q3, 'a', q2);
        //dfa.addTransition(q3, 'b', q1);

        return dfa;
    }

    public static void main(String[] args) throws IOException {

        // load DFA and alphabet
        CompactDFA<Character> target = constructSUL();
        Alphabet<Character> inputs = target.getInputAlphabet();

        // typed empty word
        Word<Character> epsilon = Word.epsilon();

        // construct a simulator membership query oracle
        // input  - Character (determined by example)
        DFAMembershipOracle<Character> sul = new DFASimulatorOracle<>(target);

        // oracle for counting queries wraps SUL
        DFACounterOracle<Character> mqOracle =
                new DFACounterOracle<>(sul, "membership queries");

        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
        DFAWMethodEQOracle<Character> conform_test =
                new DFAWMethodEQOracle<>(4, mqOracle);

        // construct L* instance
        ExtensibleLStarDFA<Character> lstar = new ExtensibleLStarDFA<>(
                inputs, // input alphabet
                mqOracle, // mq oracle
                Collections.singletonList(epsilon), // initial suffixes
                ObservationTableCEXHandlers.CLASSIC_LSTAR, // handling of counterexamples
                ClosingStrategies.CLOSE_FIRST // always choose first unclosedness found
                );

        // construct a learning experiment from
        // the learning algorithm and the conformance test.
        // The experiment will execute the main loop of
        // active learning
        /*
         * DFAExperiment(learningAlgorithm, equivalenceAlgorithm, inputs);
         *  - ExperimentImpl uses 
               this.learningAlgorithm = learningAlgorithm;
               this.equivalenceAlgorithm = equivalenceAlgorithm;
               this.inputs = inputs;
         * 
         */
        
        DFAExperiment<Character> experiment =
                new DFAExperiment<>(lstar, conform_test, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        DFA<?, Character> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(mqOracle.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // show model
        System.out.println();
        System.out.println("Model: ");
        GraphDOT.write(result, inputs, System.out); // may throw IOException!

        Writer w = DOT.createDotWriter(true);
        GraphDOT.write(result, inputs, w);
        w.close();

        System.out.println("-------------------------------------------------------");
    }
}