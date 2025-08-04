'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { Habit, DailyCompletion, fetchHabits, createHabit, updateHabit, deleteHabit, getCompletions, updateCompletion } from '@/lib/api';

interface HabitContextType {
    habits: Habit[];
    loading: boolean;
    error: string | null;
    addHabit: (habit: Omit<Habit, 'id' | 'createdAt' | 'updatedAt'>) => Promise<void>;
    updateHabit: (habit: Partial<Habit> & { id: string }) => Promise<void>;
    removeHabit: (id: string) => Promise<void>;
    getHabitCompletions: (habitId: string, startDate: string, endDate: string) => Promise<DailyCompletion[]>;
    toggleCompletion: (completion: Omit<DailyCompletion, 'id' | 'completedAt'>) => Promise<void>;
}

const HabitContext = createContext<HabitContextType | undefined>(undefined);

export function HabitProvider({ children, userId }: { children: React.ReactNode; userId: string }) {
    const [habits, setHabits] = useState<Habit[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (userId) {
            loadHabits();
        }
    }, [userId]);

    const loadHabits = async () => {
        try {
            setLoading(true);
            const data = await fetchHabits(userId);
            setHabits(data);
        } catch (err) {
            setError('Failed to load habits');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const addHabit = async (habit: Omit<Habit, 'id' | 'createdAt' | 'updatedAt'>) => {
        try {
            const newHabit = await createHabit(habit);
            setHabits(prev => [...prev, newHabit]);
        } catch (err) {
            setError('Failed to add habit');
            console.error(err);
            throw err;
        }
    };

    const updateHabitHandler = async (habit: Partial<Habit> & { id: string }) => {
        try {
            const updatedHabit = await updateHabit(habit);
            setHabits(prev => prev.map(h => (h.id === updatedHabit.id ? updatedHabit : h)));
        } catch (err) {
            setError('Failed to update habit');
            console.error(err);
            throw err;
        }
    };

    const removeHabit = async (id: string) => {
        try {
            await deleteHabit(id);
            setHabits(prev => prev.filter(habit => habit.id !== id));
        } catch (err) {
            setError('Failed to delete habit');
            console.error(err);
            throw err;
        }
    };

    const getHabitCompletions = async (habitId: string, startDate: string, endDate: string) => {
        try {
            return await getCompletions(habitId, startDate, endDate);
        } catch (err) {
            setError('Failed to load completions');
            console.error(err);
            return [];
        }
    };

    const toggleCompletion = async (completion: Omit<DailyCompletion, 'id' | 'completedAt'>) => {
        try {
            await updateCompletion(completion);
            // Refresh the habits to update the UI
            await loadHabits();
        } catch (err) {
            setError('Failed to update completion');
            console.error(err);
            throw err;
        }
    };

    return (
        <HabitContext.Provider
            value={{
                habits,
                loading,
                error,
                addHabit,
                updateHabit: updateHabitHandler,
                removeHabit,
                getHabitCompletions,
                toggleCompletion,
            }}
        >
            {children}
        </HabitContext.Provider>
    );
}

export function useHabits() {
    const context = useContext(HabitContext);
    if (context === undefined) {
        throw new Error('useHabits must be used within a HabitProvider');
    }
    return context;
}