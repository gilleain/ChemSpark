package com.lordjoe.distributed.spark.accumulators;

import com.lordjoe.algorithms.*;
import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.*;
import com.lordjoe.utilities.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spark.accumulators.SparkAccumulators
 * this class implements a similar idea to Hadoop Accumulators
 * User: Steve
 * Date: 11/12/2014
 */
public class SparkAccumulators implements ISparkAccumulators {

    public static final String MEMORY_ACCUMULATOR_NAME = "MemoryUsage";

    public static final int MAX_TRACKED_THREADS = 10;


    public static void createInstance() {
        ISparkAccumulators me = AccumulatorUtilities.getInstance();
        if (me == null) {
            me = new SparkAccumulators();
            AccumulatorUtilities.setInstance(me);
        }
//        me.createSpecialAccumulator(LogRareEventsAccumulator.LOG_RARE_EVENTS_NAME, (AccumulatorParam<LogRareEventsAccumulator>)LogRareEventsAccumulator.PARAM_INSTANCE, LogRareEventsAccumulator.empty());
//        me.createSpecialAccumulator(MEMORY_ACCUMULATOR_NAME, MemoryUseAccumulator.PARAM_INSTANCE, MemoryUseAccumulator.empty());
//        me.createSpecialAccumulator(GCTimeAccumulator.GCTIME_ACCUMULATOR_NAME,
//                GCTimeAccumulator.PARAM_INSTANCE, GCTimeAccumulator.empty());

//        for (int i = 0; i < MAX_TRACKED_THREADS; i++) {
//            //noinspection AccessStaticViaInstance
//            instance.createAccumulator(ThreadUseLogger.getThreadAccumulatorName(i));
//        }
//        instance.createMachineAccumulator();
    }

    // this is a singleton and should be serialized
    private SparkAccumulators() {
    }

    /**
     * holds accumulators by name
     */
    private final Map<String, Accumulator<Long>> accumulators = new HashMap<String, Accumulator<Long>>();
    private final Map<String, Accumulator<MachineUseAccumulator>> functionaccumulators = new HashMap<String, Accumulator<MachineUseAccumulator>>();
    // not sure what these are used for but thay are allowed
    private final Map<String, Accumulator> specialaccumulators = new HashMap<String, Accumulator>();
    //    private Accumulator<Set<String>> machines;
    private transient Set<String> deliveredMessages = new HashSet<String>();

    /**
     * append lines for all accumulators to an appendable
     * NOTE - call only in the Executor
     *
     * @param out where to append
     */
    public static void showAccumulators(Appendable out, ElapsedTimer totalTIme) {
        try {
            SparkAccumulators me = (SparkAccumulators) AccumulatorUtilities.getInstance();
            showAccumulators(out, me);

            MachineUseAccumulator totalCalls = showMachineUseAccumulators(out, me);

            showSpecialAccumulators(out, me);

            out.append("Total all Functions\n" + totalCalls.toString() + "\n");
            out.append(totalTIme.formatElapsed("Total Run Time"));
            out.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void showAccumulators(final Appendable out, final SparkAccumulators pMe) throws IOException {
        List<String> accumulatorNames = pMe.getAccumulatorNames();
        for (String accumulatorName : accumulatorNames) {
            Accumulator<Long> accumulator = pMe.getAccumulator(accumulatorName);

            Long value = accumulator.value();
            //noinspection StringConcatenationInsideStringBufferAppend
            out.append(accumulatorName + " " + Long_Formatter.format(value) + "\n");
        }
    }

    public static MachineUseAccumulator showMachineUseAccumulators(final Appendable out, final SparkAccumulators pMe) throws IOException {
        MachineUseAccumulator totalCalls = MachineUseAccumulator.empty();
        List<String> functionAccumulatorNames = pMe.getFunctionAccumulatorNames();
        for (String accumulatorName : functionAccumulatorNames) {
            Accumulator<MachineUseAccumulator> accumulator = pMe.getFunctionAccumulator(accumulatorName);
            MachineUseAccumulator value = accumulator.value();
            totalCalls.add(value);
            //noinspection StringConcatenationInsideStringBufferAppend
            out.append(accumulatorName + " " + value + "\n");
        }
        return totalCalls;
    }

    public static void showSpecialAccumulators(final Appendable out, final SparkAccumulators pMe) throws IOException {
        List<String> specialAccumulatorNames = pMe.getSpecialAccumulatorNames();
        for (String accumulatorName : specialAccumulatorNames) {
            Accumulator accumulator = pMe.getSpecialAccumulator(accumulatorName);
            Object value = accumulator.value();
            if (value instanceof IAccumulator) {
                // let the value figure out how to make a report
                out.append(accumulatorName + "\n");
                ((IAccumulator) value).buildReport(out);
                out.append("\n");
            }
            else {
                if (value instanceof SpectrumScoringAccumulator) {
                    SpectrumScoringAccumulator spectr = (SpectrumScoringAccumulator) value;
                    if (spectr.getTotalCalls() == 0)
                        continue;
                }
                //noinspection StringConcatenationInsideStringBufferAppend
                out.append(accumulatorName + "\n" + value.toString() + "\n");

            }
        }
    }


    /**
     * must be called in the Executor before accumulators can be used
     *
     * @param acc
     */

    @Override
    public Accumulator<Long> createAccumulator(String acc) {
        if (accumulators.get(acc) != null)
            return accumulators.get(acc); // already done - should an exception be thrown
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
        AccumulatorParam<Long> instance = LongAccumulableParam.INSTANCE;
        Accumulator<Long> accumulator = currentContext.accumulator(0L, acc, instance);

        accumulators.put(acc, accumulator);
        return accumulators.get(acc); // already done - should an exception be thrown
    }


    /**
     * must be called in the Executor before accumulators can be used
     *
     * @param acc
     */
    @Override
    public Accumulator<MachineUseAccumulator> createFunctionAccumulator(String acc) {
        if (functionaccumulators.get(acc) != null)
            return functionaccumulators.get(acc); // already done - should an exception be thrown
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
        String name = "function:" + acc;
        MachineUseAccumulator empty = MachineUseAccumulator.empty();
        AccumulatorParam<MachineUseAccumulator> paramInstance = MachineUseAccumulator.PARAM_INSTANCE;
        Accumulator<MachineUseAccumulator> accumulator = currentContext.accumulator(empty, name, paramInstance);
        functionaccumulators.put(acc, accumulator);
        return accumulator;
    }


    /**
     * Really just makes a scoring accumulator
     *
     * @param acc
     */
    @Override
    public <K> Accumulator<K> createSpecialAccumulator(String id, AccumulatorParam<K> param, K initialValue) {
        Accumulator<K> ret = specialaccumulators.get(id);
        if (specialaccumulators.get(id) != null)
            return ret; // already done - should an exception be thrown
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
        Accumulator<K> accumulator = currentContext.accumulator(initialValue, id, param);
        specialaccumulators.put(id, accumulator);
        return accumulator;
    }


    /**
     * append lines for all accumulators to System.out
     * NOTE - call only in the Executor
     */
    public static void showAccumulators(ElapsedTimer totalTime) {
        showAccumulators(totalTime, true);
    }


    /**
     * append lines for all accumulators to System.out
     * NOTE - call only in the Executor
     */
    public static void showAccumulators(ElapsedTimer totalTime, boolean saveFile) {
        System.out.println("=========================================");
        System.out.println("====  Accululators              =========");
        System.out.println("=========================================");
        showAccumulators(System.out, totalTime);
        if (saveFile) {
            PrintWriter savedAccumulators = SparkUtilities.getHadoopPrintWriter("Accumulators.txt");
            showAccumulators(savedAccumulators, totalTime);
            savedAccumulators.close();

        }
    }


//    protected void createMachineAccumulator() {
//        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
//
//        machines = currentContext.accumulator(new HashSet<String>(),"machines",StringSetAccumulableParam.INSTANCE);
//    }

//    public String getMachineList() {
//        List<String> machinesList = new ArrayList(machines.value());
//        Collections.sort(machinesList);
//        StringBuilder sb = new StringBuilder();
//        for (String s : machinesList) {
//          if(sb.length() > 0)
//              sb.append("\n");
//           sb.append(s);
//        }
//        return sb.toString();
//    }

    /**
     * return all registerd aaccumlators
     *
     * @return
     */
    public List<String> getAccumulatorNames() {
        List<String> keys = new ArrayList<String>(accumulators.keySet());
        Collections.sort(keys);  // alphapetize
        return keys;
    }

    /**
     * return all registerd accumulators
     *
     * @return
     */
    public List<String> getFunctionAccumulatorNames() {
        List<String> keys = new ArrayList<String>(functionaccumulators.keySet());
        Collections.sort(keys);  // alphabetize
        return keys;
    }

    /**
     * return all special accumulators
     *
     * @return
     */
    public List<String> getSpecialAccumulatorNames() {
        List<String> keys = new ArrayList<String>(specialaccumulators.keySet());
        Collections.sort(keys);  // alphabetize
        return keys;
    }

//    /**
//     * how much work are we spreading across threads
//     */
//    public void incrementThreadAccumulator() {
//        int threadNumber = ThreadUseLogger.getThreadNumber();
//        if (threadNumber > MAX_TRACKED_THREADS)
//            return; // too many threads
//        incrementAccumulator(ThreadUseLogger.getThreadAccumulatorName(threadNumber));
//        incrementMachineAccumulator();
//    }


    /**
     * true is an accumulator exists
     */
    public boolean isAccumulatorRegistered(String acc) {
        return accumulators.containsKey(acc);
    }

    /**
     * @param acc name of am existing accumulator
     * @return !null existing accumulator
     */
    public Accumulator<Long> getAccumulator(String acc) {
        Accumulator<Long> ret = accumulators.get(acc);
        if (ret == null) {
            String message = "Accumulators need to be created in advance in the executor - cannot get " + acc;
            if (!deliveredMessages.contains(message)) {
                System.err.println(message);
                deliveredMessages.add(message);
            }
        }
        return ret;
    }


    /**
     * @param acc name of am existing accumulator
     * @return !null existing accumulator
     */
    public Accumulator<MachineUseAccumulator> getFunctionAccumulator(String acc) {
        Accumulator<MachineUseAccumulator> ret = functionaccumulators.get(acc);
        if (ret == null) {
            String message = "Function Accumulators need to be created in advance in the executor - cannot get " + acc;
            if (!deliveredMessages.contains(message)) {
                System.err.println(message);
                deliveredMessages.add(message);
            }
        }
        return ret;
    }


    /**
     * @param acc name of am existing special accumulator
     * @return !null existing accumulator
     */
    public Accumulator getSpecialAccumulator(String acc) {
        Accumulator ret = specialaccumulators.get(acc);
        if (ret == null) {
            String message = "Special Accumulators need to be created in advance in the executor - cannot get " + acc;
            if (!deliveredMessages.contains(message)) {
                System.err.println(message);
                deliveredMessages.add(message);
            }
        }
        return ret;
    }


    /**
     * add one to an existing accumulator
     *
     * @param acc
     */
    public void incrementAccumulator(String acc) {
        incrementAccumulator(acc, 1);
    }

    /**
     * add added to an existing accumulator
     *
     * @param acc   name of am existing accumulator
     * @param added amount to add
     */
    public void incrementAccumulator(String acc, long added) {
        Accumulator<Long> accumulator = getAccumulator(acc);
        if (accumulator != null)
            accumulator.add(added);
    }


    /**
     * add one to an existing accumulator
     *
     * @param acc
     */
    public void incrementFunctionAccumulator(String acc, long totalTme) {
        incrementFunctionAccumulator(acc, totalTme, 1);
    }

    /**
     * add added to an existing accumulator
     *
     * @param acc   name of am existing accumulator
     * @param added amount to add
     */
    public void incrementFunctionAccumulator(String acc, long totalTme, int added) {
        Accumulator<MachineUseAccumulator> accumulator = getFunctionAccumulator(acc);
        if (accumulator != null)
            accumulator.add(new MachineUseAccumulator(added, totalTme));
    }

}
