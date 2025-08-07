'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { X, Users, Copy, Check, CheckCircle } from 'lucide-react'
import { Group } from './GroupPanel'

interface CreateGroupModalProps {
    isOpen: boolean
    onClose: () => void
    onCreateGroup: (group: Group) => void
}

const API_BASE_URL = 'http://localhost:8080';

export default function CreateGroupModal({ isOpen, onClose, onCreateGroup }: CreateGroupModalProps) {
    const [groupName, setGroupName] = useState('')
    const [groupDescription, setGroupDescription] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [createdGroup, setCreatedGroup] = useState<Group | null>(null)
    const [copied, setCopied] = useState(false)

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setLoading(true)
        setError('')
        
        console.log('=== CreateGroupModal: Making authenticated API call ===');
        console.log('Creating group:', { groupName, groupDescription });

        try {
            // Get authentication token from Supabase session
            const { createClient } = await import('@supabase/supabase-js');
            const supabase = createClient(
                process.env.NEXT_PUBLIC_SUPABASE_URL!,
                process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_OR_ANON_KEY!
            );
            
            const { data: { session } } = await supabase.auth.getSession();
            const token = session?.access_token;
            
            console.log('Token retrieved:', token ? 'SUCCESS' : 'FAILED');
            
            if (!token) {
                throw new Error('No authentication token available. Please log in again.');
            }
            
            const response = await fetch(`${API_BASE_URL}/api/groups`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ 
                    name: groupName.trim(), 
                    description: groupDescription.trim() 
                })
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error('API Error:', response.status, errorText);
                throw new Error(`Failed to create group: ${response.status}`);
            }

            const newGroup = await response.json();
            console.log('Group created successfully:', newGroup);
            
            setCreatedGroup(newGroup)
            onCreateGroup(newGroup)
            
            // Reset form
            setGroupName('')
            setGroupDescription('')
        } catch (error) {
            console.error('=== CRITICAL ERROR in CreateGroupModal handleSubmit ===');
            console.error('Error type:', typeof error);
            console.error('Error message:', error instanceof Error ? error.message : String(error));
            console.error('Error stack:', error instanceof Error ? error.stack : 'No stack trace');
            console.error('Full error object:', error);
            
            setError(error instanceof Error ? error.message : 'Failed to create group');
        } finally {
            setLoading(false)
        }
    }

    const handleCopyGroupId = async () => {
        if (createdGroup) {
            try {
                await navigator.clipboard.writeText(createdGroup.id)
                setCopied(true)
                setTimeout(() => setCopied(false), 2000)
            } catch (error) {
                console.error('Failed to copy group ID:', error)
            }
        }
    }

    const handleClose = () => {
        setGroupName('')
        setGroupDescription('')
        setError('')
        setCreatedGroup(null)
        setCopied(false)
        onClose()
    }

    if (!isOpen) return null

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4 shadow-xl">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
                        <Users className="h-5 w-5 text-ocean-600" />
                        {createdGroup ? 'Group Created!' : 'Create New Group'}
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

                {!createdGroup ? (
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label htmlFor="groupName" className="block text-sm font-medium text-gray-700 mb-1">
                                Group Name *
                            </label>
                            <Input
                                id="groupName"
                                type="text"
                                value={groupName}
                                onChange={(e) => setGroupName(e.target.value)}
                                placeholder="e.g., Medication Buddies, Health Accountability"
                                disabled={loading}
                                className="w-full"
                            />
                        </div>

                        <div>
                            <label htmlFor="groupDescription" className="block text-sm font-medium text-gray-700 mb-1">
                                Description (Optional)
                            </label>
                            <Textarea
                                id="groupDescription"
                                value={groupDescription}
                                onChange={(e) => setGroupDescription(e.target.value)}
                                placeholder="Describe the purpose of this group..."
                                disabled={loading}
                                rows={3}
                                className="w-full"
                            />
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
                                disabled={loading || !groupName.trim()}
                                className="flex-1 bg-ocean-600 hover:bg-ocean-700"
                            >
                                {loading ? 'Creating...' : 'Create Group'}
                            </Button>
                        </div>
                    </form>
                ) : (
                    <div className="space-y-4">
                        {createdGroup && (
                            <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded-lg">
                                <div className="flex items-center space-x-2 mb-3">
                                    <CheckCircle className="w-5 h-5 text-green-600" />
                                    <h4 className="font-semibold text-green-800">Group Created Successfully!</h4>
                                </div>
                                
                                <div className="space-y-3">
                                    <div>
                                        <p className="text-sm text-green-700 mb-2">
                                            <strong>Group Name:</strong> {createdGroup.name}
                                        </p>
                                        <p className="text-sm text-green-700 mb-3">
                                            Share this Group ID with others so they can join:
                                        </p>
                                    </div>
                                    
                                    <div className="flex items-center space-x-2 p-3 bg-white border border-green-300 rounded-lg">
                                        <code className="flex-1 text-sm font-mono text-gray-800 bg-gray-50 px-2 py-1 rounded">
                                            {createdGroup.id}
                                        </code>
                                        <Button
                                            onClick={handleCopyGroupId}
                                            variant="outline"
                                            size="sm"
                                            className="flex items-center space-x-1"
                                        >
                                            {copied ? (
                                                <>
                                                    <CheckCircle className="w-4 h-4 text-green-600" />
                                                    <span className="text-green-600">Copied!</span>
                                                </>
                                            ) : (
                                                <>
                                                    <Copy className="w-4 h-4" />
                                                    <span>Copy</span>
                                                </>
                                            )}
                                        </Button>
                                    </div>
                                    
                                    <div className="text-xs text-green-600 space-y-1">
                                        <p>• Others can join by clicking "Join Group" and entering this ID</p>
                                        <p>• You can also share this ID via text, email, or any messaging app</p>
                                        <p>• Keep this ID safe - anyone with it can join your group</p>
                                    </div>
                                </div>
                            </div>
                        )}
                        
                        <Button
                            onClick={handleClose}
                            className="w-full bg-ocean-600 hover:bg-ocean-700"
                        >
                            Done
                        </Button>
                    </div>
                )}
            </div>
        </div>
    )
}
