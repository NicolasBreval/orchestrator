package org.nitb.orchestrator.logging

class LogEventStackTrace(
    val fileName: String?,
    val className: String,
    val methodName: String,
    val lineNo: Int,
) {
    companion object {
        fun fromOriginal(stackTrace: StackTraceElement): LogEventStackTrace {
            return LogEventStackTrace(stackTrace.fileName, stackTrace.className, stackTrace.methodName, stackTrace.lineNumber)
        }
    }
}