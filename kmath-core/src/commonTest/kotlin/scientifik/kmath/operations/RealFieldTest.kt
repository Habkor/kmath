package scientifik.kmath.operations

import scientifik.kmath.operations.internal.FieldVerifier
import kotlin.test.Test
import kotlin.test.assertEquals

internal class RealFieldTest {
    @Test
    fun verify() = FieldVerifier(RealField, 42.0, 66.0, 2.0, 5).verify()

    @Test
    fun testSqrt() {
        val sqrt = RealField { sqrt(25 * one) }
        assertEquals(5.0, sqrt)
    }
}
