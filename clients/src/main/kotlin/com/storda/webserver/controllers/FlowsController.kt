package com.storda.webserver.controllers

import com.google.gson.Gson
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.AccountInfoByName
import com.r3.corda.lib.accounts.workflows.flows.CreateAccount
import com.r3.corda.lib.accounts.workflows.flows.RequestAccountInfoFlow
import com.r3.corda.lib.tokens.money.GBP
import com.storda.flows.*
import com.storda.webserver.NodeRPCConnection
import com.storda.webserver.models.PayInsuranceBindingModel
import com.storda.webserver.models.SignInsuranceBindingModel
import jdk.nashorn.internal.parser.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startFlow
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class FlowsController(@Autowired rpc: NodeRPCConnection) {
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

        val linearId = proxy
                .startFlowDynamic(PurchaseInitiateFlow::class.java,
                        party, signInsuranceBindingModel.accountName, signInsuranceBindingModel.price, signInsuranceBindingModel.itemId)
                .returnValue.get()

        return ResponseEntity<Any>("Seller: $seller, Price: ${signInsuranceBindingModel.price}, Transaction ID: ${linearId.id}", HttpStatus.CREATED)

    }

    @PostMapping(value = ["/create-account"])
    private fun createAccount(@RequestParam name : String) : ResponseEntity<*> {

        proxy.startFlow(::CreateAccount, name)

        return ResponseEntity<Any>("Account for name: $name, has been created", HttpStatus.CREATED)
    }

    @PostMapping(value = ["/create-house-insurance"])
    private fun createHouseInsurance(@RequestParam address : String,
                                       @RequestParam amount : String) : ResponseEntity<*> {

        proxy.startFlowDynamic(CreateHouseInsuranceFlow::class.java, address, amount.toLong())

        return ResponseEntity<Any>("New house insurance has been crated", HttpStatus.CREATED)
    }

    @PostMapping(value = ["/purchase-house-insurance"])
    private fun purchaseHouseInsurance(@RequestParam policyId : String,
                                       @RequestParam sellerAccountName : String,
                                       @RequestParam buyerAccountName : String,
                                       @RequestParam amount : String) : ResponseEntity<*> {

        val seller = getAccountInfo(sellerAccountName)
        val buyer = getAccountInfo(buyerAccountName)

        proxy.startFlowDynamic(PurchaseHouseInsuranceFlow::class.java, UniqueIdentifier.fromString(policyId),
                seller, buyer, amount.toLong().GBP)

        return ResponseEntity<Any>("House insurance has been purchased", HttpStatus.CREATED)
    }

    @PostMapping(value = ["/transfer-house-insurance"])
    private fun transferHouseInsurance(@RequestParam tokenId : String,
                                       @RequestParam sellerAccountName : String,
                                       @RequestParam buyerAccountName : String,
                                       @RequestParam amount : String) : ResponseEntity<*> {

        val seller = getAccountInfo(sellerAccountName)
        val buyer = getAccountInfo(buyerAccountName)

        proxy.startFlowDynamic(TransferHouseInsuranceFlow::class.java, UniqueIdentifier.fromString(tokenId),
                seller, buyer, amount.toLong().GBP)

        return ResponseEntity<Any>("House insurance has been purchased", HttpStatus.CREATED)
    }

    fun getAccountInfo(name: String) : AccountInfo {
        return proxy.startFlowDynamic(AccountInfoByName::class.java, name).returnValue
                .getOrThrow().first().state.data
    }
}
