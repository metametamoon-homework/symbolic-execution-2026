package com.github.metametamoon.app.interpreter

interface GgPathSelector {
  fun queryNextState(): GgState?

  fun pushAliveStates(newStates: List<GgState>)
}

data class BfsPathSelector(val queue: MutableList<GgState> = mutableListOf()) : GgPathSelector {
  override fun queryNextState(): GgState? {
    if (queue.isEmpty()) return null
    return queue.removeAt(0)
  }

  override fun pushAliveStates(newStates: List<GgState>) {
    queue.addAll(newStates)
  }
}

data class DfsPathSelector(val queue: MutableList<GgState> = mutableListOf()) : GgPathSelector {
  override fun queryNextState(): GgState? {
    if (queue.isEmpty()) return null
    return queue.removeAt(0)
  }

  override fun pushAliveStates(newStates: List<GgState>) {
    queue.addAll(0, newStates)
  }
}
