package com.lordjoe.molgen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.atomtype.IAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IElement;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.SaturationChecker;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.systemsbiology.xtandem.XTandemUtilities;

import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.spark.accumulators.AbstractLoggingFlatMapFunction;
import com.lordjoe.distributed.spark.accumulators.AbstractLoggingFunction;
import com.lordjoe.distributed.spark.accumulators.AbstractLoggingFunction2;

import app.FormulaParser;
import augment.Augmentation;
import augment.AugmentingGenerator;
import augment.atom.AtomAugmentation;
import augment.atom.AtomCanonicalChecker;
import augment.constraints.ElementConstraintSource;
import augment.constraints.ElementConstraints;
import handler.Handler;
import validate.HCountValidator;

/**
 * com.lordjoe.molgen.SparkAtomGenerator
 * User: Steve
 * Date: 12/2/2015
 */


///**
// * com.lordjoe.molgen.SparkAtomGenerator
// * similar to AtomGenerator but allows multiple handlers and
// * is immutable  and runs spark code
// * SLewis
// */
public class SparkAtomGeneratorX implements AugmentingGenerator<IAtomContainer>,Serializable {


    private final SparkAtomAugmentor augmentor;

    private List<Handler<IAtomContainer>> handlers;

    private int maxIndex;

    private HCountValidator hCountValidator;

    private AtomCanonicalChecker canonicalChecker;

    private ElementConstraints initialConstraints;

    private ElementConstraintSource initialStateSource;

    private int counter;

    public SparkAtomGeneratorX(String elementFormula, Handler<IAtomContainer> handler) {
        // XXX - parse the formula once and pass down the parser!
        this.initialConstraints = new ElementConstraints(elementFormula);

        this.hCountValidator = new HCountValidator(elementFormula);
        initialStateSource = new ElementConstraintSource(initialConstraints);
        this.augmentor = new SparkAtomAugmentor(hCountValidator.getElementSymbols());
        this.canonicalChecker = new AtomCanonicalChecker();
        this.handlers = new ArrayList<Handler<IAtomContainer>>();
        handlers.add(handler);
        this.maxIndex = hCountValidator.getElementSymbols().size() - 1;
    }

    public void run() {
        for (IAtomContainer start : initialStateSource.get()) {
            String symbol = start.getAtom(0).getSymbol();
            augment(new AtomAugmentation(start, initialConstraints.minus(symbol)), 0);
        }
//        System.out.println("counter = " + counter);
    }

    public void run(IAtomContainer initial) {
        // XXX index = atomCount?
        ElementConstraints remaining = null;    // TODO
        augment(new AtomAugmentation(initial, remaining), initial.getAtomCount() - 1);
    }

    private void augment(AtomAugmentation parent, int index) {

        counter++;
        if (index >= maxIndex) {
            IAtomContainer atomContainer = parent.getAugmentedObject();
            if (hCountValidator.isValidMol(atomContainer, maxIndex + 1)) {
                for (Handler<IAtomContainer> handler : handlers) {
                    handler.handle(atomContainer);
                }
//                System.out.println("SOLN " + io.AtomContainerPrinter.toString(atomContainer));
            }
            return;
        }

        for (AtomAugmentation augmentation : augmentor.augment(parent)) {
            if (canonicalChecker.isCanonical(augmentation)) {
//                report("C", augmentation);
                augment(augmentation, index + 1);
            } else {
//                report("N", augmentation);
            }
        }
    }

    private void report(String cOrN, AtomAugmentation augmentation) {
        System.out.println(counter + " " + cOrN + " "
            + io.AtomContainerPrinter.toString(augmentation.getAugmentedObject()));
    }


    private int count;


    @Override
    public void finish() {
        for (Handler<IAtomContainer> handler : handlers) {
            handler.finish();
        }
    }

    @Override
    public Handler<IAtomContainer> getHandler() {
        return handlers.get(0); // TODO FIXME
    }


    public SparkAtomGeneratorX(String elementFormula) {
        List<String> elementSymbols = new ArrayList<String>();
        this.hCountValidator = new HCountValidator();
        hCountValidator.setElementSymbols(elementSymbols);
        FormulaParser formulaParser = new FormulaParser(elementFormula);

        this.augmentor = new SparkAtomAugmentor(elementSymbols );
        this.maxIndex = elementSymbols.size() - 1;
    }

    public int getCount() {
        return count;
    }


    public void setCount(final int pCount) {
        count = pCount;
    }
     private int parseFormula(String elementFormula, List<String> elementSymbols) {
        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
        IMolecularFormula formula =
                MolecularFormulaManipulator.getMolecularFormula(elementFormula, builder);
        List<IElement> elements = MolecularFormulaManipulator.elements(formula);

        // count the number of non-heavy atoms
        int hCount = 0;
        for (IElement element : elements) {
            String symbol = element.getSymbol();
            int count = MolecularFormulaManipulator.getElementCount(formula, element);
            if (symbol.equals("H")) {
                hCount = count;
            }
            else {
                for (int i = 0; i < count; i++) {
                    elementSymbols.add(symbol);
                }
            }
        }
        Collections.sort(elementSymbols);

        return hCount;
    }
//
//    public void run() {
//        AtomAugmentation initial = augmentor.augment()
//        run(initial, 0);
//
//    }

    public static final int MAX_PARITIONS = 800;     // was 120 - lets try more

    private void run(AtomAugmentation init, int index) {
        List<AtomAugmentation> augment = augmentor.augment(init);
        int numberAtoms = augment.size();
        int numberPartitions = numberAtoms;
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();

        JavaRDD<AtomAugmentation> aug1 = augmentor.sparkAugment(augment);
        for (int i = index + 1; i < maxIndex; i++) {
            aug1 = aug1.flatMap(new HandleOneLevelAugmentation(i));
            if (numberPartitions < MAX_PARITIONS) {
                numberPartitions *= numberAtoms;
             }
            else {
                 numberPartitions  = (int)(1.3 * numberPartitions);
            }
            if(i < (maxIndex - 1))
                aug1 = aug1.repartition(numberPartitions); // spread the work
        }
        long[] counts = new long[1];
        //  aug1 = SparkUtilities.persistAndCount("Before Filter", aug1, counts);


        IsMoleculeConnected moleculeConnected = new IsMoleculeConnected();
//        aug1 = aug1.filter(moleculeConnected);
        //    aug1 = SparkUtilities.persistAndCount("After Connected Filter", aug1, counts);

        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
        IAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(builder);
        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(builder);
        SaturationChecker satCheck = new SaturationChecker();
   //     IAtomTypeMatcher matcher = hCountValidator.getMatcher();
   //     SaturationChecker satCheck = hCountValidator.getSatCheck();
    //    CDKHydrogenAdder hAdder = hCountValidator.getHAdder();

//        IsHydrogensCorrect hydrogensCorrect = new IsHydrogensCorrect();
 //       aug1 = aug1.filter(hydrogensCorrect);

        // we could place a handler here
        long count = aug1.count();  // force all the work here
 //       aug1 = SparkUtilities.persistAndCount("After Hydrogen Filter", aug1, counts);

        setCount((int)count);

//        IsMoleculeValid moleculeValid = new IsMoleculeValid();
//        aug1 = aug1.filter(moleculeValid);
//
//        aug1 = SparkUtilities.persistAndCount("After Molecule Valid", aug1, counts);

//        setCount((int) aug1.count()); // now force execution
        System.out.println("Count is " + getCount());
    }

//    private void augment(AtomAugmentation parent, int index) {
//        if (index >= maxIndex) {
//            IAtomContainer atomContainer = parent.getAugmentedObject();
////            AtomContainerPrinter.print(atomContainer);
//            if (hCountValidator.isValidMol(atomContainer, maxIndex + 1)) {
//                for (Handler<IAtomContainer> handler : handlers) {
//                    handler.handle(atomContainer);
//                }
//
//            }
//            return;
//        }
//
//        throw new UnsupportedOperationException("Fix This"); // ToDo
////          for (AtomAugmentation augmentation : augment) {
////            if (augmentation.isCanonical()) {
////                augment(augmentation, index + 1);
////            }
////        }
//    }

    private static class SumGoodForms extends AbstractLoggingFunction2<Integer, AtomAugmentation, Integer> {
        @Override
        public Integer doCall(final Integer v1, final AtomAugmentation v2) throws Exception {
            return null;
        }
    }

    private class HandleOneLevelAugmentation extends AbstractLoggingFlatMapFunction<AtomAugmentation, AtomAugmentation> {
        public final int index;

        public HandleOneLevelAugmentation(final int pIndex) {
            index = pIndex;
        }

        @Override
        public Iterable<AtomAugmentation> doCall(final AtomAugmentation t) throws Exception {

            List<AtomAugmentation> ret = new ArrayList<AtomAugmentation>();
            if (index >= maxIndex) {
                IAtomContainer atomContainer = t.getAugmentedObject();
                boolean validMol = hCountValidator.isValidMol(atomContainer, maxIndex + 1);
                if (validMol) {
                    for (Handler<IAtomContainer> handler : handlers) {
                        handler.handle(atomContainer);
                    }
//                    if (AtomGenerator.VERBOSE)
//                        AtomGenerator.showValueAndIndex(index, t, true);
                }
                else {
//                    if (AtomGenerator.VERBOSE)
//                        AtomGenerator.showValueAndIndex(index, t, false);
                }
                return ret;
            }

            List<AtomAugmentation> augment = augmentor.augment(t);
//            if (false && AtomGenerator.VERBOSE)
//                AtomGenerator.showArrayAndIndex(index, augment);

            for (AtomAugmentation x : augment) {
                if (true) { //canonicalChecker.isCanonical(augmentation) ) {
                    ret.add(x);
                }
                else {
                    //  x.isCanonical(); // repeat to check
                    XTandemUtilities.breakHere();
                }
            }
            return ret;
        }
    }

    /**
     * call handler for all valid molecules and return false (no more processing )
     * return true for all other cases
     */
    private class IsMoleculeConnected extends AbstractLoggingFunction<AtomAugmentation, Boolean> {


        @Override
        public Boolean doCall(final AtomAugmentation v1) throws Exception {
            IAtomContainer atomContainer = v1.getAugmentedObject();
            return handleValidConnectedMolecule(atomContainer);
        }


    }

    protected Boolean handleValidConnectedMolecule(final IAtomContainer pAtomContainer) {
        boolean validMol = hCountValidator.isValidMol(pAtomContainer, maxIndex + 1);
        return validMol;
    }

//    /**
//     * call handler for all valid molecules and return false (no more processing )
//     * return true for all other cases
//     */
//    private class IsHydrogensCorrect extends AbstractLoggingFunction<AtomAugmentation, Boolean> {
//
//
//        @Override
//        public Boolean doCall(final AtomAugmentation v1) throws Exception {
//            IAtomContainer atomContainer = v1.getAugmentedObject();
//            return handleHydrogensCorrect(atomContainer);
//        }
//
//
//    }
//
//    protected Boolean handleHydrogensCorrect(final IAtomContainer pAtomContainer) {
//        boolean validMol = hCountValidator.isHydrogensCorrect(pAtomContainer);
//        return validMol;
//    }
//

    /**
     * call handler for all valid molecules and return false (no more processing )
     * return true for all other cases
     */
    private class IsMoleculeValid extends AbstractLoggingFunction<AtomAugmentation, Boolean> {


        @Override
        public Boolean doCall(final AtomAugmentation v1) throws Exception {
            IAtomContainer atomContainer = v1.getAugmentedObject();
            return handleValidMolecule(atomContainer);
        }


    }

    protected Boolean handleValidMolecule(final IAtomContainer pAtomContainer) {
        boolean validMol = hCountValidator.isValidMol(pAtomContainer, maxIndex + 1);
        return validMol;
    }
}
