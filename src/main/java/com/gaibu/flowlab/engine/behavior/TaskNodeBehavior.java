package com.gaibu.flowlab.engine.behavior;

import com.gaibu.flowlab.engine.execution.ExecutionContext;
import com.gaibu.flowlab.engine.execution.instruction.CompleteInstruction;
import com.gaibu.flowlab.engine.execution.instruction.Instruction;
import com.gaibu.flowlab.engine.runtime.enums.InstanceStatus;
import com.gaibu.flowlab.engine.task.FlowTask;
import com.gaibu.flowlab.engine.task.TaskRegistry;
import com.gaibu.flowlab.engine.task.context.DefaultTaskContext;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * TASK 节点行为，按 nodeId 执行对应任务。
 */
public class TaskNodeBehavior implements NodeBehavior {

    /**
     * 任务注册表。
     */
    private final TaskRegistry taskRegistry;

    /**
     * 任务执行完成后的通用路由逻辑。
     */
    private final GenericNodeBehavior genericNodeBehavior = new GenericNodeBehavior();

    /**
     * 任务异步执行线程池。
     */
    private static final ExecutorService ASYNC_POOL = new ThreadPoolExecutor(
            0,
            64,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new TaskThreadFactory()
    );

    public TaskNodeBehavior(TaskRegistry taskRegistry) {
        this.taskRegistry = taskRegistry;
    }

    @Override
    public Instruction handle(ExecutionContext context) {
        String nodeId = context.node().getId().value();
        FlowTask task = taskRegistry.getTask(nodeId);
        if (task == null) {
            // 未注册任务时按空任务处理，保持流程可继续流转。
            return genericNodeBehavior.handle(context);
        }

        DefaultTaskContext taskContext = new DefaultTaskContext(context.instance(), context.token());
        int retry = parseRetry(context);
        boolean async = parseAsync(context);
        Duration timeout = parseTimeout(context);

        int maxAttempts = retry + 1;
        int currentAttempt = 0;
        while (currentAttempt < maxAttempts) {
            currentAttempt++;
            try {
                executeTask(task, taskContext, async, timeout);
                break;
            } catch (Exception ex) {
                if (currentAttempt >= maxAttempts) {
                    throw new IllegalStateException("FlowTask execute failed for nodeId: " + nodeId
                            + ", attempts=" + currentAttempt, ex);
                }
            }
        }

        if (context.instance().getStatus() == InstanceStatus.INTERRUPTED) {
            return new CompleteInstruction();
        }

        return genericNodeBehavior.handle(context);
    }

    private void executeTask(
            FlowTask task,
            DefaultTaskContext context,
            boolean async,
            Duration timeout) throws Exception {
        if (!async && timeout == null) {
            task.execute(context);
            return;
        }

        Callable<Void> callable = () -> {
            task.execute(context);
            return null;
        };
        Future<Void> future = ASYNC_POOL.submit(callable);
        try {
            if (timeout == null) {
                future.get();
            } else {
                future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw new IllegalStateException("Task execute timeout: " + timeout, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Task execution interrupted.", ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof Exception e) {
                throw e;
            }
            throw new IllegalStateException("Task execution failed.", cause);
        }
    }

    private int parseRetry(ExecutionContext context) {
        Object retry = context.node().getMetadata().get("retry");
        if (retry == null) {
            return 0;
        }
        if (retry instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        return Math.max(0, Integer.parseInt(Objects.toString(retry, "0")));
    }

    private boolean parseAsync(ExecutionContext context) {
        Object async = context.node().getMetadata().get("async");
        if (async == null) {
            return false;
        }
        if (async instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(Objects.toString(async, "false"));
    }

    private Duration parseTimeout(ExecutionContext context) {
        Object timeout = context.node().getMetadata().get("timeout");
        if (timeout == null) {
            return null;
        }
        return Duration.parse(Objects.toString(timeout));
    }

    private static class TaskThreadFactory implements ThreadFactory {

        private int sequence = 0;

        @Override
        public synchronized Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "flow-task-" + (++sequence));
            thread.setDaemon(true);
            return thread;
        }
    }
}
