/* 
 * Hypothesis.java
 *
 * Path that represent a hypothesized utterance, obtained by searching a lattice
 *
 */

public class Hypothesis {
    private double pathScore;                  // Cumulative path score
    private java.util.ArrayList<String> words; // Array of words in the path

    public Hypothesis() {
        words = new java.util.ArrayList<String>();
    }

    // If word equals "-silence-" then
    //   words is NOT modified: -silence- words are not included, but
    //   combinedScore is added to the pathScore
    // If word does not equal "-silence-" but DOES contain an underscore
    //   word is split into individual words at the underscore(s)
    //   each individual word is added, in sequence, to words
    // If word is not "-silence-" and DOES NOT contain an underscore
    //   word is added to the end of words, so that words is one longer
    //   the combinedScore is added to the pathScore
    public void addWord(String word, double combinedScore) {
        pathScore += combinedScore;
        if(!word.equals("-silence-")) {
            String[] parts = word.split("_");
            for(int i=0; i<parts.length; i++) {
                words.add(parts[i]);
            }
        }
    }

    public double getPathScore() {
        return this.pathScore;
    }

    // Returns the sentence constructed in this hypothesis.
    public String getHypothesisString() {
        String result = "";
        for( int i=0; i<words.size(); i++ ) {
            result += words.get(i) + " ";
        }    
        return result;
    }

    /*
    public double computeWER(String referenceFilename) {
        java.util.Scanner input = null;
        try {
            input = new java.util.Scanner(new java.io.File(referenceFilename));
        } catch( java.io.FileNotFoundException e ) {
            System.out.println("Error: File " + referenceFilename + " not found");
            System.exit(1);
        }

        java.util.ArrayList<String> reference = new java.util.ArrayList<String>();
        while( input.hasNext() ) {
            reference.add(input.next());
        }
        
        double[][] d = new double[words.size()+1][reference.size()+1];
        for( int i=0; i<=words.size(); i++ ) {
            d[i][0] = i;
        }
        for( int j=0; j<=reference.size(); j++ ) {
            d[0][j] = j;
        }
        for( int j=1; j<=reference.size(); j++ ) {
            for( int i=1; i<=words.size(); i++ ) {
                if( words.get(i-1).equals(reference.get(j-1)) ) {
                    d[i][j] = d[i-1][j-1];
                } else {
                    d[i][j] = 1+min3(d[i-1][j],d[i][j-1],d[i-1][j-1]);
                }
            }
        }

        return d[words.size()][reference.size()]/reference.size();
    }

    // Smallest of three values is returned.
    private double min3(double a, double b, double c) {
        if( a<b && a<c ) {
            return a;
        } else if( b<a && b<c ) {
            return b;
        } else {
            return c;
        }
    }
    */
}
