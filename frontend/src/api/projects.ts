import api from './client';
import type { Project, ProjectSummary } from '../types';

export const projectsApi = {
  getAll: async (): Promise<Project[]> => {
    const response = await api.get<Project[]>('/api/projects');
    return response.data;
  },

  create: async (data: { name: string; description?: string }): Promise<Project> => {
    const response = await api.post<Project>('/api/projects', data);
    return response.data;
  },

  getSummary: async (projectId: number): Promise<ProjectSummary> => {
    const response = await api.get<ProjectSummary>(`/api/projects/${projectId}/summary`);
    return response.data;
  },
};
