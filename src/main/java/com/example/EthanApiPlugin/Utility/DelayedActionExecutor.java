package com.example.EthanApiPlugin.Utility;

import com.google.inject.Inject;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * A self-contained utility class for scheduling actions for future game ticks.
 * Automatically registers itself with the event bus.
 */
public class DelayedActionExecutor {

    private final List<TickTask> tasks = new ArrayList<>();
    private final EventBus eventBus;

    /**
     * Creates a new DelayedActionExecutor and registers it with the event bus
     *
     * @param eventBus The EventBus to register with
     */
    @Inject
    public DelayedActionExecutor(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    /**
     * Schedule a task to run after a specific number of ticks
     *
     * @param runnable The action to perform
     * @param ticksDelay Number of ticks to wait before executing
     */
    public void schedule(Runnable runnable, int ticksDelay) {
        tasks.add(new TickTask(runnable, ticksDelay, null));
    }

    /**
     * Schedule a task that will only run if the condition is true when it's time to execute
     *
     * @param runnable The action to perform
     * @param condition A condition that must be true for the action to execute
     * @param ticksDelay Number of ticks to wait before executing
     */
    public void scheduleIf(Runnable runnable, BooleanSupplier condition, int ticksDelay) {
        tasks.add(new TickTask(runnable, ticksDelay, condition));
    }

    /**
     * Handles game tick events
     */
    @Subscribe
    public void onGameTick(GameTick event) {
        List<TickTask> remainingTasks = new ArrayList<>();

        for (TickTask task : tasks) {
            task.ticksRemaining--;

            if (task.ticksRemaining <= 0) {
                // Time to execute if condition is met (or no condition)
                if (task.condition == null || task.condition.getAsBoolean()) {
                    task.runnable.run();
                }
            } else {
                // Keep the task for future ticks
                remainingTasks.add(task);
            }
        }

        tasks.clear();
        tasks.addAll(remainingTasks);
    }

    /**
     * Clear all scheduled tasks
     */
    public void clear() {
        tasks.clear();
    }

    /**
     * Unregister from the event bus when no longer needed
     */
    public void shutdown() {
        eventBus.unregister(this);
        tasks.clear();
    }

    /**
     * Internal class to represent a scheduled task
     */
    private static class TickTask {
        private final Runnable runnable;
        private int ticksRemaining;
        private final BooleanSupplier condition;

        TickTask(Runnable runnable, int ticksRemaining, BooleanSupplier condition) {
            this.runnable = runnable;
            this.ticksRemaining = ticksRemaining;
            this.condition = condition;
        }
    }
}