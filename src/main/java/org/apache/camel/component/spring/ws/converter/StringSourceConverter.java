/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.spring.ws.converter;

import org.apache.camel.Converter;


/**
 * A helper class to transform to and from {@link StringSource} implementations available in 
 * both Camel and Spring Webservices. 
 * 
 * Rationale: most of the time this converter will not be used since both Camel and Spring-WS 
 * use the {@Source} interface abstraction. There is however a chance that you may end up 
 * with incompatible {@link StringSource} implementations, this converter handles these (corner)cases.
 * 
 * Note that conversion options are limited by Spring's {@link StringSource} since it's the most 
 * simple one. It has just one constructor that accepts a String as input.
 * 
 * @author Richard Kettelerij
 * 
 */
@Converter
public class StringSourceConverter {
   
    public StringSourceConverter() {
    }

    /**
     * Converts a Spring-WS {@link StringSource} to a Camel {@link StringSource}.
     */
    @Converter
    public org.apache.camel.converter.jaxp.StringSource toStringSourceFromSpring(org.springframework.xml.transform.StringSource springStringSource) {
        return new org.apache.camel.converter.jaxp.StringSource(springStringSource.toString());
    }
    
    /**
     * Converts a Camel {@link StringSource} to a Spring-WS {@link StringSource}.
     */
    @Converter
    public org.springframework.xml.transform.StringSource toStringSourceFromCamel(org.apache.camel.converter.jaxp.StringSource camelStringSource) {
        return new org.springframework.xml.transform.StringSource(camelStringSource.getText());
    }
}
