package envoy

abstract class DiffItem(val diffId: Any) {
    abstract override fun hashCode(): Int
    abstract override fun equals(other: Any?): Boolean
}