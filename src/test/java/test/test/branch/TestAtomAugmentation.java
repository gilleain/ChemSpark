package test.test.branch;

import branch.*;
import io.*;
import org.junit.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.silent.*;

import static org.junit.Assert.*;

/**
 * Test augmentation of molecules by single atoms and sets of bonds.
 * 
 * @author maclean
 *
 */
public class TestAtomAugmentation {
    
    private IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
    
    private IAtomContainer make(String acpString) {
        return AtomContainerPrinter.fromString(acpString, builder);
    }
    
    private AtomAugmentation makeAugmentation(IAtomContainer mol, String elementSymbol, int... points) {
        return new AtomAugmentation(mol, builder.newInstance(IAtom.class, elementSymbol), points);
    }
    
    @Test
    public void testDuplicateNines() {
        test("C0C1C2C3C4C5C6C7C8 0:1(1),0:2(1),0:3(1),1:4(2),4:5(1),5:6(1),6:7(2)", "C", 0, 0, 0, 0, 0, 0, 1, 0);
        test("C0C1C2C3C4C5C6C7C8 0:1(2),0:2(1),0:3(1),2:4(1),4:5(2),5:6(1),6:7(1)", "C", 0, 0, 0, 0, 0, 0, 1, 0);
    }
    
    @Test
    public void testCanonical() {
        test("C0C1C2C3 0:1(1),0:2(1),1:3(1),2:3(1)", "C", 1, 1, 0, 0);
        test("C0C1C2C3 0:1(1),0:2(1),0:3(1),1:2(1)", "C", 0, 1, 0, 1);
    }
    
    @Test
    public void testC3H3NPair() {
        test("C0C1C2 0:2(3)", "N", 1, 1, 1);
        test("C0C1C2 0:1(3)", "N", 1, 1, 1);
    }
    
    @Test
    public void testDisconnected() {
//        test("C0C1",        "C", 1, 0);
//        test("C0C1 0:1(1)", "C", 0, 0);
        test("C0C1",        "N", 2, 0);
        test("C0C1N2 0:2(2)", "N", 0, 2, 1);
    }
    
    private void test(String start, String atom, int... points) {
        IAtomContainer mol = make(start);
        if(mol == null) {
            mol = make(start); // repeat
            AtomAugmentation aug = makeAugmentation(mol, atom, points);
             IAtomContainer augMol = aug.getAugmentedMolecule();
            return; // added SLewis
        }
        AtomAugmentation aug = makeAugmentation(mol, atom, points);
        IAtomContainer augMol = aug.getAugmentedMolecule();
        System.out.println(aug.isCanonical() + "\t" + AtomContainerPrinter.toString(augMol));
    }
    
    @Test
    public void testCanonicalFromSingle() {
        test("C0C1 0:1(1)", "C", 2, 1);
        test("C0C1 0:1(2)", "C", 1, 1);
    }
    
    @Test
    public void testFailingPair() {
        test("C0C1C2 0:1(1),0:2(1),1:2(1)", "C", 2, 0, 0);
        test("C0C1C2 0:1(2),0:2(1)", "C", 1, 0, 1);
    }
    
    @Test
    public void testGetAugmentedMolecule() {
        IAtomContainer mol = make("C0C1C2 0:1(1),0:2(1)");
        AtomAugmentation augmentation = makeAugmentation(mol, "C", 1, 0, 1);
        IAtomContainer augmentedMol = augmentation.getAugmentedMolecule();
        AtomContainerPrinter.print(augmentedMol);
        IBond addedBond = augmentedMol.getBond(2);
        assertEquals("Added bond 1", IBond.Order.SINGLE, addedBond.getOrder());
    }

}
