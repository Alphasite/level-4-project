package com.nishadmathur.rest

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import org.hibernate.validator.constraints.NotEmpty

class Configuration: Configuration() {
    @NotEmpty
    lateinit var template: String
        @JsonProperty get
        @JsonProperty set

    @NotEmpty
    private var defaultName: String = "Stranger"
        @JsonProperty get
        @JsonProperty set
}
