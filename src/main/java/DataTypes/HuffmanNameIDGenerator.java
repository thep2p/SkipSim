package DataTypes;

import SkipGraph.SkipGraphOperations;
import SkipGraph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author Yahya Hassanzadeh
 * This class only used to generate NameID based of the LMDS algorithm for the algorithm 8
 */

abstract class HuffmanTree implements Comparable<HuffmanTree>
{
    public final int frequency; // the frequency of this tree
    public HuffmanTree(int freq) { frequency = freq; }

    // compares on the frequency
    public int compareTo(HuffmanTree tree) {
        return frequency - tree.frequency;
    }
}

class HuffmanLeaf extends HuffmanTree {
    public final int value; // the character this leaf represents

    public HuffmanLeaf(int freq, int val)
    {
        super(freq);
        value = val;
    }
}

class HuffmanNode extends HuffmanTree {
    public final HuffmanTree left, right; // subtrees

    public HuffmanNode(HuffmanTree l, HuffmanTree r) {
        super(l.frequency + r.frequency);
        left = l;
        right = r;
    }
}

public class HuffmanNameIDGenerator
	{
	    private static final Logger log = LoggerFactory.getLogger(HuffmanNameIDGenerator.class);

	    // input is an array of frequencies, indexed by character code
	    public HuffmanTree buildTree(int[] charFreqs)
	    {
	        PriorityQueue<HuffmanTree> trees = new PriorityQueue<HuffmanTree>();
	        // initially, we have a forest of leaves
	        // one for each non-empty character
	        for (int i = 0; i < charFreqs.length; i++)
	            if (charFreqs[i] > 0)
	                trees.offer(new HuffmanLeaf(charFreqs[i], i));

	        assert trees.size() > 0;
	        // loop until there is only one tree left
	        while (trees.size() > 1) {
	            // two trees with least frequency
	            HuffmanTree a = trees.poll();
	            HuffmanTree b = trees.poll();

	            // put into new SkipGraph.Node and re-insert into queue
	            trees.offer(new HuffmanNode(a, b));
	        }
	        return trees.poll();
	}

	    public void printCodes(HuffmanTree tree, StringBuffer prefix, SkipGraphOperations sg) {
	        assert tree != null;
	        if (tree instanceof HuffmanLeaf)
	        {
	            HuffmanLeaf leaf = (HuffmanLeaf)tree;

	            // print out character, frequency, and code for this leaf (which is just the prefix)
	            log.debug("Huffman: frequency={} code={}", leaf.frequency, prefix);
				((Node) sg.getTG().mNodeSet.getNode(leaf.value)).setNameID(prefix.toString());

	        } else if (tree instanceof HuffmanNode) {
	            HuffmanNode node = (HuffmanNode)tree;

	            // traverse left
	            prefix.append('0');
	            printCodes(node.left, prefix,sg);
	            prefix.deleteCharAt(prefix.length()-1);

	            // traverse right
	            prefix.append('1');
	            printCodes(node.right, prefix, sg);
	            prefix.deleteCharAt(prefix.length()-1);
	        }
	    }


	    public void buildThePrefix(int[] freq,SkipGraphOperations sg)
	    {
	    	HuffmanTree tree = buildTree(freq);
	        log.debug("WEIGHT\tHUFFMAN CODE");
	        printCodes(tree, new StringBuffer(),sg);
	    }

}
