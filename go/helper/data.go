package main

import (
	"fmt"
	"go/constant"
	"go/types"

	"golang.org/x/tools/go/ssa"
	"golang.org/x/tools/go/types/typeutil"
)

type Block struct {
	Index  int   `json:"index"`
	Succs  []int `json:"succs"`
	Instrs []int `json:"instrs"`
}

type Func struct {
	Name   string  `json:"name"`
	Blocks []Block `json:"blocks"`
}

type Program struct {
	Types  []any  `json:"types"`
	Values []any  `json:"values"`
	Instrs []any  `json:"instrs"`
	Funcs  []Func `json:"funcs"`
}

type SsaCache struct {
	Types         []any
	Values        []any
	Instrs        []any
	typeResolver  typeutil.Map
	valueResolver map[ssa.Value]int
	instrResolver map[ssa.Instruction]int
}

func newSsaCache() *SsaCache {
	return &SsaCache{
		Types:         []any{},
		Values:        []any{},
		Instrs:        []any{},
		valueResolver: map[ssa.Value]int{},
		instrResolver: map[ssa.Instruction]int{},
	}
}

func uploadTypeToCache(t types.Type, cache *SsaCache) int {
	if index, ok := cache.typeResolver.At(t).(int); ok {
		return index
	}
	index := len(cache.Types)
	cache.Types = append(cache.Types, nil)
	cache.typeResolver.Set(t, index)
	cache.Types[index] = typeRepr(t, cache)
	return index
}

func uploadTupleToCache(in *types.Tuple, cache *SsaCache) []int {
	result := make([]int, in.Len())
	for i := range result {
		result[i] = uploadTypeToCache(in.At(i).Type(), cache)
	}
	return result
}

func typeRepr(in types.Type, cache *SsaCache) map[string]any {
	switch in := in.(type) {
	case *types.Basic:
		return map[string]any{
			"kind": "basic",
			"name": in.Name(),
		}
	case *types.Pointer:
		return map[string]any{
			"kind": "pointer",
			"elem": uploadTypeToCache(in.Elem(), cache),
		}
	case *types.Slice:
		return map[string]any{
			"kind": "slice",
			"elem": uploadTypeToCache(in.Elem(), cache),
		}
	case *types.Interface:
		if !in.Empty() {
			panic(fmt.Sprintf("unhandled non-empty interface %v", in))
		}
		return map[string]any{
			"kind": "interface",
		}
	case *types.Signature:
		return map[string]any{
			"kind":    "signature",
			"params":  uploadTupleToCache(in.Params(), cache),
			"results": uploadTupleToCache(in.Results(), cache),
		}
	}
	panic(fmt.Sprintf("unhandled type %T", in))
}

func constRepr(in *ssa.Const) any {
	if in.Value != nil && in.Value.Kind() == constant.String {
		return constant.StringVal(in.Value)
	}
	return in.Value
}

func uploadValueToCache(in ssa.Value, cache *SsaCache) int {
	if index, ok := cache.valueResolver[in]; ok {
		return index
	}
	index := len(cache.Values)
	cache.Values = append(cache.Values, nil)
	cache.valueResolver[in] = index
	cache.Values[index] = valueRepr(in, cache)
	return index
}

func uploadValuesToCache(in []ssa.Value, cache *SsaCache) []int {
	result := make([]int, len(in))
	for i, v := range in {
		result[i] = uploadValueToCache(v, cache)
	}
	return result
}

func uploadInstToCache(in ssa.Instruction, cache *SsaCache) int {
	if index, ok := cache.instrResolver[in]; ok {
		return index
	}
	index := len(cache.Instrs)
	cache.Instrs = append(cache.Instrs, nil)
	cache.instrResolver[in] = index
	cache.Instrs[index] = instrRepr(in, cache)
	return index
}

func valueRepr(in ssa.Value, cache *SsaCache) map[string]any {
	if instr, ok := in.(ssa.Instruction); ok {
		return map[string]any{
			"kind":  "instr",
			"index": uploadInstToCache(instr, cache),
		}
	}
	switch in := in.(type) {
	case *ssa.Parameter:
		return map[string]any{
			"kind": "parameter",
			"type": uploadTypeToCache(in.Type(), cache),
			"name": in.Name(),
		}
	case *ssa.Builtin:
		return map[string]any{
			"kind": "builtin",
			"type": uploadTypeToCache(in.Type(), cache),
			"name": in.Name(),
		}
	case *ssa.Const:
		return map[string]any{
			"kind":       "const",
			"type":       uploadTypeToCache(in.Type(), cache),
			"constvalue": constRepr(in),
		}
	case *ssa.Function:
		return map[string]any{
			"kind": "function",
			"type": uploadTypeToCache(in.Type(), cache),
		}
	case *ssa.Global:
		return map[string]any{
			"kind": "global",
			"type": uploadTypeToCache(in.Type(), cache),
			"name": in.Name(),
		}
	}
	panic(fmt.Sprintf("unhandled value %T", in))
}

func instrRepr(in ssa.Instruction, cache *SsaCache) map[string]any {
	switch in := in.(type) {
	case *ssa.UnOp:
		return map[string]any{
			"kind": "unop",
			"type": uploadTypeToCache(in.Type(), cache),
			"op":   in.Op.String(),
			"arg":  uploadValueToCache(in.X, cache),
		}
	case *ssa.BinOp:
		return map[string]any{
			"kind": "binop",
			"type": uploadTypeToCache(in.Type(), cache),
			"op":   in.Op.String(),
			"lhs":  uploadValueToCache(in.X, cache),
			"rhs":  uploadValueToCache(in.Y, cache),
		}
	case *ssa.Alloc:
		return map[string]any{
			"kind": "alloc",
			"type": uploadTypeToCache(in.Type(), cache),
			"heap": in.Heap,
		}
	case *ssa.Call:
		return map[string]any{
			"kind":  "call",
			"type":  uploadTypeToCache(in.Type(), cache),
			"value": uploadValueToCache(in.Call.Value, cache),
			"args":  uploadValuesToCache(in.Call.Args, cache),
		}
	case *ssa.Defer:
		return map[string]any{
			"kind": "defer",
		}
	case *ssa.If:
		return map[string]any{
			"kind": "if",
			"cond": uploadValueToCache(in.Cond, cache),
		}
	case *ssa.IndexAddr:
		return map[string]any{
			"kind":       "indexaddr",
			"type":       uploadTypeToCache(in.Type(), cache),
			"collection": uploadValueToCache(in.X, cache),
			"index":      uploadValueToCache(in.Index, cache),
		}
	case *ssa.Jump:
		return map[string]any{
			"kind": "jump",
		}
	case *ssa.MakeInterface:
		return map[string]any{
			"kind":  "makeinterface",
			"type":  uploadTypeToCache(in.Type(), cache),
			"value": uploadValueToCache(in.X, cache),
		}
	case *ssa.Panic:
		return map[string]any{
			"kind":  "panic",
			"value": uploadValueToCache(in.X, cache),
		}
	case *ssa.Phi:
		return map[string]any{
			"kind":  "phi",
			"type":  uploadTypeToCache(in.Type(), cache),
			"edges": uploadValuesToCache(in.Edges, cache),
		}
	case *ssa.RunDefers:
		return map[string]any{
			"kind": "rundefers",
		}
	case *ssa.Store:
		return map[string]any{
			"kind": "store",
			"addr": uploadValueToCache(in.Addr, cache),
			"val":  uploadValueToCache(in.Val, cache),
		}
	case *ssa.Return:
		return map[string]any{
			"kind":    "return",
			"results": uploadValuesToCache(in.Results, cache),
		}
	}
	panic(fmt.Sprintf("unhandled instruction %T", in))
}
