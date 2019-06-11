package com.storda.webserver.controllers

import com.storda.PurchaseState
import com.storda.webserver.NodeRPCConnection
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatesController(rpc: NodeRPCConnection) {
    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping("/check-insurance-state")
    private fun purchaseState(model : Model) : String {
        val allPurchaseStatesAndRefs = proxy.vaultQueryBy<PurchaseState>().states
        val allPurchaseStates = allPurchaseStatesAndRefs.map { it.state.data }

        var sb  =  StringBuilder()
        allPurchaseStates.map {
            sb.append("Buyer : ${it.buyer}").append(System.lineSeparator())
                    .append("Seller : ${it.seller}").append(System.lineSeparator())
                    .append("Name : ${it.itemId}").append(System.lineSeparator())
                    .append("Price : ${it.price}").append(System.lineSeparator())
                    .append("Amount paid : ${it.amountPaid}").append(System.lineSeparator())
                    .append("Id : ${it.linearId}").append(System.lineSeparator())
                    .append("--------------------").append(System.lineSeparator())

        }

        return sb.toString()
    }
}