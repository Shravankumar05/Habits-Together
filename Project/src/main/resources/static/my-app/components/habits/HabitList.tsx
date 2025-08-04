'use client'

import { useState, useEffect } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import { fetchHabits, createHabit, type Habit } from '@/lib/api'
import HabitItem from './HabitItem'
import HabitForm from './HabitForm'
import { Button } from '@/components/ui/button'
import { Plus, AlertTriangle, Target } from 'lucide-react'

export default function HabitList() {
    const { user } = useAuth()
    const [habits, setHabits] = useState<Habit[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [showForm, setShowForm] = useState(false)

    useEffect(() => {
        loadHabits()
    }, [])

    const loadHabits = async () => {
        try {
            setLoading(true)
            const data = await fetchHabits()
            setHabits(data)
            setError('')
        } catch (err) {
            setError('Failed to load habits')
            console.error('Error loading habits:', err)
        } finally {
            setLoading(false)
        }
    }

    const handleCreateHabit = async (habitData: Omit<Habit, 'id' | 'createdAt' | 'updatedAt'>) => {
        try {
            const newHabit = await createHabit({
                ...habitData,
                userId: user?.id || '',
            })
            setHabits(prev => [...prev, newHabit])
            setShowForm(false)
        } catch (err) {
            console.error('Error creating habit:', err)
            setError('Failed to create habit')
        }
    }

    const handleHabitUpdate = (updatedHabit: Habit) => {
        setHabits(prev => prev.map(habit =>
            habit.id === updatedHabit.id ? updatedHabit : habit
        ))
    }

    const handleHabitDelete = (habitId: number) => {
        setHabits(prev => prev.filter(habit => habit.id !== habitId))
    }

    if (loading) {
        return (
            <div className="flex flex-col items-center justify-center py-16 space-y-4">
                <div className="relative">
                    <div className="w-16 h-16 border-4 border-ocean-200 rounded-full animate-spin">
                        <div className="absolute top-0 left-0 w-4 h-4 bg-ocean-500 rounded-full"></div>
                    </div>
                </div>
                <p className="text-ocean-600 text-sm">Loading your habits...</p>
            </div>
        )
    }

    return (
        <div className="space-y-8">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-xl font-semibold text-ocean-800 mb-1">Your Habits</h2>
                    <p className="text-ocean-600 text-sm">
                        {habits.length} {habits.length === 1 ? 'habit' : 'habits'} tracked
                    </p>
                </div>
                <Button 
                    onClick={() => setShowForm(true)} 
                    className="glass-button flex items-center space-x-2"
                >
                    <Plus className="w-4 h-4" />
                    <span>Add Habit</span>
                </Button>
            </div>

            {error && (
                <div className="glass-card border-red-200 bg-red-50/50 p-4">
                    <div className="flex items-center space-x-3">
                        <AlertTriangle className="w-5 h-5 text-red-500" />
                        <div>
                            <p className="text-red-700 font-medium">Error</p>
                            <p className="text-red-600 text-sm">{error}</p>
                        </div>
                    </div>
                </div>
            )}

            {showForm && (
                <div className="animate-in">
                    <HabitForm
                        onSubmit={handleCreateHabit}
                        onCancel={() => setShowForm(false)}
                    />
                </div>
            )}

            {habits.length === 0 ? (
                <div className="text-center py-16">
                    <div className="glass-card p-12 max-w-md mx-auto">
                        <div className="w-20 h-20 bg-gradient-to-br from-ocean-100 to-cream-100 rounded-full flex items-center justify-center mx-auto mb-6">
                            <Target className="w-10 h-10 text-ocean-500" />
                        </div>
                        <h3 className="text-lg font-semibold text-ocean-800 mb-2">No habits yet</h3>
                        <p className="text-ocean-600 text-sm mb-6">
                            Create your first habit to start building better routines
                        </p>
                        <Button 
                            onClick={() => setShowForm(true)}
                            className="glass-button"
                        >
                            Create Your First Habit
                        </Button>
                    </div>
                </div>
            ) : (
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                    {habits.map((habit, index) => (
                        <div 
                            key={habit.id} 
                            className="animate-in"
                            style={{ animationDelay: `${index * 100}ms` }}
                        >
                            <HabitItem
                                habit={habit}
                                onUpdate={handleHabitUpdate}
                                onDelete={handleHabitDelete}
                            />
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}