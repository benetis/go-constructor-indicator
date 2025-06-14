package internal

type Money struct {
	Cents    int64
	Currency string
}

func NewMoney(cents int64, currency string) Money {
	return Money{
		Cents:    cents,
		Currency: currency,
	}
}

type SpecialType struct {
	SpecialField Money
}
