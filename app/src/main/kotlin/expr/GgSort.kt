package com.github.metametamoon.app.expr

sealed interface GgSort

/** Integer numbers, aka ZZ */
class GgIntSort : GgSort

class GgBoolSort : GgSort

class GgArraySort<From : GgSort, To : GgSort>(val fromSort: From, val toSort: To) : GgSort
