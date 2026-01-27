public class Grid {

    private int Width;
    private int Height;
    private final Tile[][] tiles;

    public Grid(int width, int height) {
        SetSize(width, height);
        tiles = new Tile[Width][Height];
        for(int x = 0; x < Width; x++){
            for(int y = 0; y < Height; y++) {
                tiles[x][y] = new Tile(x, y);
            }
        }
    }

    public Tile GetTile(int x, int y){
        if((0 <= x) && (0 <= y) && (x < Width) && (y < Height)){
            return tiles[x][y];
        }
        else{
            return null;
        }
    }

    public int GetWidth(){
        return Width;
    }

    public int GetHeight(){
        return Height;
    }

    public void SetSize(int width, int height){
        Width = width;
        Height = height;
    }
}

