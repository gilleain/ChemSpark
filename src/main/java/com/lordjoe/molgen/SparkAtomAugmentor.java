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
public class SparkAtomAugmentor extends AtomAugmentor implements ISparkAugmentor {


    public SparkAtomAugmentor(final String elementString) {
        super(elementString);
    }

    public SparkAtomAugmentor(final List<String> elementSymbols) {
        super(elementSymbols);
    }



     public JavaRDD<AtomAugmentation>  sparkAugment(List<AtomAugmentation>  augment) {
              JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
         return currentContext.parallelize(augment);
    }
    

}
