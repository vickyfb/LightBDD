package LightBDD;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * Unit test suite for the BDD class.
 * 
 * @author Eric 'Siggy' Scott
 */
public class BDDTest
{
        
    static final ArrayList<boolean[]> input2 = Util.generateInputs(2);
    static final ArrayList<boolean[]> input3 = Util.generateInputs(3);
    static final ArrayList<boolean[]> input4 = Util.generateInputs(4);
    
    public BDDTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }
    
    /**
     * Test of execute method, of class BDD.
     */
    @Test
    public void testExecute()
    {
        System.out.println("execute");
        
        //NOT
        BDD instance = new BDD(BDD.Function.NOT);
        Util.assertArrayEquals(new boolean[] {true}, instance.execute(new boolean[] {false}));
        Util.assertArrayEquals(new boolean[] {false}, instance.execute(new boolean[] {true}));
        
        // NAND
        boolean[][] output = {{false}, {true}, {true}, {true}};
        instance = new BDD(BDD.Function.NAND);
        assertArrayEquals(output, instance.execute(input2));
        
        // AND
        output = new boolean[][] {{true}, {false}, {false}, {false}};
        instance = new BDD(BDD.Function.AND);
        assertArrayEquals(output, instance.execute(input2)); 
        
        // OR
        output = new boolean[][] {{true}, {true}, {true}, {false}};
        instance = new BDD(BDD.Function.OR);
        assertArrayEquals(output, instance.execute(input2)); 
        
        // XOR
        output = new boolean[][] {{false}, {true}, {true}, {false}};
        instance = new BDD(BDD.Function.XOR);
        assertArrayEquals(output, instance.execute(input2)); 
        
        // TEST1 (!XOR)
        output = new boolean[][] {{true}, {false}, {true}, {false}};
        instance = new BDD(BDD.Function.TEST1);
        assertArrayEquals(output, instance.execute(input2)); 
        
        //TEST3 (3 inputs)
        output = new boolean[][] {{true}, {false}, {true}, {false}, {true}, {true}, {true}, {true}};
        instance = new BDD(BDD.Function.TEST3);
        assertArrayEquals(output, instance.execute(input3)); 
    }
    
    /**
     * Test apply method, of class BDD.
     */
    @Test
    public void testApply()
    {
        System.out.println("apply");
        
        final BooleanOperator and = new BooleanOperator() {@Override public boolean operate(boolean x, boolean y) { return (x&y); }};
        final BooleanOperator or = new BooleanOperator() {@Override public boolean operate(boolean x, boolean y) { return (x|y); }};
        final BooleanOperator xor = new BooleanOperator() {@Override public boolean operate(boolean x, boolean y) { return (x^y); }};
        
        // NAND & XOR
        boolean[][] output = {{false}, {true}, {true}, {false}};
        BDD x = new BDD(BDD.Function.NAND);
        BDD y = new BDD(BDD.Function.XOR);
        BDD instance = new BDD(and, x, y);
        assertArrayEquals(output, instance.execute(input2));
        // Check for side effects
        assertArrayEquals((new BDD(BDD.Function.NAND)).execute(input2), x.execute(input2));
        assertArrayEquals((new BDD(BDD.Function.XOR)).execute(input2), y.execute(input2));
        
        // NAND & OR
        output = new boolean[][] {{false}, {true}, {true}, {false}};
        x = new BDD(BDD.Function.NAND);
        y = new BDD(BDD.Function.OR);
        instance = new BDD(and, x, y);
        assertArrayEquals(output, instance.execute(input2));
        // Check for side effects
        assertArrayEquals((new BDD(BDD.Function.NAND)).execute(input2), x.execute(input2));
        assertArrayEquals((new BDD(BDD.Function.OR)).execute(input2), y.execute(input2));
        
        // (NAND & XOR) | OR
        output = new boolean[][] {{true}, {true}, {true}, {false}};
        instance = new BDD(or, new BDD(and, new BDD(BDD.Function.NAND), new BDD(BDD.Function.XOR)), new BDD(BDD.Function.OR));
        assertArrayEquals(output, instance.execute(input2));
        
        // (NAND & XOR) ^ OR
        output = new boolean[][] {{true}, {false}, {false}, {false}};
        instance = new BDD(xor, new BDD(and, new BDD(BDD.Function.NAND), new BDD(BDD.Function.XOR)), new BDD(BDD.Function.OR));
        assertArrayEquals(output, instance.execute(input2));
    }
    
    /**
     * Test restrict method, of class BDD.
     */
    @Test
    public void testRestrict()
    {
        System.out.println("restrict");
        
        // TEST2|var1=false equals TEST3
        BDD x = new BDD(BDD.Function.TEST2);
        BDD instance = new BDD(x, 1, false);
        BDD output = new BDD(BDD.Function.TEST3);
        assertArrayEquals(output.execute(input3), instance.execute(input3));
        // Check for side effects
        assertArrayEquals((new BDD(BDD.Function.TEST2)).execute(input3), x.execute(input3));
    }
    
    /**
     * Test compose constructor, of class BDD.
     */
    @Test
    public void testCompose()
    {
        System.out.println("compose");
        testElementaryComposition(); 
        testAdvancedComposition();
    }

    private void testElementaryComposition()
    {
        BDD f1 = new BDD(BDD.Function.XOR);
        BDD f2 = new BDD(BDD.Function.XOR);
        BDD instance = new BDD(0, f1, f2);
        boolean[][] output = { {true}, {false}, {false}, {true}, {false}, {true}, {true}, {false} };
        assertArrayEquals(output, instance.execute(input3));
        // Check for side effects
        BDD xor = new BDD(BDD.Function.XOR);
        assertArrayEquals(xor.execute(input2), f1.execute(input2));
        assertArrayEquals(xor.execute(input2), f2.execute(input2));
        
        instance = new BDD(1, f1, f2);
        assertArrayEquals(output, instance.execute(input3));
        // Check for side effects
        assertArrayEquals(xor.execute(input2), f1.execute(input2));
        assertArrayEquals(xor.execute(input2), f2.execute(input2));
        
        f1 = new BDD(BDD.Function.TEST3);
        f2 = new BDD(BDD.Function.XOR);
        instance = new BDD(0, f1, f2);
        output = new boolean[][] { {true}, {true}, {true}, {true}, {true}, {false}, {true}, {false}, {true}, {false}, {true}, {false}, {true}, {true}, {true}, {true} };
        assertArrayEquals(output, instance.execute(input4));
        // Check for side effects
        assertArrayEquals((new BDD(BDD.Function.TEST3)).execute(input3), f1.execute(input3));
        assertArrayEquals((new BDD(BDD.Function.XOR)).execute(input2), f2.execute(input2));
        
        // Make sure this sequence completes in a reasonable amount of time
        long time = System.currentTimeMillis();
        instance = new BDD(0, instance, instance);
        instance = new BDD(0, instance, instance);
        instance = new BDD(0, instance, instance);
        time = System.currentTimeMillis() - time;
        assertTrue(time < 500); // This is machine specific, but it usually completes in under 300 ms on my MacBook's 2.53GHz Core 2 Duo
    }
    
    private void testAdvancedComposition()
    {
        
        BDD f1 = new BDD(BDD.Function.XOR);
        f1.preConcatonateInputs(2);
        BDD f2 = new BDD(BDD.Function.XOR);
        f2.postConcatonateInputs(2);
        BDD f3 = new BDD(2, f1, f2, false);
        boolean[][] output = { {true}, {false}, {true}, {false}, {false}, {true}, {false}, {true}, {false}, {true}, {false}, {true}, {true}, {false}, {true}, {false} };
        assertArrayEquals(output, f3.execute(input4));
        
        BDD f4 = new BDD(BDD.Function.NAND);
        f4.postConcatonateInputs(2);
        BDD instance = new BDD(3, f3, f4, false);
        output = new boolean[][] { {false}, {false}, {false}, {false}, {false}, {false}, {false}, {false}, {false}, {false}, {false}, {false}, {true}, {true}, {true}, {true} };
        assertArrayEquals(output, instance.execute(input4));
    }
    
    
}
