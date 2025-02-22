package com.wizard.app

import com.wizard.WizardApplication
import com.wizard.utils.Printer


class ExampleWebApp: WizardApplication() {

}

fun main() {
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    val message = "Hello, " + name + "!"
    val printer = Printer(message)
    printer.printMessage()


    val app = ExampleWebApp()
    app.run()
}
