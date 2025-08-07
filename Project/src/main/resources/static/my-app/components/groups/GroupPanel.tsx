'use client'

import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { Plus, Users, Settings } from 'lucide-react'
import GroupList from './GroupList'
import GroupView from './GroupView'
import CreateGroupModal from './CreateGroupModal'
import JoinGroupModal from './JoinGroupModal'

// Import the authentication function from api.ts
async function getAuthHeaders() {
    try {
        // Try to get Supabase client only if environment variables are available
        const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL;
        const supabaseKey = process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_OR_ANON_KEY;
        
        if (supabaseUrl && supabaseKey) {
            const { createClient } = await import('@supabase/supabase-js')
            const supabase = createClient(supabaseUrl, supabaseKey)
            
            // Get real Supabase session
            const { data: { session } } = await supabase.auth.getSession()
            
            if (session?.access_token) {
                console.log('Using real Supabase token for authenticated user');
                return {
                    'Authorization': `Bearer ${session.access_token}`,
                    'Content-Type': 'application/json',
                };
            } else {
                console.error('No Supabase session found - user must be logged in');
                throw new Error('User not authenticated - please log in');
            }
        } else {
            console.error('Supabase environment variables not configured');
            throw new Error('Authentication not configured');
        }
    } catch (error) {
        console.error('Authentication failed:', error instanceof Error ? error.message : 'Unknown error');
        throw error;
    }
}

export interface Group {
    id: string
    name: string
    description?: string
    created_by: string
    created_at: string
    updated_at: string
    member_count?: number
}

const API_BASE_URL = 'http://localhost:8080';

export default function GroupPanel() {
    const [groups, setGroups] = useState<Group[]>([])
    const [selectedGroup, setSelectedGroup] = useState<Group | null>(null)
    const [showCreateModal, setShowCreateModal] = useState(false)
    const [showJoinModal, setShowJoinModal] = useState(false)
    const [loading, setLoading] = useState(true)

    // Load groups from API
    useEffect(() => {
        const loadGroups = async () => {
            try {
                console.log('=== INTENSIVE DEBUGGING: GroupPanel loadGroups START ===');
                console.log('GroupPanel: Loading groups from API...');
                
                const authHeaders = await getAuthHeaders();
                
                const response = await fetch(`${API_BASE_URL}/api/groups`, {
                    method: 'GET',
                    headers: authHeaders,
                });
                
                console.log('DEBUGGING: API response status:', response.status);
                console.log('DEBUGGING: API response headers:', Object.fromEntries(response.headers.entries()));
                
                if (!response.ok) {
                    console.error('DEBUGGING: API response not OK:', response.status, response.statusText);
                    const errorText = await response.text();
                    console.error('DEBUGGING: Error response body:', errorText);
                    throw new Error(`Failed to fetch groups: ${response.status} ${response.statusText}`);
                }
                
                const rawData = await response.json();
                console.log('DEBUGGING: Raw API response type:', typeof rawData);
                console.log('DEBUGGING: Raw API response is array:', Array.isArray(rawData));
                console.log('DEBUGGING: Raw API response length:', rawData?.length);
                console.log('DEBUGGING: Raw API response full data:', JSON.stringify(rawData, null, 2));
                
                // Detailed analysis of each group object
                if (Array.isArray(rawData)) {
                    rawData.forEach((group, index) => {
                        console.log(`DEBUGGING: Group ${index}:`, JSON.stringify(group, null, 2));
                        console.log(`DEBUGGING: Group ${index} keys:`, Object.keys(group || {}));
                        console.log(`DEBUGGING: Group ${index} has id:`, !!group?.id);
                        console.log(`DEBUGGING: Group ${index} has name:`, !!group?.name);
                        console.log(`DEBUGGING: Group ${index} id value:`, group?.id);
                        console.log(`DEBUGGING: Group ${index} name value:`, group?.name);
                    });
                }
                
                const validGroups = rawData.filter((group: any) => {
                    console.log('DEBUGGING: Group validation:', {
                        group: JSON.stringify(group),
                        hasGroup: !!group,
                        hasId: !!group?.id,
                        hasName: !!group?.name,
                        groupKeys: Object.keys(group || {}),
                    });
                    
                    const isValid = group && group.id && group.name;
                    
                    if (!isValid) {
                        console.error('GroupPanel: Invalid group found:', group);
                        console.error('GroupPanel: Invalid group type:', typeof group);
                        console.error('GroupPanel: Invalid group keys:', Object.keys(group || {}));
                    }
                    
                    return isValid;
                });
                
                console.log('DEBUGGING: Valid groups after filtering:', validGroups.length);
                console.log('DEBUGGING: Valid groups data:', JSON.stringify(validGroups, null, 2));
                
                setGroups(validGroups);
                console.log('=== INTENSIVE DEBUGGING: GroupPanel loadGroups END ===');
            } catch (error) {
                console.error('=== CRITICAL ERROR in GroupPanel loadGroups ===');
                console.error('Error type:', typeof error);
                console.error('Error message:', error instanceof Error ? error.message : 'Unknown error');
                console.error('Error stack:', error instanceof Error ? error.stack : 'No stack trace');
                console.error('Full error object:', error);
                setGroups([]);
            } finally {
                setLoading(false);
            }
        };

        loadGroups();
    }, [])

    const handleCreateGroup = (newGroup: any) => {
        console.log('New group created:', newGroup);
        if (newGroup && newGroup.id) {
            setGroups(prevGroups => [...prevGroups, newGroup]);
            setSelectedGroup(newGroup);
            console.log('Selected new group:', newGroup.id);
        } else {
            console.error('Invalid group created:', newGroup);
        }
        setShowCreateModal(false);
    };

    const handleJoinGroup = async (groupId: string) => {
        // Reload groups after joining
        try {
            const authHeaders = await getAuthHeaders();
            const response = await fetch(`${API_BASE_URL}/api/groups`, {
                headers: authHeaders
            });
            if (response.ok) {
                const userGroups = await response.json();
                setGroups(userGroups);
            }
        } catch (error) {
            console.error('Error reloading groups:', error);
        }
        setShowJoinModal(false)
    }

    const handleSelectGroup = (group: any) => {
        console.log('Selecting group:', group);
        if (group && group.id) {
            setSelectedGroup(group);
            console.log('Group selected successfully:', group.id);
        } else {
            console.error('Invalid group selected:', group);
        }
    };

    const handleBackToGroups = () => {
        setSelectedGroup(null)
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
            {!selectedGroup ? (
                <>
                    {/* Groups Overview */}
                    <div className="glass-card p-6">
                        <div className="flex items-center justify-between mb-4">
                            <div className="flex items-center space-x-2">
                                <Users className="w-5 h-5 text-ocean-600" />
                                <h3 className="text-lg font-semibold text-ocean-800">
                                    Your Groups
                                </h3>
                            </div>
                            <div className="flex space-x-2">
                                <Button
                                    onClick={() => setShowCreateModal(true)}
                                    className="glass-button-sm flex items-center space-x-2"
                                >
                                    <Plus className="w-4 h-4" />
                                    <span>Create Group</span>
                                </Button>
                                <Button
                                    onClick={() => setShowJoinModal(true)}
                                    className="glass-button-sm flex items-center space-x-2"
                                >
                                    <Users className="w-4 h-4" />
                                    <span>Join Group</span>
                                </Button>
                            </div>
                        </div>
                        
                        <GroupList 
                            groups={groups}
                            onSelectGroup={handleSelectGroup}
                        />
                    </div>
                </>
            ) : (
                <GroupView 
                    group={selectedGroup}
                    onBack={handleBackToGroups}
                />
            )}

            {/* Create Group Modal */}
            <CreateGroupModal
                isOpen={showCreateModal}
                onClose={() => setShowCreateModal(false)}
                onCreateGroup={handleCreateGroup}
            />

            {/* Join Group Modal */}
            <JoinGroupModal
                isOpen={showJoinModal}
                onClose={() => setShowJoinModal(false)}
                onJoinGroup={handleJoinGroup}
            />
        </div>
    )
}
