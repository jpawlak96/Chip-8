package org.example;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.processor.Processor;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.example.processor.Processor.DISPLAY_HEIGHT;
import static org.example.processor.Processor.DISPLAY_WIDTH;

public class Window extends Application {
    private static final String FILENAME = "IBMLogo.ch8";

    private static final double EMULATION_SPEED = 1.0 / 700; // 700 Hz
    private static final double TIMERS_SPEED = 1.0 / 60; // 60 Hz

    private static final int SCREEN_WIDTH = 512;
    private static final int SCREEN_HEIGHT = 256;
    private static final int RECTANGLE_WIDTH = SCREEN_WIDTH / DISPLAY_WIDTH;
    private static final int RECTANGLE_HEIGHT = SCREEN_HEIGHT / DISPLAY_HEIGHT;

    private static final Map<Integer, Integer> keyMap = Map.ofEntries(
            Map.entry(49, 0x1),
            Map.entry(50, 0x2),
            Map.entry(51, 0x3),
            Map.entry(52, 0xC),

            Map.entry(81, 0x4),
            Map.entry(87, 0x5),
            Map.entry(69, 0x6),
            Map.entry(82, 0xD),

            Map.entry(65, 0x7),
            Map.entry(83, 0x8),
            Map.entry(68, 0x9),
            Map.entry(70, 0xE),

            Map.entry(90, 0xA),
            Map.entry(88, 0x0),
            Map.entry(67, 0xB),
            Map.entry(86, 0xF)
    );

    private final Rectangle[][] rectanglePool = new Rectangle[DISPLAY_WIDTH][DISPLAY_HEIGHT];
    private final Group rectanglesGroup = new Group();

    private static Processor processor;

    public static void main(String[] args) throws IOException, URISyntaxException {
        byte[] program = loadProgram(FILENAME);
        processor = new Processor(program);
        Application.launch(args);
    }

    private static byte[] loadProgram(String filename) throws IOException, URISyntaxException {
        Path path = Path.of(ClassLoader.getSystemResource(filename).toURI());
        return Files.readAllBytes(path);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(rectanglesGroup, SCREEN_WIDTH, SCREEN_HEIGHT);
        scene.setOnKeyPressed(getKeyEventEventHandler());
        scene.setOnKeyReleased(getKeyEventEventHandler());
        stage.setScene(scene);
        stage.setTitle("Chip-8");
        stage.show();

        fillRectanglePool();
        setTimelines();
    }

    private EventHandler<KeyEvent> getKeyEventEventHandler() {
        return event -> {
            Integer keyIndex = keyMap.get(event.getCode().getCode());
            if (keyIndex != null) {
                boolean isPressed = KeyEvent.KEY_PRESSED.equals(event.getEventType());
                processor.setKey(keyIndex, isPressed);
            }
        };
    }

    private void fillRectanglePool() {
        for (int x = 0; x < DISPLAY_WIDTH; x++) {
            for (int y = 0; y < DISPLAY_HEIGHT; y++) {
                Rectangle rectangle = new Rectangle(RECTANGLE_WIDTH * x, RECTANGLE_HEIGHT * y,
                        RECTANGLE_WIDTH, RECTANGLE_HEIGHT);
                rectangle.setStroke(Color.BLACK);
                rectanglesGroup.getChildren().add(rectangle);
                rectanglePool[x][y] = rectangle;
            }
        }
    }

    private void setTimelines() {
        Timeline timersTimeline = new Timeline();
        timersTimeline.setCycleCount(Animation.INDEFINITE);
        timersTimeline.getKeyFrames().add(new KeyFrame(
                Duration.seconds(TIMERS_SPEED),
                t -> {
                    processor.decrementTimers();
                    if (processor.isDisplayUpdated()) draw();
                    if (processor.isSound()) Toolkit.getDefaultToolkit().beep();
                }
        ));
        timersTimeline.play();
        Timeline cycleTimeline = new Timeline();
        cycleTimeline.setCycleCount(Animation.INDEFINITE);
        cycleTimeline.getKeyFrames().add(new KeyFrame(
                Duration.seconds(EMULATION_SPEED),
                t -> processor.doCycle()));
        cycleTimeline.play();
    }

    private void draw() {
        boolean[][] display = processor.getDisplay();
        for (int x = 0; x < DISPLAY_WIDTH; x++) {
            for (int y = 0; y < DISPLAY_HEIGHT; y++) {
                Color fill = display[x][y] ? Color.WHITE : Color.BLACK;
                rectanglePool[x][y].setFill(fill);
            }
        }
    }
}
