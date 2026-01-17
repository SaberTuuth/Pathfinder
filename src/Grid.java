public class Grid {

    private int size;
    private final Tile[][] tiles;

    public Grid(int Size) {
        size = Size;
        tiles = new Tile[size][size];
        for(int x = 0; x < size; x++){
            for(int y = 0; y < size; y++) {
                tiles[x][y] = new Tile(x, y);
            }
        }
    }

    public Tile GetTile(int x, int y){
        if((0 <= x) && (0 <= y) && (x < size) && (y < size)){
            return tiles[x][y];
        }
        else{
            return null;
        }
    }

    public int GetSize(){
        return size;
    }

    public void SetSize(int Size){
        size = Size;
    }
}

