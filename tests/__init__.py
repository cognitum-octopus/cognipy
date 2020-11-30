import os

os.system("cd ../ & nuget restore cognipy/CogniPy.sln")
os.system("msbuild cognipy/CogniPy.sln")
