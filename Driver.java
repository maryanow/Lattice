/* 
 * Usage 
 *    java Driver latticeListFilename lmScale outputDir
 * 
 *   latticeListFilename    a plaintext file with one line per lattice
 *                          each line contains two strings, separated by a space
 *                          the first string is the filename for a lattice file
 *                          the second string is the filename for a ref file
 *
 *   lmScale                a non-negative number that specifies how much to weight 
 *                          the "language model" score, relative to the 
 *                          "acoustic model" score
 *
 *   outputDir              a directory where output lattices and dot files will
 *                          be written, one for each line in the lattice list
 *
*/

public class Driver {
   public static void main(String[] args) {
    
      // Check and load arguments
      if(args.length != 3) {
         System.err.println("Error: Wrong number of arguments.");
         System.exit(2);
      }
   
      String latticeListFilename = args[0];
      double lmScale = Double.parseDouble(args[1]);
      String outputDir = args[2];
   
      // Read through latticeListFilename
      java.util.Scanner input = null;
      try {
         input = new java.util.Scanner(new java.io.File(latticeListFilename));
      } 
      catch( java.io.FileNotFoundException e ) {
         System.err.println("Error: Unable to open file " + latticeListFilename);
         System.exit(1);
      }
   
      int numFiles = 0;
      while( input.hasNext() ) {
         numFiles++;
      
         // Read next lattice file in latticeListFilename
         String latticeFilename = input.next();
         String refFilename = input.next(); 
        
         // Build the lattice
         Lattice lattice = new Lattice(latticeFilename);
         System.out.println("\nUtterance " + lattice.getUtteranceID());
      
         // Print reference text
         printReference(refFilename);
      
         // Decode, print best hypothesis and various statistics
         Hypothesis hypothesis = lattice.decode(lmScale);
         System.out.println("Hypothesis: " + hypothesis.getHypothesisString());
         System.out.println("Number of unique paths: " + lattice.countAllPaths());
         System.out.println("Lattice density: " + new java.text.DecimalFormat("0.000").format(lattice.getLatticeDensity()));
         //java.util.HashSet<String> words = lattice.uniqueWordsAtTime(0.5);
         //printWordSet(words, outputDir + slash + lattice.getUtteranceID() + ".wordsAtTime");
            
         System.out.print("Locations of -silence-: "); 
         lattice.printSortedHits("-silence-");
      
         // Write lattice to output dir in dot and lattice formats
         lattice.writeAsDot(outputDir + "/" + lattice.getUtteranceID() + ".dot");
         String latticeOutputFilename = outputDir + "/" + lattice.getUtteranceID() + ".lattice";
            
         if(latticeOutputFilename.equals(latticeFilename)) {
            System.out.println("Error: Output directory must not be the same as the input directory\n");
            System.exit(5);
         }
         lattice.saveAsFile(latticeOutputFilename);
      }
   }
                

   private static void printReference(String refFilename) {
      java.util.Scanner refInput = null;
        
      try {
         refInput = new java.util.Scanner(new java.io.File(refFilename));
      } 
      catch (java.io.FileNotFoundException e) {
         System.out.println("Error: Unable to open file " + refFilename);
         System.exit(1);
      }
        
      if (refInput.hasNext()) {
         System.out.println("Reference: " + refInput.nextLine());
      } 
      else {
         System.out.println("Reference: ");
      }
   }

   private static void printWordSet(java.util.HashSet<String> words, String outFilename) {
      if (words == null) {
         return;
      }	
   
      java.io.PrintStream output = null;
      try {
         output = new java.io.PrintStream(outFilename);
      } 
      catch (java.io.FileNotFoundException e) {
         System.out.println("Error: Unable to open file " + outFilename + " for writing");
         System.exit(1);
      }
        
      for (String w : words) {
         output.println(w);
      }
   }
}
