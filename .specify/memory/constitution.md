<!--
Sync Impact Report
- Version change: 1.1.0 → 1.1.0; this adjustment remains part of the same review.
- Modified principles: I, II, III, and VII; principles IV–VI are preserved.
- Modified sections: Delivery Workflow and Governance clarify confirmation and approval authority.
- Removed sections: none.
- Follow-up TODOs: none. The previously approved expected-level range 1–4 is unchanged.
-->

# NextRole Constitution

## Core Principles

### I. Preserve the Modular Architecture
NextRole MUST preserve its modular monolith and the Java, Spring Boot, React,
PostgreSQL, and Flyway technology stack. Frontend, backend, and persistence
MUST retain clear responsibilities and communicate through defined interfaces.
Any material architectural change MUST include its rationale and receive prior
approval from any project contributor. Routine compatible implementation,
refactoring, and bug fixes do not require additional approval solely because
they affect existing code.

### II. Approved Requirements Are the Source of Truth
Approved functional specifications MUST take precedence over earlier
implementations, mocks, and outdated documentation. Contributors MUST label
project statements as approved decisions, proposals, superseded decisions, or
open questions. Any project contributor MAY confirm and approve modifications
to existing approved functional decisions. Contributors and coding agents MUST
NOT silently override requirements or reinterpret business rules. When a
requested change materially conflicts with an approved decision, the agent MUST
explain the conflict and likely impact and request confirmation from the
contributor initiating the change. No specific project-owner approval is
required. Once confirmed, affected specifications and related artifacts MUST
be updated consistently before implementing the changed behavior.

### III. Specify and Design Before Implementation
Every new feature and significant change MUST follow the Specification →
Clarify → Plan → Tasks → Implement workflow, applying the relevant stages to the
scope of the change. Implementation MUST NOT begin while material functional
decisions remain unclear or unconfirmed. A clear request may establish the
needed decisions without separate approval at each workflow stage. Requirements,
approved decisions, technical design, implementation tasks, and validation
evidence MUST remain traceable to one another.

### IV. Evolve V1 Through Controlled Change
Existing components MUST be reused when they satisfy approved V2 requirements.
V1 mocks, catalogs, algorithms, and database schema MUST NOT be treated as
authoritative V2 requirements. Database changes MUST use controlled Flyway
migrations. Contributors MUST NOT assume V1 demonstration data must be retained;
data retention or migration requirements MUST be established by approved
specifications.

### V. Preserve Explainability and Historical Meaning
Self-assessment, matching, and skill-gap results MUST be explainable and MUST
support the approved history and reevaluation requirements. The system MUST
preserve the information and version context needed to interpret historical
results when questions, competencies, or role requirements change. An explicit
zero level MUST remain distinguishable from an unevaluated competency.

### VI. Verify Quality and Contracts
Features MUST have verifiable acceptance criteria and appropriate automated
tests for their behavior and business rules. Frontend-backend contracts MUST be
validated when changed. Changes MUST be reviewed for regressions, and a feature
MUST NOT be declared complete without recorded validation evidence.

### VII. Maintain Consistency While Enabling Evolution
Contributors and coding agents SHOULD review relevant approved specifications,
existing implementation, interfaces, and dependencies before making significant
changes. When a requested change materially contradicts an existing approved
functional decision, agents MUST explain the contradiction and its potential
impact, and request confirmation from the contributor initiating the change.
Any project contributor MAY confirm and approve changes to existing functional
decisions. No specific project owner approval is required. Once a change is
confirmed, agents MUST update the affected specifications and related project
artifacts consistently before implementing the affected behavior. Agents MAY
autonomously make technical decisions that remain compatible with the intended
functionality and project architecture. Routine changes, refactoring, bug fixes,
and compatible improvements MUST NOT require additional approvals solely
because they affect existing code. Agents SHOULD avoid unnecessary interruptions
and clarification requests when the intended behavior is sufficiently clear and
no material contradiction exists.

## Scope and Product Context

NextRole is an academic career-guidance platform for IT students and people
seeking their first employment opportunities. Functional scope, domain rules,
catalogs, algorithms, and user experience MUST be defined in approved feature
specifications; this Constitution does not establish those product rules.

## Delivery Workflow

For each new feature or significant change, the project artifacts MUST make its
requirements and decisions traceable through the approved specification,
clarifications, plan, tasks, implementation, and validation evidence. Reviews
MUST check compliance with this Constitution and the relevant approved
specification. For significant changes, contributors and coding agents SHOULD
check consistency with approved decisions and existing implementation, and
review impact on related modules, interfaces, dependencies, and tests before
implementation. Material contradictions MUST be surfaced to the contributor who
initiated the change for confirmation; routine compatible work MUST NOT be
delayed for separate approvals. Unresolved material questions MUST be recorded
and clarified before dependent implementation proceeds. Destructive or
difficult-to-reverse operations MUST receive specific confirmation before they
are executed, including deleting data or overwriting existing work.

## Governance

This Constitution governs feature specifications, plans, tasks, implementation,
and reviews. Any project contributor MAY approve amendments to this Constitution;
no specific project-owner approval is required. Amendments MUST update this file
with the reason for change and an appropriate semantic version increment.

Versioning follows Semantic Versioning: MAJOR for incompatible governance
changes, MINOR for new principles or materially expanded obligations, and PATCH
for clarifications or non-semantic edits. Each amendment MUST update the last
amended date. Reviewers MUST verify that proposed work and its validation
evidence comply with the Constitution before completion is accepted.

**Version**: 1.1.0 | **Ratified**: 2026-11-20 | **Last Amended**: 2026-11-20
