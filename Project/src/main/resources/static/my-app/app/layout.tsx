import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { AuthProvider } from '@/contexts/AuthContext'

const inter = Inter({ 
  subsets: ['latin'],
  weight: ['300', '400', '500', '600', '700'],
  variable: '--font-inter'
})

export const metadata: Metadata = {
  title: 'Habit Tracker | Build Better Routines',
  description: 'A beautiful, modern habit tracking app to help you build better daily routines',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={`${inter.variable} font-sans antialiased`}>
        {/* Floating background orbs */}
        <div className="fixed inset-0 overflow-hidden pointer-events-none">
          <div className="floating-orb w-72 h-72 bg-gradient-to-r from-ocean-200/30 to-cream-200/30 top-10 -left-20"></div>
          <div className="floating-orb w-96 h-96 bg-gradient-to-r from-cream-300/20 to-ocean-300/20 top-1/2 -right-32"></div>
          <div className="floating-orb w-64 h-64 bg-gradient-to-r from-ocean-400/20 to-cream-400/20 bottom-10 left-1/4"></div>
        </div>
        <AuthProvider>
          {children}
        </AuthProvider>
      </body>
    </html>
  )
}