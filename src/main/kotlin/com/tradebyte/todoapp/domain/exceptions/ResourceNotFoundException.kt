package com.tradebyte.todoapp.domain.exceptions

class ResourceNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
}
