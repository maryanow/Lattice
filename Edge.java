/* 
 * Edge.java
 *
 * Defines a new "Edge" type, which stores the information associated
 * with an edge in the lattice
 *
 */

public class Edge {
    private String label;          // The word associated with the edge
    private int amScore, lmScore;  // The acoustic and language model scores
                                   // (A speech recognizer trades off scores of
                                   //  these two models to find the best path)

    // Edge
    public Edge(String label, int amScore, int lmScore) {
        this.label = label;
        this.amScore = amScore;
        this.lmScore = lmScore;
        return;
    }

    // Edge - duplicates the content of another edge
    public Edge(Edge e) {
        this.label = e.getLabel();
        this.amScore = e.getAmScore();
        this.lmScore = e.getLmScore();
    }
    
    public String getLabel() {
        return this.label;
    }

    public int getLmScore() {
        return this.lmScore;
    }

    public int getAmScore() {
        return this.amScore;
    }
    
    // Return's the weighted sum of the two scores:
    //      amScore + lmScale * lmScore
    //  - This gives us a single weight for the edge, which is
    //    needed for finding the shortest path
    public int getCombinedScore(double lmScale) {
        return this.amScore + (int)(lmScale * this.lmScore);
    }
}
