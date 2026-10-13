package com.github.metametamoon.app.expr

sealed interface ExprSort

/** Integer numbers, aka ZZ */
class GgIntSort : ExprSort

class GgBoolSort : ExprSort
