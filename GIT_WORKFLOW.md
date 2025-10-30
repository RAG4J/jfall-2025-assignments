# Git Workflow for Workshop Repository

This document describes the git branch structure and workflow for maintaining a clean, organized repository for the JFall 2025 workshop assignments.

## Branch Structure

```
main (single "Initial Commit")
├── step_1 (1 commit: "Added the solution for step 1")
├── step_2 (1 commit: "Added the solution for step 2")
│   ├── step_3 (1 commit: "Added the solution for step 3")
│   │   └── step_7 (1 commit: "Added the solution for step 7")
│   └── step_4 (1 commit: "Added the solution for step 4")
└── step_6 (1 commit: "Added the solution for step 6")
```

## Design Principles

1. **Main branch**: Single "Initial Commit" containing all base workshop code
2. **Each step branch**: Exactly one commit with changes specific to that step
3. **Consistent naming**: All commits follow format "Added the solution for step X"
4. **Logical dependencies**:
   - step_1: Based on main (Plain Java agent)
   - step_2: Based on main (Spring AI agent)
   - step_3: Based on step_2 (adds MCP to Spring AI)
   - step_4: Based on step_2 (adds guardrails to Spring AI)
   - step_6: Based on main (Embabel implementation)
   - step_7: Based on step_3 (adds OAuth to MCP)

## Creating the Clean Structure from Scratch

### Step 1: Clean Main Branch

```bash
# Create backup tags before starting
git tag backup-original-main main
git tag backup-original-step_1 step_1
# ... repeat for all branches

# Checkout main
git checkout main

# Soft reset to first commit and squash all commits
FIRST_COMMIT=$(git rev-list --max-parents=0 HEAD)
git reset --soft $FIRST_COMMIT

# Amend to create single initial commit
git commit --amend -m "Initial Commit"
```

### Step 2: Rebase All Branches onto New Main

```bash
# For each step branch, rebase and skip old commits
for branch in step_1 step_2 step_3 step_4 step_6 step_7; do
    git checkout $branch
    git rebase main
    # Keep skipping with: git rebase --skip
    # Until rebase completes
done
```

### Step 3: Clean Up Individual Step Branches

#### For step_1 (if it has multiple commits):
```bash
git checkout step_1
git reset --soft main
git commit -m "Added the solution for step 1"
```

#### For step_2:
```bash
git checkout step_2
git commit --amend -m "Added the solution for step 2"
```

#### For step_3 (depends on step_2):
```bash
git checkout step_3
git rebase step_2
git commit --amend -m "Added the solution for step 3"
```

#### For step_4 (depends on step_2, might have multiple commits):
```bash
git checkout step_4
git rebase step_2
# If multiple commits, squash them:
git reset --soft step_2
git commit -m "Added the solution for step 4"
```

#### For step_6:
```bash
git checkout step_6
git commit --amend -m "Added the solution for step 6"
```

#### For step_7 (depends on step_3):
```bash
git checkout step_7
git rebase step_3
git commit --amend -m "Added the solution for step 7"
```

## Making Changes to Steps

### Adding Changes to a Step

1. **Checkout the step branch:**
   ```bash
   git checkout step_3
   ```

2. **Make your changes**

3. **Amend the commit:**
   ```bash
   git add .
   git commit --amend --no-edit
   ```

4. **Rebase dependent branches:**
   ```bash
   # step_7 depends on step_3, so rebase it
   git checkout step_7
   git rebase step_3
   ```

### Adding a New Step

1. **Determine the parent branch** (usually step_2 or step_3)

2. **Create and checkout new branch:**
   ```bash
   git checkout -b step_5 step_2
   ```

3. **Make changes and commit:**
   ```bash
   # Make your changes
   git add .
   git commit -m "Added the solution for step 5"
   ```

4. **Update this document** to reflect the new branch structure

## Pushing to Remote

If you've rewritten history, you'll need to force push:

```bash
# Push main
git push origin main --force-with-lease

# Push all step branches
git push origin step_1 step_2 step_3 step_4 step_6 step_7 --force-with-lease
```

**Note:** Only use `--force-with-lease` if you're sure no one else has pushed changes.

## Quick Reference: Rebase Chain

When rebasing, follow this order to maintain dependencies:

1. `main` (base for all)
2. `step_1` → `main`
3. `step_2` → `main`
4. `step_3` → `step_2`
5. `step_4` → `step_2`
6. `step_6` → `main`
7. `step_7` → `step_3`

## Automation Script

Here's a script to automate skipping old commits during rebase:

```bash
#!/bin/bash
# skip-rebase-conflicts.sh
# Usage: ./skip-rebase-conflicts.sh

while git rebase --skip 2>&1 | grep -q "could not apply\|Merge conflict"; do
    echo "Skipping commit..."
done
echo "Rebase complete!"
```

Make it executable: `chmod +x skip-rebase-conflicts.sh`

## Verification Commands

Check the structure is correct:

```bash
# View all branches in graph
git log --oneline --graph --all -20

# Count commits in each branch (should be 2: Initial + step commit)
git rev-list --count step_1
git rev-list --count step_2
# etc.

# View just the step commits
git log --oneline main..step_1
git log --oneline main..step_2
git log --oneline step_2..step_3
```

## Backup and Recovery

### Creating Backups

Before major operations:

```bash
# Create dated backup tags
DATE=$(date +%Y%m%d)
git tag backup-main-$DATE main
git tag backup-step_1-$DATE step_1
# ... repeat for all branches
```

### Restoring from Backup

```bash
# Restore a branch from backup tag
git checkout step_3
git reset --hard backup-step_3-20251030
```

### Removing Backup Tags

Once you're satisfied with changes:

```bash
# List backup tags
git tag | grep backup

# Delete backup tags
git tag -d backup-original-main backup-original-step_1
# ... or delete all at once
git tag -d $(git tag | grep backup-)
```

## Common Issues and Solutions

### Issue: "Cannot rebase: You have unstaged changes"

**Solution:**
```bash
git stash
git rebase [target]
git stash pop
```

### Issue: Many merge conflicts during rebase

**Solution:** The old commits conflict with the new main. Keep skipping:
```bash
while git status | grep -q "rebase in progress"; do
    git rebase --skip
done
```

### Issue: Lost commits after squashing

**Solution:** Use reflog to recover:
```bash
git reflog
git reset --hard HEAD@{n}  # where n is the commit number from reflog
```

## Workshop Preparation Checklist

Before the workshop:

- [ ] Verify main has only "Initial Commit"
- [ ] Verify each step has exactly one commit
- [ ] Verify all commit messages follow "Added the solution for step X" format
- [ ] Test that each step builds and runs correctly
- [ ] Verify branch dependencies are correct
- [ ] Create fresh backup tags
- [ ] Test cloning to a fresh directory
- [ ] Verify `.gitignore` is correct (logs/, data/, target/)

## Additional Notes

- **Never rebase published history** unless coordinating with all collaborators
- **Always create backups** before major history rewrites
- **Test each step** after rebasing to ensure nothing broke
- The structure prioritizes **clarity for workshop participants** over commit granularity
- Each step branch should be **self-contained** and buildable
