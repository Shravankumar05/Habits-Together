-- Migration script for AI-Powered Habit Analytics tables
-- Version: V001
-- Description: Create analytics database schema and core entities

-- Create habit_analytics table
CREATE TABLE IF NOT EXISTS habit_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    habit_id UUID NOT NULL,
    success_rate DOUBLE PRECISION,
    consistency_score DOUBLE PRECISION,
    optimal_time_start TIME,
    optimal_time_end TIME,
    formation_stage VARCHAR(20) CHECK (formation_stage IN ('INITIATION', 'LEARNING', 'STABILITY', 'MASTERY')),
    habit_strength DOUBLE PRECISION,
    last_analyzed TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT fk_habit_analytics_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_habit_analytics_habit FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_habit_analytics UNIQUE (user_id, habit_id),
    CONSTRAINT check_success_rate CHECK (success_rate >= 0 AND success_rate <= 1),
    CONSTRAINT check_consistency_score CHECK (consistency_score >= 0 AND consistency_score <= 1),
    CONSTRAINT check_habit_strength CHECK (habit_strength >= 0 AND habit_strength <= 1)
);

-- Create indexes for habit_analytics
CREATE INDEX idx_habit_analytics_user_id ON habit_analytics(user_id);
CREATE INDEX idx_habit_analytics_habit_id ON habit_analytics(habit_id);
CREATE INDEX idx_habit_analytics_formation_stage ON habit_analytics(formation_stage);
CREATE INDEX idx_habit_analytics_last_analyzed ON habit_analytics(last_analyzed);
CREATE INDEX idx_habit_analytics_success_rate ON habit_analytics(success_rate);

-- Create group_metrics table
CREATE TABLE IF NOT EXISTS group_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL,
    group_streak INTEGER DEFAULT 0,
    momentum_score DOUBLE PRECISION,
    synergistic_score DOUBLE PRECISION,
    cohesion_score DOUBLE PRECISION,
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT fk_group_metrics_group FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT check_group_streak CHECK (group_streak >= 0),
    CONSTRAINT check_momentum_score CHECK (momentum_score >= 0 AND momentum_score <= 1),
    CONSTRAINT check_synergistic_score CHECK (synergistic_score >= 0 AND synergistic_score <= 1),
    CONSTRAINT check_cohesion_score CHECK (cohesion_score >= 0 AND cohesion_score <= 1)
);

-- Create indexes for group_metrics
CREATE INDEX idx_group_metrics_group_id ON group_metrics(group_id);
CREATE INDEX idx_group_metrics_calculated_at ON group_metrics(calculated_at);
CREATE INDEX idx_group_metrics_momentum_score ON group_metrics(momentum_score);
CREATE INDEX idx_group_metrics_cohesion_score ON group_metrics(cohesion_score);

-- Create smart_notifications table
CREATE TABLE IF NOT EXISTS smart_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    habit_id UUID,
    optimal_timing TIMESTAMP WITH TIME ZONE,
    message TEXT,
    motivation_level INTEGER,
    notification_type VARCHAR(30) CHECK (notification_type IN ('HABIT_REMINDER', 'MOTIVATIONAL', 'GROUP_ENCOURAGEMENT', 'STREAK_CELEBRATION', 'OPTIMAL_TIMING', 'FORMATION_MILESTONE')),
    sent_at TIMESTAMP WITH TIME ZONE,
    response VARCHAR(20) CHECK (response IN ('OPENED', 'DISMISSED', 'COMPLETED_HABIT', 'SNOOZED', 'NO_RESPONSE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT fk_smart_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_smart_notifications_habit FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE,
    CONSTRAINT check_motivation_level CHECK (motivation_level >= 1 AND motivation_level <= 10)
);

-- Create indexes for smart_notifications
CREATE INDEX idx_smart_notifications_user_id ON smart_notifications(user_id);
CREATE INDEX idx_smart_notifications_habit_id ON smart_notifications(habit_id);
CREATE INDEX idx_smart_notifications_optimal_timing ON smart_notifications(optimal_timing);
CREATE INDEX idx_smart_notifications_sent_at ON smart_notifications(sent_at);
CREATE INDEX idx_smart_notifications_type ON smart_notifications(notification_type);
CREATE INDEX idx_smart_notifications_response ON smart_notifications(response);

-- Create habit_correlations table
CREATE TABLE IF NOT EXISTS habit_correlations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    habit1_id UUID NOT NULL,
    habit2_id UUID NOT NULL,
    correlation_coefficient DOUBLE PRECISION,
    correlation_type VARCHAR(20) CHECK (correlation_type IN ('POSITIVE', 'NEGATIVE', 'NEUTRAL', 'CAUSAL', 'INVERSE_CAUSAL')),
    confidence_level DOUBLE PRECISION,
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT fk_habit_correlations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_habit_correlations_habit1 FOREIGN KEY (habit1_id) REFERENCES habits(id) ON DELETE CASCADE,
    CONSTRAINT fk_habit_correlations_habit2 FOREIGN KEY (habit2_id) REFERENCES habits(id) ON DELETE CASCADE,
    CONSTRAINT check_correlation_coefficient CHECK (correlation_coefficient >= -1 AND correlation_coefficient <= 1),
    CONSTRAINT check_confidence_level CHECK (confidence_level >= 0 AND confidence_level <= 1),
    CONSTRAINT check_different_habits CHECK (habit1_id != habit2_id),
    CONSTRAINT unique_user_habit_correlation UNIQUE (user_id, habit1_id, habit2_id)
);

-- Create indexes for habit_correlations
CREATE INDEX idx_habit_correlations_user_id ON habit_correlations(user_id);
CREATE INDEX idx_habit_correlations_habit1_id ON habit_correlations(habit1_id);
CREATE INDEX idx_habit_correlations_habit2_id ON habit_correlations(habit2_id);
CREATE INDEX idx_habit_correlations_coefficient ON habit_correlations(correlation_coefficient);
CREATE INDEX idx_habit_correlations_type ON habit_correlations(correlation_type);
CREATE INDEX idx_habit_correlations_calculated_at ON habit_correlations(calculated_at);

-- Create privacy_settings table
CREATE TABLE IF NOT EXISTS privacy_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    share_analytics BOOLEAN DEFAULT FALSE,
    share_progress BOOLEAN DEFAULT TRUE,
    visibility_level VARCHAR(20) DEFAULT 'GROUP_ONLY' CHECK (visibility_level IN ('PRIVATE', 'GROUP_ONLY', 'ANONYMOUS', 'PUBLIC')),
    anonymize_data BOOLEAN DEFAULT FALSE,
    enable_social_features BOOLEAN DEFAULT TRUE,
    share_correlations BOOLEAN DEFAULT FALSE,
    share_optimal_timing BOOLEAN DEFAULT FALSE,
    enable_smart_notifications BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT fk_privacy_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for privacy_settings
CREATE INDEX idx_privacy_settings_user_id ON privacy_settings(user_id);
CREATE INDEX idx_privacy_settings_visibility_level ON privacy_settings(visibility_level);
CREATE INDEX idx_privacy_settings_share_analytics ON privacy_settings(share_analytics);
CREATE INDEX idx_privacy_settings_enable_social ON privacy_settings(enable_social_features);

-- Add comments for documentation
COMMENT ON TABLE habit_analytics IS 'Stores AI-generated analytics and insights for individual habits';
COMMENT ON TABLE group_metrics IS 'Stores calculated metrics and dynamics for habit groups';
COMMENT ON TABLE smart_notifications IS 'Stores intelligent, context-aware notifications for users';
COMMENT ON TABLE habit_correlations IS 'Stores calculated correlations between different habits for users';
COMMENT ON TABLE privacy_settings IS 'Stores user privacy preferences for analytics and social features';

-- Add column comments for key fields
COMMENT ON COLUMN habit_analytics.success_rate IS 'Calculated success rate (0-1) for the habit';
COMMENT ON COLUMN habit_analytics.consistency_score IS 'Consistency score (0-1) based on completion patterns';
COMMENT ON COLUMN habit_analytics.formation_stage IS 'Current stage of habit formation based on behavioral analysis';
COMMENT ON COLUMN habit_analytics.habit_strength IS 'Overall habit strength score (0-1)';

COMMENT ON COLUMN group_metrics.momentum_score IS 'Group momentum score (0-1) based on collective activity';
COMMENT ON COLUMN group_metrics.synergistic_score IS 'Synergistic effect score (0-1) of group collaboration';
COMMENT ON COLUMN group_metrics.cohesion_score IS 'Group cohesion score (0-1) based on member interactions';

COMMENT ON COLUMN habit_correlations.correlation_coefficient IS 'Pearson correlation coefficient (-1 to 1) between habits';
COMMENT ON COLUMN habit_correlations.confidence_level IS 'Statistical confidence level (0-1) of the correlation';

COMMENT ON COLUMN privacy_settings.visibility_level IS 'Data visibility level: PRIVATE, GROUP_ONLY, ANONYMOUS, or PUBLIC';