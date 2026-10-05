package com.github.metametamoon.app.expr

sealed interface ExprSort

class Int32Sort : ExprSort

class BoolSort : ExprSort
