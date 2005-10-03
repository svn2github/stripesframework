/* Copyright (C) 2005 Tim Fennell
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the license with this software. If not,
 * it can be found online at http://www.fsf.org/licensing/licenses/lgpl.html
 */
package net.sourceforge.stripes.ajax;

import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * <p>Builds a set of JavaScript statements that will re-construct the value of a Java object,
 * including all Number, String, Enum, Boolea, Collection, Map and Array properties.  Safely handles
 * object graph circularities - each object will be translated only once, and all references will
 * be valid.</p>
 *
 * <p>The JavaScript created by the builder can be evaluated in JavaScript using:</p>
 *
 * <pre>
 * var myObject = eval(generatedFragment);
 * </pre>
 *
 * @author Tim Fennell
 * @since Stripes 1.1
 */
public class JavaScriptBuilder {
    /** Log instance used to log messages. */
    static Log log = Log.getInstance(JavaScriptBuilder.class);

    /** Holds the set of classes representing the primitive types in Java. */
    static Set<Class<?>> simpleTypes = new HashSet<Class<?>>();

    /** Holds the set of types that will be skipped over by default. */
    static Set<Class<?>> ignoredTypes = new HashSet<Class<?>>();

    static {
        simpleTypes.add(Byte.TYPE);
        simpleTypes.add(Short.TYPE);
        simpleTypes.add(Integer.TYPE);
        simpleTypes.add(Long.TYPE);
        simpleTypes.add(Float.TYPE);
        simpleTypes.add(Double.TYPE);
        simpleTypes.add(Boolean.TYPE);

        ignoredTypes.add(Class.class);
    }

    /** Holds the set of objects that have been visited during conversion. */
    private Set<Object> visitedObjects = new HashSet<Object>();

    /** Holds a map of name to JSON value for JS Objects and Arrays. */
    private Map<String,String> objectValues = new HashMap<String,String>();

    /** Holds a map of object.property = object. */
    private Map<String,String> assignments = new HashMap<String,String>();

    /** Holds the root object which is to be converted to JavaScript. */
    private Object rootObject;

    /** Holds the (potentially empty) set of user classes that should be skipped over. */
    private Set<Class<?>> excludeClasses;

    /**
     * Constructs a new JavaScriptBuilder to build JS for the root object supplied.
     *
     * @param root The root object from which to being translation into JavaScript
     * @param userClassesToExclude Zero or more classes to be excluded from translation. Any class
     *        equal to, or extending,
     */
    public JavaScriptBuilder(Object root, Class<?>... userClassesToExclude) {
        this.rootObject = root;
        this.excludeClasses = new HashSet<Class<?>>();

        for (Class<?> type : userClassesToExclude) {
            this.excludeClasses.add(type);
        }

        this.excludeClasses.addAll(ignoredTypes);
    }

    /**
     * Causes the JavaScriptBuilder to navigate the properties of the supplied object and
     * convert them to JavaScript.
     *
     * @return String a fragment of JavaScript that will define and return the JavaScript
     *         equivelant of the Java object supplied to the builder.
     */
    public String build() {
        Writer writer = new StringWriter();
        build(writer);
        return writer.toString();
    }

    /**
     * Causes the JavaScriptBuilder to navigate the properties of the supplied object and
     * convert them to JavaScript, writing them to the supplied writer as it goes.
     */
    public void build(Writer writer) {
        try {
            // If for some reason a caller provided us with a simple scalar object, then
            // convert it and short-circuit return
            if (isScalarType(this.rootObject)) {
                writer.write(getScalarAsString(this.rootObject));
                writer.write(";\n");
                return;
            }

            String rootName = "_sj_root_" + new Random().nextInt(Integer.MAX_VALUE);
            buildNode(rootName, this.rootObject);

            writer.write("var ");
            writer.write(rootName);
            writer.write(";\n");

            for (Map.Entry<String,String> entry : objectValues.entrySet()) {
                writer.append("var ");
                writer.append(entry.getKey());
                writer.append(" = ");
                writer.append(entry.getValue());
                writer.append(";\n");
            }

            for (Map.Entry<String,String> entry : assignments.entrySet()) {
                writer.append(entry.getKey());
                writer.append(" = ");
                writer.append(entry.getValue());
                writer.append(";\n");
            }

            writer.append(rootName).append(";\n");
        }
        catch (Exception e) {
            throw new StripesRuntimeException("Could not build JavaScript for object. An " +
                    "exception was thrown while trying to convert a property from Java to " +
                    "JavaScript. The object being converted is: " + this.rootObject, e);
        }

    }

    /**
     * Returns true if the supplied type should be excluded from conversion, otherwise
     * returns false.
     */
    public boolean isExcludedType(Class<?> type) {
        for (Class<?> excludedType : this.excludeClasses) {
            if (excludedType.isAssignableFrom(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the object is of a type that can be converted to a simple JavaScript
     * scalar, and false otherwise.
     */
    public boolean isScalarType(Object in) {
        Class type = in.getClass();
        return simpleTypes.contains(type)
            || Number.class.isAssignableFrom(type)
            || String.class.isAssignableFrom(type)
            || Boolean.class.isAssignableFrom(type)
            || Date.class.isAssignableFrom(type);
    }

    /**
     * Fetches the value of a scalar type as a String. The input to this method may not be null,
     * and must be a of a type that will return true when supplied to isScalarType().
     */
    public String getScalarAsString(Object in) {
        Class type = in.getClass();

        if (String.class.isAssignableFrom(type)) {
            return "\"" + in + "\"";
        }
        else if(Date.class.isAssignableFrom(type)) {
            return "new Date(" + ((Date) in).getTime() + ")";
        }
        else {
            return in.toString();
        }
    }

    /**
     * Determines the type of the object being translated and dispatches to the
     * build*Node() method.  Generates the temporary name of the object being translated,
     * checks to ensure that the object has not already been translated, and ensure that
     * the object is correctly inserted into the set of assignments.
     *
     * @param name The name that should appear on the left hand side of the assignment
     *        statement once a value for the object has been generated.
     * @param in The object being translated.
     */
    void buildNode(String name, Object in) throws Exception {
        String targetName = "_sj_" + System.identityHashCode(in);

        if (this.visitedObjects.contains(in)) {
            this.assignments.put(name, targetName);
        }
        else if ( isExcludedType(in.getClass()) ) {
            // Do nothing, it's being excluded!!
        }
        else {
            this.visitedObjects.add(in);

            if (Collection.class.isAssignableFrom(in.getClass())) {
                buildCollectionNode(targetName, (Collection) in);
            }
            else if (in.getClass().isArray()) {
                buildArrayNode(targetName, (Object[]) in);
            }
            else if (Map.class.isAssignableFrom(in.getClass())) {
                buildMapNode(targetName, (Map) in);
            }
            else {
                buildObjectNode(targetName, in);
            }

            this.assignments.put(name, targetName);
        }
    }

    /**
     * <p>Processes a Java Object that conforms to JavaBean conventions. Scalar properties of the
     * object are converted to a JSON format object declaration which is inserted into the
     * "objectValues" instance level map. Nested non-scalar objects are processed separately and
     * then setup for re-attachment using the instance level "assignments" map.</p>
     *
     * <p>In most cases just the JavaBean properties will be translated.  In the case of Java 5
     * enums, two additional properties will be translated, one each for the enum's 'ordinal'
     * and 'name' properties.</p>
     *
     * @param targetName The generated name assigned to the Object being translated
     * @param in The Object who's JavaBean properties are to be translated
     */
    void buildObjectNode(String targetName, Object in) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("{");
        PropertyDescriptor[] props = Introspector.getBeanInfo(in.getClass()).getPropertyDescriptors();

        for (PropertyDescriptor property : props) {
            try {
                Object value = property.getReadMethod().invoke(in);

                if (isExcludedType(property.getPropertyType()) || value == null) {
                    continue;
                }

                if (isScalarType(value)) {
                    if (out.length() > 1) {
                        out.append(", ");
                    }
                    out.append(property.getName());
                    out.append(":");
                    out.append( getScalarAsString(value) );
                }
                else {
                    buildNode(targetName + "." + property.getName(), value);
                }
            }
            catch (Exception e) {
                log.warn(e, "Could not translate property [", property.getName(), "] of type [",
                         property.getPropertyType().getName(), "] due to an exception.");
            }
        }

        // Do something a little extra for enums
        if (Enum.class.isAssignableFrom(in.getClass())) {
            Enum e = (Enum) in;

            if (out.length() > 1) { out.append(", "); }
            out.append("ordinal:").append( getScalarAsString(e.ordinal()) );
            out.append(", name:").append( getScalarAsString(e.name()) );
        }

        out.append("}");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds a JavaScript object node from a java Map. The keys of the map are used to
     * define the properties of the JavaScript object.  As such it is assumed that the keys
     * are either primitives, Strings or toString() cleanly.  The values of the map are used
     * to generate the values of the object properties.  Scalar values are inserted directly
     * into the JSON representation, while complex types are converted separately and then
     * attached using assignments.
     *
     * @param targetName The generated name assigned to the Map being translated
     * @param in The Map being translated
     */
    void buildMapNode(String targetName, Map<?,?> in) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("{");

        for (Map.Entry<?,?> entry : in.entrySet()) {
            String propertyName = entry.getKey().toString();
            Object value = entry.getValue();

            if (isScalarType(value)) {
                if (out.length() > 1) {
                    out.append(", ");
                }
                out.append(propertyName);
                out.append(":");
                out.append( getScalarAsString(value) );
            }
            else {
                buildNode(targetName + "." + propertyName, value);
            }
        }

        out.append("}");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds a JavaScript array node from a Java array.  Scalar values are inserted directly
     * into the array definition. Complex values are processed separately - they are inserted
     * into the JSON array as null to maintain ordering, and re-attached later using assignments.
     *
     * @param targetName The generated name of the array node being translated.
     * @param in The Array being translated.
     */
    void buildArrayNode(String targetName, Object[] in) throws Exception {
        StringBuilder out = new StringBuilder();
        out.append("[");

        for (int i=0; i<in.length; i++) {
            if (isScalarType(in[i])) {
                out.append( getScalarAsString(in[i]) );
            }
            else {
                out.append("null");
                buildNode(targetName + "[" + i + "]", in[i]);
            }

            if (i != in.length-1) {
                out.append(", ");
            }
        }

        out.append("]");
        this.objectValues.put(targetName, out.toString());
    }

    /**
     * Builds an object node that is of type collection.  Simply converts the collection
     * to an array, and delegates to buildArrayNode().
     */
    void buildCollectionNode(String targetName, Collection in) throws Exception {
        buildArrayNode(targetName, in.toArray());
    }
}
