'use client'

import { useState } from 'react'
import { type Habit } from '@/lib/api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { X, Save, Palette } from 'lucide-react'

interface HabitFormProps {
    habit?: Habit
    onSubmit: (habit: Omit<Habit, 'id' | 'createdAt' | 'updatedAt'>) => void
    onCancel: () => void
}

const colors = [
    { hex: '#3b82f6', name: 'Blue' },
    { hex: '#ef4444', name: 'Red' },
    { hex: '#10b981', name: 'Green' },
    { hex: '#f59e0b', name: 'Amber' },
    { hex: '#8b5cf6', name: 'Purple' },
    { hex: '#ec4899', name: 'Pink' },
    { hex: '#06b6d4', name: 'Cyan' },
    { hex: '#84cc16', name: 'Lime' }
]

export default function HabitForm({ habit, onSubmit, onCancel }: HabitFormProps) {
    const [name, setName] = useState(habit?.name || '')
    const [description, setDescription] = useState(habit?.description || '')
    const [color, setColor] = useState(habit?.color || colors[0].hex)
    const [loading, setLoading] = useState(false)

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setLoading(true)

        try {
            await onSubmit({
                name,
                description,
                color,
                userId: habit?.userId || '',
            })
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="glass-card p-8 max-w-2xl mx-auto">
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h2 className="text-2xl font-bold text-ocean-800 mb-2">
                        {habit ? 'Edit Habit' : 'Create New Habit'}
                    </h2>
                    <p className="text-ocean-600">
                        {habit ? 'Update your habit details' : 'Add a new habit to track'}
                    </p>
                </div>
                <Button
                    onClick={onCancel}
                    className="w-10 h-10 p-0 bg-red-50/50 border border-red-200/50 text-red-500 hover:bg-red-100/50"
                >
                    <X className="w-5 h-5" />
                </Button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="space-y-2">
                    <label htmlFor="name" className="block text-sm font-medium text-ocean-700">
                        Habit Name
                    </label>
                    <Input
                        id="name"
                        placeholder="Enter habit name..."
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                        className="glass-input"
                    />
                </div>

                <div className="space-y-2">
                    <label htmlFor="description" className="block text-sm font-medium text-ocean-700">
                        Description (Optional)
                    </label>
                    <textarea
                        id="description"
                        placeholder="Enter habit description..."
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        rows={3}
                        className="w-full glass-input resize-none"
                    />
                </div>

                <div className="space-y-4">
                    <div className="flex items-center space-x-2">
                        <Palette className="w-5 h-5 text-ocean-600" />
                        <label className="text-sm font-medium text-ocean-700">
                            Choose Color
                        </label>
                    </div>
                    <div className="grid grid-cols-4 gap-3">
                        {colors.map((c) => (
                            <button
                                key={c.hex}
                                type="button"
                                className={`
                                    relative p-4 rounded-xl border-2 transition-all duration-300 group
                                    ${color === c.hex 
                                        ? 'border-ocean-400 shadow-soft' 
                                        : 'border-white/30 hover:border-ocean-300'
                                    }
                                    glass-morphism
                                `}
                                onClick={() => setColor(c.hex)}
                            >
                                <div
                                    className="w-full h-6 rounded-lg mb-2 shadow-soft"
                                    style={{ backgroundColor: c.hex }}
                                />
                                <div className="text-xs text-ocean-600 font-medium">
                                    {c.name}
                                </div>
                                {color === c.hex && (
                                    <div className="absolute top-2 right-2 w-3 h-3 bg-ocean-500 rounded-full" />
                                )}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="flex space-x-4 pt-6 border-t border-white/20">
                    <Button 
                        type="submit" 
                        disabled={loading}
                        className="glass-button flex-1 flex items-center justify-center space-x-2 py-3"
                    >
                        {loading ? (
                            <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
                        ) : (
                            <Save className="w-5 h-5" />
                        )}
                        <span className="font-medium">
                            {loading ? 'Saving...' : habit ? 'Update Habit' : 'Create Habit'}
                        </span>
                    </Button>
                    <Button 
                        type="button" 
                        onClick={onCancel}
                        className="px-8 py-3 bg-white/10 border border-white/20 text-ocean-600 hover:bg-white/20 font-medium rounded-xl"
                    >
                        Cancel
                    </Button>
                </div>
            </form>
        </div>
    )
}