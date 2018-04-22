package scientifik.kmath.structures

import scientifik.kmath.operations.Field
import scientifik.kmath.operations.FieldElement
import scientifik.kmath.operations.Real

class ShapeMismatchException(val expected: List<Int>, val actual: List<Int>) : RuntimeException()

/**
 * Field for n-dimensional arrays.
 * @param shape - the list of dimensions of the array
 * @param elementField - operations field defined on individual array element
 */
abstract class NDField<T : FieldElement<T>>(val shape: List<Int>, val elementField: Field<T>) : Field<NDArray<T>> {
    /**
     * Create new instance of NDArray using field shape and given initializer
     */
    abstract fun produce(initializer: (List<Int>) -> T): NDArray<T>

    override val zero: NDArray<T>
        get() = produce { elementField.zero }

    private fun checkShape(vararg arrays: NDArray<T>) {
        arrays.forEach {
            if (shape != it.shape) {
                throw ShapeMismatchException(shape, it.shape)
            }
        }
    }

    /**
     * Element-by-element addition
     */
    override fun add(a: NDArray<T>, b: NDArray<T>): NDArray<T> {
        checkShape(a, b)
        return produce { a[it] + b[it] }
    }

    /**
     * Multiply all elements by cinstant
     */
    override fun multiply(a: NDArray<T>, k: Double): NDArray<T> {
        checkShape(a)
        return produce { a[it] * k }
    }

    override val one: NDArray<T>
        get() = produce { elementField.one }

    /**
     * Element-by-element multiplication
     */
    override fun multiply(a: NDArray<T>, b: NDArray<T>): NDArray<T> {
        checkShape(a)
        return produce { a[it] * b[it] }
    }

    /**
     * Element-by-element division
     */
    override fun divide(a: NDArray<T>, b: NDArray<T>): NDArray<T> {
        checkShape(a)
        return produce { a[it] / b[it] }
    }
}


interface NDArray<T : FieldElement<T>> : FieldElement<NDArray<T>>, Iterable<Pair<List<Int>, T>> {

    /**
     * The list of dimensions of this NDArray
     */
    val shape: List<Int>
        get() = (context as NDField<T>).shape

    /**
     * The number of dimentsions for this array
     */
    val dimension: Int
        get() = shape.size

    /**
     * Get the element with given indexes. If number of indexes is different from {@link dimension}, throws exception.
     */
    operator fun get(vararg index: Int): T

    operator fun get(index: List<Int>): T {
        return get(*index.toIntArray())
    }

    override operator fun iterator(): Iterator<Pair<List<Int>, T>> {
        return iterateIndexes(shape).map { Pair(it, this[it]) }.iterator()
    }

    /**
     * Generate new NDArray, using given transformation for each element
     */
    fun transform(action: (List<Int>, T) -> T): NDArray<T> = (context as NDField<T>).produce { action(it, this[it]) }

    companion object {
        /**
         * Iterate over all indexes in the nd-shape
         */
        fun iterateIndexes(shape: List<Int>): Sequence<List<Int>> {
            return if (shape.size == 1) {
                (0 until shape[0]).asSequence().map { listOf(it) }
            } else {
                val tailShape = ArrayList(shape).apply { remove(0) }
                val tailSequence: List<List<Int>> = iterateIndexes(tailShape).toList()
                (0 until shape[0]).asSequence().map { firstIndex ->
                    //adding first element to each of provided index lists
                    tailSequence.map { listOf(firstIndex) + it }.asSequence()
                }.flatten()
            }
        }
    }
}


expect fun RealNDArray(shape: List<Int>, initializer: (List<Int>) -> Double): NDArray<Real>