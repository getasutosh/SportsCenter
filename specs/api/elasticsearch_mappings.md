# Elasticsearch Mappings

## Overview
This document defines the Elasticsearch index mappings for search functionality in the SportsCenter platform.

## Index Mappings

### 1. Coaches Search Index
```json
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "analysis": {
      "analyzer": {
        "sport_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "sport_synonyms"]
        }
      },
      "filter": {
        "sport_synonyms": {
          "type": "synonym",
          "synonyms": [
            "football, soccer",
            "track, athletics",
            "gym, gymnastics"
          ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "name": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "sports": {
        "type": "text",
        "analyzer": "sport_analyzer",
        "fields": {
          "raw": { "type": "keyword" }
        }
      },
      "specializations": {
        "type": "text",
        "fields": {
          "raw": { "type": "keyword" }
        }
      },
      "experience": { "type": "integer" },
      "rating": { "type": "float" },
      "location": { "type": "geo_point" },
      "availability": {
        "type": "nested",
        "properties": {
          "day": { "type": "keyword" },
          "slots": {
            "type": "nested",
            "properties": {
              "start": { "type": "date" },
              "end": { "type": "date" }
            }
          }
        }
      },
      "achievements": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "certifications": {
        "type": "keyword"
      },
      "languages": {
        "type": "keyword"
      },
      "pricing": {
        "type": "nested",
        "properties": {
          "type": { "type": "keyword" },
          "amount": { "type": "float" },
          "currency": { "type": "keyword" }
        }
      }
    }
  }
}
```

### 2. Training Programs Index
```json
{
  "settings": {
    "number_of_shards": 2,
    "number_of_replicas": 1,
    "analysis": {
      "analyzer": {
        "program_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "program_synonyms"]
        }
      },
      "filter": {
        "program_synonyms": {
          "type": "synonym",
          "synonyms": [
            "beginner, novice, starter",
            "advanced, expert, professional"
          ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "title": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "description": { "type": "text" },
      "sport": {
        "type": "text",
        "analyzer": "sport_analyzer",
        "fields": {
          "raw": { "type": "keyword" }
        }
      },
      "level": {
        "type": "keyword"
      },
      "duration": {
        "type": "integer"
      },
      "coachId": { "type": "keyword" },
      "tags": { "type": "keyword" },
      "targetAge": {
        "type": "integer_range"
      },
      "rating": { "type": "float" },
      "enrollments": { "type": "integer" },
      "price": {
        "type": "nested",
        "properties": {
          "amount": { "type": "float" },
          "currency": { "type": "keyword" }
        }
      }
    }
  }
}
```

### 3. Video Content Index
```json
{
  "settings": {
    "number_of_shards": 2,
    "number_of_replicas": 1,
    "analysis": {
      "analyzer": {
        "technique_analyzer": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "technique_synonyms"]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "title": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "description": { "type": "text" },
      "sport": {
        "type": "keyword"
      },
      "techniques": {
        "type": "nested",
        "properties": {
          "name": { "type": "text", "analyzer": "technique_analyzer" },
          "timestamp": { "type": "float" },
          "confidence": { "type": "float" }
        }
      },
      "duration": { "type": "float" },
      "uploadDate": { "type": "date" },
      "coachId": { "type": "keyword" },
      "athleteId": { "type": "keyword" },
      "sessionId": { "type": "keyword" },
      "tags": { "type": "keyword" },
      "visibility": { "type": "keyword" }
    }
  }
}
```

### 4. Community Content Index
```json
{
  "settings": {
    "number_of_shards": 2,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "id": { "type": "keyword" },
      "type": { "type": "keyword" },
      "title": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword" }
        }
      },
      "content": { "type": "text" },
      "authorId": { "type": "keyword" },
      "sport": { "type": "keyword" },
      "tags": { "type": "keyword" },
      "createdAt": { "type": "date" },
      "engagement": {
        "properties": {
          "likes": { "type": "integer" },
          "comments": { "type": "integer" },
          "shares": { "type": "integer" }
        }
      },
      "visibility": { "type": "keyword" }
    }
  }
}
```

## Search Templates

### 1. Coach Search
```json
{
  "script": {
    "lang": "mustache",
    "source": {
      "query": {
        "bool": {
          "must": [
            {
              "multi_match": {
                "query": "{{query}}",
                "fields": ["name^3", "sports^2", "specializations"]
              }
            }
          ],
          "filter": [
            {
              "geo_distance": {
                "distance": "{{distance}}km",
                "location": {
                  "lat": "{{lat}}",
                  "lon": "{{lon}}"
                }
              }
            },
            {
              "range": {
                "rating": {
                  "gte": "{{min_rating}}"
                }
              }
            }
          ]
        }
      },
      "sort": [
        {
          "_geo_distance": {
            "location": {
              "lat": "{{lat}}",
              "lon": "{{lon}}"
            },
            "order": "asc",
            "unit": "km"
          }
        },
        { "rating": "desc" }
      ]
    }
  }
}
```

### 2. Program Search
```json
{
  "script": {
    "lang": "mustache",
    "source": {
      "query": {
        "bool": {
          "must": [
            {
              "multi_match": {
                "query": "{{query}}",
                "fields": ["title^3", "description", "sport^2"]
              }
            }
          ],
          "filter": [
            {
              "term": {
                "level": "{{level}}"
              }
            },
            {
              "range": {
                "targetAge": {
                  "gte": "{{min_age}}",
                  "lte": "{{max_age}}"
                }
              }
            }
          ]
        }
      },
      "sort": [
        { "rating": "desc" },
        { "enrollments": "desc" }
      ]
    }
  }
}
```

## Index Lifecycle Management

### Hot-Warm Architecture
```json
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_size": "50GB",
            "max_age": "30d"
          }
        }
      },
      "warm": {
        "min_age": "30d",
        "actions": {
          "shrink": {
            "number_of_shards": 1
          },
          "forcemerge": {
            "max_num_segments": 1
          }
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

## Monitoring & Maintenance

### Index Stats Monitoring
```json
{
  "index_stats": {
    "indices": ["coaches", "programs", "videos", "community"],
    "metrics": [
      "search_rate",
      "indexing_rate",
      "query_latency",
      "refresh_time"
    ],
    "thresholds": {
      "query_latency_p95": "200ms",
      "indexing_errors": 0,
      "failed_searches": 0
    }
  }
}
```

### Maintenance Schedule
```json
{
  "maintenance": {
    "forcemerge": {
      "schedule": "0 0 * * 0",
      "max_num_segments": 1
    },
    "snapshot": {
      "schedule": "0 0 * * *",
      "repository": "s3_backup",
      "retention": "30d"
    }
  }
}
```
