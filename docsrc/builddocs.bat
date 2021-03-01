rmdir /s /q ..\docs
call .\make.bat html ..\docs
cp CNAME ..\docs