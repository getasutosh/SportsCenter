# MongoDB Schema Definitions

## Overview
This document defines the MongoDB collections and their schemas for storing semi-structured and dynamic data in the SportsCenter platform.

## Collections

### 1. Training Feedback
```javascript
{
  "collection": "training_feedback",
  "schema": {
    "bsonType": "object",
    "required": ["sessionId", "coachId", "athleteId", "content", "createdAt"],
    "properties": {
      "_id": { "bsonType": "objectId" },
      "sessionId": { "bsonType": "string" },
      "coachId": { "bsonType": "string" },
      "athleteId": { "bsonType": "string" },
      "content": {
        "bsonType": "object",
        "properties": {
          "technicalFeedback": { "bsonType": "string" },
          "performanceRating": { "bsonType": "number" },
          "areas": { "bsonType": "array", "items": { "bsonType": "string" } },
          "improvements": { "bsonType": "array", "items": { "bsonType": "string" } },
          "notes": { "bsonType": "string" }
        }
      },
      "mediaUrls": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "type": { "enum": ["image", "video"] },
            "url": { "bsonType": "string" },
            "timestamp": { "bsonType": "date" }
          }
        }
      },
      "createdAt": { "bsonType": "date" },
      "updatedAt": { "bsonType": "date" }
    }
  }
}
```

### 2. Performance Analytics
```javascript
{
  "collection": "performance_analytics",
  "schema": {
    "bsonType": "object",
    "required": ["athleteId", "type", "data", "createdAt"],
    "properties": {
      "_id": { "bsonType": "objectId" },
      "athleteId": { "bsonType": "string" },
      "type": { "enum": ["session", "weekly", "monthly"] },
      "data": {
        "bsonType": "object",
        "properties": {
          "metrics": {
            "bsonType": "array",
            "items": {
              "bsonType": "object",
              "properties": {
                "name": { "bsonType": "string" },
                "value": { "bsonType": "number" },
                "unit": { "bsonType": "string" },
                "timestamp": { "bsonType": "date" }
              }
            }
          },
          "trends": {
            "bsonType": "array",
            "items": {
              "bsonType": "object",
              "properties": {
                "metric": { "bsonType": "string" },
                "trend": { "bsonType": "number" },
                "period": { "bsonType": "string" }
              }
            }
          },
          "insights": {
            "bsonType": "array",
            "items": { "bsonType": "string" }
          }
        }
      },
      "createdAt": { "bsonType": "date" }
    }
  }
}
```

### 3. AI Insights
```javascript
{
  "collection": "ai_insights",
  "schema": {
    "bsonType": "object",
    "required": ["targetId", "type", "insights", "createdAt"],
    "properties": {
      "_id": { "bsonType": "objectId" },
      "targetId": { "bsonType": "string" },
      "targetType": { "enum": ["athlete", "session", "video"] },
      "type": { "enum": ["technique", "performance", "progress", "recommendation"] },
      "insights": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "category": { "bsonType": "string" },
            "confidence": { "bsonType": "number" },
            "description": { "bsonType": "string" },
            "recommendations": { "bsonType": "array", "items": { "bsonType": "string" } }
          }
        }
      },
      "metadata": {
        "bsonType": "object",
        "properties": {
          "model": { "bsonType": "string" },
          "version": { "bsonType": "string" },
          "parameters": { "bsonType": "object" }
        }
      },
      "createdAt": { "bsonType": "date" }
    }
  }
}
```

### 4. Community Content
```javascript
{
  "collection": "community_content",
  "schema": {
    "bsonType": "object",
    "required": ["authorId", "type", "content", "createdAt"],
    "properties": {
      "_id": { "bsonType": "objectId" },
      "authorId": { "bsonType": "string" },
      "type": { "enum": ["post", "story", "achievement"] },
      "content": {
        "bsonType": "object",
        "properties": {
          "title": { "bsonType": "string" },
          "body": { "bsonType": "string" },
          "mediaUrls": {
            "bsonType": "array",
            "items": {
              "bsonType": "object",
              "properties": {
                "type": { "enum": ["image", "video"] },
                "url": { "bsonType": "string" }
              }
            }
          },
          "tags": { "bsonType": "array", "items": { "bsonType": "string" } }
        }
      },
      "visibility": { "enum": ["public", "connections", "private"] },
      "reactions": {
        "bsonType": "object",
        "properties": {
          "likes": { "bsonType": "number" },
          "shares": { "bsonType": "number" },
          "saves": { "bsonType": "number" }
        }
      },
      "comments": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "userId": { "bsonType": "string" },
            "content": { "bsonType": "string" },
            "createdAt": { "bsonType": "date" }
          }
        }
      },
      "createdAt": { "bsonType": "date" },
      "updatedAt": { "bsonType": "date" }
    }
  }
}
```

### 5. Dynamic Training Plans
```javascript
{
  "collection": "training_plans",
  "schema": {
    "bsonType": "object",
    "required": ["coachId", "type", "content", "createdAt"],
    "properties": {
      "_id": { "bsonType": "objectId" },
      "coachId": { "bsonType": "string" },
      "type": { "enum": ["individual", "group", "template"] },
      "title": { "bsonType": "string" },
      "description": { "bsonType": "string" },
      "content": {
        "bsonType": "object",
        "properties": {
          "objectives": { "bsonType": "array", "items": { "bsonType": "string" } },
          "duration": {
            "bsonType": "object",
            "properties": {
              "value": { "bsonType": "number" },
              "unit": { "enum": ["days", "weeks", "months"] }
            }
          },
          "schedule": {
            "bsonType": "array",
            "items": {
              "bsonType": "object",
              "properties": {
                "day": { "bsonType": "number" },
                "activities": {
                  "bsonType": "array",
                  "items": {
                    "bsonType": "object",
                    "properties": {
                      "type": { "bsonType": "string" },
                      "duration": { "bsonType": "number" },
                      "intensity": { "bsonType": "string" },
                      "description": { "bsonType": "string" },
                      "resources": {
                        "bsonType": "array",
                        "items": {
                          "bsonType": "object",
                          "properties": {
                            "type": { "enum": ["video", "document", "link"] },
                            "url": { "bsonType": "string" }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "targetAudience": {
        "bsonType": "object",
        "properties": {
          "skillLevel": { "bsonType": "string" },
          "ageRange": {
            "bsonType": "object",
            "properties": {
              "min": { "bsonType": "number" },
              "max": { "bsonType": "number" }
            }
          },
          "sports": { "bsonType": "array", "items": { "bsonType": "string" } }
        }
      },
      "metadata": {
        "bsonType": "object",
        "properties": {
          "version": { "bsonType": "number" },
          "status": { "enum": ["draft", "active", "archived"] },
          "tags": { "bsonType": "array", "items": { "bsonType": "string" } }
        }
      },
      "createdAt": { "bsonType": "date" },
      "updatedAt": { "bsonType": "date" }
    }
  }
}
```

### 6. Mentor Profiles
```javascript
{
  "collection": "mentor_profiles",
  "schema": {
    "bsonType": "object",
    "required": ["userId", "specializations", "experience", "createdAt"],
    "properties": {
      "_id": { "bsonType": "objectId" },
      "userId": { "bsonType": "string" },
      "specializations": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "sport": { "bsonType": "string" },
            "level": { "bsonType": "string" },
            "yearsExperience": { "bsonType": "number" }
          }
        }
      },
      "experience": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "organization": { "bsonType": "string" },
            "role": { "bsonType": "string" },
            "startDate": { "bsonType": "date" },
            "endDate": { "bsonType": "date" },
            "description": { "bsonType": "string" }
          }
        }
      },
      "achievements": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "title": { "bsonType": "string" },
            "date": { "bsonType": "date" },
            "description": { "bsonType": "string" },
            "verificationUrl": { "bsonType": "string" }
          }
        }
      },
      "availability": {
        "bsonType": "object",
        "properties": {
          "timeZone": { "bsonType": "string" },
          "schedule": {
            "bsonType": "array",
            "items": {
              "bsonType": "object",
              "properties": {
                "day": { "bsonType": "string" },
                "slots": {
                  "bsonType": "array",
                  "items": {
                    "bsonType": "object",
                    "properties": {
                      "startTime": { "bsonType": "string" },
                      "endTime": { "bsonType": "string" }
                    }
                  }
                }
              }
            }
          }
        }
      },
      "rating": { "bsonType": "number" },
      "totalReviews": { "bsonType": "number" },
      "status": { "enum": ["active", "inactive", "suspended"] },
      "createdAt": { "bsonType": "date" },
      "updatedAt": { "bsonType": "date" }
    }
  }
}
```

### 7. Mentor Assessments
```javascript
{
  "collection": "mentor_assessments",
  "schema": {
    "bsonType": "object",
    "required": ["mentorId", "athleteId", "assessment", "createdAt"],
    "properties": {
      "_id": { "bsonType": "objectId" },
      "mentorId": { "bsonType": "string" },
      "athleteId": { "bsonType": "string" },
      "assessment": {
        "bsonType": "object",
        "properties": {
          "technicalSkills": {
            "bsonType": "array",
            "items": {
              "bsonType": "object",
              "properties": {
                "skill": { "bsonType": "string" },
                "rating": { "bsonType": "number" },
                "comments": { "bsonType": "string" }
              }
            }
          },
          "mentalPreparation": {
            "bsonType": "object",
            "properties": {
              "strengths": { "bsonType": "array", "items": { "bsonType": "string" } },
              "areasForImprovement": { "bsonType": "array", "items": { "bsonType": "string" } },
              "recommendations": { "bsonType": "string" }
            }
          },
          "performanceMetrics": {
            "bsonType": "array",
            "items": {
              "bsonType": "object",
              "properties": {
                "metric": { "bsonType": "string" },
                "value": { "bsonType": "number" },
                "benchmark": { "bsonType": "number" },
                "analysis": { "bsonType": "string" }
              }
            }
          }
        }
      },
      "developmentPlan": {
        "bsonType": "object",
        "properties": {
          "shortTermGoals": { "bsonType": "array", "items": { "bsonType": "string" } },
          "longTermGoals": { "bsonType": "array", "items": { "bsonType": "string" } },
          "actionItems": {
            "bsonType": "array",
            "items": {
              "bsonType": "object",
              "properties": {
                "description": { "bsonType": "string" },
                "timeline": { "bsonType": "string" },
                "priority": { "enum": ["high", "medium", "low"] }
              }
            }
          }
        }
      },
      "status": { "enum": ["draft", "published", "archived"] },
      "createdAt": { "bsonType": "date" },
      "updatedAt": { "bsonType": "date" }
    }
  }
}
```

### 8. Mentor Sessions
```javascript
{
  "collection": "mentor_sessions",
  "schema": {
    "bsonType": "object",
    "required": ["mentorId", "athleteId", "type", "status", "createdAt"],
    "properties": {
      "_id": { "bsonType": "objectId" },
      "mentorId": { "bsonType": "string" },
      "athleteId": { "bsonType": "string" },
      "type": { "enum": ["review", "guidance", "assessment", "goal-setting"] },
      "status": { "enum": ["scheduled", "in-progress", "completed", "cancelled"] },
      "scheduledTime": {
        "bsonType": "object",
        "properties": {
          "startTime": { "bsonType": "date" },
          "endTime": { "bsonType": "date" },
          "timeZone": { "bsonType": "string" }
        }
      },
      "agenda": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "topic": { "bsonType": "string" },
            "duration": { "bsonType": "number" },
            "notes": { "bsonType": "string" }
          }
        }
      },
      "materials": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "type": { "enum": ["video", "document", "assessment"] },
            "url": { "bsonType": "string" },
            "description": { "bsonType": "string" }
          }
        }
      },
      "notes": {
        "bsonType": "array",
        "items": {
          "bsonType": "object",
          "properties": {
            "timestamp": { "bsonType": "date" },
            "content": { "bsonType": "string" },
            "category": { "bsonType": "string" }
          }
        }
      },
      "outcomes": {
        "bsonType": "object",
        "properties": {
          "achievements": { "bsonType": "array", "items": { "bsonType": "string" } },
          "nextSteps": { "bsonType": "array", "items": { "bsonType": "string" } },
          "recommendations": { "bsonType": "string" }
        }
      },
      "feedback": {
        "bsonType": "object",
        "properties": {
          "athleteFeedback": {
            "bsonType": "object",
            "properties": {
              "rating": { "bsonType": "number" },
              "comments": { "bsonType": "string" }
            }
          },
          "mentorFeedback": {
            "bsonType": "object",
            "properties": {
              "progress": { "bsonType": "string" },
              "recommendations": { "bsonType": "string" }
            }
          }
        }
      },
      "createdAt": { "bsonType": "date" },
      "updatedAt": { "bsonType": "date" }
    }
  }
}
```

## Indexes

### Performance Optimization
```javascript
// Training Feedback
db.training_feedback.createIndex({ "sessionId": 1 })
db.training_feedback.createIndex({ "athleteId": 1, "createdAt": -1 })
db.training_feedback.createIndex({ "coachId": 1, "createdAt": -1 })

// Performance Analytics
db.performance_analytics.createIndex({ "athleteId": 1, "type": 1, "createdAt": -1 })
db.performance_analytics.createIndex({ "athleteId": 1, "data.metrics.name": 1 })

// AI Insights
db.ai_insights.createIndex({ "targetId": 1, "type": 1, "createdAt": -1 })
db.ai_insights.createIndex({ "targetType": 1, "createdAt": -1 })

// Community Content
db.community_content.createIndex({ "authorId": 1, "createdAt": -1 })
db.community_content.createIndex({ "type": 1, "visibility": 1, "createdAt": -1 })
db.community_content.createIndex({ "content.tags": 1 })

// Training Plans
db.training_plans.createIndex({ "coachId": 1, "type": 1 })
db.training_plans.createIndex({ "targetAudience.sports": 1 })
db.training_plans.createIndex({ "metadata.status": 1, "createdAt": -1 })

// Mentor Profile Indexes
db.mentor_profiles.createIndex({ "userId": 1 })
db.mentor_profiles.createIndex({ "specializations.sport": 1 })
db.mentor_profiles.createIndex({ "rating": -1 })
db.mentor_profiles.createIndex({ "status": 1 })

// Mentor Assessment Indexes
db.mentor_assessments.createIndex({ "mentorId": 1, "createdAt": -1 })
db.mentor_assessments.createIndex({ "athleteId": 1, "createdAt": -1 })
db.mentor_assessments.createIndex({ "status": 1 })

// Mentor Session Indexes
db.mentor_sessions.createIndex({ "mentorId": 1, "scheduledTime.startTime": 1 })
db.mentor_sessions.createIndex({ "athleteId": 1, "scheduledTime.startTime": 1 })
db.mentor_sessions.createIndex({ "status": 1, "scheduledTime.startTime": 1 })
```

## Data Validation Rules

### Document Validation
```javascript
// Example validation for training_feedback collection
db.runCommand({
  collMod: "training_feedback",
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["sessionId", "coachId", "athleteId", "content"],
      properties: {
        content: {
          bsonType: "object",
          required: ["technicalFeedback", "performanceRating"]
        }
      }
    }
  },
  validationLevel: "moderate",
  validationAction: "error"
})
```

## Data Migration Strategies

### Version Management
```javascript
// Collection versioning
{
  "version": 1,
  "migrations": [
    {
      "version": 1,
      "description": "Initial schema",
      "createdAt": ISODate()
    }
  ]
}
```

### Migration Scripts
```javascript
// Example migration script
const migration = {
  up: async function() {
    await db.training_feedback.updateMany(
      { performanceRating: { $exists: true } },
      { $rename: { "performanceRating": "content.performanceRating" } }
    )
  },
  down: async function() {
    await db.training_feedback.updateMany(
      { "content.performanceRating": { $exists: true } },
      { $rename: { "content.performanceRating": "performanceRating" } }
    )
  }
}
