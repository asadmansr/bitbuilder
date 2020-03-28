package dev.supergrecko.kllvm.core.typedefs

import dev.supergrecko.kllvm.contracts.Disposable
import dev.supergrecko.kllvm.contracts.Validatable
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.llvm.LLVM.LLVMModuleRef
import org.bytedeco.llvm.global.LLVM

public class Module internal constructor(internal val llvmModule: LLVMModuleRef) : AutoCloseable, Validatable, Disposable {
    public override var valid: Boolean = true

    override fun dispose() {
        require(valid) { "This module has already been disposed." }

        valid = false

        LLVM.LLVMDisposeModule(llvmModule)
    }

    override fun close() = dispose()

    /**
     * This should not be required once all bindings are completed
     * but till then, we need this to interface with the underlying
     * generated bindings
     */
    fun getUnderlyingReference() = llvmModule

    fun dump() {
        LLVM.LLVMDumpModule(llvmModule)
    }

    fun addFunction(name: String, retType: Type, paramTypes: Type, paramCount: Int, isVarArg: Boolean) {
        LLVM.LLVMAddFunction(llvmModule, name,
            LLVM.LLVMFunctionType(
                    retType.getUnderlyingReference(),
                    paramTypes.getUnderlyingReference(),
                    paramCount,
                    if (isVarArg) 0 else 1
            ))
    }

    companion object {
        @JvmStatic
        fun create(sourceFileName: String, context: Context? = null): Module {
            return Module(if (context == null) {
                LLVM.LLVMModuleCreateWithName(sourceFileName)
            } else {
                LLVM.LLVMModuleCreateWithNameInContext(sourceFileName, context.llvmCtx)
            })
        }
    }
}
