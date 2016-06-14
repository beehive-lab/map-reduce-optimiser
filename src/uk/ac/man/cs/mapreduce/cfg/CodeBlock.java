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

import java.util.ArrayList;
import java.util.List;
import uk.ac.man.cs.mapreduce.cfg.instructions.Instruction;

public class CodeBlock extends AbstractBlock {

    public CodeBlock() {
        super();
    }

    public CodeBlock split(int bci) {
        CodeBlock next = null;
        
        if (!instructions.isEmpty() && instructions.get(0).getBCI() < bci) {
            List<Instruction> original = new ArrayList<>(instructions);
            instructions.clear();

            for (Instruction instruction : original) {
                if (next == null) {
                    if (instruction.getBCI() < bci) {
                        instructions.add(instruction);
                    } else {
                        next = new CodeBlock();
                        next.instructions.add(instruction);
                    }
                } else {
                    next.instructions.add(instruction);
                }
            }
            
            if (next != null) {
                for (AbstractBlock successor : successors) {
                    successor.replacePredecessor(this, next);
                    next.successors.add(successor);
                }
                successors.clear();
                AbstractBlock.link(this, next);
            }
        }
        return next == null ? this : next;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Code: ");
        switch (instructions.size()) {
            case 0:
                break;
            case 1:
                sb.append(instructions.get(0).getBCI());
                break;
            default:
                sb.append(instructions.get(0).getBCI())
                        .append(" .. ")
                        .append(instructions.get(instructions.size() - 1).getBCI());
                break;
        }
        return sb.toString();
    }
}
