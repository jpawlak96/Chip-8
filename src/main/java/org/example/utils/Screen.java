package org.example.utils;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static org.example.Window.WINDOW_HEIGHT;
import static org.example.Window.WINDOW_WIDTH;
import static org.example.processor.Processor.SCREEN_HEIGHT;
import static org.example.processor.Processor.SCREEN_WIDTH;

public class Screen extends Group {
    private static final int RECTANGLE_WIDTH = WINDOW_WIDTH / SCREEN_WIDTH;
    private static final int RECTANGLE_HEIGHT = WINDOW_HEIGHT / SCREEN_HEIGHT;

    private final Rectangle[][] rectanglePool = new Rectangle[SCREEN_WIDTH][SCREEN_HEIGHT];

    public Screen() {
        for (int x = 0; x < SCREEN_WIDTH; x++) {
            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                Rectangle rectangle = new Rectangle(RECTANGLE_WIDTH * x, RECTANGLE_HEIGHT * y,
                        RECTANGLE_WIDTH, RECTANGLE_HEIGHT);
                rectangle.setStroke(Color.BLACK);
                getChildren().add(rectangle);
                rectanglePool[x][y] = rectangle;
            }
        }
    }

    public void draw(boolean[][] screen) {
        for (int x = 0; x < SCREEN_WIDTH; x++) {
            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                Color fill = screen[x][y] ? Color.WHITE : Color.BLACK;
                rectanglePool[x][y].setFill(fill);
            }
        }
    }
}
