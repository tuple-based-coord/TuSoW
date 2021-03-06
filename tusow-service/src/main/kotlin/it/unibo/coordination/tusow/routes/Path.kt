package it.unibo.coordination.tusow.routes

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ErrorHandler
import it.unibo.coordination.tusow.exceptions.BadContentError
import it.unibo.coordination.tusow.exceptions.HttpError
import it.unibo.presentation.MIMETypes
import it.unibo.presentation.MIMETypes.Companion.parse
import it.unibo.presentation.Presentation
import it.unibo.presentation.Serializer
import org.slf4j.LoggerFactory

abstract class Path(private val path: String) {
    
    private var router: Router? = null
    
    private var parentPath = ""
    
    protected abstract fun setupRoutes()
    
    protected fun addRoute(method: HttpMethod, path: String, handler: Handler<RoutingContext>): Route {
        LOGGER.info("Add route: {} {}", method, getPath() + path)
        return router!!.route(method, getPath() + path) //	        .handler(LoggerHandler.create())
                .handler { routingContext: RoutingContext -> log(routingContext) }
                .handler(BodyHandler.create())
                .handler(handler)
                .handler(ErrorHandler.create())
    }

    private fun log(routingContext: RoutingContext) {
        val req = routingContext.request()
        LOGGER.info("{} {}", req.method(), req.absoluteURI())
        routingContext.next()
    }

    protected fun addRoute(method: HttpMethod, handler: Handler<RoutingContext>): Route {
        return addRoute(method, "", handler)
    }

    protected fun getPath(): String {
        return parentPath + path
    }

    protected fun getSubPath(subResource: String): String {
        return getPath() + "/" + subResource
    }

    protected fun <X> successfulResponseHandler(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>): Handler<X> {
        return successfulResponseHandler(routingContext, marshaller, { it })
    }

    protected fun <X> responseHandler(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>): Handler<AsyncResult<X>> {
        return responseHandler(routingContext, marshaller, { it })
    }

    protected fun <X> successfulResponseHandlerWithManyContents(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>): Handler<Collection<X>> {
        return successfulResponseHandlerWithManyContents(routingContext, marshaller, { it })
    }

    protected fun <X> responseHandlerWithManyContents(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>): Handler<AsyncResult<Collection<X>>> {
        return responseHandlerWithManyContents(routingContext, marshaller, { it })
    }

    protected fun <X> successfulResponseHandler(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>, cleaner: (X) -> X): Handler<X> {
        return Handler { x: X -> handleContent(routingContext, marshaller, cleaner, x) }
    }

    protected fun <X> responseHandler(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>, cleaner: (X) -> X): Handler<AsyncResult<X>> {
        return Handler {
            if (!handleException(routingContext, it)) {
                handleContent(routingContext, marshaller, cleaner, it.result())
            }
        }
    }

    protected fun <X> handleContent(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>, cleaner: (X) -> X, content: X) {
        try {
            val cleanResult = cleaner(content)
            val mimeType = parse(routingContext.acceptableContentType)
            val result: String
            if (cleanResult is Collection<*>) {
                throw UnsupportedOperationException()
            }
            result = marshaller(mimeType).toString(cleanResult)
            routingContext.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, mimeType.toString())
                    .setStatusCode(200)
                    .end(result)
        } catch (e: Throwable) {
            handleException(routingContext, e)
        }
    }

    private fun <X> handleException(routingContext: RoutingContext, x: AsyncResult<X>): Boolean {
        if (x.failed()) {
            handleException(routingContext, x.cause())
            return true
        }
        return false
    }

    private fun handleException(routingContext: RoutingContext, e: Throwable) {
        LOGGER.warn(e.message, e)
        if (e is HttpError) {
            val exception = e
            routingContext.response()
                    .setStatusCode(exception.statusCode)
                    .end(exception.message)
        } else {
            routingContext.response()
                    .setStatusCode(500)
                    .end("Internal Server Error")
        }
    }

    private fun <X> handleManyContents(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>, cleaner: (Collection<X>) -> Collection<X>, contents: Collection<X>) {
        try {
            val cleanResult = cleaner(contents)
            val mimeType = parse(routingContext.acceptableContentType)
            val result: String
            result = marshaller(mimeType).toString(cleanResult)
            routingContext.response()
                    .putHeader(HttpHeaders.CONTENT_TYPE, mimeType.toString())
                    .setStatusCode(if (cleanResult.isEmpty()) 204 else 200)
                    .end(result)
        } catch (e: Throwable) {
            handleException(routingContext, e)
        }
    }

    protected fun <X> successfulResponseHandlerWithManyContents(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>, cleaner: (Collection<X>) -> Collection<X>): Handler<Collection<X>> {
        return Handler { handleManyContents(routingContext, marshaller, cleaner, it) }
    }

    protected fun <X> responseHandlerWithManyContents(routingContext: RoutingContext, marshaller: (MIMETypes) -> Serializer<X>, cleaner: (Collection<X>) -> Collection<X>): Handler<AsyncResult<Collection<X>>> {
        return Handler {
            if (!handleException(routingContext, it)) {
                handleManyContents(routingContext, marshaller, cleaner, it.result())
            }
        }
    }

    protected fun <X> responseHandlerWithNoContent(routingContext: RoutingContext): Handler<X> {
        return Handler {
            routingContext.response()
                    .setStatusCode(204)
                    .end()
        }
    }

    protected fun responseHandlerFallback(routingContext: RoutingContext): Handler<Throwable> {
        return Handler { handleException(routingContext, it) }
    }

    protected fun <X : Number> handleNumericContent(routingContext: RoutingContext, header: String, content: X) {
        try {
            routingContext.response()
                    .putHeader(header, content.toString())
                    .setStatusCode(200)
                    .end()
        } catch (e: Throwable) {
            handleException(routingContext, e)
        }
    }

    protected fun <X : Number> responseHandlerWithNumericContent(routingContext: RoutingContext, header: String): Handler<AsyncResult<X>> {
        return Handler {
            if (!handleException(routingContext, it)) {
                handleNumericContent(routingContext, header, it.result())
            }
        }
    }

    protected fun <X : Number> successfulResponseHandlerWithNumericContent(routingContext: RoutingContext, header: String): Handler<X> {
        return Handler { handleNumericContent(routingContext, header, it) }
    }

    protected open val presentation: Presentation
        get() = Presentation.Prototype

    fun attach(router: Router) {
        this.router = router
        setupRoutes()
    }

    fun append(route: Path): Path {
        route.parentPath = path
        route.attach(router!!)
        return route
    }

    fun append(subPath: String, route: Path): Path {
        route.parentPath = parentPath + path + subPath
        route.attach(router!!)
        return route
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(Path::class.java)

        @JvmStatic
        protected fun requireAllAreNull(x: Any?, vararg xs: Any?) {
            if (sequenceOf(x, *xs).any { it != null }) {
                throw BadContentError()
            }
        }

        @JvmStatic
        protected fun requireNoneIsNull(x: Any?, vararg xs: Any?) {
            if (sequenceOf(x, *xs).any { it == null }) {
                throw BadContentError()
            }
        }

        @JvmStatic
        protected fun requireSomeIsNonNull(x: Any?, vararg xs: Any?) {
            if (sequenceOf(x, *xs).none { it != null }) {
                throw BadContentError()
            }
        }
    }

}