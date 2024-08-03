package me.vexmc.strikesync.utils;

import org.bukkit.Bukkit;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class NMSUtils {

    private static final String versionString;
    private static final String nmsVersion;
    private static final String cbVersion;
    private static final Field modifiersField;
    private static final int javaVersion;

    private static final String ERR = "This is probably caused by your minecraft server version. Contact a DEV for more help.";

    static {
        int javaVersion1;

        versionString = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        nmsVersion = "net.minecraft.server." + versionString + '.';
        boolean isPaper = Bukkit.getServer().getName().equalsIgnoreCase("paper");
        if (isPaper && versionString.startsWith("v1_20")) {
            cbVersion = "org.bukkit.craftbukkit.";
        } else {
            cbVersion = "org.bukkit.craftbukkit." + versionString + '.';
        }

        try {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {
                version = version.substring(2, 3);
            } else {
                int dot = version.indexOf(".");
                if (dot != -1)
                    version = version.substring(0, dot);
            }
            version = version.split("-")[0];
            javaVersion1 = Integer.parseInt(version);
        } catch (Throwable throwable) {
            javaVersion1 = -1;
        }
        javaVersion = javaVersion1;

        if (javaVersion < 12) {
            modifiersField = getField(Field.class, "modifiers");
        } else {
            modifiersField = null;
        }
    }

    private NMSUtils() {}

    public static int getJavaVersion() {
        return javaVersion;
    }

    public static Class<?> getNMSClass(String pack, String name) {
        String className;

        if (versionString.startsWith("v1_17") || versionString.startsWith("v1_18") || versionString.startsWith("v1_19") || versionString.startsWith("v1_20")) {
            className = "net.minecraft." + pack + '.' + name;

            if (Bukkit.getServer().getName().equalsIgnoreCase("paper") && versionString.startsWith("v1_20")) {
                ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar();
                className = remapper.remapClassOrArrayName(className);
            }
        } else {
            className = nmsVersion + name;
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalError("Failed to get NMS class " + className + ". " + ERR, e);
        }
    }

    public static Class<?> getPacketClass(String className) {
        return getNMSClass("network.protocol.game", className);
    }

    public static Class<?> getCBClass(String className) {
        try {
            return Class.forName(cbVersion + className);
        } catch (ClassNotFoundException e) {
            throw new InternalError("Failed to get CB class " + className + ". " + ERR, e);
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameters) {
        try {
            return clazz.getConstructor(parameters);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new InternalError("Failed to get constructor. " + ERR, e);
        }
    }

    public static <T> T newInstance(Class<T> constructorSupplier, Object... parameters) {
        Class<?>[] classes = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            classes[i] = parameters[i].getClass();

            classes[i] = switch (parameters[i].getClass().getSimpleName()) {
                case "Double" -> double.class;
                case "Integer" -> int.class;
                case "Float" -> float.class;
                case "Boolean" -> boolean.class;
                case "Byte" -> byte.class;
                case "Short" -> short.class;
                case "Long" -> long.class;
                default -> classes[i];
            };
        }

        try {
            return newInstance(constructorSupplier.getConstructor(classes), parameters);
        } catch (NoSuchMethodException e) {
            throw new InternalError("Failed to instantiate class " + constructorSupplier + ". " + ERR, e);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new InternalError("Failed to instantiate class " + constructor + ". " + ERR, e);
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            throw new InternalError("Failed to get field " + fieldName + ". " + ERR, e);
        }
    }

    public static Field getField(Class<?> target, Class<?> type) {
        return getField(target, type, 0, false);
    }

    public static Field getField(Class<?> target, Class<?> type, int index) {
        return getField(target, type, index, false);
    }

    public static Field getField(Class<?> target, Class<?> type, int index, boolean skipStatic) {
        for (final Field field : target.getDeclaredFields()) {
            if (!type.isAssignableFrom(field.getType())) continue;
            if (skipStatic && Modifier.isStatic(field.getModifiers())) continue;
            if (index-- > 0) continue;

            if (!field.isAccessible()) field.setAccessible(true);
            return field;
        }

        Class<?> superClass = target.getSuperclass();
        if (superClass != null)
            return getField(superClass, type, index);

        throw new IllegalArgumentException("Cannot find field with type " + type);
    }

    public static Object invokeField(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InternalError("Failed to invoke field " + field + ". " + ERR, e);
        }
    }

    public static void setField(Field field, Object instance, Object value) {
        try {
            if (Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                if (javaVersion < 12) {
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                }
            }

            field.set(instance, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InternalError("Failed to set field " + field + ". " + ERR, e);
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameters);

            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new InternalError("Failed to find method " + methodName + ". " + ERR, e);
        }
    }

    public static Method getMethod(Class<?> target, Class<?> returnType, int index, Class<?>... params) {
        for (final Method method : target.getDeclaredMethods()) {
            if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) continue;
            if (!Arrays.equals(method.getParameterTypes(), params)) continue;
            if (index-- > 0) continue;

            if (!method.isAccessible()) method.setAccessible(true);
            return method;
        }

        if (target.getSuperclass() != null)
            return getMethod(target.getSuperclass(), returnType, index, params);

        throw new IllegalArgumentException("Cannot find field with return=" + returnType + ", params=" + Arrays.toString(params));
    }

    public static Object invokeMethod(Method method, Object instance, Object... parameters) {
        try {
            return method.invoke(instance, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new InternalError("Failed to invoke method " + method + ". " + ERR, e);
        }
    }

    public static Object getHandle(Object obj) {
        try {
            return obj.getClass().getMethod("getHandle").invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
