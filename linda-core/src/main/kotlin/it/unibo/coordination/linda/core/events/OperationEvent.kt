package it.unibo.coordination.linda.core.events

import it.unibo.coordination.linda.core.*
import it.unibo.coordination.utils.toMultiSet
import org.apache.commons.collections4.MultiSet
import java.util.*
import java.util.stream.Stream
import kotlin.streams.toList

abstract class OperationEvent<T : Tuple<T>, TT : Template<T>>

private constructor(tupleSpace: InspectableTupleSpace<T, TT, *, *, *>,
                    val operationType: OperationType,
                    val operationPhase: OperationPhase,
                    argumentTuples: Stream<out T>,
                    argumentTemplates: Stream<out TT>,
                    resultTuples: Stream<out T>,
                    resultTemplates: Stream<out TT>) : TupleSpaceEvent<T, TT>(tupleSpace) {

    val argumentTuples: List<T> = argumentTuples.toList()
    val argumentTemplates: List<TT> = argumentTemplates.toList()
    val resultTuples: MultiSet<T> = resultTuples.map { it }.toMultiSet()
    val resultTemplates: MultiSet<TT> = resultTemplates.map { it }.toMultiSet()

    val argumentTuple: Optional<T>
        get() = argumentTuples.stream().findFirst()

    val argumentTemplate: Optional<TT>
        get() = argumentTemplates.stream().findFirst()

    val isArgumentPresent: Boolean
        get() = argumentTuples.isNotEmpty() || argumentTemplates.isNotEmpty()

    val resultTuple: Optional<T>
        get() = resultTuples.stream().findFirst()

    val resultTemplate: Optional<TT>
        get() = resultTemplates.stream().findFirst()

    val isResultPresent: Boolean
        get() = resultTuples.isNotEmpty() || resultTemplates.isNotEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as OperationEvent<*, *>?
        return operationType == that!!.operationType &&
                operationPhase == that.operationPhase &&
                argumentTuples == that.argumentTuples &&
                argumentTemplates == that.argumentTemplates &&
                resultTuples == that.resultTuples &&
                resultTemplates == that.resultTemplates
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), operationType, operationPhase, argumentTuples, argumentTemplates, resultTuples, resultTemplates)
    }

    override fun toString(): String {
        return OperationEvent::class.java.simpleName + "." + javaClass.simpleName + "{" +
                "tupleSpace=" + tupleSpaceName +
                ", operationType=" + operationType +
                ", operationPhase=" + operationPhase +
                ", argumentTuples=" + argumentTuples +
                ", argumentTemplates=" + argumentTemplates +
                ", resultTuples=" + resultTuples +
                ", resultTemplates=" + resultTemplates +
                "}"
    }

    class Invocation<T : Tuple<T>, TT : Template<T>>
        internal constructor(tupleSpace: InspectableTupleSpace<T, TT, *, *, *>, 
                             operationType: OperationType, 
                             argumentTuples: Stream<out T>,
                             argumentTemplates: Stream<out TT>) 
        : OperationEvent<T, TT>(tupleSpace, 
            operationType, 
            OperationPhase.INVOCATION, 
            argumentTuples, 
            argumentTemplates, Stream.empty<T>(), 
            Stream.empty<TT>()) {

        fun toTupleReturningCompletion(tuple: T): Completion<T, TT> {
            check(OperationType.isTupleReturningSet(operationType))

            return Completion(this, Stream.of(tuple), Stream.empty())
        }

        fun toTuplesReturningCompletion(vararg tuples: T): Completion<T, TT> {
            return toTuplesReturningCompletion(Stream.of(*tuples))
        }

        fun toTuplesReturningCompletion(tuples: Stream<out T>): Completion<T, TT> {
            check(OperationType.isTuplesReturningSet(operationType))

            return Completion<T, TT>(this, tuples, Stream.empty())
        }

        fun toTuplesReturningCompletion(tuples: Collection<T>): Completion<T, TT> {
            return toTuplesReturningCompletion(tuples.stream())
        }

        fun toTemplateReturningCompletion(template: TT): Completion<T, TT> {
            check(OperationType.isTemplateReturning(operationType))

            return Completion<T, TT>(this, Stream.empty(), Stream.of(template))
        }

        fun toTemplatesReturningCompletion(templates: Collection<TT>): Completion<T, TT> {
            check(OperationType.isTemplatesReturning(operationType))

            return Completion(this, Stream.empty(), templates.stream())
        }

        fun toCompletion(resultTuples: Stream<out T>, resultTemplates: Stream<out TT>): Completion<T, TT> {
            return Completion(this, resultTuples, resultTemplates)
        }
    }

    class Completion<T : Tuple<T>, TT : Template<T>> : OperationEvent<T, TT> {

        internal constructor(tupleSpace: InspectableTupleSpace<T, TT, *, *, *>, operationType: OperationType, argumentTuples: Stream<out T>, argumentTemplates: Stream<out TT>, resultTuples: Stream<out T>, resultTemplates: Stream<out TT>) : super(tupleSpace, operationType, OperationPhase.COMPLETION, argumentTuples, argumentTemplates, resultTuples, resultTemplates) {}

        internal constructor(invocation: Invocation<T, TT>, resultTuples: Stream<out T>, resultTemplates: Stream<out TT>) : super(
                invocation.tupleSpace,
                invocation.operationType,
                OperationPhase.COMPLETION,
                invocation.resultTuples.stream(),
                invocation.argumentTemplates.stream(),
                resultTuples,
                resultTemplates
        )
    }

    companion object {

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> invocation(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, operationType: OperationType, argumentTuples: Stream<out X>, argumentTemplates: Stream<out Y>): Invocation<X, Y> {
            return Invocation(
                    tupleSpace, operationType, argumentTuples, argumentTemplates
            )
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> completion(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, operationType: OperationType, argumentTuples: Stream<out X>, argumentTemplates: Stream<out Y>, resultTuples: Stream<out X>, resultTemplates: Stream<out Y>): Completion<X, Y> {
            return Completion(
                    tupleSpace, operationType, argumentTuples, argumentTemplates, resultTuples, resultTemplates
            )
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> nothingAcceptingInvocation(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, operationType: OperationType): Invocation<X, Y> {
            require(OperationType.isNothingAccepting(operationType)) { operationType.toString() }

            return Invocation(
                    tupleSpace, operationType, Stream.empty(), Stream.empty()
            )
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> tupleAcceptingInvocation(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, operationType: OperationType, tuple: X): Invocation<X, Y> {
            require(OperationType.isTupleAcceptingSet(operationType)) { operationType.toString() }

            return Invocation(
                    tupleSpace, operationType, Stream.of(tuple), Stream.empty()
            )
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> tuplesAcceptingInvocation(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, operationType: OperationType, tuples: Collection<X>): Invocation<X, Y> {
            require(OperationType.isTuplesAcceptingSet(operationType)) { operationType.toString() }

            return Invocation(
                    tupleSpace, operationType, tuples.stream(), Stream.empty()
            )
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> templateAcceptingInvocation(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, operationType: OperationType, template: Y): Invocation<X, Y> {
            require(OperationType.isTemplateAccepting(operationType)) { operationType.toString() }

            return Invocation(
                    tupleSpace, operationType, Stream.empty(), Stream.of(template)
            )
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> templatesAcceptingInvocation(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, operationType: OperationType, templates: Collection<Y>): Invocation<X, Y> {
            require(OperationType.isTemplatesAccepting(operationType)) { operationType.toString() }

            return Invocation(
                    tupleSpace, operationType, Stream.empty(), templates.stream()
            )
        }
    }
}