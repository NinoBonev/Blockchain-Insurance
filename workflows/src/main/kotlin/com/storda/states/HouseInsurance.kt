package com.storda.states

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.storda.contracts.HouseInsuranceContract
import net.corda.core.contracts.Amount
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(HouseInsuranceContract::class)
data class HouseInsurance(
        val address: String,
        val policyAmount: Amount<TokenType>,
        override val maintainers: List<Party>,
        override val fractionDigits: Int = 0,
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : EvolvableTokenType()