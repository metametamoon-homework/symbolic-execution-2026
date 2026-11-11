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
