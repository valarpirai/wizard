package com.wizard

import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector

class WizardService private constructor() {
    private var server: Server? = null
    private var routeHandler = RouteHandler()

    companion object {
        private var instance: WizardService? = null

        fun getInstance(): WizardService {
            instance = instance ?: WizardService()
            return instance as WizardService
        }
    }

    fun run() {
        server = Server()
        val connector = ServerConnector(server)
        connector.port = 8090
        server!!.connectors = arrayOf<Connector>(connector)

        // Set a simple Handler to handle requests/responses.
        server!!.handler = routeHandler
        server!!.start()
        println("Jetty Server listening on PORT 8090..")
    }

    fun addRoute(method: HttpMethod, path: String, handler: Route) {
        routeHandler.addRoute(method, path, handler)
    }
}