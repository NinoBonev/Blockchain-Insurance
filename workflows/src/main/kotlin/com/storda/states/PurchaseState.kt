package com.storda.states

import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.storda.contracts.PurchaseContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import java.security.PublicKey

// *********
// * State *
// *********
@BelongsToContract(PurchaseContract::class)
data class PurchaseState(
        val buyer: PublicKey? = null,
        val seller: PublicKey? = null,
        val price: Amount<TokenType>,
        val amountPaid: Amount<TokenType>,
        val itemId: String,
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState {
    override val participants: List<AbstractParty>
        get() = listOfNotNull(buyer, seller).map { AnonymousParty(it) }
}