'use client'

import { useState, useEffect } from 'react'
import { updateHabit, deleteHabit, toggleHabitCompletion, getHabitCompletionForDate, type Habit } from '@/lib/api'
import HabitForm from './HabitForm'
import { Button } from '@/components/ui/button'
import { Check, Edit, Trash2, Circle } from 'lucide-react'

interface HabitItemProps {
    habit: Habit
    onUpdate: (habit: Habit) => void
    onDelete: (habitId: number) => void
}

export default function HabitItem({ habit, onUpdate, onDelete }: HabitItemProps) {
    const [isEditing, setIsEditing] = useState(false)
    const [isCompleted, setIsCompleted] = useState(false)
    const [loading, setLoading] = useState(false)
    const [checkingCompletion, setCheckingCompletion] = useState(true)

    const today = new Date().toISOString().split('T')[0]

    // Check completion status when component mounts or habit changes
    useEffect(() => {
        const checkCompletionStatus = async () => {
            try {
                setCheckingCompletion(true)
                const completion = await getHabitCompletionForDate(habit.id, today)
                setIsCompleted(completion?.completed || false)
                console.log(`Habit ${habit.name} completion status for ${today}:`, completion?.completed || false)
            } catch (err) {
                console.error('Error checking completion status:', err)
                setIsCompleted(false)
            } finally {
                setCheckingCompletion(false)
            }
        }

        checkCompletionStatus()
    }, [habit.id, today])

    const handleUpdate = async (habitData: Omit<Habit, 'id' | 'createdAt' | 'updatedAt'>) => {
        try {
            const updatedHabit = await updateHabit(habit.id, habitData)
            onUpdate(updatedHabit)
            setIsEditing(false)
        } catch (err) {
            console.error('Error updating habit:', err)
        }
    }

    const handleDelete = async () => {
        if (confirm('Are you sure you want to delete this habit?')) {
            try {
                await deleteHabit(habit.id)
                onDelete(habit.id)
            } catch (err) {
                console.error('Error deleting habit:', err)
            }
        }
    }

    const handleToggleCompletion = async () => {
        try {
            setLoading(true)
            const result = await toggleHabitCompletion(habit.id, today)
            // Update the completion state based on the API response
            if (result) {
                setIsCompleted(result.completed)
                console.log(`Habit ${habit.name} toggled to:`, result.completed)
            } else {
                // If no result, toggle the current state
                setIsCompleted(!isCompleted)
                console.log(`Habit ${habit.name} toggled to:`, !isCompleted)
            }
        } catch (err) {
            console.error('Error toggling completion:', err)
        } finally {
            setLoading(false)
        }
    }

    if (isEditing) {
        return (
            <div className="animate-in">
                <HabitForm
                    habit={habit}
                    onSubmit={handleUpdate}
                    onCancel={() => setIsEditing(false)}
                />
            </div>
        )
    }

    return (
        <div className="glass-card p-6 hover-lift group relative overflow-hidden">
            {/* Color accent */}
            <div
                className="absolute top-0 left-0 w-full h-1 rounded-t-2xl"
                style={{ backgroundColor: habit.color }}
            />
            
            {/* Status indicator */}
            <div className="absolute top-4 right-4">
                <div 
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: habit.color, opacity: 0.6 }}
                />
            </div>

            <div className="space-y-4">
                <div className="flex items-start justify-between pr-6">
                    <div className="flex items-center space-x-3">
                        <div 
                            className="w-12 h-12 rounded-2xl flex items-center justify-center shadow-soft"
                            style={{ backgroundColor: `${habit.color}20` }}
                        >
                            <Circle className="w-6 h-6" style={{ color: habit.color }} />
                        </div>
                        <div>
                            <h3 className="text-lg font-semibold text-ocean-800">
                                {habit.name}
                            </h3>
                            {habit.description && (
                                <p className="text-sm text-ocean-600">
                                    {habit.description}
                                </p>
                            )}
                        </div>
                    </div>
                </div>

                <div className="flex items-center justify-between pt-4 border-t border-white/20">
                    <Button
                        onClick={handleToggleCompletion}
                        disabled={loading || checkingCompletion}
                        className={`
                            flex items-center space-x-2 px-4 py-2 rounded-xl font-medium
                            transition-all duration-300 ${
                                isCompleted 
                                    ? 'bg-green-100 text-green-700 border border-green-200' 
                                    : 'glass-button'
                            }
                        `}
                    >
                        {loading ? (
                            <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin" />
                        ) : (
                            <Check className="w-4 h-4" />
                        )}
                        <span className="text-sm">
                            {isCompleted ? 'Completed' : 'Mark Done'}
                        </span>
                    </Button>

                    <div className="flex space-x-2">
                        <Button
                            onClick={() => setIsEditing(true)}
                            className="w-10 h-10 p-0 glass-button"
                        >
                            <Edit className="w-4 h-4" />
                        </Button>
                        <Button
                            onClick={handleDelete}
                            className="w-10 h-10 p-0 bg-red-50/50 border border-red-200/50 text-red-500 hover:bg-red-100/50 transition-all duration-300"
                        >
                            <Trash2 className="w-4 h-4" />
                        </Button>
                    </div>
                </div>

                {/* Progress indicator */}
                <div className="flex items-center justify-between text-xs text-ocean-500">
                    <span>Today's Progress</span>
                    <span className="font-medium">
                        {isCompleted ? 'Complete' : 'Pending'}
                    </span>
                </div>
            </div>
        </div>
    )
}