package com.wizard

open class WizardApplication {

    companion object {
        private fun instance(): WizardService {
            return WizardService.getInstance()
        }

        fun get(path: String, handler: Route) {
            instance().addRoute(HttpMethod.GET, path, handler)
        }

        fun post(path: String, handler: Route) {
            instance().addRoute(HttpMethod.POST, path, handler)
        }
    }

    fun run() {
        instance().run()
    }
}

