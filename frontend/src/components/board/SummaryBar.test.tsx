import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { SummaryBar } from './SummaryBar';
import type { ProjectSummary } from '../../types';

describe('SummaryBar Component', () => {
  const mockSummary: ProjectSummary = {
    projectId: 1,
    totalTasks: 12,
    todoCount: 5,
    inProgressCount: 4,
    doneCount: 3,
    overdueCount: 2,
  };

  it('correctly displays all aggregated task counts', () => {
    render(<SummaryBar summary={mockSummary} />);

    expect(screen.getByText('12')).toBeInTheDocument();
    expect(screen.getByText('5')).toBeInTheDocument();
    expect(screen.getByText('4')).toBeInTheDocument();
    expect(screen.getByText('3')).toBeInTheDocument();
    expect(screen.getByText('2')).toBeInTheDocument();
  });

  it('renders overdue card with alert styling when overdueCount > 0', () => {
    render(<SummaryBar summary={mockSummary} />);

    const overdueCard = screen.getByTestId('summary-overdue');
    expect(overdueCard).toBeInTheDocument();
    expect(overdueCard.className).toContain('bg-red-50');
  });
});
