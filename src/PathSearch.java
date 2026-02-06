import java.util.*;

public class PathSearch {

    public enum Directions {
        SQUARE_FOUR_DIR,
        SQUARE_EIGHT_DIR,
        HEX_SIX_DIR,
        TRIANGLE_TWELVE_DIR
    }

    public enum SearchMethod {
        BFS,
        DFS,
        GREEDY,
        UNIFORM,
        ASTAR
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
        public int heuristicCost; // h
        public int givenCost;     // g
        public int finalCost;     // f = g + h

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

    private static final int[][] DIRS_TRI_EVEN = {
            {0, -1},//up
            {1, -1}, // up right
            {-1, -1},// up left
            {0, 1}, // down
            {1, 0}, // right
            {-1, 0}, //left
            {1, 1}, // down right
            {-1, 1},
            {2, 0},
            {-2, 0},
            {-2, 1},
            {2, 1}
    };

    private static final int[][] DIRS_TRI_ODD = {
            {0, 1},
            {1, 0},
            {-1, 0},
            {1, -1},
            {-1, -1},
            {2, 0},
            {-2, 0},
            {1, 1},
            {-1, 1},
            {0, -1},
            {2, -1},
            {-2, -1}
    };


    // Axial directions for pointy-top hex grid
    static final int[][] AXIAL_DIRS = {
            {+1, 0},
            {+1, -1},
            {0, -1},
            {-1, 0},
            {-1, +1},
            {0, +1}
    };

    public Directions DIRS = Directions.SQUARE_FOUR_DIR;
    public static SearchMethod Search = SearchMethod.BFS;
    Grid TileGrid;
    public SearchNode StartTile;
    public SearchNode GoalTile;
    Queue<PathNode> openQueue = new ArrayDeque<>();
    Stack<PathNode> openStack = new Stack<>();
    PriorityQueue<PathNode> openPriorityQueue = new PriorityQueue<>(new GreedyComparator());

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
                currentTile.row = x;
                currentTile.colum = y;
                if (currentTile != null && currentTile.weight > 0) { // TODO: add check for weight later
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
                        // Convert offset (x,y) → axial (q,r)
                        int q = x;
                        int r = y - ((x - (x & 1)) / 2);

                        for (int[] d : AXIAL_DIRS) {
                            int nq = q + d[0];
                            int nr = r + d[1];

                            // Convert axial → offset
                            int dx = nq;
                            int dy = nr + ((nq - (nq & 1)) / 2);

                            if (dx >= 0 && dx < TileGrid.GetWidth() &&
                                    dy >= 0 && dy < TileGrid.GetHeight()) {

                                Tile neighborTile = TileGrid.GetTile(dx, dy);

                                if (neighborTile != null &&
                                        neighborTile.walkable &&
                                        Nodes.containsKey(neighborTile) &&
                                        neighborTile.weight > 0 &&
                                        !neighborTile.blocked) {

                                    currentNode.neighbors.add(Nodes.get(neighborTile));
                                }
                            }
                        }
                        continue;
                    }
                    case TRIANGLE_TWELVE_DIR -> {
                        if ((x + y) % 2 == 0) {
                            neighborDirections = DIRS_TRI_EVEN;
                        } else {
                            neighborDirections = DIRS_TRI_ODD;
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
                        if (neighborTile != null && neighborTile.walkable && Nodes.containsKey(neighborTile) && neighborTile.weight > 0 && !neighborTile.blocked) {// TODO: add check for weights later
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
        startNode.heuristicCost = heuristic(StartTile.tile, GoalTile.tile);
        startNode.givenCost = 0;
        startNode.finalCost = calculateFinalCost(startNode.givenCost, startNode.heuristicCost, startNode.searchNode.tile.weight);
        VisitedNodes.put(StartTile, startNode);
        openQueue.add(startNode);
        openStack.add(startNode);
        openPriorityQueue.add(startNode);
    }

    public boolean UpdateStep() {
        switch (Search) {
            case BFS -> {
                return BreathFirst();
            }
            case DFS -> {
                return DepthFirst();
            }
            case GREEDY -> {
                return GreedyBFS();
            }
            case UNIFORM -> {
                return UniformCostSearch();
            }
            case ASTAR -> {
                return AStarSearch();
            }
        }
        return false;
    }

    private boolean BreathFirst() {
        if (openQueue.isEmpty()) {
            return false; // finished
        }
        PathNode currentNode = openQueue.poll();
        Tile currentTile = currentNode.searchNode.tile;

        if (currentNode.searchNode == GoalTile) {
            Exit();
            return false;
        }
        for (SearchNode neighborNode : currentNode.searchNode.neighbors) {
            if (!neighborNode.tile.walkable) continue;
            if (neighborNode.tile.inClosedSet) continue;
            if (VisitedNodes.containsKey(neighborNode)) continue;
            // Create new path node
            PathNode nextNode = new PathNode(neighborNode, currentNode);

            VisitedNodes.put(neighborNode, nextNode);
            nextNode.parent = currentNode;
            neighborNode.tile.inOpenSet = true;

            openQueue.add(nextNode);

        }
        currentTile.inOpenSet = false;
        currentTile.inClosedSet = true;
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

    public boolean GreedyBFS() {
        if (openPriorityQueue.isEmpty()) {
            return false;
        }

        PathNode currentNode = openPriorityQueue.poll();
        Tile currentTile = currentNode.searchNode.tile;

        if (currentNode.searchNode == GoalTile) {
            Exit();
            return false;
        }

        for (SearchNode neighborNode : currentNode.searchNode.neighbors) {
            if (!neighborNode.tile.walkable) continue;
            if (neighborNode.tile.inClosedSet) continue;
            if (VisitedNodes.containsKey(neighborNode)) continue;

            PathNode nextNode = new PathNode(neighborNode, currentNode);
            nextNode.heuristicCost = heuristic(nextNode.searchNode.tile, GoalTile.tile);
            VisitedNodes.put(neighborNode, nextNode);
            neighborNode.tile.inOpenSet = true;
            openPriorityQueue.add(nextNode);
        }
        currentTile.inOpenSet = false;
        currentTile.inClosedSet = true;
        return true;
    }

    public boolean UniformCostSearch(){
        if(openPriorityQueue.isEmpty()){
            return false;
        }

        PathNode currentNode = openPriorityQueue.poll();
        Tile currentTile = currentNode.searchNode.tile;

        if (currentNode.searchNode == GoalTile) {
            Exit();
            return false;
        }

        for (SearchNode neighborNode : currentNode.searchNode.neighbors) {
            if (!neighborNode.tile.walkable) continue;
            if (neighborNode.tile.inClosedSet) continue;
            int newCost = currentNode.givenCost + neighborNode.tile.weight;

            // If we've seen this node before with a cheaper cost, skip it
            if (VisitedNodes.containsKey(neighborNode)) {
                int bestCost = VisitedNodes.get(neighborNode).givenCost;
                if (newCost >= bestCost) {
                    continue;
                }
            }

            PathNode nextNode = new PathNode(neighborNode, currentNode);
            nextNode.givenCost = newCost;
            neighborNode.tile.inOpenSet = true;
            VisitedNodes.put(neighborNode, nextNode);
            openPriorityQueue.add(nextNode);
        }
        currentTile.inOpenSet = false;
        currentTile.inClosedSet = true;
        return true;
    }

    public boolean AStarSearch(){
        if(openPriorityQueue.isEmpty()){
            return false;
        }

        PathNode currentNode = openPriorityQueue.poll();
        Tile currentTile = currentNode.searchNode.tile;

        if (currentNode.searchNode == GoalTile) {
            Exit();
            return false;
        }

        for (SearchNode neighborNode : currentNode.searchNode.neighbors) {
            if (!neighborNode.tile.walkable) continue;
            if (neighborNode.tile.inClosedSet) continue;
            int newCost = currentNode.givenCost + neighborNode.tile.weight;

            // If we've seen this node before with a cheaper cost, skip it
            if (VisitedNodes.containsKey(neighborNode)) {
                int bestCost = VisitedNodes.get(neighborNode).givenCost;
                if (newCost >= bestCost) {
                    continue;
                }
            }

            PathNode nextNode = new PathNode(neighborNode, currentNode);
            nextNode.givenCost = newCost;
            nextNode.heuristicCost = heuristic(nextNode.searchNode.tile, GoalTile.tile);
            nextNode.finalCost = calculateFinalCost(nextNode.givenCost, nextNode.heuristicCost, nextNode.searchNode.tile.weight);
            neighborNode.tile.inOpenSet = true;
            VisitedNodes.put(neighborNode, nextNode);
            openPriorityQueue.add(nextNode);
        }
        currentTile.inOpenSet = false;
        currentTile.inClosedSet = true;
        return true;
    }

    int calculateFinalCost(int givenCost, int distance, int weight)
    {
        return givenCost + (weight * distance);
    }

    int heuristic(Tile a, Tile b) {
        return Math.abs(a.row - b.row) + Math.abs(a.colum - b.colum);
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
        openPriorityQueue.clear();
        openStack.clear();
    }

    private void Shutdown() {
        VisitedNodes.clear();
        openQueue.clear();
        openStack.clear();
        openPriorityQueue.clear();

        for (SearchNode node : Nodes.values()) {
            Tile t = node.tile;
            t.inOpenSet = false;
            t.inClosedSet = false;
            t.inFinalPath = false;
        }
    }

    public void ResetSearch() {

        // Clear open structures
        if (openQueue != null) openQueue.clear();
        if (openStack != null) openStack.clear();
        if (openPriorityQueue != null) openPriorityQueue.clear();

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
        if (openPriorityQueue != null) {
            openPriorityQueue.add(startNode);  // DFS
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
