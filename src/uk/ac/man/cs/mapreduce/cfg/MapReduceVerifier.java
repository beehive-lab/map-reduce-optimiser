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
package uk.ac.man.cs.mapreduce.cfg;

import java.util.Iterator;
import java.util.List;
import org.bitbucket.crbb.klass.ConstantPool;
import org.bitbucket.crbb.klass.data.ByteBuffer;
import uk.ac.man.cs.mapreduce.cfg.instructions.AccessInstruction;
import uk.ac.man.cs.mapreduce.cfg.instructions.ConstantInstruction;
import uk.ac.man.cs.mapreduce.cfg.instructions.DataType;
import uk.ac.man.cs.mapreduce.cfg.instructions.Instruction;
import uk.ac.man.cs.mapreduce.cfg.instructions.InvokeInstruction;
import uk.ac.man.cs.mapreduce.cfg.instructions.NewObjectInstruction;

public class MapReduceVerifier {

    private final ControlFlowGraph graph;

    private DataType intermediateType = DataType.OBJECT;
    
    private String intermediateClass = "java/lang/Object";

    private int intermediateLocalIndex = -1;

    private int valueLocalIndex = -1;

    private int getResultCodeSize = 0;

    public MapReduceVerifier(ControlFlowGraph graph) {
        this.graph = graph;
    }

    public boolean buildGraphAndAccept(ConstantPool contants) {
        try {
            graph.build();

            if (!validateInisitalisation(contants, (CodeBlock) graph.getFirst())) {
                return false;
            }

            if (!validateValueIterator(graph.getLoop())) {
                return false;
            }

            int emitReference = contants.getInterfaceMethodReferenceIndex("uk/ac/man/cs/mapreduce/Emitter", "emit", "(Ljava/lang/Object;Ljava/lang/Object;)V");

            if (!validateGetResult((CodeBlock) graph.getLast(), emitReference)) {
                return false;
            }

            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    private boolean validateInisitalisation(ConstantPool constants, CodeBlock initialisation) {
        // Can only initialis a value and have no dependencies on arguments
        // so it is either ContantInstruction and store or new Object and
        // store.
        try {
            Instruction instruction = initialisation.getInstruction(0);

            if (instruction instanceof ConstantInstruction) {
                intermediateType = ((ConstantInstruction) instruction).getType();
                instruction = initialisation.getInstruction(1);
                if (instruction instanceof AccessInstruction) {
                    if (intermediateType == ((AccessInstruction) instruction).getType()) {
                        intermediateLocalIndex = ((AccessInstruction) instruction).getLocalIndex();
                        return true;
                    }
                }
            } else if (instruction instanceof NewObjectInstruction) {
                intermediateType = DataType.OBJECT;
                intermediateClass = constants.getAsString(((NewObjectInstruction) instruction).getIndex());
                Iterator<Instruction> iterator = initialisation.getInstructions().iterator();
                while (iterator.hasNext()) {
                    instruction = iterator.next();
                    if (instruction instanceof AccessInstruction) {
                        if (iterator.hasNext()) {
                            int index = ((AccessInstruction) instruction).getLocalIndex();
                            if (index > 0 && index < 4) {
                                // Code is using key, value or emitter!
                                return false;
                            }
                        } else {
                            intermediateLocalIndex = ((AccessInstruction) instruction).getLocalIndex();
                        }
                    }
                }
                return true;
            }
            // TODO what about a new array?
        } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
        }

        return false;
    }

    private boolean validateValueIterator(LoopBlock loop) {
        // Check that it only accesses the intermediate and current value -
        // no others are accepted at this point.
        CodeBlock body = loop.getBody();

        valueLocalIndex = loop.getValueIndex();

        for (Instruction instruction : body.getInstructions()) {
            if (instruction instanceof AccessInstruction) {
                int index = ((AccessInstruction) instruction).getLocalIndex();
                if (index != intermediateLocalIndex && index != valueLocalIndex) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean validateGetResult(CodeBlock getResult, int emitReference) {
        // Starts with the emitter - the only flexibility permitted is with
        // the intermediate value converted into the emitted value
        // everything else should be the same.
        try {
            int i = 0;
            Instruction instruction = getResult.getInstruction(i++);
            if (instruction instanceof AccessInstruction) {
                if (!((AccessInstruction) instruction).equals(AccessInstruction.READ, DataType.OBJECT, 3)) {
                    return false;
                }
            } else {
                return false;
            }
            instruction = getResult.getInstruction(i++);
            if (instruction instanceof AccessInstruction) {
                if (!((AccessInstruction) instruction).equals(AccessInstruction.READ, DataType.OBJECT, 1)) {
                    return false;
                }
            } else {
                return false;
            }
            while (true) {
                instruction = getResult.getInstruction(i++);
                if (instruction instanceof AccessInstruction) {
                    if (!((AccessInstruction) instruction).equals(AccessInstruction.READ, null, intermediateLocalIndex)) {
                        return false;
                    }
                } else if (instruction instanceof InvokeInstruction) {
                    if (((InvokeInstruction) instruction).getMethodReference() == emitReference) {
                        break;
                    }
                }
            }
            if (!(instruction instanceof InvokeInstruction)) {
                return false;
            }
            getResultCodeSize = i - 3;
            return i == getResult.getInstructionSize();
        } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
        }

        return false;
    }

    public DataType getInitialiseType() {
        return intermediateType;
    }

    public byte[] buildInitialiseBytecode() {
        Instruction instruction = ((CodeBlock) graph.getFirst()).getInstruction(0);

        if (instruction instanceof ConstantInstruction) {
            return ((ConstantInstruction) instruction).getBytecode();
        } else if (instruction instanceof NewObjectInstruction) {
            ByteBuffer buffer = new ByteBuffer();
            Iterator<Instruction> iterator = ((CodeBlock) graph.getFirst()).getInstructions().iterator();
            while (iterator.hasNext()) {
                instruction = iterator.next();
                if (instruction instanceof AccessInstruction && !iterator.hasNext()) {
                    
                } else {
                    instruction.write(buffer);
                }
            }
            return buffer.asArray();
        }
        return new byte[0];
    }

    public byte[] buildCombineBytecode(ConstantPool constants, String vType) {
        ByteBuffer buffer = new ByteBuffer();
        CodeBlock body = graph.getLoop().getBody();

        for (Instruction instruction : body.getInstructions()) {
            if (instruction instanceof AccessInstruction) {
                replaceAccessInstruction((AccessInstruction) instruction, constants, vType, buffer);
            } else {
                instruction.write(buffer);
            }
        }

        return buffer.asArray();
    }
    
    public byte[] buildGetResultBytecode(ConstantPool constants, String vType) {
        ByteBuffer buffer = new ByteBuffer();
        List<Instruction> instructions = ((CodeBlock) graph.getLast()).getInstructions().subList(2, 2 + getResultCodeSize);
        
        for (Instruction instruction : instructions) {
            if (instruction instanceof AccessInstruction) {
                replaceAccessInstruction((AccessInstruction) instruction, constants, vType, buffer);
            } else {
                instruction.write(buffer);
            }
        }
        
        return buffer.asArray();
    }
    
    private void replaceAccessInstruction(AccessInstruction instruction, ConstantPool constants, String vType, ByteBuffer buffer) {
        if (instruction.isIncrement()) {
            throw new RuntimeException("Not implemented yet");
        } else {
            if (instruction.isRead()) {
                if (instruction.getLocalIndex() == intermediateLocalIndex) {
                    String holder = "uk/ac/man/cs/mapreduce/holders/" + intermediateType + "Holder";
                    int holderIndex = constants.addClassReferenceIndex(holder);
                    int valueFieldIndex = constants.addFieldReferenceIndex(holder, "value", intermediateType.getDescriptor());
                    buffer.writeU1(0x2B); // aload_1
                    buffer.writeU1(0xC0); // checkcast
                    buffer.writeU2(holderIndex);
                    buffer.writeU1(0xB4); // getfield
                    buffer.writeU2(valueFieldIndex);
                    if (intermediateType == DataType.OBJECT) {
                        buffer.writeU1(0xC0); // checkcast
                        buffer.writeU2(constants.addClassReferenceIndex(intermediateClass));
                    }
                } else {
                    int valueTypeIndex = constants.addClassReferenceIndex(vType);
                    buffer.writeU1(0x2C); // aload_2
                    buffer.writeU1(0xC0); // checkcast
                    buffer.writeU2(valueTypeIndex);
                }
            } else {
                if (instruction.getLocalIndex() == intermediateLocalIndex) {
                    String holder = "uk/ac/man/cs/mapreduce/holders/" + intermediateType + "Holder";
                    int holderIndex = constants.addClassReferenceIndex(holder);
                    int valueFieldIndex = constants.addFieldReferenceIndex(holder, "value", intermediateType.getDescriptor());
                    buffer.writeU1(0x2B);// aload_1
                    buffer.writeU1(0xC0); // checkcast
                    buffer.writeU2(holderIndex);
                    // reference and value need to be swapped
                    if (intermediateType == DataType.LONG || intermediateType == DataType.DOUBLE) {
                        buffer.writeU1(0x5B);
                        buffer.writeU1(0x57);
                    } else {
                        buffer.writeU1(0x5F); 
                    }
                    buffer.writeU1(0xB5); // putfield
                    buffer.writeU2(valueFieldIndex);
                } else {
                    buffer.writeU1(0x4D); // astore_2
                }
            }
        }
    }
}
        
        
        
