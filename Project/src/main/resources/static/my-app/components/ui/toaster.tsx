'use client'

import { useState } from 'react'
import { type Habit } from '@/lib/api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

interface HabitFormProps {
    habit?: Habit
    onSubmit: (habit: Omit<Habit, 'id' | 'createdAt' | 'updatedAt'>) => void
    onCancel: () => void
}

const colors = [
    '#3B82F6', '#EF4444', '#10B981', '#F59E0B',
    '#8B5CF6', '#EC4899', '#06B6D4', '#84CC16'
]

export default function HabitForm({ habit, onSubmit, onCancel }: HabitFormProps) {
    const [name, setName] = useState(habit?.name || '')
    const [description, setDescription] = useState(habit?.description || '')
    const [color, setColor] = useState(habit?.color || colors[0])
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
        <Card>
            <CardHeader>
                <CardTitle>{habit ? 'Edit Habit' : 'New Habit'}</CardTitle>
            </CardHeader>
            <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="space-y-2">
                        <label htmlFor="name" className="text-sm font-medium">Habit Name</label>
                        <Input
                            id="name"
                            placeholder="Enter habit name"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </div>

                    <div className="space-y-2">
                        <label htmlFor="description" className="text-sm font-medium">Description</label>
                        <textarea
                            id="description"
                            placeholder="Enter description (optional)"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                        />
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-medium">Color</label>
                        <div className="flex flex-wrap gap-2">
                            {colors.map((c) => (
                                <button
                                    key={c}
                                    type="button"
                                    className={`w-8 h-8 rounded-full border-2 ${
                                        color === c ? 'border-gray-800' : 'border-gray-300'
                                    }`}
                                    style={{ backgroundColor: c }}
                                    onClick={() => setColor(c)}
                                    aria-label={`Select color ${c}`}
                                />
                            ))}
                        </div>
                    </div>

                    <div className="flex space-x-2">
                        <Button type="submit" disabled={loading}>
                            {loading ? 'Saving...' : habit ? 'Update' : 'Create'}
                        </Button>
                        <Button type="button" variant="outline" onClick={onCancel}>
                            Cancel
                        </Button>
                    </div>
                </form>
            </CardContent>
        </Card>
    )
}