# WebSocket Events Specification

## Overview
This document details all real-time events in the SportsCenter platform, including their payload structures, authentication requirements, and handling strategies.

## Connection Management

### Connection Establishment
```javascript
// Connection with authentication
socket.connect({
  auth: {
    token: "JWT_TOKEN"
  }
})
```

### Heartbeat
```javascript
// Every 30 seconds
socket.emit('ping')
socket.on('pong', () => { /* Update connection status */ })
```

## Event Categories

### 1. Session Events
```javascript
// Session status updates
interface SessionEvent {
  sessionId: string;
  timestamp: string;
  type: 'started' | 'ended' | 'paused' | 'resumed';
  metadata: {
    coachId: string;
    athleteId: string;
    location?: string;
  };
}

socket.on('session:status', (event: SessionEvent) => {})

// Real-time feedback
interface FeedbackEvent {
  sessionId: string;
  timestamp: string;
  feedbackId: string;
  type: 'technique' | 'performance' | 'general';
  content: string;
  from: {
    id: string;
    role: 'coach' | 'mentor';
  };
}

socket.on('session:feedback', (event: FeedbackEvent) => {})
```

### 2. Video Analysis Events
```javascript
// Video processing status
interface VideoEvent {
  videoId: string;
  status: 'uploading' | 'processing' | 'analyzed' | 'failed';
  progress?: number;
  analysis?: {
    techniques: Array<{
      timestamp: number;
      type: string;
      confidence: number;
      feedback: string;
    }>;
  };
}

socket.on('video:status', (event: VideoEvent) => {})
socket.on('video:analysis', (event: VideoEvent) => {})
```

### 3. Performance Metrics Events
```javascript
// Real-time metric updates
interface MetricEvent {
  athleteId: string;
  timestamp: string;
  type: string;
  value: number;
  unit: string;
  source: 'manual' | 'device' | 'ai';
}

socket.on('metric:recorded', (event: MetricEvent) => {})

// Goal achievement notifications
interface GoalEvent {
  athleteId: string;
  goalId: string;
  timestamp: string;
  type: 'achieved' | 'progress';
  progress?: number;
}

socket.on('goal:update', (event: GoalEvent) => {})
```

### 4. Messaging Events
```javascript
// Direct messages
interface MessageEvent {
  messageId: string;
  timestamp: string;
  from: {
    id: string;
    role: string;
  };
  to: {
    id: string;
    role: string;
  };
  content: string;
  type: 'text' | 'image' | 'video';
}

socket.on('message:new', (event: MessageEvent) => {})
socket.on('message:updated', (event: MessageEvent) => {})
socket.on('message:deleted', (event: MessageEvent) => {})
```

### 5. Notification Events
```javascript
// System notifications
interface NotificationEvent {
  id: string;
  timestamp: string;
  type: 'alert' | 'info' | 'success';
  title: string;
  message: string;
  action?: {
    type: string;
    payload: any;
  };
}

socket.on('notification:new', (event: NotificationEvent) => {})
```

### 6. Community Events
```javascript
// Community activity
interface CommunityEvent {
  type: 'post' | 'comment' | 'reaction';
  timestamp: string;
  content: {
    id: string;
    type: string;
    data: any;
  };
  user: {
    id: string;
    role: string;
  };
}

socket.on('community:activity', (event: CommunityEvent) => {})
```

### 7. Mentor Events

#### Mentor Session Events
```clojure
;; Session status updates
{:event :mentor-session/status-changed
 :data {:session-id "uuid"
        :status "in-progress|completed|cancelled"
        :timestamp "ISO-8601"}}

;; Real-time session interaction
{:event :mentor-session/note-added
 :data {:session-id "uuid"
        :note {:content "string"
               :category "string"
               :timestamp "ISO-8601"}}}

;; Session feedback
{:event :mentor-session/feedback-submitted
 :data {:session-id "uuid"
        :feedback-type "athlete|mentor"
        :feedback {:rating 5
                  :comments "string"}}}
```

#### Mentor Assessment Events
```clojure
;; Assessment status updates
{:event :mentor-assessment/status-changed
 :data {:assessment-id "uuid"
        :status "draft|published|archived"
        :timestamp "ISO-8601"}}

;; Real-time assessment updates
{:event :mentor-assessment/metric-updated
 :data {:assessment-id "uuid"
        :metric {:name "string"
                 :value 85.5
                 :benchmark 80.0}}}

;; Development plan updates
{:event :mentor-assessment/plan-updated
 :data {:assessment-id "uuid"
        :plan {:goals ["string"]
               :action-items [{:description "string"
                             :priority "high|medium|low"}]}}}
```

#### Mentor Availability Events
```clojure
;; Availability updates
{:event :mentor/availability-changed
 :data {:mentor-id "uuid"
        :availability {:time-zone "string"
                      :schedule [{:day "string"
                                :slots [{:start "HH:mm"
                                       :end "HH:mm"}]}]}}}

;; Booking notifications
{:event :mentor/session-booked
 :data {:mentor-id "uuid"
        :session {:id "uuid"
                 :athlete-id "uuid"
                 :type "review|guidance|assessment"
                 :scheduled-time {:start "ISO-8601"
                                :end "ISO-8601"}}}}
```

### 8. Analytics Events

#### Performance Analytics
```clojure
;; Real-time performance metrics
{:event :analytics/performance-metric
 :data {:athlete-id "uuid"
        :metric-type "speed|strength|endurance|technique|accuracy"
        :value 85.5
        :unit "string"
        :context {:session-id "uuid"
                 :exercise-id "uuid"
                 :equipment-id "uuid"}
        :timestamp "ISO-8601"}}

;; Trend analysis updates
{:event :analytics/trend-update
 :data {:athlete-id "uuid"
        :metric-type "string"
        :period "daily|weekly|monthly"
        :trend {:current-value 85.5
                :change-percentage 5.2
                :direction "up|down|stable"
                :confidence-score 0.95}
        :timestamp "ISO-8601"}}

;; Performance milestones
{:event :analytics/milestone-achieved
 :data {:athlete-id "uuid"
        :milestone-type "personal-best|goal-reached|streak|level-up"
        :details {:metric "string"
                 :value 90.0
                 :previous-best 85.0}
        :achievements ["achievement-id"]
        :timestamp "ISO-8601"}}
```

#### Engagement Analytics
```clojure
;; Session engagement metrics
{:event :analytics/session-engagement
 :data {:session-id "uuid"
        :user-id "uuid"
        :metrics {:duration 3600
                 :interaction-count 45
                 :focus-score 0.85
                 :completion-rate 0.95}
        :breakdown {:exercise-time 2400
                   :rest-time 600
                   :feedback-time 600}
        :timestamp "ISO-8601"}}

;; Platform usage analytics
{:event :analytics/platform-usage
 :data {:user-id "uuid"
        :user-type "athlete|coach|mentor|parent"
        :activity-type "login|feature-use|content-access"
        :context {:feature-id "string"
                 :content-type "video|article|assessment"
                 :duration 300}
        :device-info {:type "mobile|web|tablet"
                     :os "string"
                     :app-version "string"}
        :timestamp "ISO-8601"}}

;; Content engagement
{:event :analytics/content-engagement
 :data {:content-id "uuid"
        :user-id "uuid"
        :engagement-type "view|complete|share|save"
        :metrics {:view-duration 240
                 :completion-percentage 85
                 :interaction-count 12}
        :feedback {:helpful true
                  :difficulty "medium"}
        :timestamp "ISO-8601"}}
```

#### Social Analytics
```clojure
;; Community engagement
{:event :analytics/community-engagement
 :data {:user-id "uuid"
        :activity-type "post|comment|like|share"
        :content {:id "uuid"
                 :type "discussion|achievement|milestone"
                 :engagement-score 75.5}
        :reach {:views 150
               :interactions 25
               :shares 10}
        :timestamp "ISO-8601"}}

;; Collaboration metrics
{:event :analytics/collaboration-metrics
 :data {:session-id "uuid"
        :participants ["uuid"]
        :type "team-training|group-session|mentor-meeting"
        :metrics {:participant-count 5
                 :interaction-density 0.85
                 :collaboration-score 90.0}
        :feedback-summary {:satisfaction-score 4.5
                         :engagement-level "high"}
        :timestamp "ISO-8601"}}
```

#### Business Analytics
```clojure
;; Subscription analytics
{:event :analytics/subscription-metric
 :data {:metric-type "new|renewal|churn|upgrade"
        :plan-type "basic|premium|team|enterprise"
        :user-segment "athlete|coach|organization"
        :value 299.99
        :context {:previous-plan "string"
                 :subscription-length 12
                 :discount-applied 0.1}
        :timestamp "ISO-8601"}}

;; Feature usage analytics
{:event :analytics/feature-usage
 :data {:feature-id "string"
        :user-segment "athlete|coach|mentor|parent"
        :usage-metrics {:access-count 125
                       :average-duration 300
                       :success-rate 0.92}
        :user-feedback {:satisfaction 4.5
                       :difficulty 2.0
                       :usefulness 4.8}
        :timestamp "ISO-8601"}}
```

#### System Analytics
```clojure
;; Performance monitoring
{:event :analytics/system-performance
 :data {:component "api|websocket|database|cache"
        :metrics {:response-time 150
                 :error-rate 0.001
                 :throughput 1000
                 :concurrent-users 500}
        :resource-usage {:cpu 45.5
                        :memory 75.2
                        :network-io 500.0}
        :timestamp "ISO-8601"}}

;; Error analytics
{:event :analytics/error-event
 :data {:error-id "uuid"
        :type "api|client|system"
        :severity "low|medium|high|critical"
        :context {:user-id "uuid"
                 :session-id "uuid"
                 :request-id "uuid"
                 :stack-trace "string"}
        :impact {:users-affected 10
                :services-affected ["string"]
                :duration 300}
        :timestamp "ISO-8601"}}
```

### 9. Cache Invalidation Events

#### Entity-Based Invalidation
```clojure
;; Single entity invalidation
{:event :cache/entity-invalidated
 :data {:entity-type "athlete|coach|mentor|session|assessment"
        :entity-id "uuid"
        :invalidation-type "update|delete"
        :cache-keys ["athlete:profile:123"
                    "athlete:metrics:123"]
        :timestamp "ISO-8601"
        :version 1}}

;; Bulk entity invalidation
{:event :cache/bulk-entities-invalidated
 :data {:entity-type "string"
        :entity-ids ["uuid"]
        :pattern "athlete:*:123"
        :reason "bulk-update|data-migration|version-change"
        :priority "high|normal|low"
        :timestamp "ISO-8601"}}

;; Relationship invalidation
{:event :cache/relationship-invalidated
 :data {:primary-entity {:type "string"
                        :id "uuid"}
        :related-entities [{:type "string"
                           :id "uuid"
                           :relationship "parent|child|peer"}]
        :cascade true
        :timestamp "ISO-8601"}}
```

#### Pattern-Based Invalidation
```clojure
;; Pattern invalidation
{:event :cache/pattern-invalidated
 :data {:pattern "athlete:metrics:*"
        :scope "global|regional|user"
        :reason "data-update|configuration-change|deployment"
        :affected-regions ["us-east", "eu-west"]
        :timestamp "ISO-8601"}}

;; Tag-based invalidation
{:event :cache/tags-invalidated
 :data {:tags ["training-program:v2"
               "workout:strength"
               "user-preferences"]
        :scope "global|regional|user"
        :priority "high|normal|low"
        :timestamp "ISO-8601"}}
```

#### Time-Based Invalidation
```clojure
;; Time-window invalidation
{:event :cache/time-window-invalidated
 :data {:start-time "ISO-8601"
        :end-time "ISO-8601"
        :affected-types ["metrics", "sessions", "analytics"]
        :reason "data-correction|timezone-update"
        :timestamp "ISO-8601"}}

;; Schedule-based invalidation
{:event :cache/schedule-invalidation
 :data {:schedule "0 0 * * *" ; cron format
        :patterns ["daily-metrics:*"
                  "leaderboard:*"]
        :timezone "UTC"
        :next-execution "ISO-8601"
        :timestamp "ISO-8601"}}
```

#### Real-Time Data Synchronization
```clojure
;; Real-time metrics sync
{:event :cache/realtime-metrics-sync
 :data {:metric-type "performance|engagement|system"
        :entities-affected ["uuid"]
        :update-type "increment|set|compute"
        :value {:current 85.5
                :previous 82.3}
        :timestamp "ISO-8601"}}

;; Live session cache sync
{:event :cache/live-session-sync
 :data {:session-id "uuid"
        :participant-ids ["uuid"]
        :updates [{:key "session:progress"
                  :value 75
                  :ttl 300}
                 {:key "session:participants"
                  :value ["uuid"]
                  :ttl 300}]
        :timestamp "ISO-8601"}}
```

#### Cache Consistency Events
```clojure
;; Version vector update
{:event :cache/version-vector-update
 :data {:node-id "string"
        :vector {:node1 5
                 :node2 3
                 :node3 7}
        :entity-type "string"
        :timestamp "ISO-8601"}}

;; Conflict resolution
{:event :cache/conflict-detected
 :data {:entity-id "uuid"
        :cache-keys ["string"]
        :versions [{:node "string"
                   :version 5
                   :timestamp "ISO-8601"}]
        :resolution-strategy "lww|custom|manual"
        :timestamp "ISO-8601"}}

;; Cache replication status
{:event :cache/replication-status
 :data {:source-region "string"
        :target-regions ["string"]
        :status "in-progress|completed|failed"
        :metrics {:latency 50
                 :items-processed 1000
                 :errors 0}
        :timestamp "ISO-8601"}}
```

#### Cache Management Events
```clojure
;; Memory pressure handling
{:event :cache/memory-pressure
 :data {:node-id "string"
        :memory-usage 85.5
        :eviction-strategy "lru|ttl|custom"
        :priority-keys ["string"]
        :protected-patterns ["critical:*"]
        :timestamp "ISO-8601"}}

;; Cache warming
{:event :cache/warming-event
 :data {:patterns ["string"]
        :priority "high|normal|low"
        :strategy "preload|lazy|predictive"
        :progress {:total 1000
                  :processed 750
                  :errors 0}
        :timestamp "ISO-8601"}}

;; Health check
{:event :cache/health-status
 :data {:node-id "string"
        :status "healthy|degraded|unhealthy"
        :metrics {:memory-usage 75.5
                 :cpu-usage 65.2
                 :network-saturation 45.8}
        :rate-limit-store {:type "redis|memory"
                          :status "connected"
                          :latency 5}}}
```

## Rate Limiting and Connection Management

### Connection Rate Limiting
```clojure
;; Connection limit notification
{:event :ratelimit/connection-limit
 :data {:user-id "uuid"
        :connection-count 5
        :max-connections 10
        :remaining 5
        :reset-time "ISO-8601"
        :policy {:type "sliding-window"
                :window-size 3600}}}

;; Connection throttling
{:event :ratelimit/connection-throttle
 :data {:node-id "string"
        :current-load 85.5
        :max-load 95.0
        :throttle-policy "reject|queue|degrade"
        :duration 300
        :affected-user-types ["free", "basic"]}}
```

### Message Rate Limiting
```clojure
;; Message quota update
{:event :ratelimit/message-quota
 :data {:user-id "uuid"
        :quota {:messages-sent 150
                :messages-limit 200
                :reset-time "ISO-8601"}
        :type "user|session|ip"
        :window "minute|hour|day"}}

;; Burst control
{:event :ratelimit/burst-control
 :data {:user-id "uuid"
        :burst {:current-rate 50
                :max-rate 100
                :window-ms 1000}
        :action "delay|drop|throttle"
        :backoff {:initial 100
                 :max 5000
                 :multiplier 1.5}}}
```

### Resource-Based Limits
```clojure
;; Bandwidth limit
{:event :ratelimit/bandwidth-limit
 :data {:user-id "uuid"
        :bandwidth {:current 1048576  ; bytes
                   :limit 5242880
                   :window "hour"}
        :type "upload|download|total"
        :action "throttle|block"}}

;; Concurrent operations limit
{:event :ratelimit/concurrent-ops
 :data {:user-id "uuid"
        :operation-type "video-stream|file-transfer|bulk-operation"
        :current 3
        :max 5
        :queue-position 0
        :estimated-wait 30}}
```

### Dynamic Rate Limiting
```clojure
;; Adaptive rate limit
{:event :ratelimit/adaptive-limit
 :data {:user-id "uuid"
        :metrics {:error-rate 0.05
                 :latency 250
                 :success-rate 0.95}
        :adjustments [{:metric "requests-per-second"
                      :old-value 100
                      :new-value 80
                      :reason "error-rate-exceeded"}]
        :duration 300}}

;; Fair usage adjustment
{:event :ratelimit/fair-usage
 :data {:user-id "uuid"
        :usage-pattern {:peak-times ["09:00-17:00"]
                       :high-usage-days ["Monday", "Wednesday"]}
        :adjustments {:peak-limit 50
                     :off-peak-limit 100
                     :burst-allowance 1.5}}}
```

### Service Protection
```clojure
;; DDoS protection
{:event :ratelimit/ddos-protection
 :data {:trigger {:type "connection-flood|message-flood|pattern-abuse"
                 :threshold 1000
                 :window 60}
        :action "block|challenge|monitor"
        :scope "ip|subnet|global"
        :duration 3600}}

;; Circuit breaker
{:event :ratelimit/circuit-breaker
 :data {:service "realtime-metrics|video-streaming|chat"
        :state "open|half-open|closed"
        :metrics {:error-rate 0.25
                 :latency 500
                 :timeout-rate 0.1}
        :recovery {:attempts 3
                  :backoff 1000
                  :reset-time "ISO-8601"}}}
```

### User Tier Limits
```clojure
;; Tier-based limits
{:event :ratelimit/tier-limits
 :data {:user-id "uuid"
        :tier "free|premium|enterprise"
        :limits {:connections 5
                :messages-per-minute 60
                :bandwidth 1048576
                :features ["chat", "metrics"]}
        :upgrade-path {:next-tier "premium"
                      :benefits ["increased-limits", "priority-support"]}}}

;; Group rate sharing
{:event :ratelimit/group-share
 :data {:group-id "uuid"
        :members ["uuid"]
        :shared-quota {:total 1000
                      :used 750
                      :remaining 250}
        :allocation {:type "equal|weighted|dynamic"
                    :weights {"user1": 0.4, "user2": 0.6}}}}
```

### Rate Limit Notifications
```clojure
;; Limit warning
{:event :ratelimit/limit-warning
 :data {:user-id "uuid"
        :type "connection|message|bandwidth"
        :usage-percent 85
        :forecast {:time-to-limit 300
                  :suggested-reduction 20}
        :mitigation-options ["reduce-frequency", "upgrade-plan"]}}

;; Violation notification
{:event :ratelimit/violation
 :data {:user-id "uuid"
        :violation {:type "exceeded-limit|abuse-detected"
                   :details "Too many connection attempts"
                   :timestamp "ISO-8601"}
        :consequences {:action "temporary-block|rate-reduce"
                      :duration 300
                      :appeal-process "support-ticket"}}}
```

### Monitoring and Reporting
```clojure
;; Rate limit metrics
{:event :ratelimit/metrics
 :data {:timestamp "ISO-8601"
        :global {:total-connections 1000
                :message-rate 5000
                :bandwidth-usage 10485760}
        :per-user {:p95-usage 85
                  :p99-usage 95}
        :violations {:count 50
                    :top-offenders ["uuid"]}}}

;; Health check
{:event :ratelimit/health
 :data {:node-id "string"
        :status "healthy|degraded|critical"
        :metrics {:memory-usage 75.5
                 :cpu-usage 65.2
                 :network-saturation 45.8}
        :rate-limit-store {:type "redis|memory"
                          :status "connected"
                          :latency 5}}}
```

## Rate Limiting

### Connection Limits
```javascript
{
  global: {
    connections: 10000,
    eventsPerSecond: 1000
  },
  perUser: {
    connections: 3,
    eventsPerMinute: 60
  },
  perSession: {
    eventsPerSecond: 10
  }
}
```

### Backoff Strategy
```javascript
{
  initial: 1000,  // 1 second
  max: 30000,     // 30 seconds
  multiplier: 1.5
}
```

## Error Handling

### Error Events
```javascript
interface ErrorEvent {
  code: string;
  message: string;
  details?: any;
}

socket.on('error', (error: ErrorEvent) => {
  switch(error.code) {
    case 'AUTH_FAILED':
      // Handle authentication failure
      break;
    case 'RATE_LIMITED':
      // Handle rate limiting
      break;
    case 'CONNECTION_ERROR':
      // Handle connection issues
      break;
  }
})
```

## Security Measures

### Authentication
- JWT token validation on connection
- Token refresh handling
- Connection termination on auth failure

### Authorization
```javascript
// Role-based event access
const eventAccess = {
  'session:status': ['coach', 'athlete', 'parent'],
  'video:analysis': ['coach', 'athlete', 'mentor'],
  'metric:recorded': ['coach', 'athlete', 'parent'],
  'community:activity': ['all']
}
```

### Data Validation
- Payload schema validation
- Input sanitization
- Size limits on payloads

## Performance Optimization

### Connection Pooling
```javascript
{
  poolSize: 10,
  maxWaitingClients: 10,
  idleTimeoutMillis: 30000
}
```

### Event Batching
```javascript
// Batch configuration
{
  maxBatchSize: 100,
  maxWaitTime: 1000, // ms
  compressionThreshold: 1024 // bytes
}
```

## Monitoring

### Metrics Collection
```javascript
{
  connections: {
    active: number,
    peak: number,
    rejected: number
  },
  events: {
    sent: number,
    received: number,
    failed: number
  },
  latency: {
    avg: number,
    p95: number,
    p99: number
  }
}
```

### Health Checks
```javascript
socket.on('health', () => {
  return {
    status: 'healthy' | 'degraded' | 'unhealthy',
    timestamp: string,
    metrics: {
      memory: number,
      cpu: number,
      connections: number
    }
  }
})
```
