'use client'

import { Button } from '@/components/ui/button'
import { Check, X, Circle, CheckCircle2, User } from 'lucide-react'
import { GroupHabit, GroupMember, GroupHabitCompletion } from './GroupView'

interface GroupHabitListProps {
    habits: GroupHabit[]
    members: GroupMember[]
    completions: GroupHabitCompletion[]
    onToggleCompletion: (habitId: string, userId: string) => void
}

export default function GroupHabitList({ 
    habits, 
    members, 
    completions, 
    onToggleCompletion 
}: GroupHabitListProps) {
    const today = new Date().toISOString().split('T')[0]
    const currentUserId = localStorage.getItem('userId') || 'anonymous-user' // Get from localStorage or use anonymous

    const getCompletionStatus = (habitId: string, userId: string) => {
        const completion = completions.find(
            c => c.groupHabitId === habitId && 
                 c.userId === userId && 
                 c.completionDate === today
        )
        return completion?.completed || false
    }

    const getCompletedCount = (habitId: string) => {
        return completions.filter(
            c => c.groupHabitId === habitId && 
                 c.completionDate === today && 
                 c.completed
        ).length
    }

    if (habits.length === 0) {
        return (
            <div className="text-center py-8">
                <Circle className="w-12 h-12 text-ocean-300 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-ocean-600 mb-2">
                    No group habits yet
                </h3>
                <p className="text-ocean-500">
                    Add your first habit to start tracking together
                </p>
            </div>
        )
    }

    return (
        <div className="space-y-4">
            {habits.map((habit) => {
                const completedCount = getCompletedCount(habit.id)
                const totalMembers = members.length
                const completionPercentage = totalMembers > 0 ? (completedCount / totalMembers) * 100 : 0

                return (
                    <div
                        key={habit.id}
                        className="glass-card p-4 border-l-4 transition-all duration-200"
                        style={{ borderLeftColor: habit.color }}
                    >
                        {/* Habit Header */}
                        <div className="flex items-center justify-between mb-4">
                            <div className="flex items-center space-x-3">
                                <div 
                                    className="w-8 h-8 rounded-lg flex items-center justify-center text-white font-medium text-sm"
                                    style={{ backgroundColor: habit.color }}
                                >
                                    {habit.name.charAt(0).toUpperCase()}
                                </div>
                                <div>
                                    <h4 className="font-semibold text-ocean-800">
                                        {habit.name}
                                    </h4>
                                    {habit.description && (
                                        <p className="text-sm text-ocean-600">
                                            {habit.description}
                                        </p>
                                    )}
                                </div>
                            </div>
                            
                            {/* Progress Summary */}
                            <div className="text-right">
                                <div className="text-sm font-medium text-ocean-800">
                                    {completedCount}/{totalMembers} completed
                                </div>
                                <div className="w-16 h-2 bg-ocean-100 rounded-full overflow-hidden">
                                    <div 
                                        className="h-full bg-gradient-to-r from-green-400 to-green-500 transition-all duration-300"
                                        style={{ width: `${completionPercentage}%` }}
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Member Completion Status */}
                        <div className="space-y-2">
                            {members.map((member) => {
                                const isCompleted = getCompletionStatus(habit.id, member.userId)
                                const isCurrentUser = member.userId === currentUserId

                                return (
                                    <div
                                        key={member.id}
                                        className="flex items-center justify-between p-2 rounded-lg bg-white/30"
                                    >
                                        <div className="flex items-center space-x-3">
                                            <div className="w-6 h-6 bg-gradient-to-br from-ocean-400 to-ocean-600 rounded-full flex items-center justify-center">
                                                <User className="w-3 h-3 text-white" />
                                            </div>
                                            <span className="text-sm font-medium text-ocean-800">
                                                {member.display_name || member.email.split('@')[0]}
                                                {isCurrentUser && (
                                                    <span className="text-xs text-ocean-500 ml-1">(You)</span>
                                                )}
                                            </span>
                                        </div>

                                        <div className="flex items-center space-x-2">
                                            {isCompleted ? (
                                                <div className="flex items-center space-x-1 text-green-600">
                                                    <CheckCircle2 className="w-4 h-4" />
                                                    <span className="text-xs font-medium">Done</span>
                                                </div>
                                            ) : (
                                                <div className="flex items-center space-x-1 text-ocean-400">
                                                    <Circle className="w-4 h-4" />
                                                    <span className="text-xs font-medium">Pending</span>
                                                </div>
                                            )}

                                            {/* Only current user can toggle their own completion */}
                                            {isCurrentUser && (
                                                <Button
                                                    onClick={() => onToggleCompletion(habit.id, member.userId)}
                                                    className={`w-8 h-8 p-0 rounded-full transition-all duration-200 ${
                                                        isCompleted
                                                            ? 'bg-green-500 hover:bg-green-600 text-white'
                                                            : 'bg-ocean-200 hover:bg-ocean-300 text-ocean-600'
                                                    }`}
                                                >
                                                    {isCompleted ? (
                                                        <Check className="w-4 h-4" />
                                                    ) : (
                                                        <Circle className="w-4 h-4" />
                                                    )}
                                                </Button>
                                            )}
                                        </div>
                                    </div>
                                )
                            })}
                        </div>
                    </div>
                )
            })}
        </div>
    )
}
