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
import uk.ac.man.cs.mapreduce.cfg.ControlFlowGraph;
import uk.ac.man.cs.mapreduce.cfg.MapReduceVerifier;
import uk.ac.man.cs.mapreduce.cfg.instructions.DataType;

public class GeneralTransform extends MapReduceTransform {

    private MapReduceVerifier verifier = null;

    private final byte[] combineBridgeBytecode = new byte[]{
        (byte) 0x2A, // aload_0
        (byte) 0x2B, // aload_1
        (byte) 0x2C, // aload_2
        (byte) 0xC0, // checkcast <V>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB6, // invokevirtual <this#combine()>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB1 // return
    };
    
    private final byte[] getResultBridgeBytecode = new byte[] {
        (byte) 0x2A, // aload_0
        (byte) 0x2B, // aload_1
        (byte) 0xB6, // invokevirtual <this#getResult()>
        (byte) 0x00,
        (byte) 0x00,
        (byte) 0xB0 // areturn
    }; 
    
    @Override
    public boolean isMatch(ConstantPool constants, Method method) {
        verifier = new MapReduceVerifier(new ControlFlowGraph(method.getBytecode()));
        return verifier.buildGraphAndAccept(constants);
    }

    @Override
    public void applyTransform(ClassFile klass, String vType) {
        addInitialise(klass);
        addCombine(klass, vType);
        addGetResult(klass, vType);
        addIsCombineable(klass);
    }

    @Override
    protected void addInitialise(ClassFile klass) {
        DataType intermediateType = verifier.getInitialiseType();
        String holder = "uk/ac/man/cs/mapreduce/holders/" + intermediateType + "Holder";
        String descriptor = "(" + intermediateType.getDescriptor() + ")V";

        byte[] bytecode = verifier.buildInitialiseBytecode();

        ConstantPool constants = klass.getConstantPool();

        Method initiailise = klass.addMethod(initialiseName, initialiseDescriptor);

        initiailise.setAccess(Access.PUBLIC);

        int holderIndex = constants.addClassReferenceIndex(holder);

        int initIndex = constants.addMethodReferenceIndex(holder, "<init>", descriptor);

        ByteBuffer buffer = new ByteBuffer();

        buffer.writeU1(0xBB); // new
        buffer.writeU2(holderIndex);

        buffer.writeU1(0x59); // dup

        buffer.write(bytecode);

        buffer.writeU1(0xB7); // invokespecial
        buffer.writeU2(initIndex);

        buffer.writeU1(0xB0); // areturn

        initiailise.setBytecode(10, 10, buffer.asArray()); // TODO calcultate this better
    }

    private void addCombine(ClassFile klass, String vType) {
        ConstantPool constants = klass.getConstantPool();
        
        byte[] bytecode = verifier.buildCombineBytecode(constants, vType);

        Method combine = klass.addMethod("combine", "(Luk/ac/man/cs/mapreduce/Holder;L" + vType + ";)V");

        combine.setAccess(Access.PUBLIC);
        
        ByteBuffer buffer = new ByteBuffer();
        
        buffer.write(bytecode);
        
        buffer.writeU1(0xB1); // return

        combine.setBytecode(10, 10, buffer.asArray());

        Method combineBridge = klass.addMethod("combine", "(Luk/ac/man/cs/mapreduce/Holder;Ljava/lang/Object;)V");

        combineBridge.setAccess(Access.PUBLIC, Access.BRIDGE, Access.SYNTHETIC);        
        
        int index = constants.addClassReferenceIndex(vType);

        combineBridgeBytecode[4] = (byte) (index >> 8);
        combineBridgeBytecode[5] = (byte) index;

        index = constants.addMethodReferenceIndex(klass.getNameReference(), combine.getNameReference(), combine.getDescriptorReference());

        combineBridgeBytecode[7] = (byte) (index >> 8);
        combineBridgeBytecode[8] = (byte) index;

        combineBridge.setBytecode(3, 3, combineBridgeBytecode);
    }
    
    private void addGetResult(ClassFile klass, String vType) {
        ConstantPool constants = klass.getConstantPool();
        
        byte[] bytecode = verifier.buildGetResultBytecode(constants, vType);

        Method getResult = klass.addMethod("getResult", "(Luk/ac/man/cs/mapreduce/Holder;)L" + vType + ";");

        getResult.setAccess(Access.PUBLIC);        
        
        ByteBuffer buffer = new ByteBuffer();
        
        buffer.write(bytecode);
        
        buffer.writeU1(0xB0); // areturn
        
        getResult.setBytecode(10, 10, buffer.asArray());
        
        int index = constants.addMethodReferenceIndex(klass.getNameReference(), getResult.getNameReference(), getResult.getDescriptorReference());
        
        getResultBridgeBytecode[3] = (byte) (index >> 8);
        getResultBridgeBytecode[4] = (byte) index;
                
        Method getResultBridge = klass.addMethod("getResult", "(Luk/ac/man/cs/mapreduce/Holder;)Ljava/lang/Object;");
        
        getResultBridge.setAccess(Access.PUBLIC, Access.BRIDGE, Access.SYNTHETIC);        
        
        getResultBridge.setBytecode(2, 2, getResultBridgeBytecode);        
    }
}
