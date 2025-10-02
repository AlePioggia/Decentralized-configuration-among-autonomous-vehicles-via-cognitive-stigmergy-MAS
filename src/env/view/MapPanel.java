package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Map;

import javax.swing.JPanel;

import core.Cell;
import core.Position;
import road.Road;
import traffic.TrafficEnvironment;

public class MapPanel extends JPanel {
    private final TrafficEnvironment trafficEnvironment;
    private final int cellSize = 40;
    private final int margin = 40;

    public MapPanel(TrafficEnvironment trafficEnvironment) {
        this.trafficEnvironment = trafficEnvironment;
        Dimension dimension = new Dimension(
                trafficEnvironment.getGrid().getWidth() * cellSize + margin * 2,
                trafficEnvironment.getGrid().getHeight() * cellSize + margin * 2);
        setPreferredSize(dimension);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;

        int width = this.trafficEnvironment.getGrid().getWidth();
        int height = this.trafficEnvironment.getGrid().getHeight();

        g2D.setColor(Color.BLACK);
        g2D.fillRect(0, 0, getWidth(), getHeight());

        g2D.setColor(Color.WHITE);
        g2D.setFont(new Font("Arial", Font.PLAIN, 16));
        for (int x = 0; x < width; x++) {
            g2D.drawString(String.valueOf(x), margin + x * cellSize + cellSize / 2 - 8, margin - 10);
        }
        for (int y = 0; y < height; y++) {
            g2D.drawString(String.valueOf(y), 10, margin + y * cellSize + cellSize / 2 + 8);
        }

        for (Road road : this.trafficEnvironment.getRoads()) {
            for (Cell cell : road.getLines()) {
                int x = cell.getPosition().getX();
                int y = cell.getPosition().getY();
                Color roadColor = (cell.getDirection() != null) ? Color.WHITE : Color.DARK_GRAY;
                g2D.setColor(roadColor);
                g2D.fillRect(margin + x * cellSize, margin + y * cellSize, cellSize, cellSize);

                if (cell.getDirection() != null) {
                    int cx = margin + x * cellSize + cellSize / 2;
                    int cy = margin + y * cellSize + cellSize / 2;
                    int arrowLen = cellSize / 3;
                    int dx = 0, dy = 0;
                    switch (cell.getDirection().toLowerCase()) {
                        case "north":
                            dy = -arrowLen;
                            break;
                        case "south":
                            dy = arrowLen;
                            break;
                        case "east":
                            dx = arrowLen;
                            break;
                        case "west":
                            dx = -arrowLen;
                            break;
                    }
                    g2D.setColor(Color.GREEN.darker());
                    g2D.setStroke(new BasicStroke(3));
                    g2D.drawLine(cx, cy, cx + dx, cy + dy);

                    int arrSize = 6;
                    double angle = Math.atan2(dy, dx);
                    double sin = Math.sin(angle), cos = Math.cos(angle);
                    int px = cx + dx, py = cy + dy;
                    int x1 = (int) (px - arrSize * cos + arrSize * sin);
                    int y1 = (int) (py - arrSize * sin - arrSize * cos);
                    int x2 = (int) (px - arrSize * cos - arrSize * sin);
                    int y2 = (int) (py - arrSize * sin + arrSize * cos);
                    g2D.drawLine(px, py, x1, y1);
                    g2D.drawLine(px, py, x2, y2);
                    g2D.setStroke(new BasicStroke(1));
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = this.trafficEnvironment.getGrid().getCell(x, y);
                if (cell != null && cell.isOccupied()) {
                    g2D.setColor(Color.ORANGE);
                    g2D.fillRect(margin + x * cellSize, margin + y * cellSize, cellSize, cellSize);
                }
            }
        }

        for (Map.Entry<String, Position> entry : this.trafficEnvironment.getGoals().entrySet()) {
            String agentId = entry.getKey();
            Position goal = entry.getValue();
            String agentNum = agentId.replaceAll("\\D+", "");

            g2D.setColor(Color.RED);
            g2D.fillRect(margin + goal.getX() * cellSize, margin + goal.getY() * cellSize, cellSize, cellSize);
            g2D.setColor(Color.WHITE);
            g2D.drawString(agentNum, margin + goal.getX() * cellSize + 5, margin + goal.getY() * cellSize + 25);
        }

        for (Map.Entry<String, Position> entry : this.trafficEnvironment.getAgentPositions().entrySet()) {
            String agentId = entry.getKey();
            Position pos = entry.getValue();

            String agentNum = agentId.replaceAll("\\D+", "");
            g2D.setColor(Color.BLUE);
            g2D.fillRect(margin + pos.getX() * cellSize, margin + pos.getY() * cellSize, cellSize, cellSize);
            g2D.setColor(Color.WHITE);
            g2D.drawString(agentNum, margin + pos.getX() * cellSize + 5, margin + pos.getY() * cellSize + 25);
        }

        g2D.setColor(Color.GRAY);
        g2D.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                g2D.drawRect(margin + x * cellSize, margin + y * cellSize, cellSize, cellSize);
                g2D.drawString("(" + x + "," + y + ")", margin + x * cellSize + 4, margin + y * cellSize + 16);
            }
        }

    }

}
