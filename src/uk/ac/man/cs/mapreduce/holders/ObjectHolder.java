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
package uk.ac.man.cs.mapreduce.holders;

import uk.ac.man.cs.mapreduce.Holder;

public class ObjectHolder extends Holder {
     
    public Object value;
   
    public ObjectHolder() {
        this.value = null;
    }    
    
    public ObjectHolder(Object object) {
        this.value = object;
    }
    
    public Object getValue() {
        return value;
    }    
}
