import java.util.*;

public class PathSearch {

    public enum Directions {
        SQUARE_FOUR_DIR,
        SQUARE_EIGHT_DIR
    }

    public class SearchNode {
        public Tile tile;
        public List<SearchNode> neighbors;

        public SearchNode(Tile tile) {
            this.tile = tile;
            this.neighbors = new ArrayList<>();
        }
    }

    public class PathNode {
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
            {0, -1}, // up
            {0, 1}, // down
            {-1, 0}, // left
            {1, 0}  // right
    };
    private static final int[][] DIRS_8 = {
            {0, -1},    // up
            {0, 1},     // down
            {-1, 0},    // left
            {1, 0},     // right
            {-1, -1},
            {-1, 1},
            {1, -1},
            {1, 1}
    };

    Grid TileGrid;
    SearchNode StartTile;
    SearchNode GoalTile;
    Queue<PathNode> openQueue = new ArrayDeque<>();
    private List<Tile> finalPath = new ArrayList<>();

    public PathSearch() {
        TileGrid = null;
        StartTile = null;
        GoalTile = null;
        Nodes = new HashMap<>();
        VisitedNodes = new HashMap<>();
    }

    public void Initialize(Grid grid) {
        TileGrid = grid;

        for (int x = 0; x < TileGrid.GetSize(); x++) {
            for (int y = 0; y < TileGrid.GetSize(); y++) {
                Tile currentTile = TileGrid.GetTile(x, y);
                if (currentTile != null) { // TODO: add check for weight later
                    SearchNode searchNode = new SearchNode(currentTile);
                    Nodes.put(currentTile, searchNode);
                }
            }
        }
        // calculate directions
        for (int x = 0; x < TileGrid.GetSize(); x++) {
            for (int y = 0; y < TileGrid.GetSize(); y++) {
                Tile currentTile = TileGrid.GetTile(x, y);
                if (currentTile == null || !Nodes.containsKey(currentTile)) {
                    continue;
                }
                SearchNode currentNode = Nodes.get(currentTile);
                for (int[] dir : DIRS_8) {
                    int dx = x + dir[0];
                    int dy = y + dir[1];
                    if (dx >= 0 && dx < TileGrid.GetSize() &&
                            dy >= 0 && dy < TileGrid.GetSize()) {
                        Tile neighborTile = TileGrid.GetTile(dx, dy);
                        if (neighborTile != null && Nodes.containsKey(neighborTile)) {// TODO: add check for weights later
                            SearchNode neighborNode = Nodes.get(neighborTile);
                            currentNode.neighbors.add(neighborNode);
                        }
                    }
                }
            }
        }
    }

    public void Enter(int startX, int startY, int goalX, int goalY) {
        StartTile = Nodes.get(TileGrid.GetTile(startX, startY));
        GoalTile = Nodes.get(TileGrid.GetTile(goalX, goalY));
        StartTile.tile.isStart = true;
        GoalTile.tile.isGoal = true;
        if (!VisitedNodes.isEmpty()) {
            Shutdown();
        }
        PathNode startNode = new PathNode(StartTile, null);
        VisitedNodes.put(StartTile, startNode);
        openQueue.add(startNode);
    }

    public boolean UpdateStep() {
        if (openQueue.isEmpty()) {
            return false; // finished
        }
        PathNode currentNode = openQueue.poll();

        // Move current node to CLOSED set
        Tile currentTile = currentNode.searchNode.tile;
        currentTile.inOpenSet = false;
        currentTile.inClosedSet = true;

        if (currentNode.searchNode == GoalTile) {
            Exit();
            return false;
        }
        for (SearchNode neighborNode : currentNode.searchNode.neighbors) {
            if (!VisitedNodes.containsKey(neighborNode)) {
                // Create new path node
                PathNode nextNode = new PathNode(neighborNode, currentNode);

                // Mark visited immediately
                VisitedNodes.put(neighborNode, nextNode);
                neighborNode.tile.inOpenSet = true;

                openQueue.add(nextNode);
            }
        }
        return true;
    }


    private void Exit() {
        finalPath.clear();

        PathNode current = VisitedNodes.get(GoalTile);
        while (current != null) {
            finalPath.add(current.searchNode.tile);
            current.searchNode.tile.inFinalPath = true;
            current = current.parent;
        }

        Collections.reverse(finalPath);
        openQueue.clear();
    }

    private void Shutdown() {
        VisitedNodes.clear();
    }

}
