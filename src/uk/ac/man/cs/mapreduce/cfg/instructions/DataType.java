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

public enum DataType {
    
    // Order matches bytecode ordering - DO NOT MODIFY
    INTEGER("I", "Integer"), LONG("J", "Long"), FLOAT("F", "Float"), DOUBLE("D", "Double"), OBJECT("Ljava/lang/Object;", "Object");

    private final String descriptor;
    private final String name;
    
    private DataType(String descriptor, String name) {
        this.descriptor = descriptor;
        this.name = name;
    }
    
    public String getDescriptor() {
        return descriptor;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
