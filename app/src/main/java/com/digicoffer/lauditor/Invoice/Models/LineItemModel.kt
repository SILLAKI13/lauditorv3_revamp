package com.digicoffer.lauditor.Invoice.Models

class LineItemModel {
    var name: String = ""
    var unitPrice: Double = 0.0
    var quantity: Int = 1

    constructor()

    constructor(name: String, unitPrice: Double, quantity: Int) {
        this.name = name
        this.unitPrice = unitPrice
        this.quantity = quantity
    }

    fun getAmount(): Double {
        return unitPrice * quantity
    }
}
