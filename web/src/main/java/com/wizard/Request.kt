package com.wizard

import jakarta.servlet.http.HttpServletRequest

class Request(private val req: HttpServletRequest) {

    fun method(): String? {
        return req.method
    }

    fun uri(): String {
        return req.requestURI
    }

    fun accepts(): String {
        return req.getHeader("accepts")
    }
}