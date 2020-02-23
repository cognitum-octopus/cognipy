# CogniPy - Easy Semantic Technology for Python

Originally released and maintained by [Cognitum Services S.A.](https://www.cognitum.eu) - the creator of [FluentEditor](https://www.cognitum.eu/Semantics/FluentEditor/) allowing use Controlled Natural Language for Ontology development.

The package allows for Reasoning, exploration of RDF/OWL, Fluent Editor CNL files, performs reasoning with OWL/RL Reasoner (Jena) as well as allows using SPARQL Graph queries (Jena)


### Assumptions:

+ If you are on Mac or Linux You MUST have [mono installed](https://www.mono-project.com/) on your system.
+ Assuming Python is installed on your system.
+ Tested with Anaconda
+ Tested on MacOS, Winows and Linux (UBuntu)

Install `cognipy` on your system using :

```
pip install cognipy
```

### Building new version

```
msbuild cognipy\CogniPy.sln /t:Rebuild /p:Configuration=Release /p:Platform="any cpu"
bumpversion --current-version 0.1.1 patch setup.py
python setup.py bdist_wheel
python -m twine upload dist/* --verbose
```
