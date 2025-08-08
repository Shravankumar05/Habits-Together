'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { X, Users, Copy } from 'lucide-react'

interface JoinGroupModalProps {
    isOpen: boolean
    onClose: () => void
    onJoinGroup: (groupId: string) => void
}

export default function JoinGroupModal({ isOpen, onClose, onJoinGroup }: JoinGroupModalProps) {
    const [groupId, setGroupId] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!groupId.trim()) {
            setError('Please enter a Group ID')
            return
        }

        setLoading(true)
        setError('')

        try {
            // Get authentication token from Supabase session
            const { createClient } = await import('@supabase/supabase-js');
            
            const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL;
            const supabaseKey = process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_OR_ANON_KEY;
            
            if (!supabaseUrl || !supabaseKey) {
                throw new Error('Supabase configuration missing');
            }
            
            const supabase = createClient(supabaseUrl, supabaseKey);
            const { data: { session }, error: sessionError } = await supabase.auth.getSession();
            
            if (sessionError) {
                console.error('JoinGroupModal: Session error:', sessionError);
                throw new Error('Failed to get authentication session');
            }
            
            const token = session?.access_token;
            console.log('JoinGroupModal: Token retrieved:', token ? 'SUCCESS' : 'FAILED');
            
            if (!token) {
                throw new Error('No authentication token available. Please log in again.');
            }
            
            const response = await fetch(`http://localhost:8080/api/groups/${groupId}/join`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            })

            if (response.ok) {
                onJoinGroup(groupId)
                setGroupId('')
                onClose()
            } else {
                const errorData = await response.json()
                setError(errorData.message || 'Failed to join group. Please check the Group ID.')
            }
        } catch (error) {
            console.error('Error joining group:', error)
            setError('Failed to join group. Please try again.')
        } finally {
            setLoading(false)
        }
    }

    const handleClose = () => {
        setGroupId('')
        setError('')
        onClose()
    }

    if (!isOpen) return null

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4 shadow-xl">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
                        <Users className="h-5 w-5 text-ocean-600" />
                        Join Group
                    </h2>
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={handleClose}
                        className="text-gray-400 hover:text-gray-600"
                    >
                        <X className="h-4 w-4" />
                    </Button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <Label htmlFor="groupId" className="text-sm font-medium text-gray-700">
                            Group ID
                        </Label>
                        <Input
                            id="groupId"
                            type="text"
                            value={groupId}
                            onChange={(e) => setGroupId(e.target.value)}
                            placeholder="Enter the Group ID to join"
                            className="mt-1"
                            disabled={loading}
                        />
                        <p className="text-xs text-gray-500 mt-1">
                            Ask the group creator to share their Group ID with you
                        </p>
                    </div>

                    {error && (
                        <div className="text-red-600 text-sm bg-red-50 p-2 rounded">
                            {error}
                        </div>
                    )}

                    <div className="flex gap-3 pt-4">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleClose}
                            disabled={loading}
                            className="flex-1"
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            disabled={loading || !groupId.trim()}
                            className="flex-1 bg-ocean-600 hover:bg-ocean-700"
                        >
                            {loading ? 'Joining...' : 'Join Group'}
                        </Button>
                    </div>
                </form>

                <div className="mt-6 pt-4 border-t border-gray-200">
                    <h3 className="text-sm font-medium text-gray-700 mb-2">
                        How to join a group:
                    </h3>
                    <ol className="text-xs text-gray-600 space-y-1">
                        <li>1. Get the Group ID from the group creator</li>
                        <li>2. Enter the Group ID above</li>
                        <li>3. Click "Join Group" to become a member</li>
                        <li>4. Start tracking habits together!</li>
                    </ol>
                </div>
            </div>
        </div>
    )
}
