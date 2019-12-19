package com.storda.contracts

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import com.storda.states.HouseInsurance
import net.corda.core.contracts.Amount
import net.corda.core.transactions.LedgerTransaction

class HouseInsuranceContract : EvolvableTokenContract() {

    override fun additionalCreateChecks(tx: LedgerTransaction) {
        // Not much to do for this example token.
        val newHouse = tx.outputStates.single() as HouseInsurance
        newHouse.apply {
            require(policyAmount > Amount.zero(policyAmount.token)) { "Valuation must be greater than zero." }
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        val oldHouse = tx.inputStates.single() as HouseInsurance
        val newHouse = tx.outputStates.single() as HouseInsurance
        require(oldHouse.address == newHouse.address) { "The address cannot change." }
        require(newHouse.policyAmount > Amount.zero(newHouse.policyAmount.token)) { "Valuation must be greater than zero." }
    }
}