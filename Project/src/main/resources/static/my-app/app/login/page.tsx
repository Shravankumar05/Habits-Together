'use client'

import { useState } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Lock, Mail, Activity, AlertTriangle, ArrowRight } from 'lucide-react'

export default function Login() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const { signIn } = useAuth()
    const router = useRouter()

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setLoading(true)
        setError('')

        const { error } = await signIn(email, password)

        if (error) {
            setError(error.message)
            setLoading(false)
        } else {
            router.push('/')
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center relative">
            <div className="w-full max-w-md px-6">
                {/* Header */}
                <div className="text-center mb-8">
                    <div className="flex items-center justify-center space-x-3 mb-6">
                        <div className="w-14 h-14 bg-gradient-to-br from-ocean-400 to-ocean-600 rounded-2xl flex items-center justify-center shadow-soft">
                            <Activity className="w-8 h-8 text-white" />
                        </div>
                        <div>
                            <h1 className="text-4xl font-bold text-gradient-blue">Habit Tracker</h1>
                            <p className="text-ocean-600">Welcome back</p>
                        </div>
                    </div>
                </div>

                {/* Login Form */}
                <div className="glass-card p-8">
                    <div className="text-center mb-8">
                        <h2 className="text-2xl font-semibold text-ocean-800 mb-2">
                            Sign In
                        </h2>
                        <p className="text-ocean-600">
                            Enter your credentials to access your habits
                        </p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-ocean-700">
                                Email Address
                            </label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-ocean-400" />
                                <Input
                                    type="email"
                                    placeholder="Enter your email..."
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                    disabled={loading}
                                    className="pl-12 glass-input"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-ocean-700">
                                Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-ocean-400" />
                                <Input
                                    type="password"
                                    placeholder="Enter your password..."
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    disabled={loading}
                                    className="pl-12 glass-input"
                                />
                            </div>
                        </div>

                        {error && (
                            <div className="glass-card border-red-200 bg-red-50/50 p-4">
                                <div className="flex items-center space-x-3">
                                    <AlertTriangle className="w-5 h-5 text-red-500" />
                                    <div>
                                        <p className="text-red-700 font-medium text-sm">Sign in failed</p>
                                        <p className="text-red-600 text-xs">{error}</p>
                                    </div>
                                </div>
                            </div>
                        )}

                        <Button 
                            type="submit" 
                            disabled={loading}
                            className="w-full glass-button flex items-center justify-center space-x-2 py-4"
                        >
                            {loading ? (
                                <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
                            ) : (
                                <ArrowRight className="w-5 h-5" />
                            )}
                            <span className="font-medium">
                                {loading ? 'Signing in...' : 'Sign In'}
                            </span>
                        </Button>
                    </form>

                    <div className="mt-8 text-center">
                        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/30 to-transparent mb-6"></div>
                        <p className="text-sm text-ocean-600">
                            Don't have an account?{' '}
                            <Link 
                                href="/sign-up" 
                                className="text-ocean-700 hover:text-ocean-800 font-medium transition-colors duration-300"
                            >
                                Sign up
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    )
}