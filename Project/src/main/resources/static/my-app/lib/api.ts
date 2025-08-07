import { supabase } from './supabase'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'

async function getAuthHeaders() {
  const { data: { session } } = await supabase.auth.getSession()
  
  if (session?.access_token) {
    console.log('Using real Supabase token for authenticated user');
    return {
      'Authorization': `Bearer ${session.access_token}`,
      'Content-Type': 'application/json',
    };
  }
  
  console.error('No Supabase session found - user must be logged in');
  throw new Error('User not authenticated - please log in');
}

export interface Habit {
  id: string 
  name: string
  description?: string
  color: string
  userId: string
  createdAt: string
  updatedAt: string
}

export interface DailyCompletion {
  id: string 
  habitId: string
  userId: string
  completionDate: string
  completed: boolean
  notes?: string
  completedAt: string
}

// Habit API functions
export async function fetchHabits(): Promise<Habit[]> {
  const headers = await getAuthHeaders()
  const response = await fetch(`${API_BASE_URL}/habits`, { headers })
  
  if (!response.ok) {
    throw new Error('Failed to fetch habits')
  }
  
  return response.json()
}

export async function createHabit(habit: Omit<Habit, 'id' | 'createdAt' | 'updatedAt'>): Promise<Habit> {
  const headers = await getAuthHeaders()
  const response = await fetch(`${API_BASE_URL}/habits`, {
    method: 'POST',
    headers,
    body: JSON.stringify(habit),
  })
  
  if (!response.ok) {
    throw new Error('Failed to create habit')
  }
  
  return response.json()
}

export async function updateHabit(id: string, habit: Partial<Habit>): Promise<Habit> {
  const headers = await getAuthHeaders()
  const response = await fetch(`${API_BASE_URL}/habits/${id}`, {
    method: 'PUT',
    headers,
    body: JSON.stringify(habit),
  })
  
  if (!response.ok) {
    throw new Error('Failed to update habit')
  }
  
  return response.json()
}

export async function deleteHabit(id: string): Promise<void> {
  const headers = await getAuthHeaders()
  const response = await fetch(`${API_BASE_URL}/habits/${id}`, {
    method: 'DELETE',
    headers,
  })
  
  if (!response.ok) {
    throw new Error('Failed to delete habit')
  }
}

// Daily completion API functions
export async function toggleHabitCompletion(habitId: string, date: string): Promise<DailyCompletion | null> {
  const headers = await getAuthHeaders()
  const response = await fetch(`${API_BASE_URL}/daily-completions/toggle`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ habitId, date }),
  })
  
  if (!response.ok) {
    throw new Error('Failed to toggle habit completion')
  }
  
  const text = await response.text()
  return text ? JSON.parse(text) : null
}

export async function getHabitCompletionForDate(habitId: string, date: string): Promise<DailyCompletion | null> {
  try {
    console.log('getHabitCompletionForDate: Checking completion for habit:', habitId, 'date:', date);
    
    const headers = await getAuthHeaders();
    
    const response = await fetch(`${API_BASE_URL}/daily-completions/check?habitId=${habitId}&date=${date}`, {
      method: 'GET',
      headers,
    });
    
    console.log('getHabitCompletionForDate: Response status:', response.status);
    
    if (!response.ok) {
      if (response.status === 404) {
        console.log('getHabitCompletionForDate: No completion record found');
        return null; // No completion record found
      }
      console.error('getHabitCompletionForDate: Failed to check habit completion, status:', response.status);
      throw new Error('Failed to check habit completion');
    }
    
    const text = await response.text();
    const result = text ? JSON.parse(text) : null;
    console.log('getHabitCompletionForDate: Success, result:', result);
    return result;
  } catch (error) {
    console.error('getHabitCompletionForDate: Error:', error);
    throw error;
  }
}

export async function getCompletions(habitId: string, startDate: string, endDate: string): Promise<DailyCompletion[]> {
  const headers = await getAuthHeaders()
  const response = await fetch(`${API_BASE_URL}/daily-completions?habitId=${habitId}&startDate=${startDate}&endDate=${endDate}`, {
    method: 'GET',
    headers,
  })
  
  if (!response.ok) {
    throw new Error('Failed to fetch completions')
  }
  
  return response.json()
}

export async function updateCompletion(completion: Omit<DailyCompletion, 'id' | 'completedAt'>): Promise<DailyCompletion> {
  const headers = await getAuthHeaders()
  const response = await fetch(`${API_BASE_URL}/daily-completions`, {
    method: 'POST',
    headers,
    body: JSON.stringify(completion),
  })
  
  if (!response.ok) {
    throw new Error('Failed to update completion')
  }
  
  return response.json()
}

export async function checkServerHealth(): Promise<boolean> {
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000) // 5 second timeout
    
    const response = await fetch(`${API_BASE_URL}/actuator/health`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      signal: controller.signal,
    })
    
    clearTimeout(timeoutId)
    return response.ok
  } catch (error) {
    console.error('Server health check error:', error)
    return false
  }
}