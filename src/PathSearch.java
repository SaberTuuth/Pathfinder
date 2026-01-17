import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathSearch {

    public class SearchNode {
        public Tile tile;
        public List<SearchNode> neighbors;

        public SearchNode(Tile tile) {
            this.tile = tile;
            this.neighbors = new ArrayList<>();
        }
    }

    public class PathNode{
        public SearchNode searchNode;
        public PathNode parent;
        public double heuristicCost; // h
        public double givenCost;     // g
        public double finalCost;     // f = g + h

        // Add other cost variables as needed
        public PathNode(SearchNode searchNode, PathNode parent) {
            this.searchNode = searchNode;
            this.parent = parent;
        }
    }

    public Map<Tile, SearchNode> Nodes;
    public Map<SearchNode, PathNode> VisitedNodes;
    private static final int[][] DIRS_4 = {
            { 0, -1 }, // up
            { 0,  1 }, // down
            { -1, 0 }, // left
            { 1,  0 }  // right
    };

    Grid TileGrid;
    SearchNode StartTile;
    SearchNode GoalTile;

    public PathSearch(){
        TileGrid = null;
        StartTile = null;
        GoalTile = null;
        Nodes = new HashMap<>();
        VisitedNodes = new HashMap<>();
    }

    public void Initialize(Grid grid){
        TileGrid = grid;

        for(int x = 0; x < TileGrid.GetSize(); x++){
            for(int y = 0; y < TileGrid.GetSize(); y++){
                Tile currentTile = TileGrid.GetTile(x, y);
                if(currentTile != null){ // TODO: add check for weight later
                    SearchNode searchNode = new SearchNode(currentTile);
                    Nodes.put(currentTile, searchNode);
                }
            }
        }
        // calculate directions
    }
}
