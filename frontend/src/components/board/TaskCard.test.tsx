import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { TaskCard } from './TaskCard';
import type { Task } from '../../types';

describe('TaskCard Component', () => {
  const mockTask: Task = {
    id: 1,
    title: 'Design UI Mockups',
    description: 'Create high fidelity Figma designs',
    status: 'TODO',
    priority: 'HIGH',
    dueDate: '2026-10-25',
    isOverdue: true,
    assignee: {
      id: 2,
      name: 'Alex Dev',
      email: 'alex@taskflow.com',
    },
    projectId: 10,
    createdAt: '2026-10-20T10:00:00Z',
    updatedAt: '2026-10-20T10:00:00Z',
  };

  it('renders task title, priority, and overdue badge when task is overdue', () => {
    const onStatusChange = vi.fn();
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    render(
      <TaskCard
        task={mockTask}
        onStatusChange={onStatusChange}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    expect(screen.getByText('Design UI Mockups')).toBeInTheDocument();
    expect(screen.getByText('HIGH')).toBeInTheDocument();
    expect(screen.getByTestId('overdue-badge')).toBeInTheDocument();
    expect(screen.getByText('OVERDUE')).toBeInTheDocument();
    expect(screen.getByText('Alex Dev')).toBeInTheDocument();
  });

  it('triggers onStatusChange callback when a new status is selected', () => {
    const onStatusChange = vi.fn();
    const onEdit = vi.fn();
    const onDelete = vi.fn();

    render(
      <TaskCard
        task={mockTask}
        onStatusChange={onStatusChange}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    const select = screen.getByTestId('status-select');
    fireEvent.change(select, { target: { value: 'IN_PROGRESS' } });

    expect(onStatusChange).toHaveBeenCalledTimes(1);
    expect(onStatusChange).toHaveBeenCalledWith(1, 'IN_PROGRESS');
  });
});

