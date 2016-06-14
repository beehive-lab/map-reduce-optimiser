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

import java.io.PrintStream;
import java.util.List;
import uk.ac.man.cs.mapreduce.cfg.instructions.AccessInstruction;
import uk.ac.man.cs.mapreduce.cfg.instructions.BranchInstruction;

public class LoopBlock extends AbstractBlock {
    
    private CodeBlock initialisation;
    
    private CodeBlock dataAccessor;
    
    private CodeBlock condition;
    
    private BranchInstruction instruction;
    
    private CodeBlock body;
    
    public LoopBlock(CodeBlock condition) {
        super();
        
        this.condition = condition;
        
        BranchBlock start = (BranchBlock) condition.getUniqueSuccessor();
        
        this.instruction = start.getInstruction();
        
        this.body = (CodeBlock) start.getTrueSuccessor();
        
        this.successors.add(start.getFalseSuccessor());
    }
    
    public void relink(List<AbstractBlock> blocks) {
        for (AbstractBlock block : condition.getPredecessors()) {
            if (block instanceof CodeBlock) {
                int initBCI = condition.instructions.get(0).getBCI() - 8;
                if (block.containsInstruction(initBCI)) {
                    initialisation = ((CodeBlock) block).split(initBCI);
                    block.replaceSuccessor(initialisation, this);
                    initialisation.clearAssociates();
                } 
                break;
            }
        }

        for (AbstractBlock block : condition.predecessors) {
            block.replaceSuccessor(condition, this);
        }
        condition.clearAssociates();
        body.clearAssociates();
        for (AbstractBlock block : successors) {
            block.setUniquePredecessor(this);
        }
        
        dataAccessor = (CodeBlock) body;
        body = dataAccessor.split(dataAccessor.getInstructions().get(0).getBCI() + 12);
        body.clearAssociates();
        dataAccessor.clearAssociates();
    }    
    
    @Override
    public void dump(PrintStream out) {
        out.println("Loop: initialise : " + initialisation);
        out.println("        accessor : " + dataAccessor);
        out.println("       condition : " + condition);
        out.println("            body : " + body);      
        for (AbstractBlock block : successors) {
            out.println("    < " + block);
        }
    }    

    public CodeBlock getCondition() {
        return condition;
    }

    public CodeBlock getBody() {
        return body;
    }

    public int getValueIndex() {
        return ((AccessInstruction) dataAccessor.getInstruction(3)).getLocalIndex();
    }
}
