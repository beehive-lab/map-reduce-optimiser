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

public class InvokeInstruction extends Instruction {

    private static final int INVOKEINTERFACE = 0xB9;
    private static final int INVOLEDYNAMIC = 0xBA;

    private final int index, count;

    public InvokeInstruction(ReadOnlyByteBuffer buffer, int bci) {
        super(bci, buffer.peekU1(bci));
        this.index = buffer.peekU2(bci + 1);
        this.count = isDynamic() ? buffer.peekU1(bci + 3) : 0;
    }

    @Override
    public int getSize() {
        return isDynamic() ? 5 : 3;
    }

    private boolean isDynamic() {
        return opcode == INVOKEINTERFACE || opcode == INVOLEDYNAMIC;
    }

    public int getMethodReference() {
        return index;
    }

    @Override
    public void write(ByteBuffer buffer) {
        super.write(buffer);
        buffer.writeU2(index);
        if (isDynamic()) {
            buffer.writeU1(count);
            buffer.writeU1(0);
        }
    }
}
