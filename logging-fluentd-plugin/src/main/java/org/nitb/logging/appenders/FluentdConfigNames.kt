package org.nitb.logging.appenders

class FluentdConfigNames {

    companion object {
        const val LOGGING_FLUENTD_HOST = "logging.fluentd.host"
        const val LOGGING_FLUENTD_PORT = "logging.fluentd.port"
        const val LOGGING_FLUENTD_TAG_PREFIX = "logging.fluentd.tag.prefix"
        const val LOGGING_FLUENTD_TAG = "logging.fluentd.tag"
        const val LOGGING_FLUENTD_PORT_DEFAULT = 24224
    }
}