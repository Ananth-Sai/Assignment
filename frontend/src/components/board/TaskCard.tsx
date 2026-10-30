import React from 'react';
import type { Task, TaskStatus } from '../../types';
import { Calendar, User, AlertCircle, Edit2, Trash2 } from 'lucide-react';

interface TaskCardProps {
  task: Task;
  onStatusChange: (taskId: number, newStatus: TaskStatus) => void;
  onEdit: (task: Task) => void;
  onDelete: (taskId: number) => void;
}

export const TaskCard: React.FC<TaskCardProps> = ({
  task,
  onStatusChange,
  onEdit,
  onDelete,
}) => {
  const priorityStyles = {
    LOW: 'bg-slate-100 text-slate-700 border-slate-200',
    MEDIUM: 'bg-amber-50 text-amber-700 border-amber-200',
    HIGH: 'bg-red-50 text-red-700 border-red-200',
  };

  return (
    <div
      data-testid={`task-card-${task.id}`}
      className={`bg-white rounded-xl border p-4 shadow-sm hover:shadow-md transition-all duration-150 flex flex-col justify-between ${
        task.isOverdue ? 'border-red-300 ring-1 ring-red-200' : 'border-slate-200'
      }`}
    >
      <div>
        {/* Top Badges: Priority + Overdue indicator */}
        <div className="flex items-center justify-between mb-2.5">
          <span
            className={`px-2 py-0.5 text-xs font-semibold rounded-md border ${
              priorityStyles[task.priority]
            }`}
          >
            {task.priority}
          </span>

          {task.isOverdue && (
            <span
              data-testid="overdue-badge"
              className="inline-flex items-center space-x-1 px-2 py-0.5 rounded-md text-xs font-bold bg-red-100 text-red-700 border border-red-200"
            >
              <AlertCircle className="w-3.5 h-3.5 text-red-600" />
              <span>OVERDUE</span>
            </span>
          )}
        </div>

        {/* Task Title */}
        <h4 className="font-semibold text-slate-900 text-sm leading-snug line-clamp-2">
          {task.title}
        </h4>

        {/* Task Description */}
        {task.description && (
          <p className="mt-1.5 text-xs text-slate-500 line-clamp-2 leading-relaxed">
            {task.description}
          </p>
        )}
      </div>

      <div className="mt-4 pt-3 border-t border-slate-100">
        {/* Metadata: Due Date & Assignee */}
        <div className="flex items-center justify-between text-xs text-slate-500 mb-3">
          {task.dueDate ? (
            <div
              className={`flex items-center space-x-1 ${
                task.isOverdue ? 'text-red-600 font-medium' : ''
              }`}
            >
              <Calendar className="w-3.5 h-3.5" />
              <span>{task.dueDate}</span>
            </div>
          ) : (
            <span className="text-slate-400">No due date</span>
          )}

          {task.assignee && (
            <div className="flex items-center space-x-1 text-slate-600 bg-slate-50 px-2 py-0.5 rounded-md border border-slate-200">
              <User className="w-3 h-3 text-slate-400" />
              <span className="truncate max-w-[90px]">{task.assignee.name}</span>
            </div>
          )}
        </div>

        {/* Card Actions: Status dropdown + Edit/Delete buttons */}
        <div className="flex items-center justify-between gap-2">
          <select
            data-testid="status-select"
            value={task.status}
            onChange={(e) => onStatusChange(task.id, e.target.value as TaskStatus)}
            className="text-xs bg-slate-50 hover:bg-slate-100 border border-slate-300 rounded-md px-2 py-1 text-slate-700 font-medium focus:ring-1 focus:ring-blue-500 focus:outline-none"
          >
            <option value="TODO">To Do</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="DONE">Done</option>
          </select>

          <div className="flex items-center space-x-1">
            <button
              onClick={() => onEdit(task)}
              title="Edit Task"
              className="p-1 text-slate-400 hover:text-blue-600 rounded hover:bg-blue-50 transition"
            >
              <Edit2 className="w-3.5 h-3.5" />
            </button>
            <button
              onClick={() => onDelete(task.id)}
              title="Delete Task"
              className="p-1 text-slate-400 hover:text-red-600 rounded hover:bg-red-50 transition"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
