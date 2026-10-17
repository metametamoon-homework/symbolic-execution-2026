package com.github.metametamoon.app.expr

sealed interface GgSort

/** Integer numbers, aka ZZ */
class GgIntSort : GgSort

class GgBoolSort : GgSort
