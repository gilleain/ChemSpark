package com.lordjoe.molgen;

import org.apache.spark.Accumulator;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.lordjoe.distributed.spark.accumulators.AccumulatorUtilities;
import com.lordjoe.distributed.spark.accumulators.ISparkAccumulators;

import handler.Handler;

/**
 * com.lordjoe.molgen.SparkAccumulatorCountingHandler
 * User: Steve
 * Date: 12/2/2015
 */
public class SparkAccumulatorCountingHandler implements Handler<IAtomContainer> {

    public final String formula;
    private transient Accumulator<Long> count;

    public SparkAccumulatorCountingHandler(final String pFormula) {
        formula = pFormula;
        ISparkAccumulators accumulators = AccumulatorUtilities.getInstance();
        count = accumulators.createAccumulator(formula);
    }


    /**
     * when we serialize we reacquire the accumulator
     */
    protected void buildCount() {
        if (count == null) {
            ISparkAccumulators accx = AccumulatorUtilities.getInstance();
            count = accx.getAccumulator(formula);
        }
    }

    public Accumulator<Long> getCountAccumulator() {
        if (count == null)
            buildCount();
        return count;
    }


    @Override
    public void handle(final IAtomContainer object) {

        Accumulator<Long> countAccumulator = getCountAccumulator();
        countAccumulator.add(1L);  // accumulate total

    }

    @Override
    public void finish() {
        System.out.println("Finish called");
    }

    public long getCount() {
        Accumulator<Long> countAccumulator = getCountAccumulator();
        return countAccumulator.value();
    }
}
