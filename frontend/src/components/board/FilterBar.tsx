import React from 'react';
import type { TaskPriority } from '../../types';
import { Filter, ArrowUpDown, X } from 'lucide-react';

interface FilterBarProps {
  priority: TaskPriority | '';
  sortBy: string;
  sortDirection: string;
  onPriorityChange: (priority: TaskPriority | '') => void;
  onSortByChange: (sortBy: string) => void;
  onSortDirectionToggle: () => void;
  onClearFilters: () => void;
}

export const FilterBar: React.FC<FilterBarProps> = ({
  priority,
  sortBy,
  sortDirection,
  onPriorityChange,
  onSortByChange,
  onSortDirectionToggle,
  onClearFilters,
}) => {
  const hasActiveFilters = !!priority || sortBy !== 'createdAt';

  return (
    <div className="bg-white border border-slate-200 rounded-xl p-3.5 mb-6 flex flex-wrap items-center justify-between gap-3 shadow-sm">
      <div className="flex flex-wrap items-center gap-3">
        <div className="flex items-center space-x-1.5 text-xs font-semibold text-slate-500 uppercase tracking-wider">
          <Filter className="w-4 h-4 text-slate-400" />
          <span>Filters:</span>
        </div>

        {/* Priority Filter */}
        <select
          value={priority}
          onChange={(e) => onPriorityChange(e.target.value as TaskPriority | '')}
          className="text-sm bg-slate-50 border border-slate-300 rounded-lg px-2.5 py-1.5 focus:ring-1 focus:ring-blue-500 focus:outline-none"
        >
          <option value="">All Priorities</option>
          <option value="LOW">Low Priority</option>
          <option value="MEDIUM">Medium Priority</option>
          <option value="HIGH">High Priority</option>
        </select>

        {hasActiveFilters && (
          <button
            onClick={onClearFilters}
            className="inline-flex items-center space-x-1 text-xs text-red-600 hover:text-red-700 bg-red-50 px-2 py-1.5 rounded-md font-medium transition"
          >
            <X className="w-3.5 h-3.5" />
            <span>Reset</span>
          </button>
        )}
      </div>

      {/* Sorting */}
      <div className="flex items-center space-x-2">
        <div className="flex items-center space-x-1 text-xs font-semibold text-slate-500 uppercase tracking-wider">
          <ArrowUpDown className="w-4 h-4 text-slate-400" />
          <span>Sort:</span>
        </div>
        <select
          value={sortBy}
          onChange={(e) => onSortByChange(e.target.value)}
          className="text-sm bg-slate-50 border border-slate-300 rounded-lg px-2.5 py-1.5 focus:ring-1 focus:ring-blue-500 focus:outline-none"
        >
          <option value="createdAt">Date Created</option>
          <option value="dueDate">Due Date</option>
          <option value="priority">Priority</option>
        </select>
        <button
          onClick={onSortDirectionToggle}
          title={`Currently ${sortDirection.toUpperCase()}`}
          className="px-2.5 py-1.5 text-xs font-medium border border-slate-300 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-700 transition"
        >
          {sortDirection.toUpperCase()}
        </button>
      </div>
    </div>
  );
};

