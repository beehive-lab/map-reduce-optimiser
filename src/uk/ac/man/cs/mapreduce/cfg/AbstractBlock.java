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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.man.cs.mapreduce.cfg.instructions.Instruction;

public abstract class AbstractBlock {

    public static void link(AbstractBlock predecessor, AbstractBlock successor) {
        predecessor.successors.add(successor);
        successor.predecessors.add(predecessor);
    }
    
    protected final List<Instruction> instructions = new ArrayList<>();
    
    protected final Set<AbstractBlock> predecessors = new HashSet<>();
    protected final Set<AbstractBlock> successors = new HashSet<>();
    
    protected AbstractBlock() {   
    }
    
    public void addInstruction(Instruction instruction) {
        instructions.add(instruction);
    }
    
    public boolean containsInstruction(int bci) {
        for (Instruction instruction : instructions) {
            if (instruction.getBCI() == bci) {
                return true;
            }
        }
        return false;
    }
    
    public Instruction getInstruction(int index) {
        return instructions.get(index);
    }    
    
    public List<Instruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }
    
    public int getInstructionSize() {
        return instructions.size();
    }
    
    public void clearAssociates() {
        predecessors.clear();
        successors.clear();
    }
    
    public void clearPredecessors() {
        predecessors.clear();
    }    
    
    public Set<AbstractBlock> getPredecessors() {
        return Collections.unmodifiableSet(predecessors);
    }    
    
    public void replacePredecessor(AbstractBlock old, AbstractBlock withNew) {
        if (predecessors.remove(old)) {
            predecessors.add(withNew);
        }
    }        
    
    public AbstractBlock getUniquePredecessor() {
        return getUnique(predecessors);
    }
    
    public void setUniquePredecessor(AbstractBlock predecessor) {
        setUnique(predecessors, predecessor);
    }
    
    public void setPredecessor(AbstractBlock predecessor) {
        predecessors.add(predecessor);
    }    
    
    public void clearSucessors() {
        successors.clear();
    }        
    
    public Set<AbstractBlock> getSuccessors() {
        return Collections.unmodifiableSet(successors);
    }  
    
    public void replaceSuccessor(AbstractBlock old, AbstractBlock withNew) {
        if (successors.remove(old)) {
            successors.add(withNew);
        }
    }        
    
    public AbstractBlock getUniqueSuccessor() {
        return getUnique(successors);
    }

    public void setUniqueSuccessor(AbstractBlock successor) {
        setUnique(successors, successor);
    }       

    public void setSuccessor(AbstractBlock successor) {
        successors.add(successor);
    }
    
    public boolean startsAt(int bci) {
        return !instructions.isEmpty() && instructions.get(0).getBCI() == bci; 
    }    
    
    public void dump(PrintStream out) {
        out.println(this);   
        for (AbstractBlock block : successors) {
            out.println("    < " + block);
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    private AbstractBlock getUnique(Set<AbstractBlock> associates) {
        if (associates.size() == 1) {
            return associates.iterator().next();
        }
        return null;
    }

    private void setUnique(Set<AbstractBlock> associates, AbstractBlock block) {
        associates.clear();
        associates.add(block);
    }
}
