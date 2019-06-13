package com.storda.webserver.controllers

import com.google.gson.Gson
import com.storda.flows.PurchaseInitiateFlow
import com.storda.flows.PurchasePayInstallment
import com.storda.webserver.NodeRPCConnection
import com.storda.webserver.models.PayInsuranceBindingModel
import com.storda.webserver.models.SignInsuranceBindingModel
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.finance.POUNDS
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class FlowsController(rpc: NodeRPCConnection) {
    private val seller : String = "O=PartyB,L=New York,C=US"

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy
    private val gson = Gson()

    @PostMapping(value = ["/pay-insurance"])
    private fun payFlow(@RequestBody payInsuranceBindingModel : PayInsuranceBindingModel): ResponseEntity<*> {
        val id = UniqueIdentifier.fromString(payInsuranceBindingModel.itemId)

        val linearId = proxy
                .startFlowDynamic(PurchasePayInstallment.Initiator::class.java, id, payInsuranceBindingModel.amount*100)
                .returnValue.get()

        return ResponseEntity<Any>("Paid ${payInsuranceBindingModel.amount} on ${linearId.id}.", HttpStatus.CREATED)
    }

    @PostMapping(value = ["/sign-insurance"])
    private fun initFlow(@RequestBody signInsuranceBindingModel : SignInsuranceBindingModel) : ResponseEntity<*> {

        val party = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(seller))

        println(signInsuranceBindingModel)

        val contractAmount = Amount(signInsuranceBindingModel.price*100, 0.00.POUNDS.token)

        val linearId = proxy
                .startFlowDynamic(PurchaseInitiateFlow.Initiator::class.java,
                        party, contractAmount, signInsuranceBindingModel.itemId)
                .returnValue.get()

        return ResponseEntity<Any>("Seller: $seller, Price: ${signInsuranceBindingModel.price}, Transaction ID: ${linearId.id}", HttpStatus.CREATED)

    }
}