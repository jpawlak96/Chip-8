package org.example.utils;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import org.example.processor.Processor;

import java.util.Map;

public class Keyboard {
    private static final Map<Integer, Integer> keyMap = Map.ofEntries(
            Map.entry(49, 0x1), Map.entry(50, 0x2), Map.entry(51, 0x3), Map.entry(52, 0xC), // 1, 2, 3, 4
            Map.entry(81, 0x4), Map.entry(87, 0x5), Map.entry(69, 0x6), Map.entry(82, 0xD), // Q, W, E, R
            Map.entry(65, 0x7), Map.entry(83, 0x8), Map.entry(68, 0x9), Map.entry(70, 0xE), // A, S, D, F
            Map.entry(90, 0xA), Map.entry(88, 0x0), Map.entry(67, 0xB), Map.entry(86, 0xF)  // Z, X, C, V
    );

    public static EventHandler<KeyEvent> getKeyEventHandler(Processor processor) {
        return event -> {
            Integer keyIndex = keyMap.get(event.getCode().getCode());
            if (keyIndex != null) {
                boolean isPressed = KeyEvent.KEY_PRESSED.equals(event.getEventType());
                processor.setKey(keyIndex, isPressed);
            }
        };
    }
}
