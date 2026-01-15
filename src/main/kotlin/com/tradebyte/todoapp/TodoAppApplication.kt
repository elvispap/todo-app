package com.tradebyte.todoapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TodoAppApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<TodoAppApplication>(*args)
}

object SpringProfiles {
    const val DEVELOPMENT = "development"
    const val TEST = "test"
    const val PRODUCTION = "production"
}
