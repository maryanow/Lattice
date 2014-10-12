/*	
 * Lattice.java
 *
 * Directed acyclic graph that represents a large space of speech recognition hypotheses.
 *
 */

import java.util.*;
import java.lang.*;
import java.io.*;
import java.math.BigInteger;

public class Lattice	{
   private String utteranceID;      
   private int startIdx, endIdx;	 
   private int numNodes, numEdges;	  
   private Edge[][] adjMatrix;		
   private double[] nodeTimes;		 
   private BigInteger paths = new BigInteger("0");

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
         System.err.println("Error: Unable to open file " + latticeFilename);
         System.exit(1); 
      } 
      catch (NoSuchElementException e) {
         System.err.println("Error: Not able to parse file " + latticeFilename);
         System.exit(1);
      }
   }
   
   public String getUtteranceID() {
      return this.utteranceID;
   }

   public int getNumNodes() {
      return this.numNodes;
   }

   public int getNumEdges() {
      return this.numEdges;
   }

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
   
   // A new Hypothesis object is returned	that contains the shortest path
   //  (most probable path) from the startIdx to the endIdx
   public Hypothesis decode(double lmScale)	{
   
   /*
   *  Initializes costs of all nodes to infinity and parents to -1.
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

   public int[] topologicalSort() {
   
   /*
   *  Add node to the ArrayList zeroIn if in-degree equals 0. 
   *  Until zeroIn is empty, go through every node in zeroIn, adding it to the result ("order") and  
   *  decrementing all adjacent nodes. With this implementation, zeroIn will always take the numerically 
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

   // Returns the lattice density:
   //	(# of non -silence- words) / (# seconds from start to end index)
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
