import React from 'react';
import type { ProjectSummary } from '../../types';
import { CheckCircle2, Clock, ListTodo, AlertTriangle, Layers } from 'lucide-react';

interface SummaryBarProps {
  summary: ProjectSummary | null;
  loading?: boolean;
}

export const SummaryBar: React.FC<SummaryBarProps> = ({ summary, loading }) => {
  if (loading) {
    return (
      <div className="grid grid-cols-2 md:grid-cols-5 gap-3.5 mb-6 animate-pulse">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="h-20 bg-slate-200 rounded-xl" />
        ))}
      </div>
    );
  }

  if (!summary) return null;

  const cards = [
    {
      label: 'Total Tasks',
      value: summary.totalTasks,
      icon: <Layers className="w-5 h-5 text-slate-600" />,
      color: 'bg-white border-slate-200 text-slate-900',
    },
    {
      label: 'To Do',
      value: summary.todoCount,
      icon: <ListTodo className="w-5 h-5 text-blue-600" />,
      color: 'bg-white border-blue-100 text-blue-900',
    },
    {
      label: 'In Progress',
      value: summary.inProgressCount,
      icon: <Clock className="w-5 h-5 text-amber-600" />,
      color: 'bg-white border-amber-100 text-amber-900',
    },
    {
      label: 'Done',
      value: summary.doneCount,
      icon: <CheckCircle2 className="w-5 h-5 text-emerald-600" />,
      color: 'bg-white border-emerald-100 text-emerald-900',
    },
    {
      label: 'Overdue',
      value: summary.overdueCount,
      icon: <AlertTriangle className="w-5 h-5 text-red-600" />,
      color: summary.overdueCount > 0 ? 'bg-red-50 border-red-200 text-red-900 ring-1 ring-red-300' : 'bg-white border-slate-200 text-slate-900',
      isOverdueBadge: summary.overdueCount > 0,
    },
  ];

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3.5 mb-6">
      {cards.map((c) => (
        <div
          key={c.label}
          data-testid={`summary-${c.label.toLowerCase().replace(/\s+/g, '-')}`}
          className={`p-4 rounded-xl border shadow-sm flex items-center justify-between ${c.color}`}
        >
          <div>
            <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">{c.label}</p>
            <p className="mt-1 text-2xl font-extrabold tracking-tight">{c.value}</p>
          </div>
          <div className="p-2.5 rounded-lg bg-slate-50/80">{c.icon}</div>
        </div>
      ))}
    </div>
  );
};
