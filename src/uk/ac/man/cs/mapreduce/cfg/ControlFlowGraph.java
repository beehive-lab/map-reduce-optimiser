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

import uk.ac.man.cs.mapreduce.cfg.data.ReadOnlyByteBuffer;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import uk.ac.man.cs.mapreduce.cfg.instructions.*;

public class ControlFlowGraph {
    
    private final byte[] bytecode;
    
    private List<AbstractBlock> blocks = new ArrayList<>();
    
    private AbstractBlock entry = null;
    
    public ControlFlowGraph(byte[] bytecode) {
        this.bytecode = Arrays.copyOf(bytecode, bytecode.length);
    }
    
    public void build() {
        blocks.clear();
        
        ReadOnlyByteBuffer buffer = new ReadOnlyByteBuffer(bytecode);
        
        int bci = 0;
        
        AbstractBlock block = entry = new CodeBlock();
        
        blocks.add(block);
        
        while (bci < bytecode.length) {
            Instruction instruction = Instructions.getInstruction(buffer, bci);
            
            if (instruction instanceof BranchInstruction) {
                    BranchBlock branch = new BranchBlock(new BranchInstruction(buffer, bci));
                    AbstractBlock.link(block, branch);
                    blocks.add(branch);
                    block.setSuccessor(branch);
                    blocks.add(block = new CodeBlock());
                    AbstractBlock.link(branch, block);
            } else if (instruction instanceof ReturnInstruction) {
                ReturnBlock returnBlock = getReturnBlock();
                returnBlock.addInstruction(instruction);
                AbstractBlock.link(block, returnBlock);
                if ((bci + instruction.getSize()) < bytecode.length) {
                    block = new CodeBlock();
                    blocks.add(block);
                }
            } else {
                block.addInstruction(instruction);
            }
            
            bci += instruction.getSize();
        }
        
        linkBranchBlocks();
    }

    public void dump(PrintStream out) {
        for (AbstractBlock block : blocks) {
            block.dump(out);
        }
    }

    public List<AbstractBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public AbstractBlock getFirst() {
        return entry;
    }
    
    public AbstractBlock getLast() {
        return getReturnBlock().getUniquePredecessor();
    }    

    public LoopBlock getLoop() {
        for (AbstractBlock block : blocks) {
            if (block instanceof LoopBlock) {
                return (LoopBlock) block;
            }
        }
        return null;
    }    
    
    private AbstractBlock getBlock(int bci) {
        for (AbstractBlock block : blocks) {
            if (block.containsInstruction(bci)) {
                return block;
            }
        }
        throw new RuntimeException("Programming error: target branch not in bytecode");
    }

    private ReturnBlock getReturnBlock() {
        for (AbstractBlock block : blocks) {
            if (block instanceof ReturnBlock) {
                return ((ReturnBlock) block);
            }
        }
        ReturnBlock block = new ReturnBlock();
        blocks.add(block);
        return block;
    }    
    
    private void linkBranchBlocks() {
        for (AbstractBlock block : new ArrayList<>(blocks)) {
            if (block instanceof BranchBlock) {
                BranchBlock branch = (BranchBlock) block;
                int targetBCI = branch.getJump();
                AbstractBlock target = getBlock(targetBCI);
                if (target instanceof CodeBlock) {
                    CodeBlock condition = ((CodeBlock) target).split(targetBCI);
                    if (condition != target) {
                        blocks.add(condition);
                    }
                    AbstractBlock.link(branch, condition);
                    
                    if (branch.isLoopEnd()) {
                        AbstractBlock loopStart = condition.getUniqueSuccessor();
                        if (loopStart instanceof BranchBlock) {
                            LoopBlock loop = new LoopBlock(condition);
                            loop.relink(blocks);
                            
                            blocks.remove(branch);
                            blocks.remove(loopStart);
                            blocks.add(loop);
                        }
                    }
                } else {
                    branch.setSuccessor(target);
                }
            }
        }
    }
}
