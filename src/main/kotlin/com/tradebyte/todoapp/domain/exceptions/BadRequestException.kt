package com.tradebyte.todoapp.domain.exceptions

class BadRequestException : RuntimeException {
    constructor(message: String) : super(message)
}
