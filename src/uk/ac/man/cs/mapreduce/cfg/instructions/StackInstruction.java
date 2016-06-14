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

public class StackInstruction extends Instruction {
    
    private static final int NOP = 0x00;
    private static final int POP = 0x57;
    private static final int POP2 = 0x58;
    private static final int DUP = 0x59;
    private static final int DUP_X1 = 0x5A;
    private static final int DUP_X2 = 0x5B;
    private static final int DUP2 = 0x5C;
    private static final int DUP2_X1 = 0x5D;
    private static final int DUP2_X2 = 0x5E;
    private static final int SWAP = 0x5F;
    private static final int IADD = 0x60;
    private static final int LADD = 0x61;
    private static final int FADD = 0x62;
    private static final int DADD = 0x63;
    private static final int ISUB = 0x64;
    private static final int LSUB = 0x65;
    private static final int FSUB = 0x66;
    private static final int DSUB = 0x67;
    private static final int IMUL = 0x68;
    private static final int LMUL = 0x69;
    private static final int FMUL = 0x6A;
    private static final int DMUL = 0x6B;
    private static final int IDIV = 0x6C;
    private static final int LDIV = 0x6D;
    private static final int FDIV = 0x6E;
    private static final int DDIV = 0x6F;
    private static final int IREM = 0x70;
    private static final int LREM = 0x71;
    private static final int FREM = 0x72;
    private static final int DREM = 0x73;
    private static final int INEG = 0x74;
    private static final int LNEG = 0x75;
    private static final int FNEG = 0x76;
    private static final int DNEG = 0x77;
    private static final int ISHL = 0x78;
    private static final int LSHL = 0x79;
    private static final int ISHR = 0x7A;
    private static final int LSHR = 0x7B;
    private static final int IUSHR = 0x7C;
    private static final int LUSHR = 0x7D;
    private static final int IAND = 0x7E;
    private static final int LAND = 0x7F;
    private static final int IOR = 0x80;
    private static final int LOR = 0x81;
    private static final int IXOR = 0x82;
    private static final int LXOR = 0x83;
    private static final int I2L = 0x85;
    private static final int I2F = 0x86;
    private static final int I2D = 0x87;
    private static final int L2I = 0x88;
    private static final int L2F = 0x89;
    private static final int L2D = 0x8A;
    private static final int F2I = 0x8B;
    private static final int F2L = 0x8C;
    private static final int F2D = 0x8D;
    private static final int D2I = 0x8E;
    private static final int D2L = 0x8F;
    private static final int D2F = 0x90;
    private static final int I2B = 0x91;
    private static final int I2C = 0x92;
    private static final int I2S = 0x93;
    private static final int LCMP = 0x94;
    private static final int FCMPL = 0x95;
    private static final int FCMPG = 0x96;
    private static final int DCMPL = 0x97;
    private static final int DCMPG = 0x98;

    public StackInstruction(ReadOnlyByteBuffer buffer, int bci) {
        super(bci, buffer.peekU1(bci));
    }

    @Override
    public int getSize() {
        return 1;
    }
}
