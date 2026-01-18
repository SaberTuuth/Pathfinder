public class Tile {
    public boolean walkable = true;
    public boolean isStart = false;
    public boolean isGoal = false;

    public boolean inFinalPath = false;
    public boolean inOpenSet = false;     // frontier
    public boolean inClosedSet = false;   // visited / expanded
    public int row;
    public int colum;
    public double x;
    public double y;

    public Tile(){
        row = 0;
        colum = 0;
        x = 0;
        y =0;
    }
    public Tile(int Row, int Colum){
        row = Row;
        colum = Colum;
    }
}
