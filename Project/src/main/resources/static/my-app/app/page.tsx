'use client'

import { useAuth } from '@/contexts/AuthContext'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import ProtectedRoute from '@/components/ProtectedRoute'
import HabitList from '@/components/habits/HabitList'
import GroupPanel from '@/components/groups/GroupPanel'
import { Button } from '@/components/ui/button'
import { LogOut, Activity, Users, User } from 'lucide-react'

export default function Home() {
    const { user, signOut } = useAuth()
    const router = useRouter()

    const handleSignOut = async () => {
        await signOut()
        router.push('/login')
    }

    return (
        <ProtectedRoute>
            <div className="min-h-screen relative">
                <header className="glass-card border-b border-white/10 backdrop-blur-xl">
                    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                        <div className="flex justify-between items-center py-6">
                            <div className="flex items-center space-x-4">
                                <div className="flex items-center space-x-3">
                                    <div className="w-12 h-12 bg-gradient-to-br from-ocean-400 to-ocean-600 rounded-2xl flex items-center justify-center shadow-soft">
                                        <Activity className="w-7 h-7 text-white" />
                                    </div>
                                    <div>
                                        <h1 className="text-3xl font-bold text-gradient-blue">
                                            Habit Tracker
                                        </h1>
                                        <p className="text-sm text-ocean-600">
                                            Build better routines together
                                        </p>
                                    </div>
                                </div>
                                <div className="hidden md:flex items-center space-x-6 ml-8">
                                    <div className="flex items-center space-x-2 text-ocean-700">
                                        <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                                        <span className="text-sm font-medium">
                                            Welcome, {user?.email?.split('@')[0] || 'User'}
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <Button 
                                onClick={handleSignOut} 
                                className="glass-button flex items-center space-x-2"
                            >
                                <LogOut className="w-4 h-4" />
                                <span>Sign Out</span>
                            </Button>
                        </div>
                    </div>
                </header>

                <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                    {/* Two-column layout */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        {/* Left Column - Personal Habits */}
                        <div className="space-y-6">
                            <div className="flex items-center space-x-3 mb-4">
                                <div className="w-10 h-10 bg-gradient-to-br from-cream-400 to-cream-600 rounded-xl flex items-center justify-center">
                                    <User className="w-6 h-6 text-white" />
                                </div>
                                <div>
                                    <h2 className="text-2xl font-bold text-ocean-800">
                                        Personal Habits
                                    </h2>
                                    <p className="text-ocean-600">
                                        Your individual daily routines
                                    </p>
                                </div>
                            </div>
                            
                            {/* Simple Today's Progress Text */}
                            <div className="glass-card p-4 mb-6">
                                <div className="text-sm text-ocean-600 font-medium mb-1">
                                    Today's Progress
                                </div>
                                <div className="text-ocean-800">
                                    Check off your habits to track your daily progress
                                </div>
                            </div>
                            
                            <HabitList />
                        </div>

                        {/* Right Column - Group Habits */}
                        <div className="space-y-6">
                            <div className="flex items-center space-x-3 mb-4">
                                <div className="w-10 h-10 bg-gradient-to-br from-purple-400 to-purple-600 rounded-xl flex items-center justify-center">
                                    <Users className="w-6 h-6 text-white" />
                                </div>
                                <div>
                                    <h2 className="text-2xl font-bold text-ocean-800">
                                        Group Habits
                                    </h2>
                                    <p className="text-ocean-600">
                                        Shared accountability with friends and family
                                    </p>
                                </div>
                            </div>
                            
                            <GroupPanel />
                        </div>
                    </div>
                </main>
            </div>
        </ProtectedRoute>
    )
}