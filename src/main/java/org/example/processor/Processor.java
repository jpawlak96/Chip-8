package org.example.processor;

import java.util.Random;

public class Processor {
    public static final int FIRST_PROG_INSTR_ADDRESS = 0x200;
    public static final int DISPLAY_WIDTH = 64;
    public static final int DISPLAY_HEIGHT = 32;

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

    int delayTimer;
    int soundTimer;

    int programCounter;
    int stackPointer;
    int indexRegister;
    int opcode;

    int[] register;
    int[] stack;
    int[] memory;
    boolean[][] display;
    boolean[] keys;

    public Processor(byte[] program) {
        init(program);
    }
        
    public void init(byte[] program) {
        delayTimer = 0x0;
        soundTimer = 0x0;
        programCounter = FIRST_PROG_INSTR_ADDRESS;
        stackPointer = 0x0;
        indexRegister = 0x0;
        opcode = 0x0;
        register = new int[16];
        stack = new int[16];
        memory = new int[4096];
        display = new boolean[DISPLAY_WIDTH][DISPLAY_HEIGHT];
        keys = new boolean[16];

        loadMemory(program);
        cleanScreen();
    }

    public void doCycle() {
        fetchInstruction();
        decodeInstruction();
    }

    public int[] getMemory() {
        return memory;
    }

    public boolean[][] getDisplay() {
        return display;
    }

    public boolean isSound() {
        return soundTimer > 0;
    }

    public void decrementTimers() {
        if (delayTimer > 0)
            delayTimer--;
        if (soundTimer > 0)
            soundTimer--;
    }

    void loadMemory(byte[] program) {
        System.arraycopy(FONTS, 0, memory, 0, FONTS.length);
        for (int index = 0; index < program.length; index++) {
            memory[FIRST_PROG_INSTR_ADDRESS + index] = program[index] & 0xFF;
        }
    }

    void cleanScreen() {
        for (int y = 0; y < DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                display[x][y] = false;
            }
        }
    }

    int waitForInput() {
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
                programCounter = stack[--stackPointer];
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
                indexRegister = (indexRegister + register[x]) & 0xFFF;
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
                register[0xF] = 1;
                if (register[x] < 0x0) {
                    register[x] &= 0xFF;
                    register[0xF] = 0;
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
                register[0xF] = 1;
                if (register[x] < 0x0) {
                    register[x] &= 0xFF;
                    register[0xF] = 0;
                }
                return;
            case 0x800E:
                x = (opcode & 0x0F00) >>> 8;
                register[0xF] = (register[x] & 0x80) >>> 7;
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
                stack[stackPointer++] = programCounter;
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
                programCounter = (register[0x0] + (opcode & 0x0FFF)) & 0xFFF;
                return;
            case 0xC000:
                x = (opcode & 0x0F00) >>> 8;
                register[x] = randomGenerator.nextInt(0xFF + 1) & (opcode & 0x00FF);
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
