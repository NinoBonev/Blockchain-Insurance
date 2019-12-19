package com.storda.webserver.controllers

import com.storda.PurchaseState
import com.storda.webserver.NodeRPCConnection
import com.storda.webserver.models.AllStatesViewModel
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
@RequestMapping("/api")
class StatesController(rpc: NodeRPCConnection) {
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
}