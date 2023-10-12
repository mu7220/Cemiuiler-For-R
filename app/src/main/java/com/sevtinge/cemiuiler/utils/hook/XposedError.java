package com.sevtinge.cemiuiler.utils.hook;

import java.io.Serial;

public class XposedError {
    /**
     * Thrown when a class loader is unable to find a class. Unlike {@link ClassNotFoundException},
     * callers are not forced to explicitly catch this. If uncaught, the error will be passed to the
     * next caller in the stack.
     */
    public static final class ClassNotFoundError extends Error {
        @Serial
        private static final long serialVersionUID = -1070936889459514628L;

        /**
         * @hide
         */
        public ClassNotFoundError(Throwable cause) {
            super(cause);
        }

        /**
         * @hide
         */
        public ClassNotFoundError(String detailMessage, Throwable cause) {
            super(detailMessage, cause);
        }
    }

}
