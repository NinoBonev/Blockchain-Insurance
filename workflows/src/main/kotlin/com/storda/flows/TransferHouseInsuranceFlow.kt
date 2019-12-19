package com.storda.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import com.r3.corda.lib.accounts.workflows.flows.ShareAccountInfo
import com.r3.corda.lib.accounts.workflows.internal.flows.createKeyForAccount
import com.r3.corda.lib.ci.workflows.SyncKeyMappingInitiator
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.TokenPointer
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.workflows.flows.move.MoveTokensFlowHandler
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveFungibleTokens
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import com.r3.corda.lib.tokens.workflows.internal.flows.finality.ObserverAwareFinalityFlow
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import com.storda.getStateReference
import com.storda.states.HouseInsurance
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.SendStateAndRefFlow
import net.corda.core.flows.SendTransactionFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

class TransferHouseInsuranceFlow(private val tokenId: UniqueIdentifier,
                                 private val seller: AccountInfo,
                                 private val buyer: AccountInfo,
                                 private val amount: Amount<TokenType>) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        requireThat { "Seller not hosted on this node" using (seller.host == ourIdentity) }

        if (buyer.host == ourIdentity) {
            return TransferWithinNode().call()
        }

        val original = getStateReference(serviceHub, NonFungibleToken::class.java, tokenId)

        @Suppress("unchecked_cast")
        val tokenPointer = original.state.data.token.tokenType as TokenPointer<HouseInsurance>
        val reportId = tokenPointer.pointer.pointer

        val houseInsuranceRef = getStateReference(serviceHub, HouseInsurance::class.java, reportId)

        val sellerAccount = getStateReference(serviceHub, AccountInfo::class.java, seller.linearId)

        // Send seller account info to buyer
        subFlow(ShareAccountInfo(sellerAccount, listOf(buyer.host)))

        // Send key mappings of token holder to buyer
        subFlow(SyncKeyMappingInitiator(buyer.host, listOf(original.state.data.holder)))

        val tx = serviceHub.validatedTransactions.getTransaction(houseInsuranceRef.ref.txhash)!!

        val other = initiateFlow(buyer.host)

        // Send tx containing the house insurance to buyer
        subFlow(SendTransactionFlow(other, tx))

        // Send original state and reference
        subFlow(SendStateAndRefFlow(other, listOf(original)))

        // Send trade details
        other.send(TradeInfo(buyer, seller, amount))

        // Verify the transaction from the buyer is valid
        val signedTransactionFlow = object : SignTransactionFlow(other, tracker()) {
            override fun checkTransaction(stx: SignedTransaction) {

            }
        }

        val txId = subFlow(signedTransactionFlow)

        subFlow(MoveTokensFlowHandler(other))

        return txId
    }

    /**
     * Use case where both buyer and seller are on the same node
     */
    inner class TransferWithinNode {
        @Suspendable
        fun call(): SignedTransaction {
            // Create a buyer party for the transaction.
            val sellerParty = serviceHub.createKeyForAccount(seller)

            // Create a buyer party for the transaction.
            val buyerParty = serviceHub.createKeyForAccount(buyer)

            val original = getStateReference(serviceHub, NonFungibleToken::class.java, tokenId)

            // Define criteria to retrieve only cash from payer
            val criteria = QueryCriteria.VaultQueryCriteria(
                    status = Vault.StateStatus.UNCONSUMED,
                    externalIds = listOf(buyer.identifier.id)
            )

            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val builder = TransactionBuilder(notary)

            // Update the token to the new owner
            val modified = NonFungibleToken(
                    token = original.state.data.token,
                    linearId = original.state.data.linearId,
                    holder = buyerParty)

            // Add the money for the transaction
            addMoveFungibleTokens(builder, serviceHub, amount, sellerParty, buyerParty, criteria)

            // Update token owner
            addMoveTokens(builder, listOf(original), listOf(modified))

            // Create a list of local signatures for the command
            val signers = builder.toLedgerTransaction(serviceHub).ourSigningKeys(serviceHub) + ourIdentity.owningKey

            // Sign off the transaction
            val selfSignedTransaction = serviceHub.signInitialTransaction(builder, signers)

            val finalityTransaction = subFlow(ObserverAwareFinalityFlow(selfSignedTransaction, emptyList()))

            subFlow(UpdateDistributionListFlow(finalityTransaction))

            return selfSignedTransaction
        }
    }

    @CordaSerializable
    data class TradeInfo(
            val buyer: AccountInfo,
            val seller: AccountInfo,
            val price: Amount<TokenType>
    )
}