package com.wizard

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.AbstractHandler

open class WizardApplication {
    private var server: Server? = null

    fun run() {
        server = Server()
        val connector = ServerConnector(server)
        connector.port = 8090
        server!!.connectors = arrayOf<Connector>(connector)

        // Set a simple Handler to handle requests/responses.
        server!!.handler = object : AbstractHandler() {
            override fun handle(
                target: String?,
                p1: Request?,
                request: HttpServletRequest?,
                response: HttpServletResponse?
            ) {
                println("Request -> " + request?.method + ": " + request?.requestURL)
                // Mark the request as handled so that it
                // will not be processed by other handlers.
                p1?.isHandled = true

                response?.status = 200
                response?.writer?.println("Ok")
            }
        }
        server!!.start()
        println("Jetty Server listening on PORT 8090..")
    }
}

