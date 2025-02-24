package com.wizard

import jakarta.servlet.http.HttpServletResponse

class Response(private val res: HttpServletResponse) {
    var status = 0
        get() {
            if (field == 0) {
                return 200
            }
            return field
        }

    var body: Any? = null
}