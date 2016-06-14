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

import java.util.Arrays;
import org.bitbucket.crbb.klass.ClassFile;
import org.bitbucket.crbb.klass.ConstantPool;
import org.bitbucket.crbb.klass.Method;
import org.bitbucket.crbb.klass.enums.Access;

public class SizeOfTransform extends MapReduceTransform {

    private final byte[] pattern = new byte[] {
        (byte) 0x2D, // aload_3
        (byte) 0x2B, // aload_1
        (byte) 0x2C, // aload_2
        (byte) 0xB9, // invokeinterface <List#size()> 1 0
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0x01,
        (byte) 0x00,
        (byte) 0xB8, // invokestatic <Integer#valueOf()>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB9, // invokeinterface <Emitter#emit()> 3 0
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0x03,
        (byte) 0x00,
        (byte) 0xB1  // return
    };    
    
    private final byte[] combineBytecode = new byte[] {
        (byte) 0x2B, // aload_1
        (byte) 0xC0, // checkcast <IntHolder>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0x59, // dup
        (byte) 0xB4, // getfield <IntHolder#value>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0x04, // iconst_1
        (byte) 0x60, // iadd
        (byte) 0xB5, // putfield <IntHolder#value>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB1 // return
    };
    
    private final byte[] combineBridgeBytecode = new byte[] {
        (byte) 0x2A, // aload_0
        (byte) 0x2B, // aload_1
        (byte) 0x2C, // aload_2
        (byte) 0xC0, // checkcast <Integer>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB6, // invokevirtual <this#combine()>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB1 // return
    };       
    
    private final byte[] getResultBytecode = new byte[] {
        (byte) 0x2B, // aload_1
        (byte) 0xC0, // checkcast <IntHolder>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB4, // getfield <IntHolder#value>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB8, // invokestatic <Integer.valueOf()>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB0 // areturn
    };    
    
    private final byte[] getResultBridgeBytecode = new byte[] {
        (byte) 0x2A, // aload_0
        (byte) 0x2B, // aload_1
        (byte) 0xB6, // invokevirtual <this#getResult()>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB0 // areturn
    }; 
    
    public SizeOfTransform() {
    }
    
    @Override
    public boolean isMatch(ConstantPool constants, Method method) {
        int indexOfListSize = constants.getInterfaceMethodReferenceIndex("java/util/List", "size", "()I");

        if (indexOfListSize < 0) {
            return false;
        }
        
        int indexOfIntegerValueOf = constants.getMethodReferenceIndex("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");

        if (indexOfListSize < 0) {
            return false;
        }

        int indexOfEmitterEmit = constants.getInterfaceMethodReferenceIndex("uk/ac/man/cs/mapreduce/Emitter", "emit", "(Ljava/lang/Object;Ljava/lang/Object;)V");

        if (indexOfEmitterEmit < 0) {
            return false;
        }

        pattern[4] = (byte) (indexOfListSize >> 8);
        pattern[5] = (byte) indexOfListSize;

        pattern[9] = (byte) (indexOfIntegerValueOf >> 8);
        pattern[10] = (byte) indexOfIntegerValueOf;
        
        pattern[12] = (byte) (indexOfEmitterEmit >> 8);
        pattern[13] = (byte) indexOfEmitterEmit;
    
        return Arrays.equals(pattern, method.getBytecode());
    }

    @Override
    public void applyTransform(ClassFile klass, String vType) {
        addIntInitialise(klass, 0);
        addCombine(klass);
        addGetResult(klass);
        addIsCombineable(klass);
    }
    
    private synchronized void addCombine(ClassFile klass) {
        ConstantPool constants = klass.getConstantPool();
        
        int index = constants.addClassReferenceIndex("uk/ac/man/cs/mapreduce/holders/IntHolder");
             
        combineBytecode[2] = (byte) (index >> 8);
        combineBytecode[3] = (byte) index;        
        
        index = constants.addFieldReferenceIndex("uk/ac/man/cs/mapreduce/holders/IntHolder", "value", "I");
        
        combineBytecode[6] = (byte) (index >> 8);
        combineBytecode[7] = (byte) index;
        
        combineBytecode[11] = (byte) (index >> 8);
        combineBytecode[12] = (byte) index;        
        
        Method combine = klass.addMethod("combine", "(Luk/ac/man/cs/mapreduce/Holder;Ljava/lang/Integer;)V");

        combine.setAccess(Access.PUBLIC);        
        
        combine.setBytecode(3, 3, combineBytecode);
        
        index = constants.addClassReferenceIndex("java/lang/Integer");

        combineBridgeBytecode[4] = (byte) (index >> 8);
        combineBridgeBytecode[5] = (byte) index;
                
        index = constants.addMethodReferenceIndex(klass.getNameReference(), combine.getNameReference(), combine.getDescriptorReference());
        
        combineBridgeBytecode[7] = (byte) (index >> 8);
        combineBridgeBytecode[8] = (byte) index;
                
        Method combineBridge = klass.addMethod("combine", "(Luk/ac/man/cs/mapreduce/Holder;Ljava/lang/Object;)V");
        
        combineBridge.setAccess(Access.PUBLIC, Access.BRIDGE, Access.SYNTHETIC);        
        
        combineBridge.setBytecode(3, 3, combineBridgeBytecode);
    }    

    private void addGetResult(ClassFile klass) {
        ConstantPool constants = klass.getConstantPool();
        
        int index = constants.addClassReferenceIndex("uk/ac/man/cs/mapreduce/holders/IntHolder");
        
        getResultBytecode[2] = (byte) (index >> 8);
        getResultBytecode[3] = (byte) index;
        
        index = constants.addFieldReferenceIndex("uk/ac/man/cs/mapreduce/holders/IntHolder", "value", "I");
        
        getResultBytecode[5] = (byte) (index >> 8);
        getResultBytecode[6] = (byte) index;
        
        index = constants.addMethodReferenceIndex("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        
        getResultBytecode[8] = (byte) (index >> 8);
        getResultBytecode[9] = (byte) index;
        
        Method getResult = klass.addMethod("getResult", "(Luk/ac/man/cs/mapreduce/Holder;)Ljava/lang/Integer;");

        getResult.setAccess(Access.PUBLIC);        
        
        getResult.setBytecode(1, 2, getResultBytecode);
        
        index = constants.addMethodReferenceIndex(klass.getNameReference(), getResult.getNameReference(), getResult.getDescriptorReference());
        
        getResultBridgeBytecode[3] = (byte) (index >> 8);
        getResultBridgeBytecode[4] = (byte) index;
                
        Method getResultBridge = klass.addMethod("getResult", "(Luk/ac/man/cs/mapreduce/Holder;)Ljava/lang/Object;");
        
        getResultBridge.setAccess(Access.PUBLIC, Access.BRIDGE, Access.SYNTHETIC);        
        
        getResultBridge.setBytecode(2, 2, getResultBridgeBytecode);        
    }
}
