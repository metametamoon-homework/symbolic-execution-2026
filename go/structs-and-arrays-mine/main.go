package main

type Person struct {
	Age  int
	ID   int
}

func testStructBasic() int {
	var p Person
	p.Age = 25
	p.ID = 1001
	return p.Age
}

func testMultipleStructs() int {
	var p Person
	p.Age = 25
	p.ID = 1001

	var p2 Person
	p2.Age = 19
	p.ID = 1000
	return p.Age * 100 + p2.Age
}

func testStructPointer() int {
	p := &Person{Age: 30, ID: 2002}
	p.Age = p.Age + 5
	return p.Age
}
