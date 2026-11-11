package com.github.metametamoon.app.expr

import com.github.metametamoon.app.TruePointerType
import com.github.metametamoon.app.TrueType
import kotlinx.collections.immutable.PersistentMap

data class FieldId(
  val fieldIdx: Int,
  val structType: TrueType,
)

class GgMemory(
  val ctx: GgContext,
  val fieldArrays: PersistentMap<FieldId, GgExpr<GgArraySort<*, *>>>,
) {
  fun write(pointer: GgExpr<*>, value: GgExpr<*>): GgMemory {
    if (pointer is FieldPointer) {
      val fieldIdx = pointer.fieldIdx
      val structType = (pointer.structType as TruePointerType).elem
      val fieldId = FieldId(fieldIdx, structType)
      val arrname = "field-array-idx-$fieldIdx-nonce"
      val correspondingArray =
        fieldArrays[fieldId] ?: ctx.mkArrayConst(arrname, GgIntSort(), GgIntSort()) // TODO fixme
      val updatedArray =
        Store<GgIntSort, GgIntSort>(
          correspondingArray.runtimeExprCast(),
          pointer.structPointer.runtimeExprCast(),
          value.runtimeExprCast(),
        )
      return GgMemory(ctx, fieldArrays.putting(fieldId, updatedArray))
    }
    error("Not supported yet")
  }

  fun read(pointer: GgExpr<*>): GgExpr<*> {
    if (pointer is FieldPointer) {
      val fieldIdx = pointer.fieldIdx
      val structType = (pointer.structType as TruePointerType).elem
      val fieldId = FieldId(fieldIdx, structType)
      val correspondingArray = fieldArrays[fieldId] ?: error("Uninitialized array")
      val readResult =
        Select<GgIntSort, GgIntSort>(
          correspondingArray.runtimeExprCast(),
          pointer.structPointer.runtimeExprCast(),
        )
      return readResult
    }
    error("Not supported yet")
  }
}
