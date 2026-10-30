import api from './client';
import type { PageResponse, Task, TaskPriority, TaskStatus } from '../types';

export interface TaskFilterParams {
  status?: TaskStatus;
  priority?: TaskPriority;
  assigneeId?: number;
  sortBy?: string;
  sortDirection?: string;
  page?: number;
  size?: number;
}

export const tasksApi = {
  getForProject: async (projectId: number, params: TaskFilterParams = {}): Promise<PageResponse<Task>> => {
    const response = await api.get<PageResponse<Task>>(`/api/projects/${projectId}/tasks`, { params });
    return response.data;
  },

  create: async (
    projectId: number,
    data: { title: string; description?: string; priority?: TaskPriority; dueDate?: string; assigneeId?: number }
  ): Promise<Task> => {
    const response = await api.post<Task>(`/api/projects/${projectId}/tasks`, data);
    return response.data;
  },

  update: async (
    taskId: number,
    data: { title: string; description?: string; status: TaskStatus; priority: TaskPriority; dueDate?: string; assigneeId?: number }
  ): Promise<Task> => {
    const response = await api.put<Task>(`/api/tasks/${taskId}`, data);
    return response.data;
  },

  updateStatus: async (taskId: number, status: TaskStatus): Promise<Task> => {
    const response = await api.patch<Task>(`/api/tasks/${taskId}/status`, { status });
    return response.data;
  },

  delete: async (taskId: number): Promise<void> => {
    await api.delete(`/api/tasks/${taskId}`);
  },
};
