import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MazeGenerator {

	Stack<Tile> stack = new Stack<>();
	Tile current;
	int[][] dirs;
	Random rand = new Random();

	private static final int[][] Four_Dirs = { { 0, 2 }, { 0, -2 }, { 2, 0 }, { -2, 0 } };

	public static final int[][] HEX_DIRS = { 
			{ 1, 0 }, // east
			{ 1, -1 }, // northeast
			{ 0, -1 }, // northwest
			{ -1, 0 }, // west
			{ -1, 1 }, // southwest
			{ 0, 1 } // southeast
	};

	private void BlockAllTiles(Grid grid) {
		stack.clear();
		for (int x = 0; x < grid.GetWidth(); x++) {
			for (int y = 0; y < grid.GetHeight(); y++) {
				Tile t = grid.GetTile(x, y);
				if (t.isGoal || t.isStart) {
					continue;
				}
				if (t != null) {
					t.walkable = false;
					t.visitedMaze = false;
				}
			}
		}
	}

	public void Start(Grid grid) {
		BlockAllTiles(grid);
		current = grid.GetTile(0, 0);
		current.walkable = true;
		current.visitedMaze = true;
		stack.push(current);
	}

	public boolean MazeStep(Grid grid, PathSearch.Directions directions) {
		if (stack.isEmpty()) {
			return false; // Maze generation finished
		}

		// Pop the current tile
		current = stack.pop();

		// Get unvisited neighbors
		List<Tile> neighbors = new ArrayList<>();
		switch (directions) {
		case SQUARE_FOUR_DIR -> {
			dirs = Four_Dirs;
		}
		case SQUARE_EIGHT_DIR -> {
			dirs = Four_Dirs;
		}
		default -> throw new IllegalArgumentException("Unknown directions: " + dirs);
		}
		for (int[] d : dirs) {
			int dx = current.row + d[0];
			int dy = current.colum + d[1];
			if (dx >= 0 && dx < grid.GetWidth() && dy >= 0 && dy < grid.GetHeight()) {
				Tile neighbor = grid.GetTile(dx, dy);
				if (!neighbor.visitedMaze) {
					neighbors.add(neighbor);
				}
			}
		}
		if (!neighbors.isEmpty()) {
			// Push current back to stack (DFS behavior)
			stack.push(current);

			// Pick a random neighbor
			Tile chosen = neighbors.get((int) (Math.random() * neighbors.size()));

			// Carve wall between current and chosen
			int wallX = current.row + (chosen.row - current.row) / 2;
			int wallY = current.colum + (chosen.colum - current.colum) / 2;
			grid.GetTile(wallX, wallY).walkable = true;

			// Mark chosen as visited
			chosen.visitedMaze = true;
			chosen.walkable = true;

			// Push chosen onto stack
			stack.push(chosen);
		}

		return true; // Maze generation continues
	}

	public void ConnectSpecialTile(PathSearch.SearchNode node) {
		Tile tile = node.neighbors.get(rand.nextInt(node.neighbors.size())).tile;
		tile.walkable = true;
	}

	public boolean HexStep(Grid grid) {
		if (stack.isEmpty()) {
			return false;
		}

		current = stack.peek();

		List<Tile> neighbors = getUnvisitedNeighbors(current, grid);

		if (!neighbors.isEmpty()) {

			// choose random neighbor
			Tile next = neighbors.get(rand.nextInt(neighbors.size()));

			// break wall between them
			removeWall(current, next, grid);

			next.visitedMaze = true;
			next.walkable = true;

			stack.push(next);
		}
		else {
			stack.pop();
		}
		return true;
	}

	private List<Tile> getUnvisitedNeighbors(Tile t, Grid grid) {

		List<Tile> results = new ArrayList<>();

		for (int[] d : HEX_DIRS) {

			int nq = t.q + d[0] * 2;
			int nr = t.r + d[1] * 2;

			Tile neighbor = grid.GetTileHex(nq, nr);

			if (neighbor != null && !neighbor.visitedMaze && neighbor.mazeCell) {
				results.add(neighbor);
			}
		}

		return results;
	}
	
	private void removeWall(Tile a, Tile b, Grid grid) {

	    int midQ = (a.q + b.q) / 2;
	    int midR = (a.r + b.r) / 2;

	    Tile wall = grid.GetTileHex(midQ, midR);

	    if (wall != null) {
	        wall.walkable = true;
	        wall.visitedMaze = true;
	    }
	}

}
