package test.test.generate;

import app.*;
import generate.*;
import handler.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.silent.*;
import org.openscience.cdk.tools.manipulator.*;

import java.util.*;

public class BaseTest {
    
    public IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    
    public IAtomContainer makeCCEdge(IBond.Order order) {
        IAtomContainer ac = builder.newInstance(IAtomContainer.class);
        ac.addAtom(builder.newInstance(IAtom.class, "C"));
        ac.addAtom(builder.newInstance(IAtom.class, "C"));
        ac.addBond(0, 1, order);
        return ac;
    }
    
    public List<String> elementSymbols(String elementString) {
        List<String> elementSymbols = new ArrayList<String>();
        for (int i = 0; i < elementString.length(); i++) {
            elementSymbols.add(String.valueOf(elementString.charAt(i)));
        }
        return elementSymbols;
    }
    
    public IAtomContainer makeSingleAtom(String elementSymbol) {
        IAtomContainer ac = builder.newInstance(IAtomContainer.class);
        ac.addAtom(builder.newInstance(IAtom.class, elementSymbol));
        return ac;
    }
    
    public int countNFromAtom(String formulaString,
                              ListerMethod listerMethod, 
                              LabellerMethod labellerMethod,
                              ValidatorMethod validatorMethod) {
        CountingHandler handler = new CountingHandler();
        generateNFromAtom(formulaString, listerMethod, labellerMethod, validatorMethod, handler);
        return handler.getCount();
    }
    
    public void parseFormula(String formulaString, AugmentingGenerator generator) {
        IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(formulaString, builder);
        List<String> elementSymbols = new ArrayList<String>();
        int hCount = 0;
        for (IIsotope element : formula.isotopes()) {   // isotopes are not elements, I know...
            String elementSymbol = element.getSymbol();
            int count = formula.getIsotopeCount(element);
            if (elementSymbol.equals("H")) {
                hCount = count;
            } else {
                for (int i = 0; i < count; i++) {
                    elementSymbols.add(elementSymbol);
                }
            }
        }
        Collections.sort(elementSymbols);
        generator.setHCount(hCount);
        generator.setElementSymbols(elementSymbols);
    }
        
    public void generateNFromAtom(String formulaString, 
                                  ListerMethod listerMethod,
                                  LabellerMethod labellerMethod,
                                  ValidatorMethod validatorMethod,
                                  GenerateHandler handler) {
       
        AugmentingGenerator generator = 
            new AtomAugmentingGenerator(handler, listerMethod, labellerMethod, validatorMethod);
        parseFormula(formulaString, (AugmentingGenerator) generator);
        List<String> elementSymbols = generator.getElementSymbols();
        
        String firstSymbol = elementSymbols.get(0);
        IAtomContainer singleAtom = makeSingleAtom(firstSymbol);
        
        int n = elementSymbols.size();
        generator.extend(singleAtom, n);
    }
    
    public int countNFromSingleDoubleTriple(String formulaString, 
                                            ListerMethod listerMethod,
                                            LabellerMethod labellerMethod,
                                            ValidatorMethod validatorMethod) {
        return countNFromSingleDoubleTriple(
                formulaString, listerMethod, labellerMethod, validatorMethod, AugmentationMethod.ATOM);
    }
    
    public int countNFromSingleDoubleTriple(String formulaString, 
                                            ListerMethod listerMethod,
                                            LabellerMethod labellerMethod,
                                            ValidatorMethod validatorMethod,
                                            AugmentationMethod augmentMethod) {
        
        IAtomContainer ccSingle = makeCCEdge(IBond.Order.SINGLE);
        IAtomContainer ccDouble = makeCCEdge(IBond.Order.DOUBLE);
        IAtomContainer ccTriple = makeCCEdge(IBond.Order.TRIPLE);
        CountingHandler handler = new CountingHandler();
        
        AugmentingGenerator generator;
        if (augmentMethod == AugmentationMethod.ATOM) {
            generator = new AtomAugmentingGenerator(handler, listerMethod, labellerMethod, validatorMethod);
        } else {
            generator = new BondAugmentingGenerator(handler);
        }
        parseFormula(formulaString, generator);
        List<String> elementSymbols = generator.getElementSymbols();
        int n = elementSymbols.size();
        
        generator.extend(ccSingle, n);
        generator.extend(ccDouble, n);
        generator.extend(ccTriple, n);
        
        return handler.getCount();
    }
}
