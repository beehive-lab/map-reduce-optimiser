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

public class BranchInstruction extends Instruction {

    private static final int IFEQ = 0x99;
    private static final int IFNE = 0x9A;
    private static final int IFLT = 0x9B;
    private static final int IFGE = 0x9C;
    private static final int IFGT = 0x9D;
    private static final int IFLE = 0x9E;
    private static final int IF_ICMPEQ = 0x9F;
    private static final int IF_ICMPNE = 0xA0;
    private static final int IF_ICMPLT = 0xA1;
    private static final int IF_ICMPGE = 0xA2;
    private static final int IF_ICMPGT = 0xA3;
    private static final int IF_ICMPLE = 0xA4;
    private static final int IF_ACMPEQ = 0xA5;
    private static final int IF_ACMPNE = 0xA6;
    private static final int GOTO = 0xA7;

    private final int next, jump;

    public BranchInstruction(ReadOnlyByteBuffer buffer, int bci) {
        super(bci, buffer.peekU1(bci));
        this.next = bci + 3;
        this.jump = bci + (short) buffer.peekU2(bci + 1);
    }

    public int getNext() {
        return next;
    }

    public int getJump() {
        return jump;
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public String toString() {
        if (opcode == GOTO) {
            return "goto " + jump;
        } else {
            return "if " + next + " else " + jump;
        }
    }

    public boolean isLoopEnd() {
        return opcode == GOTO && jump < next;
    }

    @Override
    public void write(ByteBuffer buffer) {
        super.write(buffer);
        throw new RuntimeException("Don't have BCI to calculate this");
    }
}
