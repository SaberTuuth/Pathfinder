public class Tile {
    public boolean walkable = true;
    public boolean isStart = false;
    public boolean isGoal = false;
    boolean blocked = false;

    public boolean inFinalPath = false;
    public boolean inOpenSet = false;     // frontier
    public boolean inClosedSet = false;   // visited / expanded
    public boolean visitedMaze = false;
    public boolean hexMazeCell;
    public boolean triangleMazeCell;
    public boolean up;
    public int row;
    public int colum;
    public int q;
    public int r;
    public int weight = 1;

    public Tile(int Row, int Colum){
        row = Row;
        colum = Colum;
    }
}
