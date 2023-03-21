package org.example;

import org.example.processor.Processor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.example.processor.Processor.*;

public class Window {
    private static final String FILENAME = "test_roms/test_opcode.ch8";
    private static final int CYCLES_TO_EXECUTE = 220;
    private static final char PIXEL_ON_CHAR = ' ';
    private static final char PIXEL_OFF_CHAR = '█';
    
    private static int stepCounter = 0;

    private static Processor processor;

    public static void main(String[] args) throws IOException, URISyntaxException {
        byte[] program = loadProgram(FILENAME);
        processor = new Processor(program);
        dumpMemory(false);
        System.out.println("--- RUN PROGRAM ---");
        for (int i = 0; i < CYCLES_TO_EXECUTE; i++) {
            System.out.printf("Step: %d %s%n", stepCounter++, processor);
            processor.doCycle();
        }
        System.out.println("--- END PROGRAM ---");
        dumpScreen();
    }

    private static byte[] loadProgram(String filename) throws IOException, URISyntaxException {
        System.out.println("--- LOADING PROGRAM ---");
        Path path = Path.of(ClassLoader.getSystemResource(filename).toURI());
        return Files.readAllBytes(path);
    }

    private static void dumpMemory(boolean dumpAll) {
        System.out.println("--- MEMORY DUMP ---");
        int[] memory = processor.getMemory();
        int startAddress = dumpAll ? 0x0 : FIRST_PROG_INSTR_ADDRESS;
        for (int index = startAddress; index < memory.length; index += 2) {
            int code = (memory[index] << 8) | memory[index + 1];
            if (code == 0) {
                continue;
            }
            System.out.printf("%03x: %04x%n", index, code);
        }
    }

    private static void dumpScreen() {
        System.out.println("--- SCREEN DUMP ---");
        boolean[][] display = processor.getDisplay();
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            StringBuilder screenRow = new StringBuilder();
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                screenRow.append(display[x][y] ? PIXEL_ON_CHAR : PIXEL_OFF_CHAR);
            }
            System.out.println(screenRow);
        }
    }
}
