package com.noahcharlton.wgpuj.util;

import com.noahcharlton.wgpuj.WgpuJava;
import jnr.ffi.Runtime;
import jnr.ffi.Struct;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

/**
 * The base class for all wgpuj structs.
 */
public abstract class WgpuJavaStruct extends Struct {

    protected WgpuJavaStruct() {
        super(WgpuJava.getRuntime());
    }

    protected WgpuJavaStruct(Runtime runtime) {
        super(runtime);
    }

    /**
     * Sets this struct to use direct memory.
     *
     * <br>
     * <b>Note:</b> This does not copy the fields from the previous memory location, so
     * all fields need to be reset
     */
    public void useDirectMemory(){
        int size = Struct.size(this);

        jnr.ffi.Pointer pointer = WgpuJava.getRuntime().getMemoryManager().allocateDirect(size);
        useMemory(pointer);
    }

    public jnr.ffi.Pointer getPointerTo(){
        return Struct.getMemory(this);
    }

    /**
     * A rewrite of {@link jnr.ffi.Struct.StructRef} to allow chained
     * structs. It achieves this by calculating the size of the struct at the time
     * the memory is used, instead of in the constructor.
     *
     * Additionally, dynamic struct ref uses a non runtime constructor that is standard
     * for Wgpuj structs.
     *
     * @see <a href=https://github.com/DevOrc/wgpu-java/issues/24>Github Issue #24</a>
     */
    public class DynamicStructRef<T extends WgpuJavaStruct> extends PointerField {
        private final Constructor<T> structConstructor;
        private final Class<T> structType;

        public DynamicStructRef(Class<T> structType) {
            this.structType = structType;

            try {
                structConstructor = structType.getDeclaredConstructor();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("StructRef classes must have an empty constructor!", e);
            }
        }

        public final void set(T struct) {
            set(struct.getPointerTo());
        }

        public final void set(T[] structs) {
            if (structs.length == 0) {
                set(WgpuJava.createNullPointer());
                return;
            }
            int size = Struct.size(structs[0]);

            jnr.ffi.Pointer value = WgpuJava.createDirectPointer(size * structs.length);
            byte[] data = new byte[size];
            for (int i = 0; i < structs.length; i++) {
                Struct.getMemory(structs[i]).get(0L, data, 0, size);
                value.put(size * i, data, 0, size);
            }

            set(value);
        }

        /**
         * returns the struct from memory
         */
        public final T get() {
            T struct;
            try {
                struct = structConstructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to create " + structType.getName(), e);
            }

            struct.useMemory(getPointer());
            return struct;
        }

        /**
         * returns the struct from memory
         */
        public final T[] get(int length) {
            try {
                @SuppressWarnings("unchecked")
                T[] array = (T[]) Array.newInstance(structType, length);

                for (int i = 0; i < length; ++i) {
                    array[i] = structConstructor.newInstance();
                    array[i].useMemory(getPointer().slice(Struct.size(array[i]) * i));
                }

                return array;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to create " + structType.getName(), e);
            }
        }

        @Override
        public java.lang.String toString() {
            return "struct @ " + super.toString()
                    + '\n' + get();
        }
    }
}
