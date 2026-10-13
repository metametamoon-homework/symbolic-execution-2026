package com.github.metametamoon.app

import com.microsoft.z3.Context
import com.microsoft.z3.IntExpr
import com.microsoft.z3.Solver
import com.microsoft.z3.Status
import com.microsoft.z3.Version

fun main() {
  println("Creating Z3 context...")
  val z3ctx = Context()
  println("Z3 version: " + Version.getFullVersion())

  // Simple example: x > 0
  val x: IntExpr? = z3ctx.mkIntConst("x")
  val solver: Solver = z3ctx.mkSolver()
  solver.add(z3ctx.mkGt(x, z3ctx.mkInt(0)))

  if (solver.check() == Status.SATISFIABLE) {
    println("SAT")
    println("Model: " + solver.getModel())
  }

  z3ctx.close()
  println("Success!")
  println("hello!")
}
