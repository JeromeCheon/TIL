import { Body, Controller, Delete, Get, Param, Patch, Post, Query } from '@nestjs/common';
import { title } from 'process';
import { CreateTaskDto } from './dto/create-task.dto';
import { GetTasksFilterDto } from './dto/get-tasks-filter.dto';
import { Task, TaskStatus } from './task.model';
import { TasksService } from './tasks.service';

@Controller('tasks') // url route
export class TasksController {
  // 이렇게 파라미터를 넣어주면 자동으로 위에 생성이 돼
  constructor(private tasksService: TasksService) {}
  // controller에 다음과 같은 handler를 넣어주는 거야
  @Get()
  getTasks(@Query() filterDto: GetTasksFilterDto): Task[] {
    // if we have any filters defined, call tasksService.getTaskWilFilters
    // otherwise, just get all tasks
    if (Object.keys(filterDto).length) {
      return this.tasksService.getTasksWithFilters(filterDto);
    } else {
      return this.tasksService.getAllTasks();
    }
  }
  // http://localhost:3000.tasks/~~~
  @Get('/:id') // colon은 path 파라미터라는 뜻이야 라는 걸 전달
  getTaskById(@Param('id') id: string): Task {
    // 여기 id는 위에 co relate돼
    return this.tasksService.getTaskById(id);
  }

  @Delete('/:id')
  deleteTask(@Param('id') id: string): void {
    return this.tasksService.deleteTask(id);
  }

  @Patch('/:id/status')
  updateTaskStatus(
    @Param('id') id: string,
    @Body('status') status: TaskStatus,
  ): Task {
    return this.tasksService.updateTaskStatus(id, status);
  }
  // 첫번째 방법
  // @Post()
  // createTask(@Body() body) {
  //   console.log('body', body);
  // }
  @Post()
  createTask(
    // @Body('title') title: string,
    // @Body('description') description: string,
    @Body() createTaskDto: CreateTaskDto,
  ): Task {
    return this.tasksService.createTask(createTaskDto);
  }
}
