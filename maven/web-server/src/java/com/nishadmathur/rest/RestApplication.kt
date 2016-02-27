package com.nishadmathur.rest

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

/**
 * User: nishad
 * Date: 18/02/2016
 * Time: 15:56
 */
class RestApplication : Application<Configuration>() {

    override fun getName(): String {
        return "hello-world"
    }

    override fun initialize(bootstrap: Bootstrap<Configuration>?) {
        // nothing to do yet
    }

    override fun run(configuration: Configuration,
                     environment: Environment) {
        // nothing to do yet
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic fun main(args: Array<String>) {
            RestApplication().run(*args)
        }
    }

}
