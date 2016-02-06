package com.lordjoe.branch;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.accumulators.*;
import com.lordjoe.molgen.*;
import com.lordjoe.utilities.*;
import org.apache.log4j.*;
import org.apache.spark.*;
import scala.*;

import java.util.*;

/**
 * com.lordjoe.branch.FormulaTest
 * User: Steve
 * Date: 12/4/2015
 */
public class FormulaTest {

    public int countNFromAtom(String formula) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.WARN);
        ElapsedTimer timer = new ElapsedTimer();
        ElapsedTimer totalTime = new ElapsedTimer();

        SparkUtilities.readSparkPropertiesResource("/SparkLocalCluster.properties");

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
        SparkAtomGeneratorX generator = new SparkAtomGeneratorX(formula, handler);
      //  SparkAccumulatorCountingHandler handler = new SparkAccumulatorCountingHandler(formula);
      //  generator.addHandler(handler);

        generator.run();
        return (int)generator.getCount();
    }

}
