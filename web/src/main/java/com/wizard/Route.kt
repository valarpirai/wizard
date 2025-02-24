package com.wizard

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlin.jvm.Throws

@FunctionalInterface
interface Route {

    @Throws(Exception::class)
    fun handle(request: Request, response: Response): Any?
}
