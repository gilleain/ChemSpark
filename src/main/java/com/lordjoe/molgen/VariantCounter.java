package com.lordjoe.molgen;


import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.spark.accumulators.SparkAccumulators;
import com.lordjoe.utilities.ElapsedTimer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.openscience.cdk.DynamicFactory;
import org.openscience.cdk.exception.NoSuchAtomTypeException;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import scala.Option;

import java.util.Properties;

/**
 * com.lordjoe.molgen.VariantCounter
 * User: Steve
 * Date: 12/2/2015
 */
public class VariantCounter {
    public static final int SPARK_CONFIG_INDEX = 0;
    public static final int FORMULA_INDEX = 1;

    public static int countNFromAtom(String formula) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.WARN);
        ElapsedTimer timer = new ElapsedTimer();
        ElapsedTimer totalTime = new ElapsedTimer();


        Properties sparkProperties = SparkUtilities.getSparkProperties();

        SparkConf sparkConf = new SparkConf();
        //      sparkConf.set("spark.default.parallelism","1"); // one thread until we get the right answer
        SparkAccumulators.createInstance();

        sparkConf.setAppName("Count instances of " + formula);

        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined()) {   // use local over nothing
            sparkConf.setMaster("local[*]");
        }
        SparkAccumulatorCountingHandler handler = new SparkAccumulatorCountingHandler(formula);
        SparkAtomGenerator generator = new SparkAtomGenerator(formula, handler);
        //  SparkAccumulatorCountingHandler handler = new SparkAccumulatorCountingHandler(formula);
        //  generator.addHandler(handler);

        generator.run();
        return (int) generator.getCount();
    }

    /**
     * call with args like or20080320_s_silac-lh_1-1_11short.mzxml in Sample2
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        IChemObjectBuilder forcewLoad1 = SilentChemObjectBuilder.getInstance();
        DynamicFactory forceLoad2 = new DynamicFactory(200);
        NoSuchAtomTypeException forceLoad3 = new NoSuchAtomTypeException("foo");

        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.WARN);
        ElapsedTimer timer = new ElapsedTimer();
        ElapsedTimer totalTime = new ElapsedTimer();

        String formula = args[FORMULA_INDEX];
        String properties = args[SPARK_CONFIG_INDEX] ;
        SparkUtilities.readSparkProperties(properties);
        int varientCount = countNFromAtom(formula);


        System.out.println("Found " + varientCount + " Varients of " + formula);
        timer.showElapsed(formula);
        ;
        //   AtomAugmentation.showTries();
        SparkAccumulators.showAccumulators(timer, false);


    }

}
