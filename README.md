# Git Practice Task Manager

A tiny task-management application made for practising Git branching, commits, merging, and merge conflicts — not for learning full-stack development.

## Requirements
- Java JDK 17 or newer
- A terminal (Command Prompt / PowerShell / VS Code terminal)
- A GitHub account for the Git workflow

No Maven, Gradle, database, or external libraries are required.

## Run the application

Open a terminal in this project folder.

### Windows
```powershell
javac -d out src\TaskManagerServer.java
java -cp out TaskManagerServer
```

### macOS / Linux
```bash
javac -d out src/TaskManagerServer.java
java -cp out TaskManagerServer
```

Then open http://localhost:8080 in your browser. Keep the terminal running while using the app. Press Ctrl+C to stop it.

Tasks are stored in memory, so they reset when you stop the server. This is intentional: the app stays small and dependency-free.

## Suggested Git practice

1. Run the app and confirm it works.
2. Create a GitHub repository and push this starter version to `main`.
3. Create a feature branch, for example `feature-priority`.
4. Make one small change, commit it, and push that branch.
5. Switch to `main` and merge the feature branch.
6. Later, practise a merge conflict by editing the same line in the same file on two branches.

## First feature idea
Add a **priority** label (Low / Medium / High) to each task. Keep the change small. You can edit `public/index.html`, `public/style.css`, `public/app.js`, and the Java API only if needed.

## Useful Git commands
```bash
git status
git add .
git commit -m "Describe your change"
git branch
git switch -c feature-name
git push -u origin feature-name
git switch main
git pull
git merge feature-name
git log --oneline --graph --all
```

Important: check `git status` before switching branches, and commit or otherwise safely handle any work in progress.
