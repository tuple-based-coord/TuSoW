package it.unibo.coordination.agency.impl

import it.unibo.coordination.agency.Agent
import it.unibo.coordination.agency.AgentID
import it.unibo.coordination.agency.Behaviour
import it.unibo.coordination.control.Activity
import java.util.*


class AgentImpl(name: String) : Agent {

    private inner class ControllerImpl(private val delegate: Activity.Controller<Unit, Unit, Unit>)
        : Agent.Controller, Activity.Controller<Unit, Unit, Unit> by delegate {

        override fun addBehaviours(behaviour: Behaviour<*>, vararg behaviours: Behaviour<*>) {
            this@AgentImpl.addBehaviours(behaviour, *behaviours)
        }

        override fun removeBehaviours(behaviour: Behaviour<*>, vararg behaviours: Behaviour<*>) {
            this@AgentImpl.removeBehaviours(behaviour, *behaviours)
        }

        override fun pause() {
            delegate.pause()
        }

        override fun `continue`() {
            delegate.`continue`()
        }
    }

    override val id: AgentID = name

    private val toDoList: Queue<Behaviour<*>> = LinkedList<Behaviour<*>>()
    private val toBeRemoved: MutableSet<Behaviour<*>> = mutableSetOf()
    private val initializers: MutableList<Agent.() -> Unit> = mutableListOf()
    private val cleaners: MutableList<Agent.() -> Unit> = mutableListOf()

    override val behaviours: List<Behaviour<*>>
        get() = toDoList.toList()

    override fun addBehaviours(behaviour: Behaviour<*>, vararg behaviours: Behaviour<*>) {
        toDoList.add(behaviour)
        for (b in behaviours) {
            toDoList.add(b)
        }
    }

    override fun removeBehaviours(behaviour: Behaviour<*>, vararg behaviours: Behaviour<*>) {
        toDoList.remove(behaviour)
        for (b in behaviours) {
            toDoList.remove(b)
        }
    }

    override fun setup(initializer: Agent.() -> Unit) {
        initializers.add(initializer)
    }

    override fun tearDown(cleaner: Agent.() -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onBegin(environment: Unit, controller: Activity.Controller<Unit, Unit, Unit>) {
        setup()
    }

    override fun onStep(environment: Unit, lastData: Unit, controller: Activity.Controller<Unit, Unit, Unit>) {
        if (toDoList.isEmpty()) {
            controller.pause()
        } else {
            val skipped: Queue<Behaviour<*>> = LinkedList()
            var behaviour = toDoList.poll()
            try {
                while (behaviour != null && behaviour.isPaused) {
                    skipped.add(behaviour)
                    behaviour = toDoList.poll()
                }
                toBeRemoved.clear() // TODO notice this!
                if (behaviour != null) {
                    behaviour(ControllerImpl(controller))
                } else {
                    controller.pause()
                }
            } finally {
                if (behaviour != null && !behaviour.isOver) {
                    toDoList.add(behaviour)
                }
                toDoList.addAll(skipped)
                toDoList.removeAll(toBeRemoved) // TODO notice this!
            }

        }
    }

    override fun onEnd(environment: Unit, lastData: Unit, result: Unit, controller: Activity.Controller<Unit, Unit, Unit>) {
        tearDown()
    }

}