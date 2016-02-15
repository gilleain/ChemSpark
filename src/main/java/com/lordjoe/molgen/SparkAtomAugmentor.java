package com.lordjoe.molgen;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import com.lordjoe.distributed.SparkUtilities;

import augment.atom.AtomAugmentation;
import augment.atom.AtomAugmentor;

/**
 * com.lordjoe.molgen.SparkAtomAugmentor
 * copy of AtomAugmentor augmented to return results as a JavaRDD as well as a list
 * SLewis
 */
public class SparkAtomAugmentor  implements ISparkAugmentor {

    private AtomAugmentor augmentor;

    public AtomAugmentor getAugmentor() {
        return augmentor;
    }

    public void setAugmentor(AtomAugmentor augmentor) {
        this.augmentor = augmentor;
    }

    public SparkAtomAugmentor(final String elementString) {
        augmentor = new AtomAugmentor(elementString);
    }

    public SparkAtomAugmentor(final List<String> elementSymbols) {
        augmentor = new AtomAugmentor(elementSymbols);
    }



     public JavaRDD<AtomAugmentation>  sparkAugment(List<AtomAugmentation>  augment) {
              JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
         JavaRDD<AtomAugmentation> ret = currentContext.parallelize(augment);

         return ret;
    }


    @Override
    public List<AtomAugmentation> augment(AtomAugmentation parent) {
        return augmentor.augment(parent);
    }
}
