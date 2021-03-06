package io.vexelabs.bitbuilder.llvm.unit.ir.values.constants

import io.vexelabs.bitbuilder.llvm.ir.types.IntType
import io.vexelabs.bitbuilder.llvm.ir.values.constants.ConstantArray
import io.vexelabs.bitbuilder.llvm.ir.values.constants.ConstantInt
import io.vexelabs.bitbuilder.llvm.utils.constIntPairOf
import io.vexelabs.bitbuilder.rtti.cast
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.spekframework.spek2.Spek

internal class ConstantArrayTest : Spek({
    test("create string as constant i8 array") {
        val str = ConstantArray("Hello")

        assertTrue { str.isConstantString() }

        // LLVM strips the null-termination
        assertEquals("Hello", str.getAsString())
    }

    test("create a constant array") {
        val ty = IntType(8)
        val (one, two) = constIntPairOf(1, 2)
        val arr = ConstantArray(ty, listOf(one, two))

        val first = arr.getElementAsConstant(0)

        assertEquals(
            one.getSignedValue(),
            cast<ConstantInt>(first).getSignedValue()
        )
    }
})
