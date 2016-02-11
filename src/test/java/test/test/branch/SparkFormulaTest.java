package test.test.branch;

import augment.atom.AtomGenerator;
import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.spark.accumulators.SparkAccumulators;
import com.lordjoe.molgen.SparkAccumulatorCountingHandler;
import com.lordjoe.molgen.SparkAtomGenerator;
import com.lordjoe.utilities.ElapsedTimer;
import handler.molecule.DuplicateCountingHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.openscience.cdk.interfaces.IAtomContainer;
import scala.Option;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * test.test.branch.SparkFormulaTest
 * User: Steve
 * Date: 2/10/2016
 */
public class SparkFormulaTest {

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
        SparkAtomGenerator generator = new SparkAtomGenerator(formula, handler);
        //  SparkAccumulatorCountingHandler handler = new SparkAccumulatorCountingHandler(formula);
        //  generator.addHandler(handler);

        generator.run();
        return (int)generator.getCount();
    }


}
