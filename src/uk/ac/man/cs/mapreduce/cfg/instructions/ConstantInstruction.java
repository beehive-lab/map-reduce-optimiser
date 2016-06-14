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

import uk.ac.man.cs.mapreduce.cfg.data.ReadOnlyByteBuffer;

public class ConstantInstruction extends Instruction {
    
    private static final int ACONST_NULL = 0x01;
    private static final int ICONST_M1 = 0x02;
    private static final int ICONST_0 = 0x03;  
    private static final int ICONST_1 = 0x04;  
    private static final int ICONST_2 = 0x05;  
    private static final int ICONST_3 = 0x06;  
    private static final int ICONST_4 = 0x07;  
    private static final int ICONST_5 = 0x08;  
    private static final int LCONST_0 = 0x09;  
    private static final int LCONST_1 = 0x0A;  
    private static final int FCONST_0 = 0x0B;  
    private static final int FCONST_1 = 0x0C;  
    private static final int FCONST_2 = 0x0D;
    private static final int DCONST_0 = 0x0E;  
    private static final int DCONST_1 = 0x0F;
    
    public ConstantInstruction(ReadOnlyByteBuffer buffer, int bci) {
        super(bci, buffer.peekU1(bci));
    }
    
    public byte[] getBytecode() {
        return new byte[] { (byte) opcode };
    }

    @Override
    public int getSize() {
        return 1;
    }

    public DataType getType() {
        if (opcode == ACONST_NULL) {
            return DataType.OBJECT;
        }
        if (opcode >= ICONST_M1 && opcode <= ICONST_5) {
            return DataType.INTEGER;
        }
        if (opcode >= LCONST_0 && opcode <= LCONST_1) {
            return DataType.LONG;
        }
        if (opcode >= FCONST_0 && opcode <= FCONST_2) {
            return DataType.FLOAT;
        }
        if (opcode >= DCONST_0 && opcode <= DCONST_1) {
            return DataType.DOUBLE;
        }
        return null;
    }
}
