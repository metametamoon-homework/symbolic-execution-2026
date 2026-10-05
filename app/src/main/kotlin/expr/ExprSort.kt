package com.github.metametamoon.app.expr

sealed interface ExprSort

class Bv32Sort : ExprSort

class BoolSort : ExprSort
