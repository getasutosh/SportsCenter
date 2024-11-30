# SportsCenter API Design

## API Design Principles

### RESTful Endpoints
- Resource-oriented design
- Standard HTTP methods
- Proper status code usage
- Consistent URL structure
- HATEOAS links for navigation

### GraphQL Interface
- Complex data queries
- Real-time subscriptions
- Type-safe schema
- Optimized data fetching
- Batched requests

## API Layers

### 1. Authentication & Authorization
```clojure
["/api/v1/auth"
 ["/login" {:post auth/login}]
 ["/refresh" {:post auth/refresh}]
 ["/logout" {:post auth/logout}]
 ["/register" {:post auth/register}]
 ["/verify-email" {:post auth/verify-email}]
 ["/forgot-password" {:post auth/forgot-password}]
 ["/reset-password" {:post auth/reset-password}]]
```

### 2. User Management
```clojure
["/api/v1/users"
 ["" {:get users/list
      :post users/create}]
 ["/:id" {:get users/get
          :put users/update
          :delete users/delete}]
 ["/:id/profile" {:get profiles/get
                  :put profiles/update}]
 ["/:id/preferences" {:get prefs/get
                     :put prefs/update}]]
```

### 3. Athlete Management
```clojure
["/api/v1/athletes"
 ["" {:get athletes/list
      :post athletes/create}]
 ["/:id" {:get athletes/get
          :put athletes/update}]
 ["/:id/metrics" {:get metrics/list
                  :post metrics/record}]
 ["/:id/progress" {:get progress/get}]
 ["/:id/goals" {:get goals/list
                :post goals/create}]
 ["/:id/nutrition-plan" {:get nutrition/get
                        :put nutrition/update}]]
```

### 4. Coach Management
```clojure
["/api/v1/coaches"
 ["" {:get coaches/list
      :post coaches/create}]
 ["/:id" {:get coaches/get
          :put coaches/update}]
 ["/:id/availability" {:get availability/get
                      :put availability/update}]
 ["/:id/sessions" {:get sessions/list}]
 ["/:id/reviews" {:get reviews/list}]
 ["/:id/programs" {:get programs/list}]]
```

### 5. Training Sessions
```clojure
["/api/v1/sessions"
 ["" {:get sessions/list
      :post sessions/create}]
 ["/:id" {:get sessions/get
          :put sessions/update
          :delete sessions/cancel}]
 ["/:id/feedback" {:post feedback/create}]
 ["/:id/videos" {:get videos/list
                 :post videos/upload}]
 ["/:id/notes" {:get notes/list
                :post notes/create}]]
```

### 6. Video Analysis
```clojure
["/api/v1/videos"
 ["" {:get videos/list
      :post videos/upload}]
 ["/:id" {:get videos/get
          :delete videos/delete}]
 ["/:id/analysis" {:get analysis/get
                   :post analysis/create}]
 ["/:id/feedback" {:get feedback/list
                   :post feedback/create}]]
```

### 7. Payments & Subscriptions
```clojure
["/api/v1/payments"
 ["" {:get payments/list
      :post payments/create}]
 ["/:id" {:get payments/get}]
 ["/methods" {:get methods/list
              :post methods/add}]
 ["/subscriptions" {:get subs/list
                   :post subs/create}]
 ["/invoices" {:get invoices/list}]]
```

### 8. Mentor Management
```clojure
["/api/v1/mentors"
 ["" {:get mentors/list
      :post mentors/create}]
 ["/:id" {:get mentors/get
          :put mentors/update
          :delete mentors/delete}]
 ["/:id/profile" {:get profiles/get
                  :put profiles/update}]
 ["/:id/assessments" {:get assessments/list
                      :post assessments/create}]
 ["/:id/sessions" {:get sessions/list
                   :post sessions/create}]
 ["/:id/availability" {:get availability/get
                      :put availability/update}]
 ["/:id/athletes" {:get athletes/list}]
 ["/:id/feedback" {:get feedback/list
                   :post feedback/create}]]
```

## GraphQL Schema

### Core Types
```graphql
type User {
  id: ID!
  email: String!
  role: UserRole!
  profile: Profile!
  preferences: Preferences
}

type Athlete {
  id: ID!
  user: User!
  sports: [Sport!]!
  metrics: [Metric!]!
  sessions: [Session!]!
  progress: Progress!
  goals: [Goal!]!
}

type Coach {
  id: ID!
  user: User!
  specializations: [Sport!]!
  availability: [TimeSlot!]!
  sessions: [Session!]!
  reviews: [Review!]!
  rating: Float!
}

type Session {
  id: ID!
  coach: Coach!
  athlete: Athlete!
  status: SessionStatus!
  startTime: DateTime!
  endTime: DateTime!
  videos: [Video!]!
  feedback: [Feedback!]!
}

type Mentor {
  id: ID!
  user: User!
  specializations: [Specialization!]!
  experience: [Experience!]!
  achievements: [Achievement!]!
  availability: Schedule!
  rating: Float
  totalReviews: Int
  status: MentorStatus!
}

type MentorAssessment {
  id: ID!
  mentor: Mentor!
  athlete: Athlete!
  assessment: Assessment!
  developmentPlan: DevelopmentPlan
  status: AssessmentStatus!
  createdAt: DateTime!
  updatedAt: DateTime!
}

type MentorSession {
  id: ID!
  mentor: Mentor!
  athlete: Athlete!
  type: SessionType!
  status: SessionStatus!
  scheduledTime: TimeSlot!
  agenda: [AgendaItem!]
  materials: [Material!]
  notes: [Note!]
  outcomes: SessionOutcome
  feedback: SessionFeedback
}
```

### Queries
```graphql
type Query {
  me: User
  athlete(id: ID!): Athlete
  coach(id: ID!): Coach
  session(id: ID!): Session
  searchCoaches(input: CoachSearchInput!): [Coach!]!
  athleteProgress(id: ID!): Progress!
  upcomingSessions: [Session!]!
  mentor(id: ID!): Mentor
  mentors(filter: MentorFilter): [Mentor!]!
  mentorAssessments(mentorId: ID!, filter: AssessmentFilter): [MentorAssessment!]!
  mentorSessions(mentorId: ID!, filter: SessionFilter): [MentorSession!]!
}
```

### Mutations
```graphql
type Mutation {
  createSession(input: SessionInput!): Session!
  updateProfile(input: ProfileInput!): Profile!
  recordMetric(input: MetricInput!): Metric!
  uploadVideo(input: VideoInput!): Video!
  provideFeedback(input: FeedbackInput!): Feedback!
  createMentorAssessment(input: AssessmentInput!): MentorAssessment!
  updateMentorAssessment(id: ID!, input: AssessmentInput!): MentorAssessment!
  scheduleMentorSession(input: SessionInput!): MentorSession!
  updateMentorSession(id: ID!, input: SessionInput!): MentorSession!
  provideMentorFeedback(sessionId: ID!, input: FeedbackInput!): SessionFeedback!
}
```

### Subscriptions
```graphql
type Subscription {
  sessionUpdated(id: ID!): Session!
  newMessage(sessionId: ID!): Message!
  metricRecorded(athleteId: ID!): Metric!
  mentorSessionUpdated(mentorId: ID!): MentorSession!
  mentorAssessmentCreated(athleteId: ID!): MentorAssessment!
}
```

## WebSocket Events

### Real-time Communication
```javascript
// Session events
socket.on('session:started', (sessionId) => {})
socket.on('session:ended', (sessionId) => {})
socket.on('session:feedback', (feedback) => {})

// Video analysis events
socket.on('video:uploaded', (videoId) => {})
socket.on('video:analyzed', (analysis) => {})

// Messaging events
socket.on('message:new', (message) => {})
socket.on('message:updated', (message) => {})

// Performance events
socket.on('metric:recorded', (metric) => {})
socket.on('goal:achieved', (goal) => {})
```

## Response Format

### Success Response
```json
{
  "status": "success",
  "data": {
    "id": "123",
    "type": "session",
    "attributes": {
      "startTime": "2024-01-20T10:00:00Z",
      "status": "scheduled"
    },
    "relationships": {
      "coach": {"id": "456", "type": "coach"},
      "athlete": {"id": "789", "type": "athlete"}
    }
  }
}
```

### Error Response
```json
{
  "status": "error",
  "error": {
    "code": "SESSION_001",
    "message": "Session scheduling conflict",
    "details": {
      "conflictingSessionId": "abc123",
      "timeSlot": "2024-01-20T10:00:00Z"
    }
  }
}
```

## API Security

### Authentication
- JWT-based authentication
- Refresh token rotation
- Session management
- Multi-factor authentication support

### Authorization
- Role-based access control (RBAC)
- Resource-level permissions
- Scope-based access
- Parent/Guardian consent management

### Rate Limiting
```javascript
{
  "standard": {
    "window": "1m",
    "max": 60
  },
  "authenticated": {
    "window": "1m",
    "max": 120
  },
  "upload": {
    "window": "1h",
    "max": 50
  }
}
```

## API Versioning
- URL-based versioning (/api/v1/...)
- Header-based version fallback
- Deprecation notices
- Version lifecycle management

## Documentation
- OpenAPI/Swagger specs
- GraphQL schema documentation
- Interactive API explorer
- Code examples
- Postman collections

## Monitoring & Logging
- Request/Response logging
- Performance metrics
- Error tracking
- Usage analytics
- SLA monitoring
