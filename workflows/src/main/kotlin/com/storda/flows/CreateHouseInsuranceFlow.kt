package com.storda.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.withNotary
import com.r3.corda.lib.tokens.money.GBP
import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens
import com.r3.corda.lib.tokens.workflows.utilities.getPreferredNotary
import com.storda.states.HouseInsurance
import net.corda.core.contracts.Amount
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.transactions.SignedTransaction

@InitiatingFlow
@StartableByRPC
class CreateHouseInsuranceFlow(val address: String,
                               val amount: Long) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val insurance = HouseInsurance(
                address = address,
                policyAmount = Amount(amount, GBP),
                maintainers = listOf(ourIdentity)
        )

        val notary = getPreferredNotary(serviceHub)

        return subFlow(CreateEvolvableTokens(insurance withNotary notary))

    }
}