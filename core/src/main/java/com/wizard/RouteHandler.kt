package com.wizard

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler

class RouteHandler: AbstractHandler() {
    private var pathAndHandlerMap = HashMap<String, Route>()

    override fun handle(
        target: String?,
        p1: Request?,
        request: HttpServletRequest?,
        response: HttpServletResponse?
    ) {
        println("Request -> " + request?.method + ": " + request?.requestURI)
        // Mark the request as handled so that it
        // will not be processed by other handlers.
        val req = Request(request!!)
        val res = Response(response!!)

        val method = req.method()
        val path = req.uri()

        p1?.isHandled = true
        pathAndHandlerMap["$method:$path"]?.let {
            val handlerResponse = it.handle(req, res)

            response.status = res.status
            handlerResponse?.let {
                if (handlerResponse !is Unit)
                    response.writer?.println(handlerResponse.toString())
            }
            res.body?.let {
                response.writer?.println(res.body.toString())
            }
            return
        }
        response.status = 404
        response.writer?.println("Not found")
    }

    fun addRoute(method: HttpMethod, path: String, handler: Route) {
        pathAndHandlerMap["$method:$path"] = handler
    }
}