/*
 * Copyright 2019 asd1614
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.asd1614.text.parse.tools;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtils {

    public static void invokeSetterMethod(Object target, String name, Object value) {
        String setterMethodName = name;
        if (!name.startsWith("set")) {
            setterMethodName = "set" + StringUtils.capitalize(name);
        }

        Method method = findMethod(target.getClass(), setterMethodName, null);

        try {
            method.invoke(target, value);
        } catch (Exception ex) {
            if (ex instanceof NoSuchMethodException) {
                throw new IllegalStateException("Method not found: " + ex.getMessage());
            }
            if (ex instanceof IllegalAccessException) {
                throw new IllegalStateException("Could not access method: " + ex.getMessage());
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new UndeclaredThrowableException(ex);
        }
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = searchType.getMethods();
            for (Method method : methods) {
                if (name.equals(method.getName()) &&
                        (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

}
