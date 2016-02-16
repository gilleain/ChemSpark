package test.test.branch;


import junit.framework.Assert;
import org.junit.Test;

public class CNOTests  extends SparkFormulaTest {

    @Test
    public void cH5NOTest() {
        int ch5NO = countNFromAtom("CH5NO");
        Assert.assertEquals(3, ch5NO);
    }

    @Test
    public void c3HNOTest() {
        int c3HNO = countNFromAtom("C3HNO");
        Assert.assertEquals(46, c3HNO);
    }

    @Test
    public void c2H7NOTest() {
        int c2H7NO = countNFromAtom("C2H7NO");
        Assert.assertEquals(8, c2H7NO);
    }
}
