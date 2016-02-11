package test.test.branch;


import com.lordjoe.testing.*;
import junit.framework.Assert;
import org.junit.Test;

public class
AlkyneTests extends SparkFormulaTest {

    @Test
    public void c2H2Test() {
        Assert.assertEquals(1, countNFromAtom("C2H2"));
    }

    @Test
    public void c3H4Test() {
        int c3H4 = countNFromAtom("C3H4");
        Assert.assertEquals(3, c3H4);
    }

    @Test
    public void c4H6Test() {
        int c4H6 = countNFromAtom("C4H6");
        Assert.assertEquals(9, c4H6);
    }

    @Test
    public void c5H8Test() {
        Assert.assertEquals(26, countNFromAtom("C5H8"));
    }

    @Test
    public void c6H10Test() {
        Assert.assertEquals(77, countNFromAtom("C6H10"));
    }

   // @Test   // added SLewis
    public void c12H20Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C12H20");
//        Assert.assertEquals(77, c12H20);
        timer.showElapsed("C12H20 - found " + c12H20);
    }

    //   @Test   // added SLewis for bigger run
    public void c10H18Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C10H18");
        Assert.assertEquals(5572, c12H20);
        timer.showElapsed("C10H18- found " + c12H20);  // this test took 89 minutes
    }

     //  @Test   // added SLewis for bigger run
    public void c9H16Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C9H16");
        Assert.assertEquals(1903, c12H20);
        timer.showElapsed("C9H16- found " + c12H20);  // this test took 518  sec   292 one machine spark
    }

    @Test   // added SLewis for bigger run
    public void c8H14Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C8H14");
        Assert.assertEquals(654, c12H20);
        timer.showElapsed("C8H14- found " + c12H20);      // this test took 53 sec
    }

    @Test   // added SLewis  for bigger run
    public void c7H12Test() {
        ElapsedTimer timer = new ElapsedTimer();
        int c12H20 = countNFromAtom("C7H12");
        Assert.assertEquals(222, c12H20);
        timer.showElapsed("C7H12- found " + c12H20);
    }

}
