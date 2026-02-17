package com.mysawit.harvest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MysawitHarvestServiceApplication

fun main(args: Array<String>) {
    val context = runApplication<MysawitHarvestServiceApplication>(*args)
    if (context.environment.getProperty("app.test.close-context", Boolean::class.java, false)) {
        context.close()
    }
}
