package it.unibo.coordination.tusow.api;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import it.unibo.coordination.tusow.presentation.ListRepresentation;
import it.unibo.coordination.tusow.presentation.TemplateRepresentation;
import it.unibo.coordination.tusow.presentation.TupleRepresentation;

public interface TupleSpaceApi<T extends TupleRepresentation, TT extends TemplateRepresentation> extends Api {

//    <TL extends ListRepresentation<? extends T>> void createNewTuples(String tupleSpaceName,
//                                                                      boolean bulk,
//                                                                      TL tuples,
//                                                                      Handler<? extends AsyncResult<? extends ListRepresentation<T>>> handler);

    void createNewTuples(String tupleSpaceName,
                        boolean bulk,
                        ListRepresentation<T> tuples,
                        Handler<? extends AsyncResult<? extends ListRepresentation<T>>> handler);

    void observeTuples(String tupleSpaceName,
                      boolean bulk,
                      boolean predicative,
                      boolean negated,
                      TT template,
                      Handler<? extends AsyncResult<? extends ListRepresentation<T>>> handler);

    void consumeTuples(String tupleSpaceName,
                      boolean bulk,
                      boolean predicative,
                      TT template,
                      Handler<? extends AsyncResult<? extends ListRepresentation<T>>> handler);

}

