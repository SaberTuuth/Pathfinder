import java.util.*;

public class PathSearch {

    public enum Directions {
        SQUARE_FOUR_DIR,
        SQUARE_EIGHT_DIR,
        HEX_SIX_DIR
    }

    public enum SearchMethod {
        BFS,
        DFS
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

    private static final int[][] DIRS_6_HEX_ODD = {
            {1, 0},
            {1, -1},
            {0, -1},
            {-1, 0},
            {0, 1},
            {1, 1}
    };


    private static final int[][] DIRS_6_HEX_EVEN = {
            {1, 0},
            {0, -1},
            {-1, -1},
            {-1, 0},
            {-1, 1},
            {0, 1}
    };


    public Directions DIRS = Directions.HEX_SIX_DIR;
    public SearchMethod Search = SearchMethod.BFS;
    Grid TileGrid;
    SearchNode StartTile;
    SearchNode GoalTile;
    Queue<PathNode> openQueue = new ArrayDeque<>();
    Stack<PathNode> openStack = new Stack<>();
    private final List<Tile> finalPath = new ArrayList<>();

    public PathSearch() {
        TileGrid = null;
        StartTile = null;
        GoalTile = null;
        Nodes = new HashMap<>();
        VisitedNodes = new HashMap<>();
    }

    public void Initialize(Grid grid) {
        TileGrid = grid;

        for (int x = 0; x < TileGrid.GetWidth(); x++) {
            for (int y = 0; y < TileGrid.GetHeight(); y++) {
                Tile currentTile = TileGrid.GetTile(x, y);
                if (currentTile != null) { // TODO: add check for weight later
                    SearchNode searchNode = new SearchNode(currentTile);
                    Nodes.put(currentTile, searchNode);
                }
            }
        }
        // calculate directions
        for (int x = 0; x < TileGrid.GetWidth(); x++) {
            for (int y = 0; y < TileGrid.GetHeight(); y++) {
                Tile currentTile = TileGrid.GetTile(x, y);
                if (currentTile == null || !Nodes.containsKey(currentTile)) {
                    continue;
                }
                SearchNode currentNode = Nodes.get(currentTile);
                int[][] neighborDirections;
                switch (DIRS) {
                    case SQUARE_FOUR_DIR -> neighborDirections = DIRS_4;
                    case SQUARE_EIGHT_DIR -> neighborDirections = DIRS_8;
                    case HEX_SIX_DIR -> {
                        if (y % 2 == 0) {
                            neighborDirections = DIRS_6_HEX_EVEN;
                        } else {
                            neighborDirections = DIRS_6_HEX_ODD;
                        }
                    }
                    default -> throw new IllegalArgumentException("Unknown directions: " + DIRS);
                }
                for (int[] dir : neighborDirections) {
                    int dx = x + dir[0];
                    int dy = y + dir[1];
                    if (dx >= 0 && dx < TileGrid.GetWidth() &&
                            dy >= 0 && dy < TileGrid.GetHeight()) {
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
        openStack.add(startNode);
    }

    public boolean UpdateStep() {
        switch (Search) {
            case BFS -> {
                return BreathFirst();
            }
            case DFS -> {
                return DepthFirst();
            }
        }
        return false;
    }

    private boolean BreathFirst() {
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
            if (!VisitedNodes.containsKey(neighborNode) && neighborNode.tile.walkable) {
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

    private boolean DepthFirst() {
        if (openStack.isEmpty()) {
            return false; // finished, no path
        }

        // 1. Get next node (LIFO)
        PathNode currentNode = openStack.pop();

        // 2. Mark closed
        Tile currentTile = currentNode.searchNode.tile;
        currentTile.inOpenSet = false;
        currentTile.inClosedSet = true;

        // 3. Goal check
        if (currentNode.searchNode == GoalTile) {
            Exit();
            return false;
        }

        // 4. Expand neighbors
        for (SearchNode neighborNode : currentNode.searchNode.neighbors) {

            // 🚫 Skip walls / blocked tiles
            if (!neighborNode.tile.walkable) continue;

            // Skip visited / closed
            if (neighborNode.tile.inClosedSet) continue;
            if (VisitedNodes.containsKey(neighborNode)) continue;

            // Create next node
            PathNode nextNode = new PathNode(neighborNode, currentNode);

            // Mark visited immediately
            VisitedNodes.put(neighborNode, nextNode);
            neighborNode.tile.inOpenSet = true;

            // DFS behavior: push onto stack
            openStack.push(nextNode);
        }

        return true; // continue searching
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

    public void ResetSearch() {

        // Clear open structures
        if (openQueue != null) openQueue.clear();
        if (openStack != null) openStack.clear();

        // Clear visited nodes
        VisitedNodes.clear();

        // Reset tiles
        for (SearchNode node : Nodes.values()) {
            Tile tile = node.tile;
            tile.inOpenSet = false;
            tile.inClosedSet = false;
        }

        // Re-add start node
        PathNode startNode = new PathNode(StartTile, null);
        VisitedNodes.put(StartTile, startNode);

        if (openQueue != null) {
            openQueue.add(startNode);   // BFS / Dijkstra / A*
        }
        if (openStack != null) {
            openStack.push(startNode);  // DFS
        }

        // Optional: clear final path visualization
        ClearPathVisuals();
    }

    public void ClearPathVisuals() {
        for (SearchNode node : Nodes.values()) {
            node.tile.inFinalPath = false;
        }
    }
}
