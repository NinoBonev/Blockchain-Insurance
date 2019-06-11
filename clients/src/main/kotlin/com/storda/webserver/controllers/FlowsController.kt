package com.storda.webserver.controllers

import com.google.gson.Gson
import com.storda.flows.PurchaseInitiateFlow
import com.storda.flows.PurchasePayInstallment
import com.storda.webserver.NodeRPCConnection
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.finance.POUNDS
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/flows")
class FlowsController(rpc: NodeRPCConnection) {
    private val seller : String = "O=PartyB,L=New York,C=US"

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy
    private val gson = Gson()

    @PostMapping(value = "/payflow")
    private fun payFlow(
            @RequestParam("id") idString: String,
            @RequestParam("amount") amount: String
    ): ResponseEntity<*> {
        val id = UniqueIdentifier.fromString(idString)

        val linearId = proxy.startFlowDynamic(PurchasePayInstallment.Initiator::class.java, id, amount.toLong())
                .returnValue.get()

        return ResponseEntity<Any>("Paid $amount on ${linearId.id}.", HttpStatus.CREATED)
    }

    @PostMapping(value = "/initflow")
    private fun initFlow(@RequestParam("price") price: String, @RequestParam("itemId") itemId : String): ResponseEntity<*> {

        val party = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(seller))
        val contractAmount = Amount(price.toLong(), 0.POUNDS.token)

        val linearId = proxy.startFlowDynamic(PurchaseInitiateFlow.Initiator::class.java, party, contractAmount, itemId)
                .returnValue.get()

        return ResponseEntity<Any>("Seller: $seller, Price: $price, Transaction ID: ${linearId.id}", HttpStatus.CREATED)
    }
}