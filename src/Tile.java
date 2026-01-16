public class Tile {
    public boolean walkable = true;
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
    public Tile(int Row, int Colum, double X, double Y){
        row = Row;
        colum = Colum;
        x = X;
        y = Y;
    }
}
