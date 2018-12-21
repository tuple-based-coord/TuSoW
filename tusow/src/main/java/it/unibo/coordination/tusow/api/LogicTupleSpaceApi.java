package it.unibo.coordination.tusow.api;

import io.vertx.ext.web.RoutingContext;
import it.unibo.coordination.tusow.presentation.LogicTemplateRepresentation;
import it.unibo.coordination.tusow.presentation.LogicTupleRepresentation;

public interface LogicTupleSpaceApi extends TupleSpaceApi<LogicTupleRepresentation, LogicTemplateRepresentation> {
    static LogicTupleSpaceApi get(RoutingContext context) {
        return new LogicTupleSpaceApiImpl(context);
    }
}
