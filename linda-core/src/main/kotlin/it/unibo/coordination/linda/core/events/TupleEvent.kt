package it.unibo.coordination.linda.core.events

import it.unibo.coordination.linda.core.InspectableTupleSpace
import it.unibo.coordination.linda.core.Template
import it.unibo.coordination.linda.core.Tuple
import java.util.*

class TupleEvent<T : Tuple<T>, TT : Template<T>>
    private constructor(tupleSpace: InspectableTupleSpace<T, TT, *, *, *>, val isBefore: Boolean, val effect: Effect, val tuple: T?, val template: TT?)
        : TupleSpaceEvent<T, TT>(tupleSpace) {

    enum class Effect {
        WRITTEN, READ, TAKEN, ABSENT
    }

    val isAfter: Boolean
        get() = !isBefore

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as TupleEvent<*, *>?
        return isBefore == that!!.isBefore &&
                effect == that.effect &&
                tuple == that.tuple &&
                template == that.template
    }

    override fun toString(): String {
        return "TupleEvent{" +
                "tupleSpace=" + tupleSpaceName +
                ", effect=" + effect +
                ", before=" + isBefore +
                ", tuple=" + tuple +
                ", template=" + template +
                "}"
    }

    override fun hashCode(): Int {
        return Objects.hash(effect, isBefore, tuple, template)
    }

    companion object {

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> of(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, before: Boolean, effect: Effect, tuple: X, template: Y): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, before, effect, tuple, template)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> beforeWriting(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, tuple: X): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, true, Effect.WRITTEN, tuple, null)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> afterWriting(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, tuple: X): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, false, Effect.WRITTEN, tuple, null)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> beforeTaking(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, tuple: X): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, true, Effect.TAKEN, tuple, null)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> afterTaking(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, tuple: X): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, false, Effect.TAKEN, tuple, null)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> beforeReading(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, tuple: X): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, true, Effect.READ, tuple, null)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> afterReading(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, tuple: X): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, false, Effect.READ, tuple, null)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> beforeAbsent(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, template: Y): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, true, Effect.ABSENT, null, template)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> afterAbsent(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, template: Y): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, false, Effect.ABSENT, null, template)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> beforeAbsent(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, template: Y, counterexample: X): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, true, Effect.ABSENT, counterexample, template)
        }

        @JvmStatic
        fun <X : Tuple<X>, Y : Template<X>> afterAbsent(tupleSpace: InspectableTupleSpace<X, Y, *, *, *>, template: Y, counterexample: X): TupleEvent<X, Y> {
            return TupleEvent(tupleSpace, false, Effect.ABSENT, counterexample, template)
        }
    }
}
