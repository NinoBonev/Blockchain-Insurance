package com.storda.webserver.models

class PayInsuranceBindingModel(val itemId : String, val amount : Long) {

    override fun toString(): String {
        return "PayInsuranceBindingModel(itemId='$itemId', amount='$amount')"
    }
}
