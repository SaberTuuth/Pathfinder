import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class MapIO {
    /**
     * Loads a grid from a file. Format:
     * - Lines starting with # or empty are skipped.
     * - A line with two integers: width height.
     * - Then height lines, each with width digit characters.
     *   '0' = unwalkable, '1'-'9' = walkable with that weight.
     */
    public static Grid load(File file) {
        try (Scanner scanner = new Scanner(file)) {
            int width = 0;
            int height = 0;

            // Skip comments and read width, height
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                try (Scanner lineScan = new Scanner(line)) {
                    if (!lineScan.hasNextInt()) continue;
                    width = lineScan.nextInt();
                    if (!lineScan.hasNextInt()) continue;
                    height = lineScan.nextInt();
                    break;
                }
            }

            if (width <= 0 || height <= 0) return null;

            // Skip comments before grid data
            String firstDataLine = null;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                firstDataLine = line;
                break;
            }
            if (firstDataLine == null) return null;

            Grid grid = new Grid(width, height);

            for (int row = 0; row < height; row++) {
                String rowLine = (row == 0) ? firstDataLine : (scanner.hasNextLine() ? scanner.nextLine().trim() : "");
                while ((rowLine.isEmpty() || rowLine.startsWith("#")) && scanner.hasNextLine()) {
                    rowLine = scanner.nextLine().trim();
                }
                for (int col = 0; col < width && col < rowLine.length(); col++) {
                    char c = rowLine.charAt(col);
                    Tile tile = grid.GetTile(col, row);
                    if (tile == null) continue;
                    if (c == '0') {
                        tile.blocked = true;
                        continue;
                    } else {
                        tile.walkable = true;
                        tile.weight = Character.isDigit(c) ? Character.digit(c, 10) : 1;
                    }
                }
            }

            return grid;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
