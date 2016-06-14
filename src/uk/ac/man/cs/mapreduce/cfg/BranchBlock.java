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

import uk.ac.man.cs.mapreduce.cfg.instructions.BranchInstruction;

public final class BranchBlock extends AbstractBlock {

    private final BranchInstruction instruction;
    
    public BranchBlock(BranchInstruction instruction) {
        this.instruction = instruction;
    } 

    public int getJump() {
        return instruction.getJump();
    }
    
    public boolean isLoopEnd() {
        return instruction.isLoopEnd();
    }    
    
    @Override
    public String toString() {
        return "Branch: " + instruction.toString();
    }

    public BranchInstruction getInstruction() {
        return instruction;
    }

    public AbstractBlock getFalseSuccessor() {
        for (AbstractBlock block : successors) {
            if (block.startsAt(instruction.getJump())) {
                return block;
            }
        }
        return null;
    }    
    
    public AbstractBlock getTrueSuccessor() {
        for (AbstractBlock block : successors) {
            if (block.startsAt(instruction.getNext())) {
                return block;
            }
        }
        return null;
    }
}
