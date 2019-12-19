package com.storda.webserver.models

import com.r3.corda.lib.tokens.contracts.types.TokenType
import net.corda.core.contracts.Amount
import net.corda.core.identity.Party

class HouseInsuranceViewModel(val address: String,
                              val maintainers: List<String?>
)
