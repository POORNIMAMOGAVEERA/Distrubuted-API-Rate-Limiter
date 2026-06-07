# ADR-001

## Status

Accepted

## Context

The rate limiter will evolve from an in-memory implementation
to a distributed Redis-backed implementation.

## Decision

Adopt a Clean Architecture approach with:

- Domain layer
- Application layer
- Infrastructure layer

Rate limiting algorithms are implemented through the
Strategy Pattern.

Storage is abstracted behind a RateLimitStore interface.

## Consequences

Positive:

- Easier testing
- Easier Redis migration
- Better extensibility

Negative:

- Slightly more initial complexity