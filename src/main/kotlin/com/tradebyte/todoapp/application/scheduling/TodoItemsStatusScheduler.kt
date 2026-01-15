package com.tradebyte.todoapp.application.scheduling

import com.tradebyte.todoapp.application.utils.logger
import com.tradebyte.todoapp.domain.service.TodoService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
@ConditionalOnProperty(
    value = ["app.scheduling.todo-items-status-update.enabled"],
    havingValue = "true"
)
class TodoItemsStatusScheduler(
    private val todoService: TodoService,
) {

    private val logger = logger()

    init {
        logger.info("TodoItemStatusScheduler is running.")
    }

    @Scheduled(cron = "\${app.scheduling.todo-items-status-update.cron}")
    fun run() {
        todoService.checkForOverdueTodoItemsStatusUpdate()
    }
}
