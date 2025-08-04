'use client'

import { useState } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Lock, Mail, Activity, AlertTriangle, CheckCircle, UserPlus } from 'lucide-react'

export default function Signup() {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [confirmPassword, setConfirmPassword] = useState('')
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState(false)
    const { signUp } = useAuth()

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setLoading(true)
        setError('')

        if (password !== confirmPassword) {
            setError('Passwords do not match')
            setLoading(false)
            return
        }

        const { error } = await signUp(email, password)

        if (error) {
            setError(error.message)
            setLoading(false)
        } else {
            setSuccess(true)
            setLoading(false)
        }
    }

    if (success) {
        return (
            <div className="min-h-screen flex items-center justify-center relative">
                <div className="w-full max-w-md px-6">
                    <div className="glass-card p-8 text-center">
                        <div className="w-16 h-16 bg-gradient-to-br from-green-400 to-green-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-soft">
                            <CheckCircle className="w-10 h-10 text-white" />
                        </div>
                        <h2 className="text-2xl font-bold text-ocean-800 mb-4">
                            Check Your Email
                        </h2>
                        <p className="text-ocean-600 text-sm mb-8">
                            We've sent you a confirmation link. Please check your email and click the link to activate your account.
                        </p>
                        <Link href="/login">
                            <Button className="glass-button w-full py-3">
                                Back to Sign In
                            </Button>
                        </Link>
                    </div>
                </div>
            </div>
        )
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
                            <p className="text-ocean-600">Create your account</p>
                        </div>
                    </div>
                </div>

                {/* Registration Form */}
                <div className="glass-card p-8">
                    <div className="text-center mb-8">
                        <h2 className="text-2xl font-semibold text-ocean-800 mb-2">
                            Sign Up
                        </h2>
                        <p className="text-ocean-600">
                            Create an account to start tracking your habits
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
                                    placeholder="Create a password..."
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                    disabled={loading}
                                    minLength={6}
                                    className="pl-12 glass-input"
                                />
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-ocean-700">
                                Confirm Password
                            </label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-ocean-400" />
                                <Input
                                    type="password"
                                    placeholder="Confirm your password..."
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                    disabled={loading}
                                    minLength={6}
                                    className="pl-12 glass-input"
                                />
                            </div>
                        </div>

                        {error && (
                            <div className="glass-card border-red-200 bg-red-50/50 p-4">
                                <div className="flex items-center space-x-3">
                                    <AlertTriangle className="w-5 h-5 text-red-500" />
                                    <div>
                                        <p className="text-red-700 font-medium text-sm">Registration failed</p>
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
                                <UserPlus className="w-5 h-5" />
                            )}
                            <span className="font-medium">
                                {loading ? 'Creating account...' : 'Create Account'}
                            </span>
                        </Button>
                    </form>

                    <div className="mt-8 text-center">
                        <div className="w-full h-px bg-gradient-to-r from-transparent via-white/30 to-transparent mb-6"></div>
                        <p className="text-sm text-ocean-600">
                            Already have an account?{' '}
                            <Link 
                                href="/login" 
                                className="text-ocean-700 hover:text-ocean-800 font-medium transition-colors duration-300"
                            >
                                Sign in
                            </Link>
                        </p>
                    </div>
                </div>
            </div>
        </div>
    )
}