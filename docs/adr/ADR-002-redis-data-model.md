# ADR-002: Redis Data Model

## Status

Accepted

## Decision

Use Redis keys in the format:

rate_limit:{algorithm}:{identifier}

Examples:

- rate_limit:fixed:user-123
- rate_limit:token:user-123

Fixed Window counters are stored as Redis Strings with TTL.

## Rationale

- Easy debugging
- Supports multiple algorithms
- Enables future Redis Cluster support
- Leverages Redis expiration mechanism