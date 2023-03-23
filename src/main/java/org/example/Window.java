package org.example;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.processor.Processor;
import org.example.utils.Keyboard;
import org.example.utils.Screen;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Window extends Application {
    private static final double EMULATION_SPEED = 1.0 / 700; // 700 Hz
    private static final double TIMERS_SPEED = 1.0 / 60; // 60 Hz

    public static final int WINDOW_WIDTH = 512;
    public static final int WINDOW_HEIGHT = 256;

    private final Screen screen = new Screen();
    private final Processor processor = new Processor();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        byte[] program = getProgramFromFileChooser(stage);
        Scene scene = new Scene(screen, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setOnKeyPressed(Keyboard.getKeyEventHandler(processor));
        scene.setOnKeyReleased(Keyboard.getKeyEventHandler(processor));
        stage.setScene(scene);
        stage.setTitle("Chip-8");
        stage.show();

        processor.loadMemory(program);
        setTimelines();
    }

    private byte[] getProgramFromFileChooser(Stage stage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Chip-8", "*.ch8");
        fileChooser.getExtensionFilters().add(extensionFilter);
        Path filePath = fileChooser.showOpenDialog(stage).toPath();
        return Files.readAllBytes(filePath);
    }

    private void setTimelines() {
        Timeline timersTimeline = new Timeline();
        timersTimeline.setCycleCount(Animation.INDEFINITE);
        timersTimeline.getKeyFrames().add(new KeyFrame(
                Duration.seconds(TIMERS_SPEED),
                t -> {
                    processor.decrementTimers();
                    if (processor.isScreenUpdated()) {
                        boolean[][] screenContent = processor.getScreen();
                        screen.draw(screenContent);
                    }
                    if (processor.isSound()) Toolkit.getDefaultToolkit().beep();
                }
        ));
        timersTimeline.play();
        Timeline cycleTimeline = new Timeline();
        cycleTimeline.setCycleCount(Animation.INDEFINITE);
        cycleTimeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(EMULATION_SPEED), t -> processor.doCycle()));
        cycleTimeline.play();
    }
}
