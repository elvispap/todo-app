package com.tradebyte.todoapp.domain.exceptions

class ResourceCannotBeModifiedException : RuntimeException {
    constructor(message: String) : super(message)
}
