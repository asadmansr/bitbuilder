package dev.supergrecko.kllvm.dsl

import dev.supergrecko.kllvm.contracts.Builder
import dev.supergrecko.kllvm.core.typedefs.LLVMType
import dev.supergrecko.kllvm.core.enumerations.LLVMTypeKind
import dev.supergrecko.kllvm.core.types.VectorType
import dev.supergrecko.kllvm.factories.TypeFactory

/**
 * Builder class to construct a vector type
 *
 * This is a DSL for building [LLVMTypeKind.Vector] types.
 */
public class VectorBuilder(public val size: Int) : Builder<VectorType> {
    public lateinit var type: LLVMType

    public override fun build(): VectorType {
        return TypeFactory.vector(type, size)
    }
}
