package com.sevtinge.cemiuiler.utils.hook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

public abstract class MemberCacheKey {
    /**
     * Note that we use object key instead of string here, because string calculation will lose all
     * the benefits of 'HashMap', this is basically the solution of performance traps.
     * <p>
     * So in fact we only need to use the structural comparison results of the reflection object.
     *
     * @see <a href="https://github.com/RinOrz/LSPosed/blob/a44e1f1cdf0c5e5ebfaface828e5907f5425df1b/benchmark/src/result/ReflectionCacheBenchmark.json">benchmarks for ART</a>
     * @see <a href="https://github.com/meowool-catnip/cloak/blob/main/api/src/benchmark/kotlin/com/meowool/cloak/ReflectionObjectAccessTests.kt#L37-L65">benchmarks for JVM</a>
     */
    public final int hash;

    protected MemberCacheKey(int hash) {
        this.hash = hash;
    }

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final int hashCode() {
        return hash;
    }

    /*构造函数使用*/
    static final class Constructor extends MemberCacheKey {
        public final Class<?> clazz;
        public final Class<?>[] parameters;
        public final boolean isExact;

        public Constructor(Class<?> clazz, Class<?>[] parameters, boolean isExact) {
            super(31 * Objects.hash(clazz, isExact) + Arrays.hashCode(parameters));
            this.clazz = clazz;
            this.parameters = parameters;
            this.isExact = isExact;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Constructor)) return false;
            Constructor that = (Constructor) o;
            return isExact == that.isExact && Objects.equals(clazz, that.clazz) && Arrays.equals(parameters, that.parameters);
        }

        @NonNull
        @Override
        public String toString() {
            String str = clazz.getName() + getParametersString(parameters);
            if (isExact) {
                return str + "#exact";
            } else {
                return str;
            }
        }
    }

    /*字段使用*/
    static final class Field extends MemberCacheKey {
        public final Class<?> clazz;
        public final String name;

        public Field(Class<?> clazz, String name) {
            super(Objects.hash(clazz, name));
            this.clazz = clazz;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Field)) return false;
            Field field = (Field) o;
            return Objects.equals(clazz, field.clazz) && Objects.equals(name, field.name);
        }

        @NonNull
        @Override
        public String toString() {
            return clazz.getName() + "#" + name;
        }
    }

    /*方法使用*/
    static final class Method extends MemberCacheKey {
        public final Class<?> clazz;
        public final String name;
        public final Class<?>[] parameters;
        public final boolean isExact;

        public Method(Class<?> clazz, String name, Class<?>[] parameters, boolean isExact) {
            super(31 * Objects.hash(clazz, name, isExact) + Arrays.hashCode(parameters));
            this.clazz = clazz;
            this.name = name;
            this.parameters = parameters;
            this.isExact = isExact;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Method)) return false;
            Method method = (Method) o;
            return isExact == method.isExact && Objects.equals(clazz, method.clazz) && Objects.equals(name, method.name) && Arrays.equals(parameters, method.parameters);
        }

        @NonNull
        @Override
        public String toString() {
            String str = clazz.getName() + '#' + name + getParametersString(parameters);
            if (isExact) {
                return str + "#exact";
            } else {
                return str;
            }
        }
    }

    private static String getParametersString(Class<?>... clazzes) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (Class<?> clazz : clazzes) {
            if (first)
                first = false;
            else
                sb.append(",");

            if (clazz != null)
                sb.append(clazz.getCanonicalName());
            else
                sb.append("null");
        }
        sb.append(")");
        return sb.toString();
    }

}
