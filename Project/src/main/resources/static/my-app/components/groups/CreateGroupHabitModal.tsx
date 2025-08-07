'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { X, Circle, Plus } from 'lucide-react'
import { GroupHabit } from './GroupView'

interface CreateGroupHabitModalProps {
    isOpen: boolean
    onClose: () => void
    onCreateHabit: (habit: GroupHabit) => void
    groupId: string
}

const HABIT_COLORS = [
    '#3B82F6', // Blue
    '#10B981', // Green
    '#F59E0B', // Yellow
    '#EF4444', // Red
    '#8B5CF6', // Purple
    '#06B6D4', // Cyan
    '#F97316', // Orange
    '#84CC16', // Lime
    '#EC4899', // Pink
    '#6B7280'  // Gray
]

export default function CreateGroupHabitModal({ 
    isOpen, 
    onClose, 
    onCreateHabit, 
    groupId 
}: CreateGroupHabitModalProps) {
    const [habitName, setHabitName] = useState('')
    const [habitDescription, setHabitDescription] = useState('')
    const [selectedColor, setSelectedColor] = useState(HABIT_COLORS[0])
    const [loading, setLoading] = useState(false)
    const [errors, setErrors] = useState<{ [key: string]: string }>({})

    const validateForm = () => {
        const newErrors: { [key: string]: string } = {}

        if (!habitName.trim()) {
            newErrors.habitName = 'Habit name is required'
        }

        setErrors(newErrors)
        return Object.keys(newErrors).length === 0
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        
        if (!validateForm()) {
            return
        }

        setLoading(true)

        try {
            // Simulate API call
            await new Promise(resolve => setTimeout(resolve, 1000))

            const newHabit: GroupHabit = {
                id: Date.now().toString(),
                group_id: groupId,
                name: habitName.trim(),
                description: habitDescription.trim() || undefined,
                color: selectedColor,
                created_by: 'current_user_id',
                created_at: new Date().toISOString(),
                updated_at: new Date().toISOString()
            }

            onCreateHabit(newHabit)
            
            // Reset form
            setHabitName('')
            setHabitDescription('')
            setSelectedColor(HABIT_COLORS[0])
            setErrors({})
        } catch (error) {
            console.error('Error creating group habit:', error)
        } finally {
            setLoading(false)
        }
    }

    const handleClose = () => {
        if (!loading) {
            setHabitName('')
            setHabitDescription('')
            setSelectedColor(HABIT_COLORS[0])
            setErrors({})
            onClose()
        }
    }

    if (!isOpen) return null

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full border border-gray-200">
                <div className="p-6">
                    {/* Header */}
                    <div className="flex items-center justify-between mb-6">
                        <div className="flex items-center space-x-3">
                            <div 
                                className="w-8 h-8 rounded-lg flex items-center justify-center text-white font-medium"
                                style={{ backgroundColor: selectedColor }}
                            >
                                <Circle className="w-4 h-4" />
                            </div>
                            <h2 className="text-xl font-bold text-ocean-800">
                                Add Group Habit
                            </h2>
                        </div>
                        <Button
                            onClick={handleClose}
                            disabled={loading}
                            className="w-8 h-8 p-0 glass-button-sm"
                        >
                            <X className="w-4 h-4" />
                        </Button>
                    </div>

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="space-y-4">
                        {/* Habit Name */}
                        <div>
                            <label className="block text-sm font-medium text-ocean-700 mb-2">
                                Habit Name *
                            </label>
                            <Input
                                type="text"
                                value={habitName}
                                onChange={(e) => setHabitName(e.target.value)}
                                placeholder="e.g., Take Morning Vitamins, 30-min Walk"
                                disabled={loading}
                                className={errors.habitName ? 'border-red-300' : ''}
                            />
                            {errors.habitName && (
                                <p className="text-red-500 text-xs mt-1">{errors.habitName}</p>
                            )}
                        </div>

                        {/* Habit Description */}
                        <div>
                            <label className="block text-sm font-medium text-ocean-700 mb-2">
                                Description (Optional)
                            </label>
                            <Textarea
                                value={habitDescription}
                                onChange={(e) => setHabitDescription(e.target.value)}
                                placeholder="Add details about this habit..."
                                disabled={loading}
                                rows={3}
                            />
                        </div>

                        {/* Color Selection */}
                        <div>
                            <label className="block text-sm font-medium text-ocean-700 mb-2">
                                Choose Color
                            </label>
                            <div className="grid grid-cols-5 gap-2">
                                {HABIT_COLORS.map((color) => (
                                    <button
                                        key={color}
                                        type="button"
                                        onClick={() => setSelectedColor(color)}
                                        disabled={loading}
                                        className={`w-10 h-10 rounded-lg transition-all duration-200 ${
                                            selectedColor === color
                                                ? 'ring-2 ring-ocean-400 ring-offset-2 scale-110'
                                                : 'hover:scale-105'
                                        }`}
                                        style={{ backgroundColor: color }}
                                    >
                                        {selectedColor === color && (
                                            <div className="w-full h-full flex items-center justify-center">
                                                <div className="w-2 h-2 bg-white rounded-full" />
                                            </div>
                                        )}
                                    </button>
                                ))}
                            </div>
                        </div>

                        {/* Submit Buttons */}
                        <div className="flex space-x-3 pt-4">
                            <Button
                                type="button"
                                onClick={handleClose}
                                disabled={loading}
                                className="flex-1 glass-button-secondary"
                            >
                                Cancel
                            </Button>
                            <Button
                                type="submit"
                                disabled={loading}
                                className="flex-1 glass-button flex items-center justify-center space-x-2"
                            >
                                {loading ? (
                                    <>
                                        <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                                        <span>Creating...</span>
                                    </>
                                ) : (
                                    <>
                                        <Plus className="w-4 h-4" />
                                        <span>Add Habit</span>
                                    </>
                                )}
                            </Button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    )
}
