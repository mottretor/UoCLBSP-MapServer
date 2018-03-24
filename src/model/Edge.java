package model;

public strictfp class Edge  {
    private final int id;
    private final Vertex source;
    private final Vertex destination;
    private final double weight;

    public Edge(int id, Vertex source, Vertex destination, double weight) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }
    public Vertex getDestination() {
        return destination;
    }

    public Vertex getSource() {
        return source;
    }
    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }
    
    
}

