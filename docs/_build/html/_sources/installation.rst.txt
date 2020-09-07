.. meta::
   :description:
      Free command line tool to download photos from Instagram.
      Scrapes public and private profiles, hashtags, stories, feeds,
      saved media, and their metadata, comments and captions.
      Written in Python.

.. title:: CogniPy — Easy Semantic Technology for Python

Project description
===================

.. highlight:: none

**CogniPy** - Easy Semantic Technology for Python

Originally released and maintained by Cognitum Services S.A. - the creator of FluentEditor allowing use Controlled Natural Language for Ontology development.

The package allows for Reasoning, exploration of RDF/OWL, Fluent Editor CNL files, performs reasoning with OWL/RL Reasoner (Jena) as well as allows using SPARQL Graph queries (Jena)



**Open Source Libraties this project is build on:**

1. IKVM

2. CommandLineParser

3. Newtonsoft.JSon

4. ELK - ELK is an ontology reasoner that aims to support the OWL 2 EL profile. See http://elk.semanticweb.org/ for further information.

5. HermiT - HermiT is a conformant OWL 2 DL reasoner that uses the direct semantics. It supports all OWL2 DL constructs and the datatypes required by the OWL 2 specification.

6. Apache Jena -   Jena is a Java framework for building semantic web applications. It provides  tools and Java libraries to help you to develop semantic web and linked-data apps, tools and servers.

7. OWLAPI



**Why it is done this way?**

The sorfware emerged as an offspring of FluentEditor and therefore it has some common parts. One of them is the .net. Anyway, with your help we are going to move to java :). The convert_to_java branch already contains the project files converted automatically from .net to java. Anyway, manual crafting is now required to make it all work.



Installation
------------
Install cognipy on your system using ::

    $ pip install cognipy


Assumptions:

- If you are on Mac or Linux You MUST have mono installed on your system.

- Assuming Python is installed on your system.

- Tested with Anaconda

- Tested on MacOS, Winows and Linux (UBuntu)

Build
-----
Building new version ::

    $ nuget restore cognipy\CogniPy.sln
    $ msbuild cognipy\CogniPy.sln /t:Rebuild /p:Configuration=Release /p:Platform="any cpu"
    $ python setup.py bdist_wheel
    $ python -m twine upload dist/* --verbose







Useful Links
------------

- `Git Repository (on GitHub) <https://github.com/cognitum-octopus/cognipy>`__


Contributing
------------

As an open source project, CogniPy heavily depends on the contributions from
its community. See  for how you may help CogniPy to
become an even greater tool.
















