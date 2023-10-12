package com.sevtinge.cemiuiler.utils.hook;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class XposedField extends XposedError {
    private static final ConcurrentHashMap<MemberCacheKey.Field, Optional<Field>> fieldCache = new ConcurrentHashMap<>();
    private static final WeakHashMap<Object, HashMap<String, Object>> additionalFields = new WeakHashMap<>();

    /**
     * Look up a field in a class and set it to accessible.
     *
     * @param clazz     The class which either declares or inherits the field.
     * @param fieldName The field name.
     * @return A reference to the field.
     * @throws NoSuchFieldError In case the field was not found.
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        MemberCacheKey.Field key = new MemberCacheKey.Field(clazz, fieldName);

        return fieldCache.computeIfAbsent(key, k -> {
            try {
                Field newField = findFieldRecursiveImpl(k.clazz, k.name);
                newField.setAccessible(true);
                return Optional.of(newField);
            } catch (NoSuchFieldException e) {
                return Optional.empty();
            }
        }).orElseThrow(() -> new NoSuchFieldError(key.toString()));
    }

    public static Field findFieldRecursiveImpl(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class))
                    break;

                try {
                    return clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw e;
        }
    }

    /**
     * Returns the first field of the given type in a class.
     * Might be useful for Proguard'ed classes to identify fields with unique types.
     *
     * @param clazz The class which either declares or inherits the field.
     * @param type  The type of the field.
     * @return A reference to the first field of the given type.
     * @throws NoSuchFieldError In case no matching field was not found.
     */
    public static Field findFirstFieldByExactType(Class<?> clazz, Class<?> type) {
        Class<?> clz = clazz;
        do {
            for (Field field : clz.getDeclaredFields()) {
                if (field.getType() == type) {
                    field.setAccessible(true);
                    return field;
                }
            }
        } while ((clz = clz.getSuperclass()) != null);

        throw new NoSuchFieldError("Field of type " + type.getName() + " in class " + clazz.getName());
    }

    /**
     * Returns the index of the first parameter declared with the given type.
     *
     * @throws NoSuchFieldError if there is no parameter with that type.
     * @hide
     */
    public static int getFirstParameterIndexByType(Member method, Class<?> type) {
        Class<?>[] classes = (method instanceof Method) ?
            ((Method) method).getParameterTypes() : ((Constructor<?>) method).getParameterTypes();
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] == type) {
                return i;
            }
        }
        throw new NoSuchFieldError("No parameter of type " + type + " found in " + method);
    }

    /**
     * Returns the index of the parameter declared with the given type, ensuring that there is exactly one such parameter.
     *
     * @throws NoSuchFieldError if there is no or more than one parameter with that type.
     * @hide
     */
    public static int getParameterIndexByType(Member method, Class<?> type) {
        Class<?>[] classes = (method instanceof Method) ?
            ((Method) method).getParameterTypes() : ((Constructor<?>) method).getParameterTypes();
        int idx = -1;
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] == type) {
                if (idx == -1) {
                    idx = i;
                } else {
                    throw new NoSuchFieldError("More than one parameter of type " + type + " found in " + method);
                }
            }
        }
        if (idx != -1) {
            return idx;
        } else {
            throw new NoSuchFieldError("No parameter of type " + type + " found in " + method);
        }
    }

    //#################################################################################################

    /**
     * Sets the value of an object field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setObjectField(Object obj, String fieldName, Object value) {
        try {
            findField(obj.getClass(), fieldName).set(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a {@code boolean} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setBooleanField(Object obj, String fieldName, boolean value) {
        try {
            findField(obj.getClass(), fieldName).setBoolean(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a {@code byte} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setByteField(Object obj, String fieldName, byte value) {
        try {
            findField(obj.getClass(), fieldName).setByte(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a {@code char} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setCharField(Object obj, String fieldName, char value) {
        try {
            findField(obj.getClass(), fieldName).setChar(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a {@code double} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setDoubleField(Object obj, String fieldName, double value) {
        try {
            findField(obj.getClass(), fieldName).setDouble(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a {@code float} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setFloatField(Object obj, String fieldName, float value) {
        try {
            findField(obj.getClass(), fieldName).setFloat(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of an {@code int} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setIntField(Object obj, String fieldName, int value) {
        try {
            findField(obj.getClass(), fieldName).setInt(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a {@code long} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setLongField(Object obj, String fieldName, long value) {
        try {
            findField(obj.getClass(), fieldName).setLong(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a {@code short} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static void setShortField(Object obj, String fieldName, short value) {
        try {
            findField(obj.getClass(), fieldName).setShort(obj, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    //#################################################################################################

    /**
     * Returns the value of an object field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static Object getObjectField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).get(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * For inner classes, returns the surrounding instance, i.e. the {@code this} reference of the surrounding class.
     */
    public static Object getSurroundingThis(Object obj) {
        return getObjectField(obj, "this$0");
    }

    /**
     * Returns the value of a {@code boolean} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean getBooleanField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getBoolean(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Returns the value of a {@code byte} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static byte getByteField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getByte(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Returns the value of a {@code char} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static char getCharField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getChar(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Returns the value of a {@code double} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static double getDoubleField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getDouble(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Returns the value of a {@code float} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static float getFloatField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getFloat(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Returns the value of an {@code int} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static int getIntField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getInt(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Returns the value of a {@code long} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static long getLongField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getLong(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Returns the value of a {@code short} field in the given object instance. A class reference is not sufficient! See also {@link #findField}.
     */
    public static short getShortField(Object obj, String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getShort(obj);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    //#################################################################################################

    /**
     * Sets the value of a static object field in the given class. See also {@link #findField}.
     */
    public static void setStaticObjectField(Class<?> clazz, String fieldName, Object value) {
        try {
            findField(clazz, fieldName).set(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code boolean} field in the given class. See also {@link #findField}.
     */
    public static void setStaticBooleanField(Class<?> clazz, String fieldName, boolean value) {
        try {
            findField(clazz, fieldName).setBoolean(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code byte} field in the given class. See also {@link #findField}.
     */
    public static void setStaticByteField(Class<?> clazz, String fieldName, byte value) {
        try {
            findField(clazz, fieldName).setByte(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code char} field in the given class. See also {@link #findField}.
     */
    public static void setStaticCharField(Class<?> clazz, String fieldName, char value) {
        try {
            findField(clazz, fieldName).setChar(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code double} field in the given class. See also {@link #findField}.
     */
    public static void setStaticDoubleField(Class<?> clazz, String fieldName, double value) {
        try {
            findField(clazz, fieldName).setDouble(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code float} field in the given class. See also {@link #findField}.
     */
    public static void setStaticFloatField(Class<?> clazz, String fieldName, float value) {
        try {
            findField(clazz, fieldName).setFloat(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code int} field in the given class. See also {@link #findField}.
     */
    public static void setStaticIntField(Class<?> clazz, String fieldName, int value) {
        try {
            findField(clazz, fieldName).setInt(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code long} field in the given class. See also {@link #findField}.
     */
    public static void setStaticLongField(Class<?> clazz, String fieldName, long value) {
        try {
            findField(clazz, fieldName).setLong(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code short} field in the given class. See also {@link #findField}.
     */
    public static void setStaticShortField(Class<?> clazz, String fieldName, short value) {
        try {
            findField(clazz, fieldName).setShort(null, value);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    //#################################################################################################

    /**
     * Returns the value of a static object field in the given class. See also {@link #findField}.
     */
    public static Object getStaticObjectField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).get(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Returns the value of a static {@code boolean} field in the given class. See also {@link #findField}.
     */
    public static boolean getStaticBooleanField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getBoolean(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code byte} field in the given class. See also {@link #findField}.
     */
    public static byte getStaticByteField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getByte(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code char} field in the given class. See also {@link #findField}.
     */
    public static char getStaticCharField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getChar(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code double} field in the given class. See also {@link #findField}.
     */
    public static double getStaticDoubleField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getDouble(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code float} field in the given class. See also {@link #findField}.
     */
    public static float getStaticFloatField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getFloat(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code int} field in the given class. See also {@link #findField}.
     */
    public static int getStaticIntField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getInt(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code long} field in the given class. See also {@link #findField}.
     */
    public static long getStaticLongField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getLong(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    /**
     * Sets the value of a static {@code short} field in the given class. See also {@link #findField}.
     */
    public static short getStaticShortField(Class<?> clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).getShort(null);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        }
    }

    //#################################################################################################

    /**
     * Attaches any value to an object instance. This simulates adding an instance field.
     * The value can be retrieved again with {@link #getAdditionalInstanceField}.
     *
     * @param obj   The object instance for which the value should be stored.
     * @param key   The key in the value map for this object instance.
     * @param value The value to store.
     * @return The previously stored value for this instance/key combination, or {@code null} if there was none.
     */
    public static Object setAdditionalInstanceField(Object obj, String key, Object value) {
        if (obj == null)
            throw new NullPointerException("object must not be null");
        if (key == null)
            throw new NullPointerException("key must not be null");

        HashMap<String, Object> objectFields;
        synchronized (additionalFields) {
            objectFields = additionalFields.computeIfAbsent(obj, k -> new HashMap<>());
        }

        synchronized (objectFields) {
            return objectFields.put(key, value);
        }
    }

    /**
     * Returns a value which was stored with {@link #setAdditionalInstanceField}.
     *
     * @param obj The object instance for which the value has been stored.
     * @param key The key in the value map for this object instance.
     * @return The stored value for this instance/key combination, or {@code null} if there is none.
     */
    public static Object getAdditionalInstanceField(Object obj, String key) {
        if (obj == null)
            throw new NullPointerException("object must not be null");
        if (key == null)
            throw new NullPointerException("key must not be null");

        HashMap<String, Object> objectFields;
        synchronized (additionalFields) {
            objectFields = additionalFields.get(obj);
            if (objectFields == null)
                return null;
        }

        synchronized (objectFields) {
            return objectFields.get(key);
        }
    }

    /**
     * Removes and returns a value which was stored with {@link #setAdditionalInstanceField}.
     *
     * @param obj The object instance for which the value has been stored.
     * @param key The key in the value map for this object instance.
     * @return The previously stored value for this instance/key combination, or {@code null} if there was none.
     */
    public static Object removeAdditionalInstanceField(Object obj, String key) {
        if (obj == null)
            throw new NullPointerException("object must not be null");
        if (key == null)
            throw new NullPointerException("key must not be null");

        HashMap<String, Object> objectFields;
        synchronized (additionalFields) {
            objectFields = additionalFields.get(obj);
            if (objectFields == null)
                return null;
        }

        synchronized (objectFields) {
            return objectFields.remove(key);
        }
    }

    /**
     * Like {@link #setAdditionalInstanceField}, but the value is stored for the class of {@code obj}.
     */
    public static Object setAdditionalStaticField(Object obj, String key, Object value) {
        return setAdditionalInstanceField(obj.getClass(), key, value);
    }

    /**
     * Like {@link #getAdditionalInstanceField}, but the value is returned for the class of {@code obj}.
     */
    public static Object getAdditionalStaticField(Object obj, String key) {
        return getAdditionalInstanceField(obj.getClass(), key);
    }

    /**
     * Like {@link #removeAdditionalInstanceField}, but the value is removed and returned for the class of {@code obj}.
     */
    public static Object removeAdditionalStaticField(Object obj, String key) {
        return removeAdditionalInstanceField(obj.getClass(), key);
    }

    /**
     * Like {@link #setAdditionalInstanceField}, but the value is stored for {@code clazz}.
     */
    public static Object setAdditionalStaticField(Class<?> clazz, String key, Object value) {
        return setAdditionalInstanceField(clazz, key, value);
    }

    /**
     * Like {@link #setAdditionalInstanceField}, but the value is returned for {@code clazz}.
     */
    public static Object getAdditionalStaticField(Class<?> clazz, String key) {
        return getAdditionalInstanceField(clazz, key);
    }

    /**
     * Like {@link #setAdditionalInstanceField}, but the value is removed and returned for {@code clazz}.
     */
    public static Object removeAdditionalStaticField(Class<?> clazz, String key) {
        return removeAdditionalInstanceField(clazz, key);
    }

}
