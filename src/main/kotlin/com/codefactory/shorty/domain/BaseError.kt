package com.codefactory.shorty.domain

abstract class BaseError (
    open val message: String = "",
    open val reason: BaseError? = null,
) {
    override fun toString(): String =
        "Error(class = ${this.javaClass.simpleName}, message='$message', cause= [$reason])"
}
