/*******************************************************************************
 * Copyright 2019 UIA
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package uia.dao;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

public class Reflections {

    private Reflections() {
    }

    public static List<Class<?>> findClasses(String packageName, ClassLoader loader) throws IOException {
        ClassLoader classLoader = loader == null
                ? Thread.currentThread().getContextClassLoader()
                : loader;
        assert classLoader != null;

        ClassPath cp = ClassPath.from(classLoader);
        ImmutableSet<ClassPath.ClassInfo> cis = cp.getTopLevelClasses(packageName);
        ArrayList<Class<?>> result = new ArrayList<>();
        cis.forEach(ci -> {
            try {
                result.add(Class.forName(ci.getName()));
            }
            catch (Exception ex) {

            }
        });
        return result;
    }

    public static List<Class<?>> findClasses(String packageName, Class<? extends Annotation> annotationClass, ClassLoader loader) throws IOException {
        ClassLoader classLoader = loader == null
                ? Thread.currentThread().getContextClassLoader()
                : loader;
        assert classLoader != null;

        ClassPath cp = ClassPath.from(classLoader);
        ImmutableSet<ClassPath.ClassInfo> cis = cp.getTopLevelClasses(packageName);
        ArrayList<Class<?>> result = new ArrayList<>();
        cis.forEach(ci -> {
            try {
                Class<?> clz = Class.forName(ci.getName());
                if (clz.isAnnotationPresent(annotationClass)) {
                    result.add(clz);
                }
            }
            catch (Exception ex) {

            }
        });
        return result;
    }
}
