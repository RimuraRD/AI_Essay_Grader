@echo off

.\mvnw.cmd package
java -cp .\target\ai_essay_grader-1.0-SNAPSHOT.jar com.flores.aiessaygrader.EssayGrader
pause
