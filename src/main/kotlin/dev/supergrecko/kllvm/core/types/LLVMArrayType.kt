package dev.supergrecko.kllvm.core.types

import dev.supergrecko.kllvm.core.LLVMType
import dev.supergrecko.kllvm.utils.iterateIntoType
import org.bytedeco.javacpp.PointerPointer
import org.bytedeco.llvm.LLVM.LLVMTypeRef
import org.bytedeco.llvm.global.LLVM

public class LLVMArrayType internal constructor(llvmType: LLVMTypeRef) : LLVMType(llvmType) {
    public fun getLength(): Int {
        return LLVM.LLVMGetArrayLength(llvmType)
    }

    public fun getSubtypes(): List<LLVMType> {
        // TODO: Learn how to test this
        val dest = PointerPointer<LLVMTypeRef>(getLength().toLong())
        LLVM.LLVMGetSubtypes(llvmType, dest)

        return dest.iterateIntoType { LLVMType(it) }
    }

    public fun getElementType(): LLVMType {
        return LLVMType(LLVM.LLVMGetElementType(llvmType))
    }
}