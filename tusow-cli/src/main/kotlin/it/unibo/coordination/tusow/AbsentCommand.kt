package it.unibo.coordination.tusow

import it.unibo.coordination.linda.core.Match
import it.unibo.coordination.linda.core.Template
import it.unibo.coordination.linda.core.Tuple
import it.unibo.coordination.linda.logic.LogicSpace
import it.unibo.coordination.linda.text.TextualSpace
import it.unibo.coordination.tusow.TupleSpaceTypes.LOGIC
import it.unibo.coordination.tusow.TupleSpaceTypes.TEXT

class AbsentCommand(
        epilog: String = "",
        name: String? = "absent",
        invokeWithoutSubcommand: Boolean = false,
        printHelpOnEmptyArgs: Boolean = false,
        helpTags: Map<String, String> = emptyMap(),
        autoCompleteEnvvar: String? = ""
) : AbstractObserveCommand(
        action = "checking the absence of",
        epilog = epilog,
        name = name,
        invokeWithoutSubcommand = invokeWithoutSubcommand,
        printHelpOnEmptyArgs = printHelpOnEmptyArgs,
        helpTags = helpTags,
        autoCompleteEnvvar = autoCompleteEnvvar)  {

    override fun run() {
        when {
            bulk -> TODO("Currently not supported operation")
            predicative -> when (type) {
                LOGIC -> getTupleSpace<LogicSpace>(tupleSpaceID)
                        .tryAbsent(template)
                        .defaultHandlerForSingleResult()
                TEXT -> getTupleSpace<TextualSpace>(tupleSpaceID)
                        .tryAbsent(template)
                        .defaultHandlerForSingleResult()
            }
            else -> when (type) {
                LOGIC -> getTupleSpace<LogicSpace>(tupleSpaceID)
                        .absent(template)
                        .defaultHandlerForSingleResult()
                TEXT -> getTupleSpace<TextualSpace>(tupleSpaceID)
                        .absent(template)
                        .defaultHandlerForSingleResult()
            }
        }
    }

    override fun <T : Tuple<T>, TT : Template<T>, K, V, M : Match<T, TT, K, V>> M.isSuccess(): Boolean = !this.isMatching

    override fun <T : Tuple<T>, TT : Template<T>, K, V, M : Match<T, TT, K, V>> M.getResult(): Any =
            if (isMatching) tuple.get().value else template

    override fun <T : Tuple<T>, TT : Template<T>, K, V, M : Match<T, TT, K, V>, C : Collection<M>> C.isSuccess(): Boolean = isEmpty()
}