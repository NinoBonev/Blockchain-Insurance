package com.storda.webserver

import com.r3.corda.lib.accounts.workflows.internal.accountService
import net.corda.core.node.AppServiceHub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
private open class Starter

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    val app = SpringApplication(Starter::class.java)
    app.setBannerMode(Banner.Mode.OFF)
    app.webApplicationType = SERVLET
    app.run(*args)
}
