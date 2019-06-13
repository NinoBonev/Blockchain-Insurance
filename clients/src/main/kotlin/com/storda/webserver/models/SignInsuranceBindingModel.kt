package com.storda.webserver.models

class SignInsuranceBindingModel(val price: Long, val itemId : String) {

    override fun toString(): String {
        return "SignInsuranceBindingModel(price='$price', itemId='$itemId')"
    }
}