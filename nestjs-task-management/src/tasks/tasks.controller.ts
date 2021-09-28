import { Controller, Get, Post } from '@nestjs/common';
import { Task } from './task.model';
import { TasksService } from './tasks.service';

@Controller('tasks')
export class TasksController {
  // 이렇게 파라미터를 넣어주면 자동으로 위에 생성이 돼
  constructor(private tasksService: TasksService) { }
  // controller에 다음과 같은 handler를 넣어주는 거야
  @Get()
  getAllTasks(): Task[] {
    return this.tasksService.getAllTasks();
  }

}
