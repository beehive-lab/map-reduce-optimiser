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
package uk.ac.man.cs.mapreduce;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bitbucket.crbb.klass.ClassFile;
import org.bitbucket.crbb.klass.KlassException;
import org.bitbucket.crbb.klass.Method;
import org.bitbucket.crbb.klass.attributes.Signature;
import org.bitbucket.crbb.klass.enums.Access;
import uk.ac.man.cs.mapreduce.transforms.GeneralTransform;
import uk.ac.man.cs.mapreduce.transforms.MapReduceTransform;
import uk.ac.man.cs.mapreduce.transforms.SingletonTransform;
import uk.ac.man.cs.mapreduce.transforms.SizeOfTransform;

public class MapReduceTransformer implements ClassFileTransformer {

    private static final Pattern OBJECT_PATTERN = Pattern.compile("L([\\w/\\$]+);");

    private static final MapReduceTransform[] TRANSFORMS = new MapReduceTransform[]{
        new SingletonTransform(),
        new SizeOfTransform(),
        new GeneralTransform()
    };

    private static String getReduceDescriptor(String kType) {
        return String.format("(L%s;Ljava/util/List;Luk/ac/man/cs/mapreduce/Emitter;)V", kType);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className != null && !className.startsWith("java") && !className.startsWith("sun") && !className.startsWith("jdk")) {
            try {
                ClassFile klass = new ClassFile(classfileBuffer);

                klass.parseSpecification();

                if ("uk/ac/man/cs/mapreduce/Reducer".equals(klass.getSuper())) {
                    String kType, vType;

                    klass.parseBody();

                    kType = vType = "java/lang/Object";

                    try {
                        String signature = klass.getAttribute(Signature.class).getSignature();
                        Matcher m = OBJECT_PATTERN.matcher(signature);
                        if (m.find()) {
                            kType = m.group(1);
                        }
                        if (m.find()) {
                            vType = m.group(1);
                        }
                    } catch (NullPointerException ex) {
                        throw new RuntimeException("Need to implement an alternative way of doing this");
                    }

                    Method reduce = klass.getMethod("reduce", getReduceDescriptor(kType));

                    for (MapReduceTransform transform : TRANSFORMS) {
                        if (transform.isMatch(klass.getConstantPool(), reduce)) {
                            transform.applyTransform(klass, vType);
                            //return klass.asArray();
                            byte[] data = klass.asArray();
                            
                            try {
                                Files.write(Paths.get("output.class"), data);
                            } catch (IOException ex) {
                                
                            }
                                    
                            return data;
                        }
                    }
                }

                if ("uk/ac/man/cs/mapreduce/Reducer".equals(klass.getName())) {
                    klass.parseBody();
                    for (Method method : klass.getMethods()) {
                        EnumSet<Access> access = method.getAccess();
                        if (access.size() == 1 && access.contains(Access.FINAL)) {
                            method.setAccess(Access.PUBLIC);
                        }
                    }
                    return klass.asArray();
                }
            } catch (KlassException ex) {
                System.out.println(ex.getMessage());
            }
        }

        return null;
    }
}
