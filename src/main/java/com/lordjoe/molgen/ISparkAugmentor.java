package com.lordjoe.molgen;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;

import augment.Augmentor;
import augment.atom.AtomAugmentation;

/**
 * com.lordjoe.molgen.SparkAugmentor
 * User: Steve
 * Date: 12/2/2015
 */
public interface ISparkAugmentor extends Augmentor<AtomAugmentation>  {
    public JavaRDD<AtomAugmentation>  sparkAugment(List<AtomAugmentation> augment);

}
