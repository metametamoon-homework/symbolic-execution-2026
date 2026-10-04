package main

import (
	"encoding/json"
	"fmt"
	"os"

	"golang.org/x/tools/go/packages"
	"golang.org/x/tools/go/ssa"
	"golang.org/x/tools/go/ssa/ssautil"
)

type InstrHead struct {
	Kind string `json:"kind"`
	Text string `json:"text"`
}

type ValueRef struct {
	Kind  string  `json:"kind"`
	Type  int     `json:"type"`
	Name  string  `json:"name,omitempty"`
	Index int     `json:"index"`
	Value *string `json:"value,omitempty"`
}

func main() {
	if len(os.Args) < 2 {
		fmt.Fprintln(os.Stderr, "usage: go-ssa-dump <dir>")
		os.Exit(2)
	}
	dir := os.Args[1]

	cfg := &packages.Config{Mode: packages.LoadAllSyntax, Dir: dir}
	pkgs, err := packages.Load(cfg, "./...")
	if err != nil {
		fmt.Fprintln(os.Stderr, "load:", err)
		os.Exit(1)
	}
	if len(pkgs) == 0 {
		fmt.Fprintln(os.Stderr, "no packages matched in", dir)
		os.Exit(1)
	}
	if n := packages.PrintErrors(pkgs); n > 0 {
		fmt.Fprintf(os.Stderr, "%d package errors\n", n)
		os.Exit(1)
	}

	prog, ssaPkgs := ssautil.AllPackages(pkgs, ssa.InstantiateGenerics)
	prog.Build()

	mine := map[*ssa.Package]bool{}
	for _, p := range ssaPkgs {
		if p != nil {
			mine[p] = true
		}
	}

	cache := newSsaCache()
	var funcs []Func
	for fn := range ssautil.AllFunctions(prog) {
		if !mine[fn.Pkg] || fn.Blocks == nil {
			continue
		}
		f := Func{Name: fn.String()}
		for _, b := range fn.Blocks {
			bb := Block{Index: b.Index}
			for _, s := range b.Succs {
				bb.Succs = append(bb.Succs, s.Index)
			}
			for _, in := range b.Instrs {
				bb.Instrs = append(bb.Instrs, uploadInstToCache(in, cache))
			}
			f.Blocks = append(f.Blocks, bb)
		}
		funcs = append(funcs, f)
	}
	out := Program{Types: cache.Types, Values: cache.Values, Instrs: cache.Instrs, Funcs: funcs}

	enc := json.NewEncoder(os.Stdout)
	enc.SetIndent("", "   ")
	if err := enc.Encode(out); err != nil {
		fmt.Fprintln(os.Stdout, "encode:", err)
		os.Exit(1)
	}
}
