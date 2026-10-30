import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tasksApi } from '../api/tasks';
import { projectsApi } from '../api/projects';
import { Navbar } from '../components/common/Navbar';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { SummaryBar } from '../components/board/SummaryBar';
import { FilterBar } from '../components/board/FilterBar';
import { TaskCard } from '../components/board/TaskCard';
import { TaskFormModal } from '../components/board/TaskFormModal';
import type { Task, TaskPriority, TaskStatus } from '../types';
import { Plus, ArrowLeft, AlertCircle } from 'lucide-react';

export const ProjectBoardPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);
  const queryClient = useQueryClient();

  // Filters & Sorting state
  const [priorityFilter, setPriorityFilter] = useState<TaskPriority | ''>('');
  const [sortBy, setSortBy] = useState<string>('createdAt');
  const [sortDirection, setSortDirection] = useState<string>('desc');

  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [taskToEdit, setTaskToEdit] = useState<Task | null>(null);

  // Fetch summary
  const { data: summary, isLoading: isSummaryLoading } = useQuery({
    queryKey: ['project-summary', projectId],
    queryFn: () => projectsApi.getSummary(projectId),
    enabled: !!projectId,
  });

  // Fetch tasks with active filters and sorting
  const {
    data: tasksPage,
    isLoading: isTasksLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['tasks', projectId, priorityFilter, sortBy, sortDirection],
    queryFn: () =>
      tasksApi.getForProject(projectId, {
        priority: priorityFilter || undefined,
        sortBy,
        sortDirection,
        size: 100,
      }),
    enabled: !!projectId,
  });

  // Task Mutations
  const createMutation = useMutation({
    mutationFn: (data: { title: string; description?: string; priority?: TaskPriority; dueDate?: string }) =>
      tasksApi.create(projectId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-summary', projectId] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ taskId, data }: { taskId: number; data: { title: string; description?: string; status: TaskStatus; priority: TaskPriority; dueDate?: string } }) =>
      tasksApi.update(taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-summary', projectId] });
    },
  });

  const statusMutation = useMutation({
    mutationFn: ({ taskId, status }: { taskId: number; status: TaskStatus }) =>
      tasksApi.updateStatus(taskId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-summary', projectId] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (taskId: number) => tasksApi.delete(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      queryClient.invalidateQueries({ queryKey: ['project-summary', projectId] });
    },
  });

  const tasks = tasksPage?.content || [];

  const columns: { status: TaskStatus; title: string; headerColor: string }[] = [
    { status: 'TODO', title: 'To Do', headerColor: 'border-blue-500 text-blue-700 bg-blue-50/50' },
    { status: 'IN_PROGRESS', title: 'In Progress', headerColor: 'border-amber-500 text-amber-700 bg-amber-50/50' },
    { status: 'DONE', title: 'Done', headerColor: 'border-emerald-500 text-emerald-700 bg-emerald-50/50' },
  ];

  const handleOpenCreateModal = () => {
    setTaskToEdit(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (task: Task) => {
    setTaskToEdit(task);
    setIsModalOpen(true);
  };

  const handleDeleteTask = (taskId: number) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      deleteMutation.mutate(taskId);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Navigation Breadcrumb */}
        <div className="flex items-center justify-between mb-4">
          <Link
            to="/projects"
            className="inline-flex items-center space-x-1.5 text-xs font-semibold text-slate-500 hover:text-slate-800 transition"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Projects</span>
          </Link>

          <button
            onClick={handleOpenCreateModal}
            className="inline-flex items-center space-x-1.5 px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shadow-sm transition"
          >
            <Plus className="w-4 h-4" />
            <span>Add Task</span>
          </button>
        </div>

        {/* Top Summary Bar */}
        <SummaryBar summary={summary || null} loading={isSummaryLoading} />

        {/* Filter & Sort Controls */}
        <FilterBar
          priority={priorityFilter}
          sortBy={sortBy}
          sortDirection={sortDirection}
          onPriorityChange={setPriorityFilter}
          onSortByChange={setSortBy}
          onSortDirectionToggle={() => setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc')}
          onClearFilters={() => {
            setPriorityFilter('');
            setSortBy('createdAt');
            setSortDirection('desc');
          }}
        />

        {/* Error State */}
        {isError && (
          <div className="my-6 rounded-xl bg-red-50 border border-red-200 p-4 flex items-start space-x-3 text-red-700 text-sm">
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-500 mt-0.5" />
            <span>{(error as Error)?.message || 'Failed to load tasks for this project.'}</span>
          </div>
        )}

        {/* Loading Spinner */}
        {isTasksLoading && (
          <div className="py-20">
            <LoadingSpinner size="lg" text="Loading Kanban board..." />
          </div>
        )}

        {/* 3-Column Kanban Board */}
        {!isTasksLoading && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
            {columns.map((col) => {
              const columnTasks = tasks.filter((t) => t.status === col.status);

              return (
                <div
                  key={col.status}
                  className="bg-slate-100/70 rounded-2xl p-4 border border-slate-200 shadow-sm flex flex-col min-h-[500px]"
                >
                  {/* Column Header */}
                  <div
                    className={`flex items-center justify-between px-3.5 py-2 rounded-xl border-l-4 font-bold text-sm mb-4 ${col.headerColor}`}
                  >
                    <span>{col.title}</span>
                    <span className="text-xs px-2 py-0.5 bg-white rounded-full shadow-sm text-slate-700">
                      {columnTasks.length}
                    </span>
                  </div>

                  {/* Task Cards */}
                  <div className="space-y-3 flex-1">
                    {columnTasks.length === 0 ? (
                      <div className="p-8 text-center text-xs text-slate-400 border border-dashed border-slate-300 rounded-xl">
                        No tasks in {col.title}
                      </div>
                    ) : (
                      columnTasks.map((task) => (
                        <TaskCard
                          key={task.id}
                          task={task}
                          onStatusChange={(taskId, newStatus) =>
                            statusMutation.mutate({ taskId, status: newStatus })
                          }
                          onEdit={handleOpenEditModal}
                          onDelete={handleDeleteTask}
                        />
                      ))
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      {/* Task Create / Edit Modal */}
      <TaskFormModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        taskToEdit={taskToEdit}
        onSubmit={async (data) => {
          if (taskToEdit) {
            await updateMutation.mutateAsync({
              taskId: taskToEdit.id,
              data: {
                title: data.title,
                description: data.description,
                status: data.status || taskToEdit.status,
                priority: data.priority,
                dueDate: data.dueDate,
              },
            });
          } else {
            await createMutation.mutateAsync(data);
          }
        }}
      />
    </div>
  );
};
