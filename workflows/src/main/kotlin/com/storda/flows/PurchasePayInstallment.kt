package com.storda.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.workflows.accountService
import com.storda.contracts.PurchaseContract
import com.storda.states.PurchaseState
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

class PurchasePayInstallment {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(
        private val purchaseId: UniqueIdentifier,
        private val amount: Long
    ) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val notary = serviceHub.networkMapCache.notaryIdentities.first()
            val stateAndRef = getPurchaseByLinearId(purchaseId)
            val inputState = stateAndRef.state.data

            val buyer = accountService.accountInfo(inputState.buyer!!)

            if (ourIdentity.owningKey != buyer!!.state.data.host.owningKey) {
                throw FlowException("Only the purchase buyer can pay installments")
            }

            val amountPaid = Amount(inputState.amountPaid.quantity + amount, inputState.amountPaid.token)

            val outputState = inputState.copy(amountPaid = amountPaid)
            val signerKeys = inputState.participants.map { it.owningKey }
            val command = Command(
                PurchaseContract.Commands.PayInstallment(),
                signerKeys
            )

            val builder = TransactionBuilder(notary = notary)
                .addInputState(stateAndRef)
                .addOutputState(outputState, PurchaseContract.PROGRAM_ID)
                .addCommand(command)

            builder.verify(serviceHub)
            val partiallySignedTransaction = serviceHub.signInitialTransaction(builder)
            val sessions = initiateFlow(buyer.state.data.host)
            val signedTransaction = subFlow(CollectSignaturesFlow(partiallySignedTransaction, listOf(sessions)))

            return subFlow(FinalityFlow(signedTransaction, listOf(sessions)))
        }

        @Suspendable
        private fun getPurchaseByLinearId(linearId: UniqueIdentifier): StateAndRef<PurchaseState> {
            val queryCriteria = QueryCriteria.LinearStateQueryCriteria(
                linearId = listOf(linearId),
                status = Vault.StateStatus.UNCONSUMED
            )
            return serviceHub.vaultService.queryBy<PurchaseState>(queryCriteria).states.singleOrNull()
                    ?: throw FlowException("Purchase with id $linearId not found.")
        }

    }
}

@InitiatedBy(PurchasePayInstallment.Initiator::class)
class PurchasePayInstallmentResponder(private val counterPartySession: FlowSession) : FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call() : SignedTransaction {
        val signedTransactionFlow = object : SignTransactionFlow(counterPartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val purchaseState = stx.tx.outputStates.single()
                "The output state must be a PurchaseState" using (purchaseState is PurchaseState)
            }
        }

        val signedId = subFlow(signedTransactionFlow)

        return subFlow(ReceiveFinalityFlow(otherSideSession = counterPartySession, expectedTxId = signedId.id))
    }
}
