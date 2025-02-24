package com.example.fideicomisoapproverring.recovery.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * A JUnit Jupiter extension that configures coroutine testing environment.
 * This extension ensures consistent behavior across all tests by:
 * 1. Using a StandardTestDispatcher for predictable execution
 * 2. Providing helper methods for time control
 * 3. Automatically cleaning up after each test
 */
@ExperimentalCoroutinesApi
class TestCoroutineRule : BeforeEachCallback, AfterEachCallback {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    override fun beforeEach(context: ExtensionContext) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext) {
        Dispatchers.resetMain()
    }

    /**
     * Executes a test block with the test scope
     */
    fun runTest(block: suspend TestScope.() -> Unit) = testScope.runTest(block)

    /**
     * Advances the virtual time by the specified amount
     */
    fun advanceTimeBy(delayTimeMillis: Long) {
        testScope.testScheduler.advanceTimeBy(delayTimeMillis)
    }

    /**
     * Runs all pending coroutines until no more are scheduled
     */
    fun runPending() {
        testScope.testScheduler.runCurrent()
    }
} 