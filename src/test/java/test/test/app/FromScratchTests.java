package test.test.app;

import app.*;
import org.junit.*;

// SLewis - tests commented out as they cause other tests not to run
public class FromScratchTests {
    
    /**
     * A random choice.
     */
    private String formula = "C5H10";
    
    public void fromScratchTest(String outputFormat) throws Exception {
        ArgumentHandler argsH = new ArgumentHandler();
        argsH.setStartingFromScratch(true);
        argsH.setOutputStringFormat(outputFormat);
        argsH.setFormula(formula);
        argsH.setIsStdOut(true);
        AMG.run(argsH);
        return; // added Slewis
    }
    
    // @Test
    public void smilesFromScratchTest() throws Exception {
        fromScratchTest("SMI");
    }
    
  //  @Test
    public void signaturesFromScratchTest() throws Exception {
        fromScratchTest("SIG");
    }

   //  @Test
    public void sdfFromScratchTest() throws Exception {
        fromScratchTest("SDF");
    }

}
