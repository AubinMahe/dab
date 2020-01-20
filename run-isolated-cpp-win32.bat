@echo off
PATH=C:\MinGW\bin;C:\windows\system32;C:\windows;C:\windows\system32\wbem;Z:\usr\lib\gcc\i686-w64-mingw32\7.3-win32;H:\Dev\git\dab\dab-lib;H:\Dev\git\dab\dab-bin
start "Banque"       isolated-sc-cpp-win32.exe
start "Distributeur" isolated-ihm1-cpp-win32.exe
start "Controleur"   isolated-udt1-cpp-win32.exe
pause
