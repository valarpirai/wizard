package com.wizard

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach

class WizardServiceTest {
    private var service: WizardService? = null

    @BeforeEach
    fun setUp() {
        service = WizardService.getInstance()
    }

    @AfterEach
    fun tearDown() {
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun test(): Unit {
            var service = WizardService.getInstance()
        }
    }
}
