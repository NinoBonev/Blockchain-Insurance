package com.storda.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.internal.flows.createKeyForAccount
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import com.r3.corda.lib.tokens.workflows.flows.issue.IssueTokensFlowHandler
import com.r3.corda.lib.tokens.workflows.flows.issue.addIssueTokens
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveFungibleTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.utilities.heldBy
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import com.storda.getStateReference
import com.storda.hasNoToken
import com.storda.states.HouseInsurance
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.SendTransactionFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

class PurchaseHouseInsuranceFlow(private val policyId: UniqueIdentifier,
                                 private val seller: AccountInfo,
                                 private val buyer: AccountInfo,
                                 private val amount: Amount<TokenType>) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        requireThat { "Dealer not hosted on this node" using (seller.host == ourIdentity) }

        val houseInsuranceRef = getStateReference(serviceHub, HouseInsurance::class.java, policyId)
        val houseInsurance = houseInsuranceRef.state.data
        val houseInsurancePointer = houseInsurance.toPointer<HouseInsurance>()

        // Create a marker token type that is parallel to the house insurance token type
        val markerType = TokenType("Marker$policyId", 0)

        // Check that the marker is not already present
        requireThat { "Report already in use" using (hasNoToken(serviceHub, markerType, ourIdentity)) }

        if (buyer.host == ourIdentity) {
            return SellHouseInsuranceWithinNode().call()
        }

        val token = houseInsurancePointer issuedBy ourIdentity
        val marker = markerType issuedBy ourIdentity

        val nonFToken = marker heldBy ourIdentity
        val other = initiateFlow(buyer.host)

        // Send the full TokenPointer details to the buyer, this must be
        // available in the buyer's vault
        val tx = serviceHub.validatedTransactions.getTransaction(houseInsuranceRef.ref.txhash)!!

        subFlow(SendTransactionFlow(other, tx))

        // Send trade details
        other.send(TradeInfo(seller, buyer, amount, token, nonFToken))

        val signedTransactionFlow = object : SignTransactionFlow(other, tracker()) {
            override fun checkTransaction(stx: SignedTransaction) {

            }
        }
        val txId = subFlow(signedTransactionFlow)

        subFlow(IssueTokensFlowHandler(other))

        return txId
    }

    inner class SellHouseInsuranceWithinNode {
        @Suspendable
        fun call(): SignedTransaction {
            // Create a dealer party for the transaction
            val dealerParty = serviceHub.createKeyForAccount(seller)

            // Create a buyer party for the transaction
            val buyerParty = serviceHub.createKeyForAccount(buyer)

            val houseInsuranceRef = getStateReference(serviceHub, HouseInsurance::class.java, policyId)
            val houseInsurance = houseInsuranceRef.state.data
            val houseInsurancePointer = houseInsurance.toPointer<HouseInsurance>()

            val markerType = TokenType("Marker$policyId", 0)
            val marker = markerType issuedBy ourIdentity heldBy ourIdentity

            val token = houseInsurancePointer issuedBy ourIdentity heldBy buyerParty

            val criteria = QueryCriteria.VaultQueryCriteria(
                    status = Vault.StateStatus.UNCONSUMED,
                    externalIds = listOf(buyer.identifier.id)
            )

            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val builder = TransactionBuilder(notary)

            // Add the money for the transaction
            addMoveFungibleTokens(builder, serviceHub, amount, dealerParty, buyerParty, criteria)

            // Issue the token
            addIssueTokens(builder, listOf(token, marker))

            // Create a list of local signatures for the command
            val signers = builder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub) + ourIdentity.owningKey

            // Sign off the transaction
            val selfSignedTransaction = serviceHub.signInitialTransaction(builder, signers)

            subFlow(ObserverAwareFinalityFlow(selfSignedTransaction, emptyList()))

            return selfSignedTransaction
        }
    }

    @CordaSerializable
    data class TradeInfo(
            val seller: AccountInfo,
            val buyer: AccountInfo,
            val price: Amount<TokenType>,
            val token: IssuedTokenType,
            val marker: NonFungibleToken
    )
}