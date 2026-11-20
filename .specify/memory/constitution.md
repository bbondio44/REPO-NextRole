<!--
Sync Impact Report
- Version change: unversioned template → 1.0.0
- Modified principles: none; this establishes six initial principles.
- Added sections: Scope and Product Context; Delivery Workflow; Governance.
- Removed sections: unresolved template placeholder sections.
- Follow-up TODOs: none.
-->

# NextRole Constitution

## Core Principles

### I. Preserve the Modular Architecture
NextRole MUST preserve its modular monolith and the Java, Spring Boot, React,
PostgreSQL, and Flyway technology stack. Frontend, backend, and persistence
MUST retain clear responsibilities and communicate through defined interfaces.
Any material architectural change MUST include its rationale and receive prior
project approval.

### II. Approved Requirements Are the Source of Truth
Approved functional specifications MUST take precedence over earlier
implementations, mocks, and outdated documentation. Contributors MUST label
project statements as approved decisions, proposals, superseded decisions, or
open questions. They MUST NOT invent business rules or resolve material
conflicts between documents unilaterally. A material ambiguity MUST be clarified
with the project owner before implementation.

### III. Specify and Design Before Implementation
Every new feature and significant change MUST follow Specification → Clarify →
Plan → Tasks → Implement. Implementation MUST NOT begin until its specification
is sufficiently clear and reviewed. Requirements, approved decisions, technical
design, implementation tasks, and validation evidence MUST remain traceable to
one another.

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
specification. Unresolved material questions MUST be recorded and clarified
before dependent implementation proceeds.

## Governance

This Constitution governs feature specifications, plans, tasks, implementation,
and reviews. Amendments require project-owner approval and MUST update this file
with the reason for change and an appropriate semantic version increment.

Versioning follows Semantic Versioning: MAJOR for incompatible governance
changes, MINOR for new principles or materially expanded obligations, and PATCH
for clarifications or non-semantic edits. Each amendment MUST update the last
amended date. Reviewers MUST verify that proposed work and its validation
evidence comply with the Constitution before completion is accepted.

**Version**: 1.0.0 | **Ratified**: 2026-11-20 | **Last Amended**: 2026-11-20
