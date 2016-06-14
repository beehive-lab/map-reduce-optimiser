/*
 * Copyright 2016 University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.man.cs.mapreduce.cfg.instructions;

import org.bitbucket.crbb.klass.data.ByteBuffer;
import uk.ac.man.cs.mapreduce.cfg.data.ReadOnlyByteBuffer;

public class AccessInstruction extends Instruction {

    public static final boolean READ = true;
    public static final boolean WRITE = true;

    private static int ILOAD = 0x15;
    private static int ALOAD = 0x19;

    private static int SALOAD = 0x35;

    private static int ISTORE = 0x36;
    private static int ASTORE = 0x3A;

    private static int SASTORE = 0x56;

    private static final int IINC = 0x84;

    private final int index, size;

    private final boolean readNotWrite;

    public AccessInstruction(ReadOnlyByteBuffer buffer, int bci) {
        super(bci, buffer.peekU1(bci));

        if (opcode == IINC) {
            readNotWrite = false; // Is both read and write
            index = buffer.peekU1(bci + 1);
            size = 3;
        } else {
            readNotWrite = opcode < ISTORE;
            int temp = opcode - (readNotWrite ? ILOAD : ISTORE);
            if (temp < 5) {
                index = buffer.peekU1(bci + 1);
                size = 2;
            } else {
                index = (temp - 5) & 0x03;
                size = 1;
            }
        }
    }

    public AccessInstruction(boolean readNotWrite, DataType type, int index) {
        super(-1, getOpcode(readNotWrite, type, index));
        this.readNotWrite = readNotWrite;
        this.index = index;
        this.size = index < 4 ? 1 : 2;
    }

    public boolean equals(boolean readNotWrite, DataType type, int index) {
        return (this.readNotWrite == readNotWrite)
                && (type == null || getType() == type)
                && this.index == index;
    }

    @Override
    public int getSize() {
        return size;
    }

    public boolean isRead() {
        return opcode == IINC || readNotWrite;
    }

    public boolean isWrite() {
        return opcode == IINC || !readNotWrite;
    }

    public int getLocalIndex() {
        return index;
    }

    public DataType getType() {
        if (opcode == IINC) {
            return DataType.INTEGER;
        }
        int temp = opcode - (readNotWrite ? ILOAD : ISTORE);
        if (temp >= 5) {
            temp = (temp - 5) >> 2;
        }
        return DataType.values()[temp];
    }

    private static int getOpcode(boolean readNotWrite, DataType type, int index) {
        int code = readNotWrite ? ILOAD : ISTORE;
        if (index < 4) {
            return (code + 5) + (type.ordinal() * index);
        } else {
            return code + type.ordinal();
        }
    }

    @Override
    public void write(ByteBuffer buffer) {
        super.write(buffer);
        if (index > 3) {
            buffer.writeU1(index);
        }
    }

    public boolean isIncrement() {
        return opcode == IINC;
    }
}
