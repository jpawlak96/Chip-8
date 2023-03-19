package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessorTest {
    private static final int SECOND_PROG_INSTR_ADDRESS = Processor.FIRST_PROG_INSTR_ADDRESS + 2;

    private Processor processor;

    @BeforeEach
    public void initProcessor() {
        processor = new Processor();
    }

    @Test
    void shouldFetchCorrectAddressFromMemoryWhenFetch() {
        int address = 0x666;
        int opcode = 0xABCD;

        processor.memory[address] = opcode >> 8;
        processor.memory[address + 1] = opcode & 0x00FF;
        processor.programCounter = address;
        processor.fetchInstruction();

        assertEquals(opcode, processor.opcode);
    }

    @Test
    void shouldSetProgramCounterWhen1NNNOpcode() {
        int instruction = 0x1000;
        int address = 0x0123;

        processor.opcode = instruction | address;
        processor.decodeInstruction();

        assertEquals(address, processor.programCounter);
    }

    @Test
    void shouldSetRegisterWhen6XNNOpcode() {
        int instruction = 0x6000;
        int registerSelector = 0x0700;
        int value = 0x0023;

        processor.opcode = instruction | registerSelector | value;
        processor.decodeInstruction();

        assertEquals(value, processor.register[registerSelector >> 8]);
        assertEquals(SECOND_PROG_INSTR_ADDRESS, processor.programCounter);
    }

    @Test
    void shouldAddToRegisterWhen7XNNOpcode() {
        int instruction = 0x7000;
        int registerSelector = 0x0700;
        int value = 0x0056;
        int registerInitValue = 0x34;

        processor.register[registerSelector >> 8] = registerInitValue;
        processor.opcode = instruction | registerSelector | value;
        processor.decodeInstruction();

        assertEquals(value + registerInitValue, processor.register[registerSelector >> 8]);
        assertEquals(SECOND_PROG_INSTR_ADDRESS, processor.programCounter);
    }

    @Test
    void shouldSetIndexRegistryWhenANNNOpcode() {
        int instruction = 0xA000;
        int value = 0x0321;

        processor.opcode = instruction | value;
        processor.decodeInstruction();

        assertEquals(value, processor.indexRegister);
        assertEquals(SECOND_PROG_INSTR_ADDRESS, processor.programCounter);
    }

    @Test
    void shouldCleanScreenWhen00E0Opcode() {
        int instruction = 0x00E0;

        for (int x = 0; x < Processor.DISPLAY_WIDTH; x++) {
            for (int y = 0; y < Processor.DISPLAY_HEIGHT; y++) {
                processor.display[x][y] = true;
            }
        }
        processor.opcode = instruction;
        processor.decodeInstruction();

        boolean isEmpty = true;
        testLoop:
        for (int x = 0; x < Processor.DISPLAY_WIDTH; x++) {
            for (int y = 0; y < Processor.DISPLAY_HEIGHT; y++) {
                if (processor.display[x][y]) {
                    isEmpty = false;
                    break testLoop;
                }
            }
        }
        assertTrue(isEmpty);
        assertEquals(SECOND_PROG_INSTR_ADDRESS, processor.programCounter);
    }

    @Test
    void shouldDisplaySpriteWhenDXYNOpcode() {
        int instruction = 0xD000;
        int registerXSelector = 0x0900;
        int registerYSelector = 0x0080;
        int rows = 0x0004;
        int registerXInitValue = 0x04;
        int registerYInitValue = 0x04;
        int spriteInitAddress = 0x100;
        int[] sprite = new int[]{
                0xFF, // 11111111
                0x99, // 10011001
                0x99, // 10011001
                0xFF, // 11111111
        };

        System.arraycopy(sprite, 0, processor.memory, spriteInitAddress, sprite.length);
        processor.register[registerXSelector >> 8] = registerXInitValue;
        processor.register[registerYSelector >> 4] = registerYInitValue;
        processor.indexRegister = spriteInitAddress;
        processor.opcode = instruction | registerXSelector | registerYSelector | rows;
        processor.decodeInstruction();

        for (int y = 0; y < rows; y++) {
            int spriteRow = 0;
            for (int x = 0; x < 8; x++) {
                int displayValue = processor.display[registerXInitValue + x][registerYInitValue + y] ? 1 : 0;
                spriteRow += displayValue << x;
            }
            assertEquals(sprite[y], spriteRow);
        }
        assertEquals(SECOND_PROG_INSTR_ADDRESS, processor.programCounter);
        assertEquals(0, processor.register[0xF]);
    }
}
