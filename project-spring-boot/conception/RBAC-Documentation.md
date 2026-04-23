# Role-Based Access Control (RBAC) System

## Overview

This document describes the role-based access control system for the Agile Project Management platform, including role definitions, permissions, and the special tester-only Kanban column feature.

---

## Project Roles

### 1. Product Owner

**Primary Responsibilities:**

- Create projects
- Define product vision through epics and user stories
- Assign work to Scrum Master for detailed planning
- Prioritize backlog items
- Accept/reject completed work

**Permissions:**

- ✅ Create new projects
- ✅ Add Scrum Master to project
- ✅ Create epics
- ✅ Create user stories
- ✅ Assign epics/stories to Scrum Master
- ✅ Prioritize backlog items
- ✅ View project dashboards and reports
- ❌ **Cannot add developers or testers** (only Scrum Master can)
- ❌ Cannot define acceptance criteria (Scrum Master does this)
- ❌ Cannot estimate story points (Scrum Master does this)
- ❌ Cannot manage sprints
- ❌ Cannot configure Kanban board
- ❌ Cannot move tasks

**Workflow:**

1. Product Owner creates project
2. Product Owner adds Scrum Master to project
3. Product Owner creates epics and user stories
4. Product Owner assigns them to Scrum Master
5. Product Owner prioritizes the backlog
6. Scrum Master handles all detailed planning and team management

---

### 2. Scrum Master

**Primary Responsibilities:**

- Build and manage development team (add developers and testers)
- Define detailed acceptance criteria for user stories
- Estimate story points
- Create and manage sprints
- Configure Kanban workflow
- Decompose stories into tasks
- Assign tasks to team members
- Remove team impediments

**Permissions:**

- ✅ Add developers to team
- ✅ Add testers to team
- ✅ Remove team members
- ✅ Create user stories
- ✅ Define acceptance criteria
- ✅ Estimate story points
- ✅ Create and manage sprints
- ✅ Add stories to sprints
- ✅ Configure Kanban board columns
- ✅ Set column restrictions (testerOnly flag)
- ✅ Create tasks from user stories
- ✅ Assign tasks to team members
- ✅ View team velocity and reports
- ✅ Add comments and mentions
- ❌ Cannot create projects (only Product Owner can)
- ❌ Cannot move tasks to tester-only columns

**Workflow:**

1. Scrum Master receives assigned epics/stories from Product Owner
2. Scrum Master adds developers and testers to team
3. Scrum Master defines detailed acceptance criteria
4. Scrum Master estimates story points
5. Scrum Master creates sprint and adds stories
6. Scrum Master decomposes stories into tasks
7. Scrum Master assigns tasks to developers/testers
8. Scrum Master monitors sprint progress

---

### 3. Developer

**Primary Responsibilities:**

- Implement user stories
- Create and estimate tasks
- Update task progress
- Move tasks through Kanban workflow

**Permissions:**

- ✅ Create tasks from user stories
- ✅ Assign tasks to themselves or others
- ✅ Update task status and hours
- ✅ Move tasks in Kanban board
- ✅ Log task progress
- ✅ Add comments and mentions
- ✅ View notifications
- ❌ **Cannot move tasks to columns with `testerOnly=true`**

**Kanban Restrictions:**

- Can move tasks to: TODO, In Progress, Code Review, etc.
- Cannot move tasks to: Testing, QA Review, Done (if marked testerOnly)

---

### 4. Tester

**Primary Responsibilities:**

- Validate acceptance criteria
- Execute test cases
- Report bugs and defects
- Verify task completion

**Permissions:**

- ✅ Create tasks from user stories
- ✅ Assign tasks to themselves or others
- ✅ Update task status and hours
- ✅ Move tasks in Kanban board
- ✅ **Move tasks to columns with `testerOnly=true`**
- ✅ Execute test cases
- ✅ Validate acceptance criteria
- ✅ Report bugs
- ✅ Move tasks to Done
- ✅ Add comments and mentions
- ✅ View notifications

**Special Access:**

- Has same task management permissions as Developer
- **Plus**: Can move tasks to tester-restricted columns

---

## Kanban Column Configuration

### Column Structure

Each Kanban column has the following attributes:

```java
@Entity
@Table(name = "kanban_columns")
public class KanbanColumn {
    private Long id;
    private String name;
    private Integer positionIndex;
    private boolean isDoneStatus;

    // ROLE-BASED ACCESS CONTROL
    @Column(name = "tester_only")
    private boolean testerOnly = false; // Default: accessible to all
}
```

### Tester-Only Columns

When `testerOnly = true`, the column has restricted access:

**Example Configuration:**
| Column Name | Position | isDoneStatus | testerOnly | Who Can Move Here |
|------------|----------|--------------|------------|-------------------|
| TODO | 1 | false | false | All team members |
| In Progress | 2 | false | false | All team members |
| Code Review | 3 | false | false | All team members |
| **Testing** | **4** | **false** | **true** | **TESTER only** |
| **QA Review** | **5** | **false** | **true** | **TESTER only** |
| **Done** | **6** | **true** | **true** | **TESTER only** |

---

## Business Logic Implementation

### Task Movement Validation

```java
// KanbanAuthorizationService.java

public boolean canMoveTaskToColumn(ProjectMember member, KanbanColumn targetColumn) {
    ProjectRole role = member.getRole();

    // If column is testerOnly, only TESTER can move tasks there
    if (targetColumn.isTesterOnly()) {
        return role == ProjectRole.TESTER;
    }

    // All team members can move to non-restricted columns
    return role == ProjectRole.DEVELOPER
        || role == ProjectRole.TESTER
        || role == ProjectRole.SCRUM_MASTER;
}
```

### Workflow Example

**Scenario: Developer completes coding a task**

1. Developer moves task from "In Progress" → "Code Review" ✅ (Allowed)
2. After code review, Developer tries to move → "Testing" ❌ (Blocked)
   - Error: "Only TESTER role can move tasks to 'Testing' column"
3. Tester moves task from "Code Review" → "Testing" ✅ (Allowed)
4. Tester executes tests and moves → "Done" ✅ (Allowed)

**Scenario: Tester finds a bug**

1. Tester can move task back from "Testing" → "In Progress" ✅ (Allowed)
2. Developer fixes the issue and moves → "Code Review" ✅ (Allowed)
3. Tester re-tests and moves → "Testing" → "Done" ✅ (Allowed)

---

## Use Case Descriptions

### UC19: Move Task in Kanban Board

**Actors:** Developer, Tester  
**Preconditions:**

- User is a project member
- Task exists in current sprint
- User has permission to update tasks

**Main Flow:**

1. User selects a task from Kanban board
2. User drags task to target column
3. System validates user's role against target column restrictions
4. If `targetColumn.testerOnly == true` AND `user.role != TESTER`:
   - System rejects the move
   - Display error: "Only TESTER can move tasks to this column"
5. Else:
   - System updates task status
   - System logs the change
   - System triggers notification if task is reassigned

**Alternative Flow (Validation Failure):**
3a. User is DEVELOPER trying to move to testerOnly column

- System displays error message
- Task remains in current column
- User must request TESTER to move the task

---

### UC22: Move Task to Testing Column

**Actors:** Tester  
**Preconditions:**

- User role is TESTER
- Task is in "Code Review" or earlier stage
- Testing column is configured with testerOnly=true

**Main Flow:**

1. Tester reviews completed code
2. Tester moves task to "Testing" column
3. System validates acceptance criteria are defined
4. System updates task status
5. System assigns task to tester (if not already assigned)
6. Tester executes test cases

**Postconditions:**

- Task is in Testing column
- Task status is updated
- Testing activities can begin

---

## Database Schema Impact

### ProjectRole Enum Values

```sql
-- Project roles enum
CREATE TYPE project_role AS ENUM (
    'PRODUCT_OWNER',
    'SCRUM_MASTER',
    'DEVELOPER',
    'TESTER'  -- NEW ROLE ADDED
);
```

### KanbanColumn Table

```sql
CREATE TABLE kanban_columns (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    position_index INTEGER NOT NULL,
    is_done_status BOOLEAN DEFAULT FALSE,
    tester_only BOOLEAN DEFAULT FALSE,  -- NEW COLUMN ADDED
    project_id BIGINT REFERENCES projects(id),
    UNIQUE(project_id, position_index)
);
```

---

## API Endpoints (Suggested)

### Check Column Access

```http
GET /api/projects/{projectId}/kanban/columns/{columnId}/can-move
Headers: Authorization: Bearer {token}
Response: {
  "canMove": true|false,
  "reason": "Only TESTER can move to this column"
}
```

### Move Task with Validation

```http
PUT /api/tasks/{taskId}/move
Headers: Authorization: Bearer {token}
Body: {
  "targetColumnId": 5
}
Response (Success): {
  "success": true,
  "task": { ... }
}
Response (Error): {
  "success": false,
  "error": "Only TESTER role can move tasks to 'Testing' column"
}
```

---

## Security Considerations

1. **Frontend Validation**: UI should disable tester-only columns for non-testers
2. **Backend Enforcement**: All API endpoints must validate role permissions
3. **Audit Trail**: Log all task movements with user role information
4. **Column Configuration**: Only SCRUM_MASTER can modify testerOnly flag

---

## Testing Strategy

### Unit Tests

```java
@Test
void developer_cannot_move_to_tester_column() {
    ProjectMember developer = createMember(ProjectRole.DEVELOPER);
    KanbanColumn testingColumn = createColumn("Testing", true);

    boolean canMove = service.canMoveTaskToColumn(developer, testingColumn);

    assertFalse(canMove);
}

@Test
void tester_can_move_to_tester_column() {
    ProjectMember tester = createMember(ProjectRole.TESTER);
    KanbanColumn testingColumn = createColumn("Testing", true);

    boolean canMove = service.canMoveTaskToColumn(tester, testingColumn);

    assertTrue(canMove);
}
```

---

## Migration Guide

### For Existing Projects

1. Add TESTER role to ProjectRole enum
2. Add testerOnly column to kanban_columns table (default: false)
3. Update existing "Testing" and "QA" columns: `SET tester_only = true`
4. Deploy KanbanAuthorizationService
5. Update frontend to respect column restrictions

---

## Summary

### Clear Separation of Duties

**Product Owner:**

- Strategic level: Creates project, defines vision (epics/stories), prioritizes
- Assigns work to Scrum Master
- **Cannot manage team or technical details**

**Scrum Master:**

- Tactical level: Builds team, defines details (acceptance criteria, estimates)
- Manages sprints and Kanban workflow
- Decomposes work and assigns tasks
- **Receives assignments from Product Owner**

**Developer & Tester:**

- Execution level: Implements and tests tasks
- Moves tasks through workflow
- **Tester has special access to quality gates**

### Permission Matrix

| Capability                 | Product Owner | Scrum Master | Developer | Tester |
| -------------------------- | ------------- | ------------ | --------- | ------ |
| Create Project             | ✅            | ❌           | ❌        | ❌     |
| Add Scrum Master           | ✅            | ❌           | ❌        | ❌     |
| Add Developers/Testers     | ❌            | ✅           | ❌        | ❌     |
| Create Epic                | ✅            | ❌           | ❌        | ❌     |
| Create User Story          | ✅            | ✅           | ❌        | ❌     |
| Assign to Scrum Master     | ✅            | ❌           | ❌        | ❌     |
| Define Acceptance Criteria | ❌            | ✅           | ❌        | ❌     |
| Estimate Story Points      | ❌            | ✅           | ❌        | ❌     |
| Prioritize Backlog         | ✅            | ❌           | ❌        | ❌     |
| Manage Sprints             | ❌            | ✅           | ❌        | ❌     |
| Configure Kanban           | ❌            | ✅           | ❌        | ❌     |
| Create Tasks               | ❌            | ✅           | ✅        | ✅     |
| Assign Tasks               | ❌            | ✅           | ❌        | ❌     |
| Move Tasks (Regular)       | ❌            | ✅           | ✅        | ✅     |
| **Move to Tester Columns** | ❌            | ❌           | ❌        | **✅** |
| Add Comments               | ✅            | ✅           | ✅        | ✅     |
| View Reports               | ✅            | ✅           | ❌        | ❌     |

**Key Workflow:**

1. PO creates project → adds Scrum Master
2. PO creates epics/stories → assigns to Scrum Master
3. Scrum Master adds team members (developers/testers)
4. Scrum Master defines details (acceptance criteria, estimates)
5. Scrum Master creates sprint → decomposes stories → assigns tasks
6. Developers implement → move tasks (except tester columns)
7. Testers test → move to testing/done columns

**Key Point**: The `testerOnly` flag on KanbanColumn ensures quality gates are enforced, preventing developers from bypassing testing phases.
