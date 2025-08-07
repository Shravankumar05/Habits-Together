'use client'

import { Button } from '@/components/ui/button'
import { Users, ChevronRight, Calendar } from 'lucide-react'
import { Group } from './GroupPanel'

interface GroupListProps {
    groups: Group[]
    onSelectGroup: (group: Group) => void
}

export default function GroupList({ groups, onSelectGroup }: GroupListProps) {
    if (groups.length === 0) {
        return (
            <div className="text-center py-8">
                <Users className="w-12 h-12 text-ocean-300 mx-auto mb-4" />
                <h3 className="text-lg font-medium text-ocean-600 mb-2">
                    No groups yet
                </h3>
                <p className="text-ocean-500 mb-4">
                    Create your first group to start building habits together
                </p>
            </div>
        )
    }

    return (
        <div className="space-y-3">
            {groups.filter(group => group && group.id && group.name).map((group) => (
                <div
                    key={group.id}
                    onClick={() => {
                        console.log('GroupList: Clicking group:', group);
                        if (group && group.id) {
                            onSelectGroup(group);
                        } else {
                            console.error('GroupList: Invalid group clicked:', group);
                        }
                    }}
                    className="glass-card p-4 cursor-pointer hover:bg-white/20 transition-all duration-200 group"
                >
                    <div className="flex items-center justify-between">
                        <div className="flex-1">
                            <div className="flex items-center space-x-3 mb-2">
                                <div className="w-8 h-8 bg-gradient-to-br from-purple-400 to-purple-600 rounded-lg flex items-center justify-center">
                                    <Users className="w-4 h-4 text-white" />
                                </div>
                                <div>
                                    <h4 className="font-semibold text-ocean-800 group-hover:text-ocean-900">
                                        {group.name}
                                    </h4>
                                    {group.description && (
                                        <p className="text-sm text-ocean-600">
                                            {group.description}
                                        </p>
                                    )}
                                </div>
                            </div>
                            
                            <div className="flex items-center space-x-4 text-xs text-ocean-500">
                                <div className="flex items-center space-x-1">
                                    <Users className="w-3 h-3" />
                                    <span>{group.member_count || 0} members</span>
                                </div>
                                <div className="flex items-center space-x-1">
                                    <Calendar className="w-3 h-3" />
                                    <span>
                                        Created {new Date(group.created_at).toLocaleDateString()}
                                    </span>
                                </div>
                            </div>
                        </div>
                        
                        <ChevronRight className="w-5 h-5 text-ocean-400 group-hover:text-ocean-600 transition-colors" />
                    </div>
                </div>
            ))}
        </div>
    )
}
