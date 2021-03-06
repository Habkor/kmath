package scientifik.kmath.ast

import scientifik.kmath.operations.Algebra
import scientifik.kmath.operations.NumericAlgebra
import scientifik.kmath.operations.RealField

/**
 * A Mathematical Syntax Tree node for mathematical expressions.
 */
sealed class MST {
    /**
     * A node containing raw string.
     *
     * @property value the value of this node.
     */
    data class Symbolic(val value: String) : MST()

    /**
     * A node containing a numeric value or scalar.
     *
     * @property value the value of this number.
     */
    data class Numeric(val value: Number) : MST()

    /**
     * A node containing an unary operation.
     *
     * @property operation the identifier of operation.
     * @property value the argument of this operation.
     */
    data class Unary(val operation: String, val value: MST) : MST() {
        companion object
    }

    /**
     * A node containing binary operation.
     *
     * @property operation the identifier operation.
     * @property left the left operand.
     * @property right the right operand.
     */
    data class Binary(val operation: String, val left: MST, val right: MST) : MST() {
        companion object
    }
}

// TODO add a function with named arguments

/**
 * Interprets the [MST] node with this [Algebra].
 *
 * @receiver the algebra that provides operations.
 * @param node the node to evaluate.
 * @return the value of expression.
 */
fun <T> Algebra<T>.evaluate(node: MST): T = when (node) {
    is MST.Numeric -> (this as? NumericAlgebra<T>)?.number(node.value)
        ?: error("Numeric nodes are not supported by $this")
    is MST.Symbolic -> symbol(node.value)
    is MST.Unary -> unaryOperation(node.operation, evaluate(node.value))
    is MST.Binary -> when {
        this !is NumericAlgebra -> binaryOperation(node.operation, evaluate(node.left), evaluate(node.right))

        node.left is MST.Numeric && node.right is MST.Numeric -> {
            val number = RealField.binaryOperation(
                node.operation,
                node.left.value.toDouble(),
                node.right.value.toDouble()
            )

            number(number)
        }

        node.left is MST.Numeric -> leftSideNumberOperation(node.operation, node.left.value, evaluate(node.right))
        node.right is MST.Numeric -> rightSideNumberOperation(node.operation, evaluate(node.left), node.right.value)
        else -> binaryOperation(node.operation, evaluate(node.left), evaluate(node.right))
    }
}

/**
 * Interprets the [MST] node with this [Algebra].
 *
 * @receiver the node to evaluate.
 * @param algebra the algebra that provides operations.
 * @return the value of expression.
 */
fun <T> MST.interpret(algebra: Algebra<T>): T = algebra.evaluate(this)
