package com.wizard

open class WizardApplication {

    companion object {
        private var instance: WizardService? = null

        private fun instance(): WizardService {
            instance?.let {
                return instance as WizardService
            }

            instance = WizardService.getInstance()
            return instance as WizardService
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

