---
status: accepted
---
# Offline-first with no backend and no login

73% of Peruvian public schools have no connectivity and the target user is the
rural primary teacher. All data lives on the device in SQLite; there is no
server, no account and no network dependency in v1. The existing Retrofit
client and auth feature are removed rather than kept dormant.

## Consequences

- Every entity uses a UUID primary key so a future sync backend can merge
  devices without id collisions.
- Backup and restore is a single file shared through the Android share sheet;
  that is the only data transfer mechanism.
- Analytics and crash reporting stay best-effort and must never block a flow.
