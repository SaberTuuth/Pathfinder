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
                int q = x;
                int r = y - ((x - (x & 1)) / 2);

                tiles[x][y].q = q;
                tiles[x][y].r = r;
                if ((q % 2 == 0) && (r % 2 == 0))
                	 tiles[x][y].hexMazeCell = true;
                else
                	 tiles[x][y].hexMazeCell = false;
                if (x % 2 == 0 && y % 2 == 0)
                    tiles[x][y].triangleMazeCell = true;
                else
                    tiles[x][y].triangleMazeCell = false;
                tiles[x][y].up = (x + y) % 2 == 0;
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
    
    public Tile GetTileHex(int q, int r) {

	    // axial → offset (odd-q vertical layout)
	    int x = q;
	    int y = r + ((q - (q & 1)) / 2);

	    if (x < 0 || x >= GetWidth() || y < 0 || y >= GetHeight())
	        return null;

	    return GetTile(x, y);
	}
}

