'use client'

import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { ArrowLeft, Plus, Users, Settings, UserCheck, UserX } from 'lucide-react'
import { Group } from './GroupPanel'
import GroupHabitList from './GroupHabitList'
import CreateGroupHabitModal from './CreateGroupHabitModal'

export interface GroupHabit {
    id: string
    groupId: string
    name: string
    description?: string
    color: string
    createdBy: string
    createdAt: string
    updatedAt: string
}

export interface GroupMember {
    id: string
    userId: string
    email: string
    displayName?: string
    role: 'admin' | 'member'
    joinedAt: string
}

export interface GroupHabitCompletion {
    id: string
    groupHabitId: string
    userId: string
    completionDate: string
    completed: boolean
    notes?: string
    completedAt: string
}

interface GroupViewProps {
    group: Group
    onBack: () => void
}

export default function GroupView({ group, onBack }: GroupViewProps) {
    const [groupHabits, setGroupHabits] = useState<GroupHabit[]>([])
    const [groupMembers, setGroupMembers] = useState<GroupMember[]>([])
    const [completions, setCompletions] = useState<GroupHabitCompletion[]>([])
    const [showCreateHabitModal, setShowCreateHabitModal] = useState(false)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const loadGroupData = async () => {
            console.log('GroupView: Loading data for group:', group);
            
            if (!group || !group.id) {
                console.error('GroupView: Invalid group provided:', group);
                setGroupHabits([]);
                setGroupMembers([]);
                setCompletions([]);
                return;
            }

            try {
                const headers = {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                };

                console.log('GroupView: Making API calls for group ID:', group.id);

                // Load group habits
                const habitsResponse = await fetch(`http://localhost:8080/api/groups/${group.id}/habits`, { headers });
                if (habitsResponse.ok) {
                    const habits = await habitsResponse.json();
                    console.log('GroupView: Loaded habits:', habits);
                    setGroupHabits(habits);
                } else {
                    console.error('GroupView: Failed to load habits:', habitsResponse.status);
                    setGroupHabits([]);
                }

                // Load group members with real emails
                const membersResponse = await fetch(`http://localhost:8080/api/groups/${group.id}`, { headers });
                if (membersResponse.ok) {
                    const groupDetails = await membersResponse.json();
                    console.log('GroupView: Loaded group details:', groupDetails);
                    setGroupMembers(groupDetails.members || []);
                } else {
                    console.error('GroupView: Failed to load group details:', membersResponse.status);
                    setGroupMembers([]);
                }

                // Load today's completions
                const today = new Date().toISOString().split('T')[0];
                const completionsResponse = await fetch(`http://localhost:8080/api/groups/${group.id}/habits/completions?date=${today}`, { headers });
                if (completionsResponse.ok) {
                    const todayCompletions = await completionsResponse.json();
                    console.log('GroupView: Loaded completions:', todayCompletions);
                    setCompletions(todayCompletions);
                } else {
                    console.error('GroupView: Failed to load completions:', completionsResponse.status);
                    setCompletions([]);
                }
            } catch (error) {
                console.error('GroupView: Error loading group data:', error);
                setGroupHabits([]);
                setGroupMembers([]);
                setCompletions([]);
            } finally {
                setLoading(false);
            }
        };

        loadGroupData();
    }, [group]);

    const handleCreateHabit = (newHabit: GroupHabit) => {
        setGroupHabits(prev => [...prev, newHabit])
        setShowCreateHabitModal(false)
    }

    const handleToggleCompletion = async (habitId: string, userId: string) => {
        try {
            const today = new Date().toISOString().split('T')[0]
            console.log('GroupView: Toggling completion for habit:', habitId, 'user:', userId, 'date:', today);
            
            const headers = {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            };

            // Call backend API to toggle completion
            const response = await fetch(`http://localhost:8080/api/groups/${group.id}/habits/${habitId}/completions/toggle`, {
                method: 'POST',
                headers,
                body: JSON.stringify({ date: today })
            });

            if (response.ok) {
                const updatedCompletion = await response.json();
                console.log('GroupView: Completion toggled successfully:', updatedCompletion);
                
                // Update local state with the response from backend
                const existingCompletion = completions.find(
                    c => c.groupHabitId === habitId && c.userId === userId && c.completionDate === today
                );

                if (existingCompletion) {
                    // Update existing completion
                    setCompletions(prev => 
                        prev.map(c => 
                            c.id === existingCompletion.id 
                                ? { ...updatedCompletion }
                                : c
                        )
                    );
                } else {
                    // Add new completion
                    setCompletions(prev => [...prev, updatedCompletion]);
                }
            } else {
                console.error('GroupView: Failed to toggle completion:', response.status, response.statusText);
            }
        } catch (error) {
            console.error('GroupView: Error toggling completion:', error);
        }
    }

    if (loading) {
        return (
            <div className="glass-card p-6">
                <div className="animate-pulse space-y-4">
                    <div className="h-4 bg-ocean-200 rounded w-3/4"></div>
                    <div className="h-4 bg-ocean-200 rounded w-1/2"></div>
                    <div className="h-4 bg-ocean-200 rounded w-2/3"></div>
                </div>
            </div>
        )
    }

    return (
        <div className="space-y-6">
            {/* Group Header */}
            <div className="glass-card p-6">
                <div className="flex items-center justify-between mb-4">
                    <Button
                        onClick={onBack}
                        className="glass-button-sm flex items-center space-x-2"
                    >
                        <ArrowLeft className="w-4 h-4" />
                        <span>Back to Groups</span>
                    </Button>
                    <Button className="glass-button-sm flex items-center space-x-2">
                        <Settings className="w-4 h-4" />
                        <span>Settings</span>
                    </Button>
                </div>

                <div className="flex items-center space-x-4 mb-4">
                    <div className="w-12 h-12 bg-gradient-to-br from-purple-400 to-purple-600 rounded-xl flex items-center justify-center">
                        <Users className="w-6 h-6 text-white" />
                    </div>
                    <div>
                        <h2 className="text-2xl font-bold text-ocean-800">
                            {group.name}
                        </h2>
                        {group.description && (
                            <p className="text-ocean-600">
                                {group.description}
                            </p>
                        )}
                    </div>
                </div>

                {/* Group Members */}
                <div className="flex items-center space-x-2 mb-4">
                    <Users className="w-4 h-4 text-ocean-600" />
                    <span className="text-sm text-ocean-600 font-medium">Members:</span>
                    <div className="flex items-center space-x-2">
                        {groupMembers.map((member, index) => (
                            <span key={member.id} className="text-sm text-ocean-700">
                                {member.displayName || member.email.split('@')[0]}
                                {index < groupMembers.length - 1 && ','}
                            </span>
                        ))}
                    </div>
                </div>
            </div>

            {/* Group Habits */}
            <div className="glass-card p-6">
                <div className="flex items-center justify-between mb-6">
                    <h3 className="text-lg font-semibold text-ocean-800">
                        Group Habits
                    </h3>
                    <Button
                        onClick={() => setShowCreateHabitModal(true)}
                        className="glass-button-sm flex items-center space-x-2"
                    >
                        <Plus className="w-4 h-4" />
                        <span>Add Habit</span>
                    </Button>
                </div>

                <GroupHabitList
                    habits={groupHabits}
                    members={groupMembers}
                    completions={completions}
                    onToggleCompletion={handleToggleCompletion}
                />
            </div>

            {/* Create Group Habit Modal */}
            <CreateGroupHabitModal
                isOpen={showCreateHabitModal}
                onClose={() => setShowCreateHabitModal(false)}
                onCreateHabit={handleCreateHabit}
                groupId={group.id}
            />
        </div>
    )
}
