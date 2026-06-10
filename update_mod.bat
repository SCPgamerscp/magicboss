@echo off
echo MODファイルを更新します...
echo.

set SOURCE=C:\Users\ecrea\Downloads\magicboss\build\libs\elementmagicboss-1.0.jar
set DEST=C:\Users\ecrea\curseforge\minecraft\Instances\datapacktest\mods\elementmagicboss-1.0.jar

echo 古いファイルを削除...
del "%DEST%" 2>nul

echo 新しいファイルをコピー...
copy "%SOURCE%" "%DEST%"

echo.
echo 完了しました！
echo Minecraftを再起動してください。
pause
