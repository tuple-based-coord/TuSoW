package it.unibo.coordination.tusow.exceptions

class NotImplementedError : ServerError {
    constructor() : super(CODE, MSG)
    constructor(message: String) : super(CODE, "$MSG: $message")
    constructor(message: String, cause: Throwable?) : super(CODE, "$MSG: $message", cause)
    constructor(cause: Throwable?) : super(CODE, MSG, cause)

    companion object {
        private const val CODE = 501
        private const val MSG = "Not Implemented"
    }
}