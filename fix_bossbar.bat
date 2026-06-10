@echo off
echo ボスバーの問題を修正します...
echo.

echo 1. Gradleクリーン実行中...
cd /d "%~dp0"
call gradlew clean

echo.
echo 2. ビルドキャッシュを削除中...
rmdir /s /q build 2>nul
rmdir /s /q .gradle\caches 2>nul

echo.
echo 3. 再ビルド実行中...
call gradlew build

echo.
echo 完了しました！
echo Minecraftを再起動して確認してください。
pause
