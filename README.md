![CogniPy](docsrc/_static/figures/cognipy_wide.png)
# CogniPy for Pandas - In-memory Graph Database and Knowledge Graph with Natural Language Interface

### Whats in the box

Reasoning, exploration of RDF/OWL, [FluentEditor](https://www.cognitum.eu/Semantics/FluentEditor/) CNL files, with OWL/RL Reasoner (Jena) as well as SPARQL Graph queries (Jena) and visualization.

What you can do with this:
1. Write your graph/ontology in Controlled Natural Language or import it from RDF/OWL
2. Add reasoning rules/T-Box in Controlled Natural Language
3. Import data using Pandas or scrap them from the Internet
4. Draw the resulting, materialized graph
5. Use SPARQL to execute graph query
6. Use output Dataframe for further processing with Pandas

### Getting started

#### Installation
Prerequisites:
+ If you are on Mac or Linux You MUST have [mono installed](https://www.mono-project.com/) on your system.
+ Graph drawing based on [pydot](https://pypi.org/project/pydot/) that is dependent on GraphViz - you should try to download it and instally manually. Or just `conda install pydot graphviz`
+ Tested with Anaconda
+ Tested on MacOS, Winows and Linux (UBuntu)

Install `cognipy` on your system using :
```
pip install cognipy
```

#### Hello world program
In Jupyter you write:
```
from cognipy.ontology import Ontology #the ontology processing class
```
```
%%writefile hello.encnl
World says Hello.
Hello is a word.
```
```
onto = Ontology("cnl/file","hello.encnl")
print(onto.select_instances_of("a thing that says a word")[["says","Instance"]])
```
Output (Pandas DataFrame):
>|  | says  | Instance|
>|--|:-----:|:-------:|
>|0 | Hello | World   |


#### Examples

Example Jupyter notebooks that use CogniPy in several scenarios can be found in the [Examples section](https://github.com/cognitum-octopus/cognipy/tree/master/docsrc/jupyter_notebooks)

### Cognipy documentation
Compiled documentation is stored on github pages here: [Cognipy Documentation](https://cognitum-octopus.github.io/cognipy/)

### Related research papers

1. [Semantic rules representation in controlled natural language in FluentEditor](https://ieeexplore.ieee.org/document/6577807)
2. [Collaborative Editing of Ontologies Using Fluent Editor and Ontorion](https://link.springer.com/chapter/10.1007/978-3-319-33245-1_5)
3. [Semantic OLAP with FluentEditor and Ontorion Semantic Excel Toolchain](http://www.thinkmind.org/index.php?view=article&articleid=semapro_2015_3_30_30051)
4. [Ontology-aided software engineering](https://www.semanticscholar.org/paper/Ontology-aided-software-engineering-Kaplanski/24100da2431d6f8a3cd9114c7d4a9050fb421d22?p2df)
5. [Ontology of the Design Pattern Language for Smart Cities Systems](https://link.springer.com/chapter/10.1007/978-3-662-53580-6_6)

### How to cite CogniPy
We would be grateful if scientific publications resulting from projects that make use of CogniPy would include the following sentence in the acknowledgments section: "This work was conducted using the CogniPy package, which is an open-source project maintained by [Cognitum Services S.A.](https://www.cognitum.eu) <https://www.cognitum.eu>"

### Contributors
* Maintained by [Cognitum Services S.A.](https://www.cognitum.eu) <https://www.cognitum.eu>
* Contact us: <office@cognitum.eu>
* CogniPy heavily depends on the contributions from its community. See  for how you may help CogniPy to become an even greater tool.

### Open Source Libraries this project is build on
1. IKVM
2. CommandLineParser
3. Newtonsoft.JSon
4. ELK - ELK is an ontology reasoner that aims to support the OWL 2 EL profile. See http://elk.semanticweb.org/ for further information.
5. HermiT - HermiT is a conformant OWL 2 DL reasoner that uses the direct semantics. It
supports all OWL2 DL constructs and the datatypes required by the OWL 2 specification.   
6. Apache Jena -   Jena is a Java framework for building semantic web applications. It provides  tools and Java libraries to help you to develop semantic web and linked-data apps, tools and servers. 
7. OWLAPI

### Building new version
```
nuget restore cognipy\CogniPy.sln
msbuild cognipy\CogniPy.sln /t:Rebuild /p:Configuration=Release /p:Platform="any cpu"
python setup.py bdist_wheel
python -m twine upload dist/* --verbose
```


## FAQ
> Why it is done this way?

The software emerged as an offspring of FluentEditor and therefore it has some common parts. One of them is the .net. We are planning to move these parts to java so whole stack will be more technology consistent. The `convert_to_java` branch already contains the project files converted automatically from .net to java. Anyway, manual crafting is now required to make it all work.
