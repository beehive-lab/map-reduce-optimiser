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
package uk.ac.man.cs.mapreduce.cfg.data;

import java.util.Arrays;

public class ReadOnlyByteBuffer {
    
    protected byte[] buffer;
    protected int readPosition = 0;

    public ReadOnlyByteBuffer() {
        this.buffer = new byte[0];
    }    
    
    public ReadOnlyByteBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public byte[] asArray() {
        return Arrays.copyOf(buffer, buffer.length);
    }

    public byte[] peek(int size) {
        return peek(0, size);
    }
    
    public byte[] peek(int offset, int size) {
        int from = readPosition;
        int to = from + size;
        return Arrays.copyOfRange(buffer, from, to);
    }    
    
    public int peekU1() {
        return peekU1(readPosition);
    }
    
    public int peekU1(int offset) {
        return buffer[offset] & 0xFF;
    }
    
    public int peekU2() {
        return peekU2(readPosition);
    }    

    public int peekU2(int offset) {
        int value = buffer[offset] & 0xFF;
        value = (value << 8) + (buffer[offset + 1] & 0xFF);
        return value;
    }

    public int peekU4() {
        return peekU4(readPosition);
    }      
    
    public int peekU4(int offset) {
        int value = buffer[offset] & 0xFF;
        value = (value << 8) + (buffer[offset + 1] & 0xFF);
        value = (value << 8) + (buffer[offset + 1] & 0xFF);
        value = (value << 8) + (buffer[offset + 1] & 0xFF);
        return value;
    }
    
    public void forward(int size) {
        readPosition += size;
    }

    public byte[] read(int size) {
        byte[] value = peek(size);
        readPosition += size;
        return value;
    }

    public int readU1() {
        int value = peekU1();
        readPosition++;
        return value;
    }

    public int readU2() {
        int value = peekU2();
        readPosition += 2;
        return value;
    }

    public int readU4() {
        int value = peekU4();
        readPosition += 4;
        return value;
    }

    public int size() {
        return buffer.length;
    }
}
