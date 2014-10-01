/*	
 *	Lattice.java
 *
 *	Defines a new "Lattice"	type,	which	is	a directed acyclic graph that
 *	compactly represents	a very large space of speech recognition hypotheses
 *
 *	Note that the Lattice type	is	immutable: after the	fields are initialized
 *	in	the constructor, they cannot be modified.
 *
 *	Students	may only	use functionality	provided	in	the packages
 *		 java.lang
 *		 java.util 
 *		 java.io
 *		 
 *	as	well as the	class	java.math.BigInteger
 *	
 *	Use of any additional Java	Class	Library components is not permitted	
 *	
 *	Iain Maryanow
 *
 */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.math.BigInteger;

public class Lattice	{
   private	String utteranceID;		  // A unique ID for	the sentence
   private	int startIdx, endIdx;	  // Indices of the special start and end	tokens
   private	int numNodes, numEdges;	  // The	number of nodes and edges,	respectively
   private	Edge[][]	adjMatrix;		  // Adjacency	matrix representing the	lattice
												  //	 Two dimensional array of Edge objects
												  //	 adjMatrix[i][j] == null means no edge	(i,j)
   private	double[]	nodeTimes;		  // Stores	the timestamp for	each node
   private  BigInteger paths = new BigInteger("0");

	// Constructor

	// Lattice
	// Preconditions:
	//	  - latticeFilename contains the	path of a valid lattice	file
	// Post-conditions
	//	  - Field id is set to the	lattice's ID
	//	  - Field startIdx contains the node number for	the start node
	//	  - Field endIdx contains the	node number	for the end	node
	//	  - Field numNodes contains the number	of	nodes	in	the lattice
	//	  - Field numEdges contains the number	of	edges	in	the lattice
	//	  - Field adjMatrix encodes the edges in the	lattice:
	//		  If an edge exists from node	i to node j, adjMatrix[i][j] contains
	//		  the	address of an Edge object,	which	itself contains
   //			  1) The	edge's label (word)
	//			  2) The	edge's acoustic model score (amScore)
   //			  3) The	edge's language model score (lmScore)
	//		  If no edge exists from node	i to node j, adjMatrix[i][j] == null
	// Notes:
	//	  - If you encounter	a FileNotFoundException, print to standard error
	//			"Error: Unable	to	open file "	+ latticeFilename
	//		 and exit with	status (return	code)	1
	//	  - If you encounter	a NoSuchElementException, print to standard error
	//			"Error: Not	able to parse file "	+ latticeFilename
	//		 and exit with	status (return	code)	2
   public Lattice(String latticeFilename) {
   
   /*
   *  Steps through first five lines for ID, START, END, NUMNODES, NUMEDGES. These will be the same for all lattice files.
   *  Goes through the amounts of nodes for their times, adding them to the initialized array.
   *  Initializes adjMatrix to all null values.
   *  Creates a new edge object for the rest of the lines in the file. 'i' is the source, 'j' is the destination. 'j' is adjacent to 'i'.
   */
   
      try (Scanner input = new Scanner(new File(latticeFilename))) {
         String[] line;
         int lineCount = 0;
         
         while (lineCount <= 4) {
            line = input.nextLine().split(" ");
            
            if (lineCount == 0) {
               this.utteranceID = line[1];
            }
            else if (lineCount == 1) {
               this.startIdx = Integer.parseInt(line[1]);
            }
            else if (lineCount == 2) {
               this.endIdx = Integer.parseInt(line[1]);
            }
            else if (lineCount == 3) {
               this.numNodes = Integer.parseInt(line[1]);
            }
            else if (lineCount == 4) {
               this.numEdges = Integer.parseInt(line[1]);
            }    
            lineCount++;
         }
         
         nodeTimes = new double[numNodes];
         adjMatrix = new Edge[numNodes][numNodes];
         
         for (int i = 0; i < numNodes; i++) {
            line = input.nextLine().split(" ");
            nodeTimes[i] = Double.parseDouble(line[2]);
         }
         
         for (int row = 0; row < numNodes; row ++) {
            for (int col = 0; col < numNodes; col++) {
               adjMatrix[row][col] = null;
            }
         }
         
         while (input.hasNextLine()) {
            line = input.nextLine().split(" ");
            adjMatrix[Integer.parseInt(line[1])][Integer.parseInt(line[2])] = new Edge(line[3], Integer.parseInt(line[4]), Integer.parseInt(line[5]));
         }
         
      } 
      catch (FileNotFoundException e) {
         System.err.println("Error: Unable to open file " +	latticeFilename);
         System.exit(1); 
      } 
      catch (NoSuchElementException e) {
         System.err.println("Error: Not able to parse file " + latticeFilename);
         System.exit(1);
      }
      
      return;
   }
	 
	 // Accessors 

	 // getUtteranceID
	 // Pre-conditions:
	 //	 -	None
	 // Post-conditions:
	 //	 -	Returns the	utterance ID
   public String getUtteranceID() {
      return this.utteranceID;
   }

	 // getNumNodes
	 // Pre-conditions:
	 //	 -	None
	 // Post-conditions:
	 //	 -	Returns the	number of nodes in the lattice
   public int getNumNodes() {
      return this.numNodes;
   }

	 // getNumEdges
	 // Pre-conditions:
	 //	 -	None
	 // Post-conditions:
	 //	 -	Returns the	number of edges in the lattice
   public int getNumEdges() {
      return this.numEdges;
   }

	 // toString
	 // Pre-conditions:
	 //	 -	None
	 // Post-conditions:
	 //	 -	Constructs and	returns a string that is identical to the	contents
	 //		of	the lattice	file used in the constructor
	 // Notes:
	 //	 -	Do	not store the input string	verbatim: reconstruct it on the	fly
	 //		from the	class's fields
	 //	 -	toString	simply returns	a string, it should not	print	anything	itself
	 // Hints:
	 //	 -	new java.text.DecimalFormat("0.00").format(x) returns	the string
	 //		representation	of	floating	point	value	x with two decimal places
   public String toString() {
   
   /*
   *  Creates strings representing the fields that will have the same format for all lattice files (ID -> START -> END -> NODES -> EDGES).
   *  Then creates a string for all nodes with given time.  
   *  Searches through adjMatrix for any non-null spot, adds the edge to a string.
   *  Returns a string of all these fields together with new lines appropriately.
   */
   
      String allNodes = "";
      String allEdges = "";
      String id = "id " + this.utteranceID;
      String start = "start " + this.startIdx;
      String end = "end " + this.endIdx;
      String nodes = "numNodes " + this.numNodes;
      String edges = "numEdges " + this.numEdges;
      
      for (int i = 0; i < numNodes; i++) {
         allNodes += "node " + i + " " + new java.text.DecimalFormat("0.00").format(nodeTimes[i]) + "\n";
      }
      
      for (int row = 0; row < numNodes; row++) {
         for (int col = 0; col < numNodes; col++) {
            if (adjMatrix[row][col] != null) {
               allEdges += "edge " + row + " " + col + " " + adjMatrix[row][col].getLabel() + " " + adjMatrix[row][col].getAmScore() + " " + adjMatrix[row][col].getLmScore() + "\n";
            }  
         }
      }
      
      return (id + "\n" + start + "\n" + end + "\n" + nodes + "\n" + edges + "\n" + allNodes + allEdges);
   }

	 // decode
	 // Pre-conditions:
	 //	 -	lmScale specifies	how much	lmScore should	be	weighted
	 //		  the	overall weight	for an edge	is	amScore + lmScale	* lmScore
	 // Post-conditions:
	 //	 -	A new	Hypothesis object	is	returned	that contains the	shortest	path
	 //		(aka most probable path) from	the startIdx to the endIdx
	 // Hints:
	 //	 -	You can create	a new	empty	Hypothesis object	and then
	 //		repeatedly call Hypothesis's addWord method to add	the words and 
	 //		weights,	but this	needs	to	be	done in order (first	to	last word)
	 //		Backtracking will	give you	words	in	reverse order.
	 //	 -	java.lang.Double.POSITIVE_INFINITY represents positive infinity
   public Hypothesis decode(double lmScale)	{
   
   /*
   *  Initializes costs and parents of all nodes to infinity and -1(since there is no -1st node).
   *  Runs through all edges in adjMatrix to check for the lowest cost of getting from startIdx to the next 
   *   node in topologically sorted order.
   *  Backtracks through endIdx and all parents until startIdx, this gives the reverse order of the path.
   *  Adds the label of each edge in the path from last to first from the reverse order, giving the correct 
   *   order of the shortest path through the lattice.
   */
   
      double inf = java.lang.Double.POSITIVE_INFINITY;
      Hypothesis hypothesis = new Hypothesis();
      double[] cost = new double[this.numNodes];
      int[] parent = new int[this.numNodes];
      int[] topSort = this.topologicalSort();
      ArrayList<Integer> revOrder = new ArrayList<Integer>();
      Edge edge;
      int node;
      
      for (int i = 0; i < this.numNodes; i++) {
         cost[i] = inf;
         parent[i] = -1;
      }
      
      cost[this.startIdx] = 0;
      
      for (int i = 0; i < topSort.length; i++) {
         for (int n = 0; n < this.numNodes; n++) {
            edge = adjMatrix[n][i];
            if (edge != null && (((edge.getLmScore() * lmScale) + edge.getAmScore())) + cost[n] < cost[i] ) {
               cost[i] = ((edge.getLmScore() * lmScale) + edge.getAmScore()) + cost[n];
               parent[i] = n;
            }
         }
      }
      
      node = endIdx;
      while (node != startIdx) {
         revOrder.add(node);
         node = parent[node];
      }
      
      for (int i = revOrder.size() - 1; i >= 0; i--) {
         hypothesis.addWord(adjMatrix[parent[revOrder.get(i)]][revOrder.get(i)].getLabel(), cost[revOrder.get(i)]);
      }
   
      return hypothesis;
   }
	 
	 // topologicalSort
	 // Pre-conditions:
	 //	 -	None
	 // Post-conditions:
	 //	 -	A new	int[]	is	returned	with a topological sort	of	the nodes
	 //		For example, the 0'th element	of	the returned array has no 
	 //		incoming	edges.  More generally,	the node	in	the i'th	element 
	 //		has no incoming edges from	nodes	in	the i+1'th or later elements
   public int[] topologicalSort() {
   
   /*
   *  Go through nodes to find in-degrees. Add node to the ArrayList zeroIn if in-degree is 0. 
   *  Until zeroIn is empty, go through every node in zeroIn, adding it to the result ("order") and  
   *   decrementing all adjacent nodes. With this implementation, zeroIn will always take the numerically 
   *  lowest node.
   */
   
      int[] order = new int[this.numNodes];
      int[] inDegrees = new int[this.numNodes];   
      ArrayList<Integer> zeroIn = new ArrayList<Integer>();   
      int degreeCount;                     
      int orderCount = 0;        
                                             
      for (int col = 0; col < this.numNodes; col++) {     // In-degrees is the number that a node is adjacent to, so count all
         degreeCount = 0;                                 // nodes that are in each nodes' column.
         for (int row = 0; row < this.numNodes; row++) {
            if (adjMatrix[row][col] != null) {
               degreeCount++;
            }
         }
         inDegrees[col] = degreeCount;
      }      
      
      for (int i = 0; i < this.numNodes; i++) {           // zeroIn contains the node(s) that have inDegree of 0.
         if (inDegrees[i] == 0) {
            zeroIn.add(i);
         }
      }
      
      while (!zeroIn.isEmpty()) {
         order[orderCount] = zeroIn.get(0);
         zeroIn.remove(0);
         
         for (int i = 0; i < numNodes; i++) {               
            if (adjMatrix[order[orderCount]][i] != null) {  // Check node that was just pulled from zeroIn for any nodes that are adjacent to it.
               inDegrees[i]--;                              // Decrement the adjacent nodes' in-degree
               if (inDegrees[i] == 0) {                     // Add the adjacent node to zeroIn if it now has an in-degree of 0.
                  zeroIn.add(i);
               }
            }
         }         
         
         orderCount++;
      }
      
      return order;
   }

	 // countAllPaths
	 // Pre-conditions:
	 //	 -	None
	 // Post-conditions:
	 //	 -	Returns the	total	number of distinct paths from	startIdx	to	endIdx
	 // Hints:
	 //	 -	The straightforward recursive	traversal is prohibitively	slow
	 //	 -	This can	be	solved efficiently using something similar to the 
	 //		  shortest path algorithm used in decode
	 //		  Instead of min'ing	scores over	the incoming edges, you'll	want to 
	 //		  do some other operation...
   public java.math.BigInteger countAllPaths()	{
   
   /*
   *  The total paths in the graph is the sum of all paths to the end from all children from the start node.
   *  The total paths from all children to the end node is the sum of all their children to the end node, and so on. 
   */
      
      BigInteger paths = BigInteger.ZERO;
      BigInteger sum;
      BigInteger[] childPaths = new BigInteger[this.numNodes];
      int[] topSort = this.topologicalSort();
      
      for (int i = 0; i < this.numNodes - 1; i++) {
         childPaths[i] = BigInteger.ZERO;
      }
      childPaths[this.numNodes - 1] = BigInteger.ONE;
      
      for (int i = topSort.length - 2; i >= 0; i--) {
         sum = BigInteger.ZERO;
         
         for (int j = 0; j < this.numNodes; j++) {         
            if (adjMatrix[topSort[i]][j] != null) {
               childPaths[topSort[i]] = childPaths[topSort[i]].add(childPaths[j]);   
            }
         }   
         
      }
      
      return childPaths[startIdx];
   }

	 // getLatticeDensity
	 // Pre-conditions:
	 //	 -	None
	 // Post-conditions:
	 //	 -	Returns the	lattice density, which is defined to be:
	 //		(#	of	non -silence- words)	/ (# seconds from	start	to	end index)
   public double getLatticeDensity() {
   
   /* 
   *  getLatticeDensity() checks through the adjMatrix for all non -silence- words, keeping count along the way.
   *  Then divides the count of non -silence- words by the time between the startIdx and endIdx.
   */
   
      double totalTime = nodeTimes[endIdx] - nodeTimes[startIdx];
      int wordCount = 0;
      
      for (int i = 0; i < this.numNodes; i++) {
         for (int j = 0; j < this.numNodes; j++) {
            if (adjMatrix[i][j] != null && !adjMatrix[i][j].getLabel().equals("-silence-")) {
               wordCount++;
            }
         }
      }
      
      return (wordCount / totalTime);
   }

	 // writeAsDot	- write lattice in dot format
	 // Pre-conditions:
	 //	 -	dotFilename	is	the name	of	the intended output file
	 // Post-conditions:
	 //	 -	The lattice	is	written in the	specified dot format	to	dotFilename
	 // Notes:
	 //	 -	See the assignment description for the	exact	formatting to use
	 //	 -	For context	on	the dot format, see	  
	 //		  - http://en.wikipedia.org/wiki/DOT_%28graph_description_language%29
	 //		  - http://www.graphviz.org/pdf/dotguide.pdf
   public void writeAsDot(String dotFilename) {
   
   /*
   *  Given the dot format, writes, to the given file, the header followed by every edge's source node, destination node, and label.
   */
   
      try {
         PrintWriter writer = new PrintWriter(dotFilename);
         writer.print("digraph g {\n\trankdir=\"LR\"\n"); 
         
         for (int i = 0; i < this.numNodes; i++) {
            for (int j = 0; j < this.numNodes; j++) {
               if (adjMatrix[i][j] != null) {
                  writer.print("    " + i + " -> " + j + " [label = \"" + adjMatrix[i][j].getLabel() + "\"]\n");
               }  
            }
         }
         
         writer.print("}");
         writer.close();
      } catch (FileNotFoundException e) {
         System.err.println("Error: Unable to write to file " + dotFilename);
         System.exit(1); 
      }
      
   }

	 // saveAsFile	- write in the	simplified lattice format (same as input format)
	 // Pre-conditions:
	 //	 -	latticeOutputFilename is the name of the intended output	file
	 // Post-conditions:
	 //	 -	The lattice's toString() representation is written	to	the output file
	 // Note:
	 //	 -	This output	file should	be	identical to the original input .lattice file
   public void saveAsFile(String latticeOutputFilename) {
   
   /*
   *  Writes the lattice, in the original format implemented through this.toString(), to the given file.
   */
   
      try {
         PrintWriter writer = new PrintWriter(latticeOutputFilename);
         writer.print(this.toString());
         writer.close();
      } catch (FileNotFoundException e) {
         System.err.println("Error: Unable to write to file " + latticeOutputFilename);
         System.exit(1); 
      } 
        
   }

	 // uniqueWordsAtTime -	find all	words	at	a certain point in time
	 // Pre-conditions:
	 //	 -	time is the	time you	want to query
	 // Post-conditions:
	 //	 -	A HashSet is returned containing	all unique words that overlap	
	 //		with the	specified time
	 //	  (If	the time	is	not within the	time range of the	lattice,	the Hashset	should be empty)
   public java.util.HashSet<String> uniqueWordsAtTime(double time) { 
   
   /*
   *  Creates an empty HashSet in the case that no words are found for the time.
   *  Goes through every edge checking to see if the time parameter is between the source node and the destination node, inclusive to both.
   *  Adds the edge's label to the HashSet, automatically doesn't include more than one instance of a word.
   */
   
      HashSet<String> wordSet = new HashSet<String>(0);
      
      for (int row = 0; row < this.numNodes; row++) {
         for (int col = 0; col < this.numNodes; col++) {
            if (adjMatrix[row][col] != null) {
               if (nodeTimes[row] <= time && time <= nodeTimes[col]) {
                  wordSet.add(adjMatrix[row][col].getLabel());
               }
            }     
         }
      }   
   
      return wordSet;
   }

	 // printSortedHits - print in sorted order all	times	where	a given token appears
	 // Pre-conditions:
	 //	 -	word is the	word (or	multiword) that you want to find	in	the lattice
	 // Post-conditions:
	 //	 -	The midpoint (halfway between	start	and end time) for	each instance of word
	 //		in	the lattice	is	printed to two	decimal places	in	sorted (ascending) order
	 //		All times should be printed on the same line, separated by a single space character
	 //		(If no instances appear, nothing	is	printed)
	 // Note:
	 //	 - java.util.Arrays.sort can be used to sort
	 //	 -	new java.text.DecimalFormat("0.00").format(x) can be used to print to 2	decimal places
   public void printSortedHits(String word)	{
   
   /*
   *  Finds all times that a word appears, then gets the midpoint between the two connecting nodes for the edge.
   *  Sorts the array of hits and prints their times.
   */
   
      ArrayList<Double> hits = new ArrayList<Double>(); 
      double[] sortedHits;
      
      for (int row = 0; row < this.numNodes; row++) {
         for (int col = 0; col < this.numNodes; col++) {
            if (adjMatrix[row][col] != null && adjMatrix[row][col].getLabel().equals(word)) {
               hits.add((nodeTimes[col] + nodeTimes[row]) / 2);
            }  
         }
      }
      
      sortedHits = new double[hits.size()];
      for (int i = 0; i < sortedHits.length; i++) {
         sortedHits[i] = hits.get(i);
      }
      
      Arrays.sort(sortedHits);
      for (int i = 0; i < sortedHits.length; i++) {
         System.out.print(new java.text.DecimalFormat("0.00").format(sortedHits[i]) + " ");
      }  
      System.out.println();
      
   }
   
}
