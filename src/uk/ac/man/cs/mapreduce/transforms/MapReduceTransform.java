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
package uk.ac.man.cs.mapreduce.transforms;

import org.bitbucket.crbb.klass.ClassFile;
import org.bitbucket.crbb.klass.ConstantPool;
import org.bitbucket.crbb.klass.Method;
import org.bitbucket.crbb.klass.data.ByteBuffer;
import org.bitbucket.crbb.klass.enums.Access;

public abstract class MapReduceTransform {
    
    private static final String isCombinableName = "isCombinable";
    private static final String isCombinableDescriptor = "()Z";
    
    private static final byte[] isCombinableBytecode = new byte[] {
        (byte) 0x04, // iconst_1
        (byte) 0xAC // ireturn
    };
    
    protected static final String initialiseName = "initialise";
    protected static final String initialiseDescriptor = "()Luk/ac/man/cs/mapreduce/Holder;";
    
    protected MapReduceTransform() {
    }
    
    public abstract boolean isMatch(ConstantPool constants, Method method);
    
    public abstract void applyTransform(ClassFile klass, String vType);
    
    protected void addIsCombineable(ClassFile klass) {
        Method isCombinable = klass.addMethod(isCombinableName, isCombinableDescriptor);
        
        isCombinable.setAccess(Access.PUBLIC);
        
        isCombinable.setBytecode(1, 1, isCombinableBytecode);
    }
    
    protected synchronized void addInitialise(ClassFile klass) {
        String holder = "uk/ac/man/cs/mapreduce/holders/ObjectHolder";
        
        ConstantPool constants = klass.getConstantPool();
        
        Method initiailise = klass.addMethod(initialiseName, initialiseDescriptor);
        
        initiailise.setAccess(Access.PUBLIC);
        
        int holderIndex = constants.addClassReferenceIndex(holder);

        int initIndex = constants.addMethodReferenceIndex(holder, "<init>", "()V");

        ByteBuffer buffer = new ByteBuffer();
        
        buffer.writeU1(0xBB); // new
        buffer.writeU2(holderIndex);

        buffer.writeU1(0x59); // dup
        
        buffer.writeU1(0xB7); // invokespecial
        buffer.writeU2(initIndex);

        buffer.writeU1(0xB0); // areturn
        
        initiailise.setBytecode(3, 1, buffer.asArray());
    }    
    
    protected synchronized void addIntInitialise(ClassFile klass, int value) {
        String holder = "uk/ac/man/cs/mapreduce/holders/IntHolder";
        
        ConstantPool constants = klass.getConstantPool();
        
        Method initiailise = klass.addMethod(initialiseName, initialiseDescriptor);
        
        initiailise.setAccess(Access.PUBLIC);
        
        int holderIndex = constants.addClassReferenceIndex(holder);
       
        int initIndex = constants.addMethodReferenceIndex(holder, "<init>", "(I)V");

        ByteBuffer buffer = new ByteBuffer();
        
        buffer.writeU1(0xBB); // new
        buffer.writeU2(holderIndex);

        buffer.writeU1(0x59); // dup
        
        if (value >= -1 && value <= 5) {
            buffer.writeU1(0x03 + value); // iconst_x
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            buffer.writeU1(0x10); // bipush
            buffer.writeU1(value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            buffer.writeU1(0x11); // sipush
            buffer.writeU2(value);
        } else {
            throw new UnsupportedOperationException("");
        }
        
        buffer.writeU1(0xB7); // invokespecial
        buffer.writeU2(initIndex);
        
        buffer.writeU1(0xB0); // areturn

        initiailise.setBytecode(3, 1, buffer.asArray());
    }    
    
    protected synchronized void addLongInitialise(ClassFile klass, long value) {
        String holder = "uk/ac/man/cs/mapreduce/holders/LongHolder";
        
        ConstantPool constants = klass.getConstantPool();
        
        Method initiailise = klass.addMethod(initialiseName, initialiseDescriptor);
        
        initiailise.setAccess(Access.PUBLIC);
        
        int holderIndex = constants.addClassReferenceIndex(holder);
        
        int initIndex = constants.addMethodReferenceIndex(holder, "<init>", "(L)V");

        ByteBuffer buffer = new ByteBuffer();
        
        buffer.writeU1(0xBB); // new
        buffer.writeU2(holderIndex);

        buffer.writeU1(0x59); // dup
        
        if (value >= -1 && value <= 5) {
            buffer.writeU1(0x03 + (int) value); // iconst_x
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            buffer.writeU1(0x10); // bipush
            buffer.writeU1((int) value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            buffer.writeU1(0x11); // sipush
            buffer.writeU2((int) value);
        } else {
            throw new UnsupportedOperationException("");
        }
        
        buffer.writeU1(0x85); // i2l
        
        buffer.writeU1(0xB7); // invokespecial
        buffer.writeU2(initIndex);
        
        buffer.writeU1(0xB0); // areturn

        initiailise.setBytecode(3, 1, buffer.asArray());
    }
}
