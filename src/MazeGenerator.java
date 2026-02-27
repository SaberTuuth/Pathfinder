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

	public static final int[][] HEX_DIRS = { { 1, 0 }, // east
			{ 1, -1 }, // northeast
			{ 0, -1 }, // northwest
			{ -1, 0 }, // west
			{ -1, 1 }, // southwest
			{ 0, 1 } // southeast
	};

	private static final int[][] TRI_UP = { { -2, 0 }, { 2, 0 }, { 0, 2 } };

	private static final int[][] TRI_DOWN = { { -2, 0 }, { 2, 0 }, { 0, -2 } };

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

	public void Start(Grid grid, int x, int y) {
		BlockAllTiles(grid);
		current = grid.GetTile(x, y);
		current.walkable = true;
		current.visitedMaze = true;
		stack.clear();
		stack.push(current);
	}

	public boolean MazeStep(Grid grid, PathSearch.Directions directions) {
		if (stack.isEmpty()) {
			return false;
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
		Tile tile = node.neighbors.get((int) (Math.random() * node.neighbors.size())).tile;
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
			Tile next = neighbors.get((int) Math.random() * neighbors.size());

			// break wall between them
			removeWall(current, next, grid);

			next.visitedMaze = true;
			next.walkable = true;

			stack.push(next);
		} /*else {
			stack.pop();
		}*/
		return true;
	}

	public boolean TriangleStep(Grid grid) {
		if (stack.isEmpty())
			return false;

		current = stack.peek();
		List<Tile> neighbors = getTriangleNeighbors(current, grid);

	    if (!neighbors.isEmpty()) {

	        Tile next = neighbors.get(rand.nextInt(neighbors.size()));
	        
	        removeTriangleWall(current, next, grid);

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

			if (neighbor != null && !neighbor.visitedMaze && neighbor.hexMazeCell) {
				results.add(neighbor);
			}
		}

		return results;
	}

	private List<Tile> getTriangleNeighbors(Tile t, Grid grid) {
		List<Tile> neighbors = new ArrayList<>();

		int[][] dirs = t.up ? TRI_UP : TRI_DOWN;

		for (int[] d : dirs) {

			int nx = t.row + d[0];
			int ny = t.colum + d[1];

			// Proper bounds check FIRST
			if (nx < 0 || nx >= grid.GetWidth() || ny < 0 || ny >= grid.GetHeight())
				continue;

			Tile neighbor = grid.GetTile(nx, ny);

			if (neighbor != null && !neighbor.visitedMaze && neighbor.triangleMazeCell) {
				neighbors.add(neighbor);
			}
		}

		return neighbors;
	}

	private void removeTriangleWall(Tile a, Tile b, Grid grid) {
		int midX =  (a.row + b.row) / 2;
		int midY =  (a.colum + b.colum) / 2;

		if (midX < 0 || midX >= grid.GetWidth() || midY < 0 || midY >= grid.GetHeight())
			return;

		Tile wall = grid.GetTile(midX, midY);
		if (wall != null) {
			wall.walkable = true;
			wall.visitedMaze = true;
		}
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
