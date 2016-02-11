package test.test.branch;


import junit.framework.Assert;
import org.junit.Test;

public class CNOTests  extends SparkFormulaTest {

    @Test
    public void cH5NOTest() {
        Assert.assertEquals(3, countNFromAtom("CH5NO"));
    }

    @Test
    public void c3HNOTest() {
        Assert.assertEquals(46, countNFromAtom("C3HNO"));
    }

    @Test
    public void c2H7NOTest() {
        Assert.assertEquals(8, countNFromAtom("C2H7NO"));
    }
}
