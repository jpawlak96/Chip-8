package org.example.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ProcessorTest {
    private final byte[] emptyProgram = new byte[]{};
    private Processor processor;

    @BeforeEach
    public void initProcessor() {
        processor = spy(new Processor(emptyProgram));
    }

    @Test
    void shouldCleanScreenWhenCleanScreenTriggered() {
        for (int x = 0; x < Processor.DISPLAY_WIDTH; x++) {
            for (int y = 0; y < Processor.DISPLAY_HEIGHT; y++) {
                processor.display[x][y] = true;
            }
        }

        processor.cleanScreen();

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
        assertEquals(address + 2, processor.programCounter);
    }

    @Test
    void shouldCleanScreenWhen00E0Opcode() {
        clearInvocations(processor);

        processor.opcode = 0x00E0;
        processor.decodeInstruction();

        verify(processor).cleanScreen();
    }

    @Test
    void shouldReturnFromSubroutineWhen00EEOpcode() {
        int instruction = 0x00EE;

        processor.stackPointer = 0x1;
        processor.stack[processor.stackPointer] = 0x1234;
        processor.opcode = instruction;
        processor.decodeInstruction();

        assertEquals(processor.stack[processor.stackPointer], processor.programCounter);
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
    void shouldCallSubroutineWhen2NNNOpcode() {
        int instruction = 0x2000;
        int expectedAddress = 0x033A;
        int oldStackPointer = 0x2;
        int oldProgramCounter = processor.programCounter;

        processor.stackPointer = oldStackPointer;
        processor.opcode = instruction | expectedAddress;
        processor.decodeInstruction();

        assertEquals(oldStackPointer + 1, processor.stackPointer);
        assertEquals(oldProgramCounter, processor.stack[processor.stackPointer - 1]);
        assertEquals(expectedAddress, processor.programCounter);
    }

    @Test
    void shouldSkipIfVXEqualsNNWhen3XNNOpcode() {
        int instruction = 0x3000;
        int registerSelector = 0x0300;
        int value = 0x0045;
        int oldProgramCounter = processor.programCounter;

        processor.register[registerSelector >>> 8] = value;
        processor.opcode = instruction | registerSelector | value;
        processor.decodeInstruction();

        assertEquals(oldProgramCounter + 2, processor.programCounter);
    }

    @Test
    void shouldSkipIfVXNotEqualNNWhen4XNNOpcode() {
        int instruction = 0x4000;
        int registerSelector = 0x0300;
        int value = 0x0045;
        int registerValue = 0x0099;
        int oldProgramCounter = processor.programCounter;

        processor.register[registerSelector >>> 8] = registerValue;
        processor.opcode = instruction | registerSelector | value;
        processor.decodeInstruction();

        assertEquals(oldProgramCounter + 2, processor.programCounter);
    }

    @Test
    void shouldSkipIfVXEqualNNWhen5XY0Opcode() {
        int instruction = 0x5000;
        int registerXSelector = 0x0300;
        int registerYSelector = 0x0040;
        int registersValue = 0x0045;
        int oldProgramCounter = processor.programCounter;

        processor.register[registerXSelector >>> 8] = registersValue;
        processor.register[registerYSelector >>> 4] = registersValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(oldProgramCounter + 2, processor.programCounter);
    }

    @Test
    void shouldSetRegisterWhen6XNNOpcode() {
        int instruction = 0x6000;
        int registerSelector = 0x0700;
        int value = 0x0023;

        processor.opcode = instruction | registerSelector | value;
        processor.decodeInstruction();

        assertEquals(value, processor.register[registerSelector >> 8]);
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
    }

    @Test
    void shouldPutRegisterYIntoXWhen8XY0Opcode() {
        int instruction = 0x8000;
        int registerXSelector = 0x0300;
        int registerYSelector = 0x0040;
        int registerYValue = 0x45;

        processor.register[registerYSelector >>> 4] = registerYValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(registerYValue, processor.register[registerXSelector >>> 8]);
    }

    @Test
    void shouldORRegisterYAndXWhen8XY1Opcode() {
        int instruction = 0x8001;
        int registerXSelector = 0x0300;
        int registerYSelector = 0x0040;
        int registerXValue = 0xFF;
        int registerYValue = 0x0F;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.register[registerYSelector >>> 4] = registerYValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue | registerYValue, processor.register[registerXSelector >>> 8]);
    }

    @Test
    void shouldANDRegisterYAndXWhen8XY2Opcode() {
        int instruction = 0x8002;
        int registerXSelector = 0x0300;
        int registerYSelector = 0x0040;
        int registerXValue = 0xFF;
        int registerYValue = 0x47;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.register[registerYSelector >>> 4] = registerYValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue & registerYValue, processor.register[registerXSelector >>> 8]);
    }

    @Test
    void shouldXORRegisterYAndXWhen8XY3Opcode() {
        int instruction = 0x8003;
        int registerXSelector = 0x0300;
        int registerYSelector = 0x0040;
        int registerXValue = 0x66;
        int registerYValue = 0x55;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.register[registerYSelector >>> 4] = registerYValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue ^ registerYValue, processor.register[registerXSelector >>> 8]);
    }

    @Test
    void shouldAddRegisterYAndXWhen8XY4Opcode() {
        int instruction = 0x8004;
        int registerXSelector = 0x0D00;
        int registerYSelector = 0x0020;
        int registerXValue = 0x22;
        int registerYValue = 0x7D;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.register[registerYSelector >>> 4] = registerYValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue + registerYValue, processor.register[registerXSelector >>> 8]);
        assertEquals(registerXValue + registerYValue > 0xFF, processor.register[0xF] > 0);
    }

    @Test
    void shouldSubtractRegisterXFromYWhen8XY5Opcode() {
        int instruction = 0x8005;
        int registerXSelector = 0x0100;
        int registerYSelector = 0x00B0;
        int registerXValue = 0xF5;
        int registerYValue = 0x67;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.register[registerYSelector >>> 4] = registerYValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue - registerYValue, processor.register[registerXSelector >>> 8]);
        assertEquals(registerXValue - registerYValue < 0x0, processor.register[0xF] == 0);
    }

    @Test
    void shouldDivideXBy2When8XY6Opcode() {
        int instruction = 0x8006;
        int registerXSelector = 0x0100;
        int registerXValue = 0xF5;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.opcode = instruction | registerXSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue / 2, processor.register[registerXSelector >>> 8]);
    }

    @Test
    void shouldSubtractRegisterYFromXWhen8XY7Opcode() {
        int instruction = 0x8007;
        int registerXSelector = 0x0A00;
        int registerYSelector = 0x0060;
        int registerXValue = 0x99;
        int registerYValue = 0xA6;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.register[registerYSelector >>> 4] = registerYValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(registerYValue - registerXValue, processor.register[registerXSelector >>> 8]);
        assertEquals(registerYValue - registerXValue < 0x0, processor.register[0xF] == 0);
    }

    @Test
    void shouldMultiplyXBy2When8XYEOpcode() {
        int instruction = 0x800E;
        int registerXSelector = 0x0300;
        int registerXValue = 0x0F;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.opcode = instruction | registerXSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue * 2, processor.register[registerXSelector >>> 8]);
    }

    @Test
    void shouldSkipIfVXNotEqualNNWhen9XY0Opcode() {
        int instruction = 0x9000;
        int registerXSelector = 0x0300;
        int registerYSelector = 0x0040;
        int registerXValue = 0x0045;
        int registerYValue = 0x00EE;
        int oldProgramCounter = processor.programCounter;

        processor.register[registerXSelector >>> 8] = registerXValue;
        processor.register[registerYSelector >>> 4] = registerYValue;
        processor.opcode = instruction | registerXSelector | registerYSelector;
        processor.decodeInstruction();

        assertEquals(oldProgramCounter + 2, processor.programCounter);
    }

    @Test
    void shouldSetIndexRegistryWhenANNNOpcode() {
        int instruction = 0xA000;
        int value = 0x0321;

        processor.opcode = instruction | value;
        processor.decodeInstruction();

        assertEquals(value, processor.indexRegister);
    }

    @Test
    void shouldSetProgramCounterWhenBNNNOpcode() {
        int instruction = 0xB000;
        int address = 0x0123;
        int registerValue = 0x02;

        processor.register[0x0] = registerValue;
        processor.opcode = instruction | address;
        processor.decodeInstruction();

        assertEquals(address + registerValue, processor.programCounter);
    }

    @Test
    void shouldGenerateRandomValueWhenCXNNOpcode() {
        int instruction = 0xC000;
        int registerSelector = 0x0E00;
        int mask = 0x000F;

        processor.opcode = instruction | registerSelector | mask;
        processor.decodeInstruction();

        assertTrue(processor.register[registerSelector >>> 8] >= 0);
        assertTrue(processor.register[registerSelector >>> 8] <= mask);
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
        assertEquals(0, processor.register[0xF]);
    }

    @Test
    void shouldSkipIfKeyIsPressedWhenEX9EOpcode() {
        int instruction = 0xE09E;
        int registerSelector = 0x0A00;
        int pressedKey = 0x04;
        int oldProgramCounter = processor.programCounter;

        processor.register[registerSelector >>> 8] = pressedKey;
        processor.keys[pressedKey] = true;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(oldProgramCounter + 2, processor.programCounter);
    }

    @Test
    void shouldSkipIfKeyIsNotPressedWhenEXA1Opcode() {
        int instruction = 0xE0A1;
        int registerSelector = 0x0B00;
        int pressedKey = 0x0C;
        int oldProgramCounter = processor.programCounter;

        processor.register[registerSelector >>> 8] = pressedKey;
        processor.keys[pressedKey] = false;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(oldProgramCounter + 2, processor.programCounter);
    }

    @Test
    void shouldSetXRegistryWithDelayTimerWhenFX07Opcode() {
        int instruction = 0xF007;
        int registerSelector = 0x0800;
        int delayTimerValue = 0x0C;

        processor.delayTimer = delayTimerValue;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(delayTimerValue, processor.register[registerSelector >>> 8]);
    }

    @Test
    void shouldWaitForKeyWhenFX07Opcode() {
        int instruction = 0xF00A;
        int registerSelector = 0x0800;
        int pressedKey = 0x0C;

        when(processor.waitForInput()).thenReturn(pressedKey);
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(pressedKey, processor.register[registerSelector >>> 8]);
    }

    @Test
    void shouldSetDelayTimerWithXRegistryWhenFX15Opcode() {
        int instruction = 0xF015;
        int registerSelector = 0x0800;
        int delayTimerValue = 0xAB;

        processor.register[registerSelector >>> 8] = delayTimerValue;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(delayTimerValue, processor.delayTimer);
    }

    @Test
    void shouldSetSoundTimerWithXRegistryWhenFX18Opcode() {
        int instruction = 0xF018;
        int registerSelector = 0x0900;
        int soundTimerValue = 0xDD;

        processor.register[registerSelector >>> 8] = soundTimerValue;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(soundTimerValue, processor.soundTimer);
    }

    @Test
    void shouldAddXRegisterToIndexWhenFX1EOpcode() {
        int instruction = 0xF01E;
        int registerSelector = 0x0200;
        int registerXValue = 0x04;
        int indexInitValue = 0x34;

        processor.indexRegister = indexInitValue;
        processor.register[registerSelector >> 8] = registerXValue;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue + indexInitValue, processor.indexRegister);
    }

    @Test
    void shouldSetFontLocationToIndexWhenFX29Opcode() {
        int instruction = 0xF029;
        int registerSelector = 0x0500;
        int registerXValue = 0x04;

        processor.register[registerSelector >> 8] = registerXValue;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(registerXValue * 0x05, processor.indexRegister);
    }

    @Test
    void shouldStoreBinaryCodedDecimalConversionInMemoryWhenFX33Opcode() {
        int instruction = 0xF033;
        int registerSelector = 0x0500;
        int registerXValue = 0xFE; // 254
        int indexValue = 0xF00;

        processor.indexRegister = indexValue;
        processor.register[registerSelector >> 8] = registerXValue;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        assertEquals(2, processor.memory[indexValue]);
        assertEquals(5, processor.memory[indexValue + 1]);
        assertEquals(4, processor.memory[indexValue + 2]);
    }

    @Test
    void shouldCopyXRegistersToMemoryWhenFX55Opcode() {
        int instruction = 0xF055;
        int registerSelector = 0x0F00;
        int indexValue = 0xC00;

        for (int i = 0; i < (registerSelector >> 8); i++) {
            processor.register[i] = i;
        }
        processor.indexRegister = indexValue;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        boolean isCorrect = true;
        for (int i = 0; i < (registerSelector >> 8); i++) {
            if (processor.memory[i + indexValue] != i) {
                isCorrect = false;
                break;
            }
        }
        assertTrue(isCorrect);
    }

    @Test
    void shouldCopyMemoryToXRegistersWhenFX65Opcode() {
        int instruction = 0xF065;
        int registerSelector = 0x0F00;
        int indexValue = 0xC00;

        for (int i = 0; i < (registerSelector >> 8); i++) {
            processor.memory[i + indexValue] = i;
        }
        processor.indexRegister = indexValue;
        processor.opcode = instruction | registerSelector;
        processor.decodeInstruction();

        boolean isCorrect = true;
        for (int i = 0; i < (registerSelector >> 8); i++) {
            if (processor.register[i] != i) {
                isCorrect = false;
                break;
            }
        }
        assertTrue(isCorrect);
    }
}
