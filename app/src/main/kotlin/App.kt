package com.wizard.app

import com.wizard.Request
import com.wizard.Response
import com.wizard.Route
import com.wizard.WizardApplication
import com.wizard.WizardApplication.Companion.get
import com.wizard.WizardApplication.Companion.post
import com.wizard.utils.Printer


class ExampleWebApp: WizardApplication() {
    companion object {
        fun registerRoutes () {
            get(
                "/status",
                object : Route {
                    override fun handle(
                        request: Request,
                        response: Response
                    ) {
                        println("success")
                        response.body = "Status page"
                    }
                })
        }
    }
}

fun main() {
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    val message = "Hello, $name!"
    val printer = Printer(message)
    printer.printMessage()


    val app = ExampleWebApp()
    app.run()
    ExampleWebApp.registerRoutes()

    get("/", object: Route {
        override fun handle(
            request: Request,
            response: Response
        ): String {
            return "Home page"
        }
    })

    get("/health", object: Route {
        override fun handle(
            request: Request,
            response: Response
        ): String {
            return "Ok"
        }
    })
}
