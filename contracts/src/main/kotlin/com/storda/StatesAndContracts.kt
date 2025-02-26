package com.storda

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.core.transactions.LedgerTransaction
import java.util.*

// *****************
// * Contract Code *
// *****************
// This is used to identify our contract when building a transaction

class PurchaseContract : Contract {
    companion object {
        const val PROGRAM_ID: ContractClassName = "com.storda.PurchaseContract"
    }

    interface Commands : CommandData {
        class Initiate : TypeOnlyCommandData(), Commands
        class PayInstallment : TypeOnlyCommandData(), Commands
        class Complete : TypeOnlyCommandData(), Commands
    }

    // A transaction is considered valid if the verify() function of the contract of each of the transaction's input
    // and output states does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Initiate -> requireThat {
                "No input states should be consumed when initating a purchase" using (tx.inputs.isEmpty())
                "Only one output should be produced when initiating a purchase" using (tx.outputs.size == 1)
                val outputState = tx.outputStates.single() as PurchaseState
                "Price should be greater than zero when initiating a purchase" using (outputState.price.quantity > 0)
                "The paid amount should be zero when initiating a purchase" using (
                        outputState.amountPaid.quantity == 0L)
                "Buyer and seller should be different identities when initiating a purchase" using (
                        outputState.buyer != outputState.seller)
                "Both buyer and seller should sign the transaction when initiating a purchase" using (
                        command.signers.toSet() == outputState.participants.map { it.owningKey }.toSet())
            }

            is Commands.PayInstallment -> requireThat {
                "Only one input state should be consumed when paying an installment" using (tx.inputs.size == 1)
                "Only one output should be produced when paying an installment" using (tx.outputs.size == 1)
                val inputState = tx.inputStates.single() as PurchaseState
                val outputState = tx.outputStates.single() as PurchaseState
                "Only the amount paid should change when paying an installment" using (
                        inputState == outputState.copy(amountPaid = inputState.amountPaid))
                "Amount paid should not be greater than the price when paying an installment" using (
                        inputState.price >= outputState.amountPaid)
                "Paid amound should increase when paying an installment" using (
                        outputState.amountPaid > inputState.amountPaid)
                val inputStateSigners = inputState.participants.map { it.owningKey }.toSet()
                val outputStateSigners = outputState.participants.map { it.owningKey }.toSet()
                "Both buyer and seller should sign the transaction when paying an installment" using (
                        command.signers.toSet() == inputStateSigners union outputStateSigners)
            }

            is Commands.Complete -> requireThat {
                "Only one input state should be consumed when completing a purchase" using (tx.inputs.size == 1)
                "No output states should be produced when completing a purchase" using (tx.outputs.isEmpty())
                val inputState = tx.inputStates.single() as PurchaseState
                "Paid amount should be equal to price before completing a purchase" using (
                        inputState.price == inputState.amountPaid)
                "Both buyer and seller should sign the transaction when completing a purchase" using (
                        command.signers.toSet() == inputState.participants.map { it.owningKey }.toSet())
            }

            else -> throw IllegalArgumentException("Invalid command")
        }
    }

}

// *********
// * State *
// *********
data class PurchaseState(
    val buyer: Party,
    val seller: Party,
    val price: Amount<Currency>,
    val amountPaid: Amount<Currency>,
    val itemId: String,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState {
    override val participants: List<Party> get() = listOf(buyer, seller)
}
