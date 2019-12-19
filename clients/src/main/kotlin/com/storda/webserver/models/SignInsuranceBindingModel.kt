package com.storda.webserver.models

class SignInsuranceBindingModel(val price: Double, val itemId : String, val accountName : String) {

    override fun toString(): String {
        return "SignInsuranceBindingModel(price='$price', itemId='$itemId')"
    }
}