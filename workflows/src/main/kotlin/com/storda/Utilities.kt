package com.storda

import com.r3.corda.lib.tokens.contracts.types.TokenType
import com.r3.corda.lib.tokens.workflows.utilities.heldTokensByTokenIssuer
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.vault.QueryCriteria

fun <T : ContractState> getStateReference(serviceHub: ServiceHub, clazz: Class<T>, id: UniqueIdentifier): StateAndRef<T> {
    val vaultPage = serviceHub.vaultService.queryBy(clazz,
            QueryCriteria.LinearStateQueryCriteria(linearId = listOf(id)))

    requireThat {
        "State not found" using (vaultPage.states.size == 1)
    }

    return vaultPage.states.first()
}

fun hasNoToken(serviceHub: ServiceHub, tokenType: TokenType, issuer: Party): Boolean {
    return serviceHub.vaultService.heldTokensByTokenIssuer(tokenType, issuer).states.isEmpty()
}
