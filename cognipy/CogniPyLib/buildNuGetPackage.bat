cd %~dp0
REM nuget spec -f
nuget pack FluentEditorClientLib.csproj -build -IncludeReferencedProjects -Prop Configuration=Release
REM COPY *.nupkg "\\cogserver01\PUBLIC\NuGet packages library"