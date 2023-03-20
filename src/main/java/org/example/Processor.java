package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Processor {
    private static final String FILENAME = "IBMLogo.ch8";
    private static final int CYCLES_TO_EXECUTE = 22;
    private static final char PIXEL_ON_CHAR = '■';
    private static final char PIXEL_OFF_CHAR = ' ';

    static final int DISPLAY_WIDTH = 64;
    static final int DISPLAY_HEIGHT = 32;
    static final int FIRST_PROG_INSTR_ADDRESS = 0x200;

    final int[] register = new int[16];
    int programCounter = FIRST_PROG_INSTR_ADDRESS;
    int indexRegister = 0x0;
    int opcode = 0x0;

    final int[] memory = new int[4096];
    final boolean[][] display = new boolean[DISPLAY_WIDTH][DISPLAY_HEIGHT];

    public static void main(String[] args) throws IOException, URISyntaxException {
        Processor processor = new Processor();
        processor.loadProgram(FILENAME);
        processor.dumpMemory();
        processor.cleanScreen();
        System.out.println("--- RUN PROGRAM ---");
        for (int i = 0; i < CYCLES_TO_EXECUTE; i++) {
            processor.fetchInstruction();
            processor.decodeInstruction();
        }
        System.out.println("--- END PROGRAM ---");
        processor.dumpScreen();
    }

    private void loadProgram(String filename) throws IOException, URISyntaxException {
        System.out.println("--- LOADING PROGRAM ---");
        Path path = Path.of(ClassLoader.getSystemResource(filename).toURI());
        byte[] program = Files.readAllBytes(path);
        for (int index = 0; index < program.length; index++) {
            memory[FIRST_PROG_INSTR_ADDRESS + index] = program[index] & 0xFF;
        }
        System.out.println("--- LOADED ---");
    }

    private void dumpMemory() {
        System.out.println("--- MEMORY DUMP ---");
        for (int index = FIRST_PROG_INSTR_ADDRESS; index < memory.length; index += 2) {
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

    void fetchInstruction() {
        opcode = (memory[programCounter] << 8) | memory[programCounter + 1];
    }

    void decodeInstruction() {
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
                register[temp] += (opcode & 0x00FF);
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
}
