package com.lordjoe.molgen;

import org.apache.spark.AccumulatorParam;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.signature.MoleculeSignature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.lordjoe.molgen.DuplicateCounter
 * User: Steve
 * Date: 2/14/2016
 */
public class DuplicateCounter {
    /**
     * allow this to be used as an accumulator
     */
    public static final AccumulatorParam<DuplicateCounter> INSTANCE = new AccumulatorParam<DuplicateCounter> () {
        @Override
        public DuplicateCounter addAccumulator(final DuplicateCounter r, final DuplicateCounter t) {
             return new DuplicateCounter(r,t);
        }
        @Override
        public DuplicateCounter addInPlace(final DuplicateCounter r1, final DuplicateCounter r2) { return new DuplicateCounter(r1,r2);
        }
        @Override
        public DuplicateCounter zero(final DuplicateCounter initialValue) {
            return new DuplicateCounter(initialValue);
        }
    };



    private Map<String, IAtomContainer> sigMap;

    private Map<String, List<IAtomContainer>> dupMap;

    public DuplicateCounter() {
        sigMap = new HashMap<String, IAtomContainer>();
        dupMap = new HashMap<String, List<IAtomContainer>>();
    }

    public DuplicateCounter(IAtomContainer atomContainer) {
        this();
        handle(atomContainer);
    }

    public DuplicateCounter(DuplicateCounter... atomContainer) {
        this();
        for (DuplicateCounter duplicateCounter : atomContainer) {
            addTo(duplicateCounter);
        }
    }

    public List<IAtomContainer> getUnique()
    {
        List<IAtomContainer> holder = new ArrayList<IAtomContainer>(sigMap.values());

        return holder;
    }

    private void addToDupMap(IAtomContainer atomContainer, String canonicalSignature) {
        IAtomContainer original = sigMap.get(canonicalSignature);
        List<IAtomContainer> dups = new ArrayList<IAtomContainer>();
        dups.add(original);
        dupMap.put(canonicalSignature, dups);
    }


    public void handle(IAtomContainer atomContainer) {
        String canonicalSignature = new MoleculeSignature(atomContainer).toCanonicalString();
        if (sigMap.containsKey(canonicalSignature)) {
            if (dupMap.containsKey(canonicalSignature)) {
                dupMap.get(canonicalSignature).add(atomContainer);
            } else {
                addToDupMap(atomContainer, canonicalSignature);
            }
        } else {
            sigMap.put(canonicalSignature, atomContainer);
            addToDupMap(atomContainer, canonicalSignature);
        }
    }

    public Map<String, List<IAtomContainer>> getDupMap() {
        return dupMap;
    }

    public void addTo(DuplicateCounter added) {
        for (String s : added.sigMap.keySet()) {
            handle(added.sigMap.get(s));
        }
        for (String s : added.dupMap.keySet()) {
            List<IAtomContainer> iAtomContainers = added.dupMap.get(s);
            for (IAtomContainer iAtomContainer : iAtomContainers) {
                addToDupMap(iAtomContainer, s);
            }
        }
    }

    public DuplicateCounter combine(DuplicateCounter added) {
        DuplicateCounter ret = new DuplicateCounter(this, added);
        return ret;
    }

}
