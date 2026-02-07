import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class MapIO {

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
                    } else if(c == 'N'){
                        tile.walkable = false;
                    }else {
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

    public static boolean save(File file, Grid grid) {

        if (grid == null || file == null)
            return false;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            int width = grid.GetWidth();
            int height = grid.GetHeight();

            // ----- WRITE SIZE -----
            writer.write(width + " " + height);
            writer.newLine();

            // ----- WRITE GRID -----
            for (int row = 0; row < height; row++) {

                StringBuilder line = new StringBuilder();

                for (int col = 0; col < width; col++) {

                    Tile tile = grid.GetTile(col, row);

                    if (tile == null || tile.blocked) {
                        line.append('0'); // wall
                    } else if(!tile.walkable){
                        line.append('N');
                    }else {
                        int w = tile.weight;

                        // safety: prevent weird values
                        if (w < 1) w = 1;
                        if (w > 9) w = 9;

                        line.append(w);
                    }
                }

                writer.write(line.toString());
                writer.newLine();
            }

            System.out.println("Map saved to: " + file.getAbsolutePath());
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
