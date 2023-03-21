package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

public class Processor {
    private static final String FILENAME = "IBMLogo.ch8";
    private static final int CYCLES_TO_EXECUTE = 22;
    private static final char PIXEL_ON_CHAR = ' ';
    private static final char PIXEL_OFF_CHAR = '█';

    static final int DISPLAY_WIDTH = 64;
    static final int DISPLAY_HEIGHT = 32;
    static final int FIRST_PROG_INSTR_ADDRESS = 0x200;

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

    private static final Random randomGenerator = new Random();

    private static int stepCounter = 0;

    final int[] register = new int[16];

    int delayTimer = 0x0;
    int soundTimer = 0x0;

    int programCounter = FIRST_PROG_INSTR_ADDRESS;
    int stackPointer = 0x0;
    int indexRegister = 0x0;
    int opcode = 0x0;

    final int[] stack = new int[16];
    final int[] memory = new int[4096];
    final boolean[][] display = new boolean[DISPLAY_WIDTH][DISPLAY_HEIGHT];
    final boolean[] keys = new boolean[16];

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

    public void decrementTimers() {
        if (delayTimer > 0)
            delayTimer--;
        if (soundTimer > 0)
            soundTimer--;
    }

    public boolean isSound() {
        return soundTimer > 0;
    }

    private void loadProgram(String filename) throws IOException, URISyntaxException {
        System.out.println("--- LOADING PROGRAM ---");
        Path path = Path.of(ClassLoader.getSystemResource(filename).toURI());
        byte[] program = Files.readAllBytes(path);
        System.arraycopy(FONTS, 0, memory, 0, FONTS.length);
        for (int index = 0; index < program.length; index++) {
            memory[FIRST_PROG_INSTR_ADDRESS + index] = program[index] & 0xFF;
        }
        System.out.println("--- LOADED ---");
    }

    private void dumpMemory(boolean dumpAll) {
        System.out.println("--- MEMORY DUMP ---");
        int startAddress = dumpAll ? 0x0 : FIRST_PROG_INSTR_ADDRESS;
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

    private int waitForInput() {
        return 0;
    }

    void fetchInstruction() {
        opcode = (memory[programCounter] << 8) | memory[programCounter + 1];
        programCounter += 2;
    }

    void decodeInstruction() {
        int x, y;
        switch (opcode) {
            case 0x00E0:
                cleanScreen();
                return;
            case 0x00EE:
                programCounter = stack[stackPointer];
                if (stackPointer > 0)
                    stackPointer--;
                return;
        }
        switch (opcode & 0xF0FF) {
            case 0xE09E:
                x = (opcode & 0x0F00) >>> 8;
                if (keys[register[x]])
                    programCounter += 2;
                return;
            case 0xE0A1:
                x = (opcode & 0x0F00) >>> 8;
                if (!keys[register[x]])
                    programCounter += 2;
                return;
            case 0xF007:
                x = (opcode & 0x0F00) >>> 8;
                register[x] = delayTimer;
                return;
            case 0xF00A:
                x = (opcode & 0x0F00) >>> 8;
                register[x] = waitForInput();
                return;
            case 0xF015:
                x = (opcode & 0x0F00) >>> 8;
                delayTimer = register[x];
                return;
            case 0xF018:
                x = (opcode & 0x0F00) >>> 8;
                soundTimer = register[x];
                return;
            case 0xF01E:
                x = (opcode & 0x0F00) >>> 8;
                indexRegister += register[x];
                return;
            case 0xF029:
                x = (opcode & 0x0F00) >>> 8;
                indexRegister = register[x] * 5;
                return;
            case 0xF033:
                x = (opcode & 0x0F00) >>> 8;
                memory[indexRegister] = register[x] / 100;
                memory[indexRegister + 1] = (register[x] - memory[indexRegister] * 100) / 10;
                memory[indexRegister + 2] = register[x] - memory[indexRegister] * 100 - memory[indexRegister + 1] * 10;
                return;
            case 0xF055:
                x = (opcode & 0x0F00) >>> 8;
                System.arraycopy(register, 0, memory, indexRegister, x + 1);
                return;
            case 0xF065:
                x = (opcode & 0x0F00) >>> 8;
                System.arraycopy(memory, indexRegister, register, 0, x + 1);
                return;
        }
        switch (opcode & 0xF00F) {
            case 0x5000:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                if (register[x] == register[y])
                    programCounter += 2;
                return;
            case 0x8000:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                register[x] = register[y];
                return;
            case 0x8001:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                register[x] |= register[y];
                return;
            case 0x8002:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                register[x] &= register[y];
                return;
            case 0x8003:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                register[x] ^= register[y];
                return;
            case 0x8004:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                register[x] = register[x] + register[y];
                if (register[x] > 0xFF) {
                    register[x] &= 0xFF;
                    register[0xF] = 1;
                }
                return;
            case 0x8005:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                register[x] = register[x] - register[y];
                if (register[x] < 0x0) {
                    register[x] &= 0xFF;
                    register[0xF] = 1;
                }
                return;
            case 0x8006:
                x = (opcode & 0x0F00) >>> 8;
                register[0xF] = register[x] & 0x01;
                register[x] = (register[x] / 2) & 0xFF;
                return;
            case 0x8007:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                register[x] = register[y] - register[x];
                if (register[x] < 0x0) {
                    register[x] &= 0xFF;
                    register[0xF] = 1;
                }
                return;
            case 0x800E:
                x = (opcode & 0x0F00) >>> 8;
                register[0xF] = register[x] & 0x80;
                register[x] = (register[x] * 2) & 0xFF;
                return;
            case 0x9000:
                x = (opcode & 0x0F00) >>> 8;
                y = (opcode & 0x00F0) >>> 4;
                if (register[x] != register[y])
                    programCounter += 2;
                return;
        }
        switch (opcode & 0xF000) {
            case 0x1000:
                programCounter = opcode & 0x0FFF;
                return;
            case 0x2000:
                stackPointer++;
                stack[stackPointer] = programCounter;
                programCounter = opcode & 0x0FFF;
                return;
            case 0x3000:
                x = (opcode & 0x0F00) >>> 8;
                if (register[x] == (opcode & 0x00FF))
                    programCounter += 2;
                return;
            case 0x4000:
                x = (opcode & 0x0F00) >>> 8;
                if (register[x] != (opcode & 0x00FF))
                    programCounter += 2;
                return;
            case 0x6000:
                x = (opcode & 0x0F00) >>> 8;
                register[x] = opcode & 0x00FF;
                return;
            case 0x7000:
                x = (opcode & 0x0F00) >>> 8;
                register[x] = (register[x] + (opcode & 0x00FF)) & 0xFF;
                return;
            case 0xA000:
                indexRegister = opcode & 0x0FFF;
                return;
            case 0xB000:
                programCounter = register[0x0] + (opcode & 0x0FFF);
                return;
            case 0xC000:
                x = (opcode & 0x0F00) >>> 8;
                register[x] = randomGenerator.nextInt(0xFF) & (opcode & 0x00FF);
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
                return;
        }
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
