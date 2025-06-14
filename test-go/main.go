package main

import (
	"fmt"
	"testgo/internal"
	"testgo/internal/currency"
)

func main() {
	someCode := MyType{temp: 8}
	fmt.Println(someCode)

	withNew := NewMyType(6)
	fmt.Println(withNew)

	oneEuro := internal.Money{
		Cents:    100,
		Currency: currency.EUR,
	}
	twoEuros := internal.NewMoney(200, currency.EUR)

	fmt.Println(oneEuro)
	fmt.Println(twoEuros)

	threeEuro := internal.Money{
		Cents:    300,
		Currency: currency.EUR,
	}
	fmt.Println(threeEuro)

	special := internal.SpecialType{SpecialField: twoEuros}
	fmt.Println(special)

	testas := internal.Money{}
	fmt.Println(testas)
}

type MyType struct {
	temp int8
}

func NewMyType(temp int8) *MyType {
	return &MyType{temp: temp}
}
