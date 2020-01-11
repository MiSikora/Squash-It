package io.mehow.squashit

class ColumnPrimitive<T> private constructor(val value: T?) {
  companion object {
    operator fun invoke(value: Short?): ColumnPrimitive<Short> = ColumnPrimitive(value)
    operator fun invoke(value: Int?): ColumnPrimitive<Int> = ColumnPrimitive(value)
    operator fun invoke(value: Long?): ColumnPrimitive<Long> = ColumnPrimitive(value)
    operator fun invoke(value: Float?): ColumnPrimitive<Float> = ColumnPrimitive(value)
    operator fun invoke(value: Double?): ColumnPrimitive<Double> = ColumnPrimitive(value)
    operator fun invoke(value: String?): ColumnPrimitive<String> = ColumnPrimitive(value)
    operator fun invoke(value: ByteArray?): ColumnPrimitive<ByteArray> = ColumnPrimitive(value)
  }
}
