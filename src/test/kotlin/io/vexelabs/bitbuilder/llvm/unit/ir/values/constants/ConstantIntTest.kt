package io.vexelabs.bitbuilder.llvm.unit.ir.values.constants

import io.vexelabs.bitbuilder.llvm.ir.IntPredicate
import io.vexelabs.bitbuilder.llvm.ir.TypeKind
import io.vexelabs.bitbuilder.llvm.ir.types.FloatType
import io.vexelabs.bitbuilder.llvm.ir.types.IntType
import io.vexelabs.bitbuilder.llvm.ir.types.PointerType
import io.vexelabs.bitbuilder.llvm.ir.values.constants.ConstantInt
import io.vexelabs.bitbuilder.llvm.setup
import io.vexelabs.bitbuilder.llvm.utils.constIntPairOf
import io.vexelabs.bitbuilder.llvm.utils.runAll
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.spekframework.spek2.Spek

internal object ConstantIntTest : Spek({
    setup()

    val int32 by memoized { IntType(32) }
    val rhs by memoized { ConstantInt(int32, 100) }
    val lhs by memoized { ConstantInt(int32, 100) }

    test("create integer from long words constructor") {
        val ty = IntType(32)
        val value = ConstantInt(ty, listOf(100L, 20L))

        assertEquals(100, value.getSignedValue())
    }

    test("specify sign extension for integer") {
        val ty = IntType(32)

        val v1 = ConstantInt(ty, 100, true)
        val v2 = ConstantInt(ty, 200, false)

        assertEquals(100, v1.getSignedValue())
        assertEquals(200, v2.getUnsignedValue())
    }

    test("get the negation value") {
        val neg = lhs.getNeg()

        assertEquals(-100, neg.getSignedValue())
    }

    test("get the inversion value") {
        val not = lhs.getNot()

        assertEquals(-101, not.getSignedValue())
    }

    test("get sum of two integers") {
        val sum = lhs.getAdd(rhs)

        assertEquals(200, sum.getSignedValue())
        assertEquals(200, sum.getUnsignedValue())
    }

    test("get difference between two integers") {
        val diff = lhs.getSub(rhs, true, false)

        assertEquals(0, diff.getSignedValue())
        assertEquals(0, diff.getUnsignedValue())
    }

    test("get the product of two integers") {
        val product = lhs.getMul(rhs)

        assertEquals(10000, product.getSignedValue())
        assertEquals(10000, product.getUnsignedValue())
    }

    test("get the quotient of two integers") {
        val quotient1 = lhs.getSDiv(rhs, exact = true)
        val quotient2 = lhs.getSDiv(rhs, exact = false)
        val quotient3 = lhs.getUDiv(rhs, exact = true)
        val quotient4 = lhs.getUDiv(rhs, exact = false)

        assertEquals(1, quotient1.getSignedValue())
        assertEquals(1, quotient2.getSignedValue())
        assertEquals(1, quotient3.getSignedValue())
        assertEquals(1, quotient4.getSignedValue())
    }

    test("get the truncated value from floating point division") {
        // 10 div 3 is not an even number
        val v1 = ConstantInt(int32, 10, false)
        val v2 = ConstantInt(int32, 3, false)

        val quotient1 = v1.getSDiv(v2, exact = true)
        val quotient2 = v1.getSDiv(v2, exact = false)
        val quotient3 = v1.getUDiv(v2, exact = true)
        val quotient4 = v1.getUDiv(v2, exact = false)

        assertEquals(3, quotient1.getSignedValue())
        assertEquals(3, quotient2.getSignedValue())
        assertEquals(3, quotient3.getSignedValue())
        assertEquals(3, quotient4.getSignedValue())
    }

    test("get the remainder of this and another integer") {
        val rem1 = lhs.getURem(rhs)
        val rem2 = lhs.getSRem(rhs)

        assertEquals(0, rem1.getUnsignedValue())
        assertEquals(0, rem1.getSignedValue())
        assertEquals(0, rem2.getUnsignedValue())
        assertEquals(0, rem2.getSignedValue())
    }

    test("get the logical and result with this and another integer") {
        val res = lhs.getAnd(rhs).getSignedValue()

        assertEquals(100 and 100, res)
    }

    test("get the logical or result with this and another integer") {
        val res = lhs.getOr(rhs).getSignedValue()

        assertEquals(100 or 100, res)
    }

    test("get the logical xor result with this and another integer") {
        val res = lhs.getXor(rhs).getSignedValue()

        assertEquals(100 xor 100, res)
    }

    test("perform comparison of two integers") {
        val left = ConstantInt(int32, 10)
        val right = ConstantInt(int32, 20)

        val expected = arrayOf<Long>(
            0, 1, // eq, ne
            0, 0, // ugt, uge
            1, 1, // ult ule
            0, 0, // sgt sge
            1, 1 // slt sle
        )

        for ((idx, it) in IntPredicate.values().withIndex()) {
            val res = left.getICmp(it, right).getUnsignedValue()
            val expect = expected[idx]

            assertEquals(expect, res)
        }
    }

    test("get this shifted left of another integer") {
        val left = ConstantInt(int32, 4)
        val right = ConstantInt(int32, 9)
        val res = left.getShl(right).getSignedValue()

        assertEquals(4 shl 9, res)
    }

    test("get this shifted right of another integer") {
        val left = ConstantInt(int32, 4)
        val right = ConstantInt(int32, 9)
        val res = left.getLShr(right).getSignedValue()

        assertEquals(4 shr 9, res)
    }

    test("get this arithmetically shifted right of another integer") {
        val left = ConstantInt(int32, 4)
        val right = ConstantInt(int32, 9)
        val res = left.getAShr(right).getSignedValue()

        assertEquals(4 shr 9, res)
    }

    test("truncating to a tinier integer type") {
        val left = ConstantInt(IntType(8), 64)
        val trunc = left.getTrunc(IntType(1))

        assertEquals(0, trunc.getUnsignedValue())
    }

    test("zero or sign-extend to a larger integer type") {
        val left = ConstantInt(IntType(8), 64)
        val sext = left.getSExt(IntType(16))
        val zext = left.getZExt(IntType(16))

        assertEquals(64, sext.getSignedValue())
        assertEquals(64, zext.getUnsignedValue())
        assertEquals(TypeKind.Integer, sext.getType().getTypeKind())
        assertEquals(TypeKind.Integer, zext.getType().getTypeKind())
    }

    test("cast to floating point type") {
        val left = ConstantInt(IntType(64), 64)

        val si = left.getSIToFP(FloatType(TypeKind.Float))
        val ui = left.getUIToFP(FloatType(TypeKind.Double))

        assertEquals(64.0, si.getDouble())
        assertEquals(64.0, ui.getDouble())
    }

    test("cast into pointer type") {
        val ty = IntType(64)
        val left = ConstantInt(ty, 100)
        val ptr = left.getIntToPtr(PointerType(ty))

        assertTrue { ptr.isConstant() }

        val num = ptr.getIntCast(ty)

        assertEquals(left.getSignedValue(), num.getSignedValue())
    }

    test("cast to different int type") {
        val targetTy = IntType(128)
        val left = ConstantInt(IntType(32), 100000)

        val second = left.getIntCast(targetTy, true)

        assertEquals(left.getSignedValue(), second.getSignedValue())
    }

    test("casting to its own type does nothing") {
        val left = ConstantInt(int32, 100000)

        left.getIntCast(IntType(32), true)
    }

    test("selecting between two values on a condition") {
        // i1 true
        val cond = ConstantInt(IntType(1), 1)
        val left = ConstantInt(int32, 10)
        val right = ConstantInt(int32, 20)

        val res = cond.getSelect(left, right)

        assertEquals(10, ConstantInt(res.ref).getSignedValue())
    }
})
