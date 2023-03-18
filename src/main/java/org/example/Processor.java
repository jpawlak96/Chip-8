package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class Processor {
    public static final String FILENAME = "IBMLogo.ch8";
    public static final int CYCLES_TO_EXECUTE = 22;
    
    private static final char PIXEL_ON_CHAR = '#';
    private static final char PIXEL_OFF_CHAR = ' ';

    private static final int DISPLAY_WIDTH = 64;
    private static final int DISPLAY_HEIGHT = 32;

    private static int stepCounter = 0;
    
    private static final int[] FONTS = new int[]{
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    private final int[] register = new int[16];

    private int delayTimer = 0x0;
    private int soundTimer = 0x0;

    private int programCounter = 0x200;
    private int stackPointer = 0x0;
    private int indexRegister = 0x0;
    private int opcode = 0x0;

    private final int[] memory = new int[4096];
    private final boolean[][] display = new boolean[DISPLAY_WIDTH][DISPLAY_HEIGHT];
    private final boolean[] keys = new boolean[16];

    private final Random randomGenerator = new Random();

    public static void main(String[] args) throws IOException, URISyntaxException {
        Processor processor = new Processor();
        processor.loadProgram(FILENAME);
        processor.dumpMemory(false);
        processor.cleanScreen();
        System.out.println("--- RUN PROGRAM ---");
        for (int i = 0; i < CYCLES_TO_EXECUTE; i++) {
            processor.fetchInstruction();
            System.out.printf("Step: %d %s%n", stepCounter++, processor);
            processor.decodeInstruction();
        }
        System.out.println("--- END PROGRAM ---");
        processor.dumpScreen();
    }

    private void loadProgram(String filename) throws IOException, URISyntaxException {
        System.out.println("--- LOADING PROGRAM ---");
        Path path = Path.of(ClassLoader.getSystemResource(filename).toURI());
        byte[] program = Files.readAllBytes(path);
        for (int index = 0; index < FONTS.length; index++) {
            memory[index] = FONTS[index];
        }
        for (int index = 0; index < program.length; index++) {
            memory[0x200 + index] = program[index] & 0xFF;
        }
        System.out.println("--- LOADED ---");
    }

    private void dumpMemory(boolean dumpAll) {
        System.out.println("--- MEMORY DUMP ---");
        int startAddress = dumpAll ? 0x0 : 0x200;
        for (int index = startAddress; index < memory.length; index += 2) {
            int code = (memory[index] << 8) | memory[index + 1];
            if (code == 0) {
                continue;
            }
            System.out.printf("%03x: %04x%n", index, code);
        }
    }

    private void dumpScreen() {
        System.out.println("--- SCREEN DUMP ---");
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            StringBuilder screenRow = new StringBuilder();
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                screenRow.append(display[x][y] ? PIXEL_ON_CHAR : PIXEL_OFF_CHAR);
            }
            System.out.println(screenRow);
        }
    }

    private void cleanScreen() {
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                display[x][y] = false;
            }
        }
    }

    private void fetchInstruction() {
        opcode = (memory[programCounter] << 8) | memory[programCounter + 1];
    }

    private void decodeInstruction() {
        int temp;
        switch (opcode) {
            case 0x00E0:
                cleanScreen();
                programCounter += 2;
                return;
        }
        switch (opcode & 0xF000) {
            case 0x1000:
                programCounter = opcode & 0x0FFF;
                return;
            case 0x6000:
                temp = (opcode & 0x0F00) >>> 8;
                register[temp] = opcode & 0x00FF;
                programCounter += 2;
                return;
            case 0x7000:
                temp = (opcode & 0x0F00) >>> 8;
                int result = register[temp] + (opcode & 0x00FF);
                if (result >= 256) {
                    register[temp] = result - 256;
                } else {
                    register[temp] = result;
                }
                programCounter += 2;
                return;
            case 0xA000:
                indexRegister = opcode & 0x0FFF;
                programCounter += 2;
                return;
            case 0xD000:
                int xPos = register[(opcode & 0x0F00) >>> 8] % DISPLAY_WIDTH;
                int yPos = register[(opcode & 0x00F0) >>> 4] % DISPLAY_HEIGHT;
                register[0xF] = 0;
                for (int row = 0; row < (opcode & 0x000F); row++) {
                    int spriteByte = memory[indexRegister + row];
                    int yOffset = (yPos + row) % DISPLAY_HEIGHT;
                    for (int column = 0; column < 8; column++) {
                        int color = spriteByte & (0x1 << (7 - column));
                        if (color > 0) {
                            int xOffset = (xPos + column) % DISPLAY_WIDTH;
                            if (display[xOffset][yOffset]) {
                                display[xOffset][yOffset] = false;
                                register[0xF] = 1;
                            } else {
                                display[xOffset][yOffset] = true;
                            }
                        }
                    }
                }
                programCounter += 2;
                return;
        }
        programCounter += 2;
    }

    @Override
    public String toString() {
        return "[registers=" + Arrays.toString(register) +
                ", programCounter=0x" + Integer.toHexString(programCounter) +
                ", opcode=0x" + Integer.toHexString(opcode) +
                ", indexRegister=0x" + Integer.toHexString(indexRegister) +
                ", stackPointer=0x" + Integer.toHexString(stackPointer) +
                ", delayTimer=0x" + Integer.toHexString(delayTimer) +
                ", soundTimer=0x" + Integer.toHexString(soundTimer) +
                "]";
    }
}
