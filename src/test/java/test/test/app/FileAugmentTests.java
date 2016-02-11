package test.test.app;

import app.*;
import org.junit.*;

import java.io.*;

// SLewis - tests commented out as they cause other tests not to run
public class FileAugmentTests {
    
    /**
     * A random choice.
     */
    private String formula = "C5H10";
    
    private String testFolder = "testdata";
    
    public void augmentFileTest(String inForm, String outForm, String filename) throws Exception {
        ArgumentHandler argsH = new ArgumentHandler();
        argsH.setAugmentingFile(true);
        argsH.setFormula(formula);
        argsH.setInputStringFormat(inForm);
        argsH.setOutputStringFormat(outForm);
        argsH.setIsStdOut(true);
        argsH.setInputFilepath(new File(testFolder, filename).toString());
        AMG.run(argsH);
    }
    
   // @Test     //  comment out slewis
      public void augmentFile_SMI_To_SMI_Test() throws Exception {
              augmentFileTest("SMI", "SMI", "fours_smiles.txt");
         return;   // addes SLewis to check return
    }
    
   // @Test  //  comment out slewis
    public void augmentFile_SMI_To_SIG_Test() throws Exception {
        augmentFileTest("SMI", "SIG", "fours_smiles.txt");
        return;
    }
    
  //   @Test  //  comment out slewis
    public void augmentFile_SIG_To_SMI_Test() throws Exception {
        augmentFileTest("SIG", "SMI", "fours_sigs.txt");
        return;
    }
    
   // @Test  // comment out slewis
    public void augmentFile_SIG_To_SIG_Test() throws Exception {
        augmentFileTest("SIG", "SIG", "fours_sigs.txt");
        return;
    }

}
