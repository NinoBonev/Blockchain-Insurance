package com.storda.webserver.controllers

import com.r3.corda.lib.accounts.workflows.flows.AllAccounts
import com.storda.states.HouseInsurance
import com.storda.states.PurchaseState
import com.storda.webserver.NodeRPCConnection
import com.storda.webserver.models.AccountsViewModel
import com.storda.webserver.models.AllStatesViewModel
import com.storda.webserver.models.HouseInsuranceViewModel
import net.corda.core.contracts.ContractState
import net.corda.core.internal.requiredContractClassName
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class StatesController(@Autowired rpc: NodeRPCConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping("/check-status")
    private fun purchaseState() : List<AllStatesViewModel> {
        val allPurchaseStatesAndRefs = proxy.vaultQueryBy<PurchaseState>().states
        val allPurchaseStates = allPurchaseStatesAndRefs.map { it.state.data }

        var list = mutableListOf<AllStatesViewModel>()
        for (purchaseState in allPurchaseStates) {
            var current : AllStatesViewModel = AllStatesViewModel(purchaseState.seller.toString(), purchaseState.buyer.toString(),
                    purchaseState.price.toString(), purchaseState.amountPaid.toString(),
                    purchaseState.itemId, purchaseState.linearId.toString())

            list.add(current)
        }

        return list
    }

    @GetMapping("/accounts")
    private fun accounts() : List<AccountsViewModel> {

        val accountInfo = proxy.startFlow(::AllAccounts).returnValue.getOrThrow()

        val list = mutableListOf<AccountsViewModel>()
        accountInfo.forEach { list.add(AccountsViewModel(it.state.data.name, it.state.data.host.name.toString())) }

        return list
    }

    @GetMapping("/house-insurances")
    private fun houseInsurances() : List<HouseInsuranceViewModel> {

        val houseInsurancesStatesAndRefs = proxy.vaultQueryBy<ContractState>().states
        val houseInsurancesStates = houseInsurancesStatesAndRefs.map { it.state.data }

        val list = mutableListOf<HouseInsuranceViewModel>()
        houseInsurancesStates.forEach { list.add(HouseInsuranceViewModel(it.toString(), it.participants.map { it.nameOrNull()?.organisation })) }

        return list
    }
}