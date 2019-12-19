package com.storda.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.workflows.accountService
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount
import com.r3.corda.lib.accounts.workflows.internal.flows.createKeyForAccount
import com.r3.corda.lib.tokens.money.GBP
import com.storda.contracts.PurchaseContract
import com.storda.states.PurchaseState
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class PurchaseInitiateFlow(private val seller: Party,
                           private val accountName: String,
                           private val price: Double,
                           private val itemId: String) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {

            /**
             * Looking for the [AccountInfo] and creating/requesting Public Key for this transaction
             */
            progressTracker.currentStep = RETRIEVING_NOTARY_IDENTITY
            val notary = serviceHub.networkMapCache.notaryIdentities.first()


            progressTracker.currentStep = RETRIEVING_ACCOUNT_INFO
            val accountToMineInto = accountService.accountInfo(accountName).firstOrNull()
                    ?: throw RuntimeException("Account for name: $accountName not found")

            progressTracker.currentStep = GET_ACCOUNT_PUBLIC_KEY
            val buyerOwningKey = accountToMineInto.let {
                if (accountToMineInto.state.data.host == ourIdentity) {
                    serviceHub.createKeyForAccount(accountToMineInto.state.data).owningKey
                } else {
                    subFlow(RequestKeyForAccount(accountToMineInto.state.data)).owningKey
                }
            }

            progressTracker.currentStep = TX_BUILDING;
            val outputState = PurchaseState(buyerOwningKey, seller.owningKey, price.GBP, 0.00.GBP, itemId)

            val keysToSignWith =
                    setOf(buyerOwningKey, seller.owningKey)

            progressTracker.currentStep = (TX_BUILDING);
            val transactionBuilder = TransactionBuilder(notary = notary)
                    .addOutputState(outputState, PurchaseContract.PROGRAM_ID)
                    .addCommand(PurchaseContract.Commands.Initiate(), keysToSignWith.toList())

            progressTracker.currentStep = TX_SIGNING
            val locallySignedTx = serviceHub.signInitialTransaction(transactionBuilder, ourIdentity.owningKey)

            val listOfSigners = listOf(seller, accountToMineInto.state.data.host)
            val sessions = listOfSigners.map { initiateFlow(it) }

            val fullySignedExceptForNotaryTx = subFlow(CollectSignaturesFlow(locallySignedTx,
                    sessions, SIGNATURE_GATHERING.childProgressTracker()))

            return subFlow(FinalityFlow(fullySignedExceptForNotaryTx,
                    sessions.filter { it.counterparty.owningKey != ourIdentity.owningKey }, FINALISATION.childProgressTracker()))
        }

        companion object {
            // Giving our flow a progress tracker allows us to see the flow's
            // progress visually in our node's CRaSH shell.
            private val RETRIEVING_NOTARY_IDENTITY = ProgressTracker.Step("Getting a reference to the notary node.")
            private val RETRIEVING_ACCOUNT_INFO = ProgressTracker.Step("Looking for the account info")
            private val GET_ACCOUNT_PUBLIC_KEY = ProgressTracker.Step("Creating/Requesting account Public Key")
            private val TX_BUILDING = ProgressTracker.Step("Building a transaction.")
            private val TX_SIGNING = ProgressTracker.Step("Signing a transaction.")
            private val SIGNATURE_GATHERING = object : ProgressTracker.Step("Gathering a transaction's signatures.") {
                // Wiring up a child progress tracker allows us to see the
                // subflow's progress steps in our flow's progress tracker.
                override fun childProgressTracker(): ProgressTracker {
                    return CollectSignaturesFlow.tracker()
                }
            }

            private val FINALISATION = object : ProgressTracker.Step("Finalising a transaction.") {
                override fun childProgressTracker(): ProgressTracker {
                    return FinalityFlow.tracker()
                }
            }
        }

        override val progressTracker = ProgressTracker(
                RETRIEVING_NOTARY_IDENTITY,
                RETRIEVING_ACCOUNT_INFO,
                GET_ACCOUNT_PUBLIC_KEY,
                TX_BUILDING,
                TX_SIGNING,
                SIGNATURE_GATHERING,
                FINALISATION
        )
}


@InitiatedBy(PurchaseInitiateFlow::class)
class PurchaseInitiateFlowResponder(val counterPartySession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a Purchase transaction" using (output is PurchaseState)
            }
        }
        val signedId = subFlow(signTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = counterPartySession, expectedTxId = signedId.id))
    }

}

