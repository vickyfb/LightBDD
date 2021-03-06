/* BDD.java
 * 
 * LightBDD is a library for handling Reduced Ordered Binary Decision Diagrams.
 * 
 */
package LightBDD;

import java.util.HashMap;

/**
 * Reduced Ordered Binary Decision diagram.
 * 
 * For an explanation of some of the internals, see Henrik Reif Anderson,
 * "An Introduction to Binary Decision Diagrams," 1997:
 * http://www.cs.unb.ca/~gdueck/courses/cs6805/Notes/bdd97.pdf
 * 
 * @author Eric 'Siggy' Scott
 */
public class BDD extends Executable implements Graph
{   
    /* If you add fields, don't forget to update the copy constructor! */
    private BDDTree tree;
    
    /**
     *  Manual constructor
     */
    public BDD(BDDTree tree)
    {
        this.tree = tree;
    }
    
    /**
     * Deep copy constructor
     */
    public BDD(BDD x)
    {
        this(x, false);
    }
    
    /**
     * Deep copy constructor
     * 
     * @param x Function to copy
     * @param negation If true, builds the compliment of x
     */
    public BDD(BDD x, boolean negation)
    {
        this.tree = (negation ? x.tree.BuildNegation() : new BDDTree(x.tree));
    }
    
    /**
     * Builds a BDD out of the specified boolean function.
     * 
     * Based on Anderson (1997).
     * 
     * @param f Function to clone
     * @param output The function may be multi-output, but a BDD has only one output.  This selects one. 
     */
    public BDD(Executable f, int output)
    {
        this.tree = new BDDTree(f.getNumInputs());
        buildThisFromFunction(f, output, new boolean[f.getNumInputs()], 0);
    }
    
    public BDD(BooleanFunction f)
    {
        assert(f.getNumOutputs() == 1);
        this.tree = new BDDTree(f.getNumInputs());
        buildThisFromFunction(f, 0, new boolean[f.getNumInputs()], 0);
    }
    
    private int buildThisFromFunction(Executable f, int output, boolean[] inputString, int inputIndex)
    {
        if (inputIndex == f.getNumInputs())
            return (f.execute(inputString)[output] ? 1 : 0);
        else
        {
            inputString[inputIndex] = false;
            int lowChild = buildThisFromFunction(f, output, inputString, inputIndex + 1);
            inputString[inputIndex] = true;
            int highChild = buildThisFromFunction(f, output, inputString, inputIndex + 1);
            return mk(new Node(lowChild, highChild, inputIndex));
        }
    }
    
    public enum Function { TRUE, FALSE, NOT, NAND, AND, OR, XOR, SHUNT, TEST1, TEST2, TEST3, TEST4, TEST5, XOR_POSTCAT2, XOR_PRECAT2 };
    /**
     *  Constructor for pre-defined functions
     */
    public BDD(Function function)
    {
        
        switch (function)
        {
            default:
            case TRUE:
                this.tree = new BDDTree(true);
                break;
                
            case FALSE:
                this.tree = new BDDTree(false);
                break;
            
            case NOT:
                this.tree = new BDDTree(1);
                tree.addNode(new Node(1, 0, 0));
                break;
                
            case NAND:
                this.tree = new BDDTree(2);
                int highNodeNAND = tree.addNode(new Node(1, 0, 1));
                tree.addNode(new Node(1, highNodeNAND, 0));
                break;
                
            case AND:
                this.tree = new BDDTree(2);
                int highNodeAND = tree.addNode(new Node(0, 1, 1));
                tree.addNode(new Node(0, highNodeAND, 0));
                break;
                
            case OR:
                this.tree = new BDDTree(2);
                int lowNodeOR = tree.addNode(new Node(0, 1, 1));
                tree.addNode(new Node(lowNodeOR, 1, 0));
                break;
                
            case XOR:
                this.tree = new BDDTree(2);
                int lowNodeXOR = tree.addNode(new Node(0, 1, 1));
                int highNodeXOR = tree.addNode(new Node(1, 0, 1));
                tree.addNode(new Node(lowNodeXOR, highNodeXOR, 0));
                break;
                
            case SHUNT:
                this.tree = new BDDTree(1);
                tree.addNode(new Node(0, 1, 0));
                break;
            
            case TEST1:
                this.tree = new BDDTree(2);
                tree.addNode(new Node(0,1, 1));
                break;
                
            case TEST2: // Corresponds to the example Anderson applies restrict() to
                this.tree = new BDDTree(3);
                int x3 = tree.addNode(new Node(0, 1, 2));
                int x2a = tree.addNode(new Node(1, x3, 1));
                int x2b = tree.addNode(new Node(x3, 1, 1));
                tree.addNode(new Node(x2a, x2b, 0));
                break;
                
            case TEST3: // Corresponds to Anderson's application of restrict() to TEST2
                this.tree = new BDDTree(3);
                x3 = tree.addNode(new Node(0, 1, 2));
                tree.addNode(new Node(1, x3, 0));
                break;
                
            case TEST4: // From Anderson's example of apply()
                this.tree = new BDDTree(5);
                int n2 = tree.addNode(new Node(1, 0, 4));
                int n3 = tree.addNode(new Node(n2, 0, 3));
                int n4 = tree.addNode(new Node(0, n2, 3));
                int n5 = tree.addNode(new Node(n3, n4, 2));
                int n6 = tree.addNode(new Node(n5, 0, 1));
                int n7 = tree.addNode(new Node(0, n5, 1));
                tree.addNode(new Node(n6, n7, 0));
                break;
                
            case TEST5: // From Anderson's example of apply()
                this.tree = new BDDTree(5);
                n2 = tree.addNode(new Node(1, 0, 4));
                n3 = tree.addNode(new Node(n2, 0, 2));
                n4 = tree.addNode(new Node(0, n2, 2));
                tree.addNode(new Node(n3, n4, 0));
                break;
                
            case XOR_POSTCAT2:
                this.tree = new BDDTree(4);
                n2 = tree.addNode(new Node(0, 1, 1));
                n3 = tree.addNode(new Node(1, 0, 1));
                tree.addNode(new Node(n2, n3, 0));
                break;
                
            case XOR_PRECAT2:
                this.tree = new BDDTree(4);
                n2 = tree.addNode(new Node(0, 1, 3));
                n3 = tree.addNode(new Node(1, 0, 3));
                tree.addNode(new Node(n2, n3, 2));
                break;
        }
    }
       
    /**
     * Build a BDD by *applying* the specified boolean operator to two preexisting
     * BDDs.
     * 
     */
    public BDD(BooleanOperator op, BDD x, BDD y)
    {
        this.tree = new BDDTree(x.tree.getNumInputs());
        HashMap<Integer[], Integer> dynamicProgrammingMemory = new HashMap();
        apply(dynamicProgrammingMemory, op, x.tree, y.tree, x.tree.getRootIndex(), y.tree.getRootIndex());
    }
    
    /**
     *  Build a BDD by *restricting* (fixing or, rather, ignoring) one of the
     *  inputs of a pre-existing BDD.
     */
    public BDD(BDD x, int inputToFix, boolean value)
    {
        this.tree = new BDDTree(x.tree);
        int newRoot = restrict(x.tree, x.tree.getRootIndex(), inputToFix, value, new HashMap<Integer, Integer>());
        tree.setRootIndex(newRoot);
    }
    
    /**
     * Elementary composition.  If |f1| and |f2| are the number of inputs of the
     * argument BDDs, the result has |f1| + |f2| - 1 inputs.
     * 
     * @param var The output of f2 is fed into the varth input of f1
     * @param f1 "Female" function
     * @param f2 "Male" function
     */
    public BDD(int var, BDD f1, BDD f2)
    {
        this(var, f1, f2, true);
    }
    
    /**
     * Build a BDD by composing f1 and f2.
     * 
     * @param var The output of f2 is fed into the varth input of f1
     * @param f1 "Female" function
     * @param f2 "Male" function
     * @param autoConcatonate If true, does elementary composition.
     *          If false, the inputs are unmodified, and the resulting
     *          composition isn't as intuitive.  (To understand how it works,
     *          have a good ponder of Shannon's Expansion).
     */
    public BDD(int var, BDD f1, BDD f2, boolean autoConcatonate)
    {
        assert(var < f1.getNumInputs());
        f1 = new BDD(f1);
        f2 = new BDD(f2);
        if (autoConcatonate)
        {
            int oldF1NumInputs = f1.tree.getNumInputs();
            f1.tree.preConcatonateInputs(f2.tree.getNumInputs());
            var += f2.tree.getNumInputs(); // The varth input of f1 has moved
            f2.tree.postConcatonateInputs(oldF1NumInputs);
        }
        
        buildThisFromComposition(var, f1, f2);
        
        if (autoConcatonate)
            this.tree.collapseInput(var);
    }
    
    private void buildThisFromCompositionFast(int var, BDD f1, BDD f2)
    {
        // TODO The existing algorithm is O(m^2 n^2), where m and n are the
        // number of nodes in f1 and f2, respectively.  An O(m^2 n) algorithm
        // exists in Bryant.  It should be implemented here.
    }
    
    private void buildThisFromComposition(int var, BDD f1, BDD f2)
    {
        
        /* This algorithm uses the Shannon Expansion of boolean functions to
         * express the composition in terms of apply() and restrict().  If
         * f|var1=0 represents f.restrict(1, false), then the composition
         * f1|var=f2 = (f2 & f1|var=1) | (!f2 & f1|var=0).
         * 
         * See Randall E. Bryant, "Graph-Based Algorithms for Boolean Function
         * Manipulation," IEEE Transactions on Computers, 1986.
         * 
         * We simply run this expression.  This is not the fastest way to
         * do composition, but it's easier to understand/implement int terms of
         * apply() and restrict().
         */
        
        // Special case: If f2 is a constant, just restrict.
        if (f2.isConstant())
        {
            BDD f1_restricted = new BDD(f1, var, f2.tree.getRootNode().terminalValue);
            this.tree = new BDDTree(f1_restricted.tree);
            return;
        }           
        
        final BooleanOperator and = new BooleanOperator() {@Override public boolean operate(boolean x, boolean y) { return (x&y); }};
        final BooleanOperator or = new BooleanOperator() {@Override public boolean operate(boolean x, boolean y) { return (x|y); }};
        
        BDD f1_restrictedHigh = new BDD(f1, var, true);
        BDD f1_restrictedLow = new BDD(f1, var, false);
        BDD x = new BDD(and, f2, f1_restrictedHigh);
        BDD f2_not = new BDD(f2, true);
        BDD y = new BDD(and, f2_not, f1_restrictedLow);
        
        this.tree = new BDDTree(x.tree.getNumInputs());
        HashMap<Integer[], Integer> dynamicProgrammingMemory = new HashMap();
        apply(dynamicProgrammingMemory, or, x.tree, y.tree, x.tree.getRootIndex(), y.tree.getRootIndex());
    }
   
    @Override
    public int getNumInputs()
    {
        return this.tree.getNumInputs();
    }
    
    @Override
    public int getNumOutputs()
    {
        return 1;
    }
    
    public BDDTree getTree()
    {
        return this.tree;
    }
    
    /**
     *  Test whether this is equal to the reference BDD.  Runs a DFS-based
     *  rooted directed acyclic graph isomorphism algorithm, which
     *  is linear in the number of nodes.
     */
    @Override
    public boolean equals(Object reference)
    {
        if (! (reference instanceof BDD))
            return false;
        return this.tree.equals(((BDD)reference).getTree());
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 47 * hash + (this.tree != null ? this.tree.hashCode() : 0);
        return hash;
    }
    
    public boolean isConstant()
    {
        return (this.tree.getRootNode().isTerminal());
    }
    
    public void preConcatonateInputs(int numInputsToAdd)
    {
        this.tree.preConcatonateInputs(numInputsToAdd);
    }
    
    public void postConcatonateInputs(int numInputsToAdd)
    {
        this.tree.postConcatonateInputs(numInputsToAdd);
    }
    
    public void collapseInput(int var)
    {
        this.tree.collapseInput(var);
    }
    
    /**
     * Execute the boolean function represented by this BDD.
     */
    @Override
    public boolean[] execute(boolean[] input)
    {
        assert(input.length == this.tree.getNumInputs());
        Node currentNode = tree.getNode(tree.getRootIndex());
        for (int i = 0; i < input.length; i++)
        {
            if (currentNode.inputIndex == i)
                currentNode = (input[i] ? tree.getNode(currentNode.high) : tree.getNode(currentNode.low));
        }
        return new boolean[] { currentNode.terminalValue };
    }
    
    /**
     * Reduce this BDD into a compact representation.  Normally, BDDs
     * have memory complexity exponential in the number of inputs.  Reducing
     * them collapses isomorphic subgraphs (i.e. eliminates redundancy in the
     * decision tree) to make it much more compact.
     */
    public void reduce()
    {
        //TODO
    }
    
    
    /**
     * Build a new BDD by applying a boolean operator to two existing ones.
     * See Anderson (1997) for an explanation of this algorithm.
     */
    private void apply(HashMap dynamicProgrammingMemory, BooleanOperator op, BDDTree xTree, BDDTree yTree, int xIndex, int yIndex)
    {
        int newRoot = applyLoop(dynamicProgrammingMemory, op, xTree, yTree, xIndex, yIndex);
        tree.setRootIndex(newRoot); // Required in the case that the new tree returns false for all inputs (without this line it returns all trues!)
    }
    private int applyLoop(HashMap dynamicProgrammingMemory, BooleanOperator op, BDDTree xTree, BDDTree yTree, int xIndex, int yIndex)
    {
        Integer[] key = new Integer[] {xIndex,yIndex};
        Node x = xTree.getNode(xIndex);
        Node y = yTree.getNode(yIndex);
        int output;
        if (dynamicProgrammingMemory.containsKey(key))
            return (Integer) dynamicProgrammingMemory.get(key);
        else if (x.isTerminal() && y.isTerminal())
            output = (op.operate(x.terminalValue, y.terminalValue) ? 1 : 0);
        else if (x.inputIndex == y.inputIndex)
            output = mk(new Node(applyLoop(dynamicProgrammingMemory, op, xTree, yTree, x.low, y.low), applyLoop(dynamicProgrammingMemory, op, xTree, yTree, x.high, y.high), x.inputIndex));
        else if (x.inputIndex < y.inputIndex)
            output = mk(new Node(applyLoop(dynamicProgrammingMemory, op, xTree, yTree, x.low, yIndex), applyLoop(dynamicProgrammingMemory, op, xTree, yTree, x.high, yIndex), x.inputIndex));
        else //(x.inputIndex > y.inputIndex)
            output = mk(new Node(applyLoop(dynamicProgrammingMemory, op, xTree, yTree, xIndex, y.low), applyLoop(dynamicProgrammingMemory, op, xTree, yTree, xIndex, y.high), y.inputIndex));
        dynamicProgrammingMemory.put(key, output);
        return output;
    }
    
    
    /**
     * Build a new BDD by restricting one of the inputs of a pre-existing one.
     */
    private int restrict(BDDTree xTree, int currentIndex, int inputToFix, boolean value, HashMap<Integer, Integer> dpMemory)
    {
        /* We search for all nodes with Node.inputIndex = inputIndex and replace
         * them with their low- or high-child depending on the parity of value.
         * From Anderson (1997). */
        if (dpMemory.containsKey(currentIndex))
            return dpMemory.get(currentIndex);
        
        Node u = xTree.getNode(currentIndex);
        int returnValue = 0;
        
        if (u.inputIndex > inputToFix)
            returnValue = currentIndex;
        else if (u.inputIndex < inputToFix)
            returnValue = mk(new Node(restrict(xTree, u.low, inputToFix, value, dpMemory), restrict(xTree, u.high, inputToFix, value, dpMemory), u.inputIndex));
        else if (!value)
            returnValue = restrict(xTree, u.low, inputToFix, value, dpMemory);
        else
            returnValue = restrict(xTree, u.high, inputToFix, value, dpMemory);
        
        dpMemory.put(currentIndex, returnValue);
        return returnValue;
    }
    
    /**
     * Combination function for a bottom-up assembly of a BDD tree.
     * Again, see Anderson (1997) to understand why we do it this way.
     */
    private int mk(Node node)
    {
        if (node.high == node.low)
            return node.low;
        else if (tree.contains(node))
            return tree.getNodeIndex(node);
        else
            return tree.addNode(node);
    }
    
    @Override
    public String toDot(String name)
    {
        String dot = "digraph " + name + " {\n";
        dot += dotBody("");
        dot += "}";
        return dot;
    }
    
    @Override
    public String toSubDot(String prefix)
    {
        String dot = "subgraph cluster" + prefix + " {\n";
        dot += dotBody(prefix);
        dot += "}";
        return dot;
    }
    
    private String dotBody(String prefix)
    {
        String dot = "True [shape=box];\nFalse [shape=box];\n";
        int r = tree.getRootIndex();
        boolean[] visited = new boolean[r+1];
        for (int i = 0; i < r+1; i++)
            visited[i] = false;
        return dot + toDotDFS(r, prefix, tree.getNode(r), null, false, visited);
    }
    
    private String toDotDFS(int i, String prefix, Node currentNode, String parentName, boolean lowEdge, boolean[] visited)
    {
        String currentName = prefix + "Node" + i + "_Var" + currentNode.inputIndex;
        if (currentNode.isTerminal())
            return parentName + "->" + (currentNode.terminalValue ? "True" : "False") + (lowEdge ? "[style=dashed];\n" : ";\n");
        else
        {
            String output = "";
            if (parentName != null)
                output += parentName + "->" + currentName + (lowEdge ? "[style=dashed];\n" : ";\n");
            if (!visited[i])
            { // Only visit this nodes children if we haven't been down this route already
                output += toDotDFS(currentNode.low, prefix, tree.getNode(currentNode.low), currentName, true, visited);
                output += toDotDFS(currentNode.high, prefix, tree.getNode(currentNode.high), currentName, false, visited);
            }
            visited[i] = true;
            return output;
        }
            
    }
}
