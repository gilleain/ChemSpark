package test.test.branch;

import junit.framework.Assert;

import org.junit.Test;

public class CarbonNitrogenTests extends FormulaTest {
    
    @Test
    public void cHNTest() {
        Assert.assertEquals(1, countNFromAtom("CHN"));
    }
    
    @Test
    public void cH3NTest() {
        Assert.assertEquals(1, countNFromAtom("CH3N"));
    }
    
    @Test
    public void cH5NTest() {
        Assert.assertEquals(1, countNFromAtom("CH5N"));
    }
    
    @Test
    public void c2HNTest() {
        Assert.assertEquals(2, countNFromAtom("C2HN"));
    }
    
    @Test
    public void cN2Test() {
        Assert.assertEquals(1, countNFromAtom("CN2"));
    }
    
    @Test
    public void c2H3NTest() {
        Assert.assertEquals(5, countNFromAtom("C2H3N"));
    }

}
