.. meta::
   :description:
     CogniPy for Pandas - In-memory Graph Database and Knowledge Graph with Natural Language Interface, Reasoning, exploration of RDF/OWL, FluentEditor CNL files, with OWL/RL Reasoner (Jena) as well as SPARQL Graph queries (Jena) and visualization.

.. title:: CogniPy for Pandas - In-memory Graph Database and Knowledge Graph with Natural Language Interface

.. image:: _static/figures/cognipy_wide.png
    :width: 300

**CogniPy for Pandas**
======================

In-memory Graph Database and Knowledge Graph with Natural Language Interface

**Whats in the box**

Reasoning, exploration of RDF/OWL, [FluentEditor](https://www.cognitum.eu/Semantics/FluentEditor/) CNL files, with OWL/RL Reasoner (Jena) as well as SPARQL Graph queries (Jena) and visualization

1. Write your graph/ontology in Controlled Natural Language or import it from RDF/OWL

2. Add reasoning rules/T-Box in Controlled Natural Language

3. Import data using Pandas/scrap them from the internet

4. Draw the resulting, materialized graph

5. Use SPARQL to execute graph query

6. Use output Dataframe for further processing with Pandas


Installation
------------

Prerequisites:

- If you are on Mac or Linux You MUST have mono installed on your system.

- Tested with Anaconda

- Tested on MacOS, Winows and Linux (UBuntu)

Install cognipy on your system using ::

    $ pip install cognipy



Open Source repository
----------------------

- `Git Repository (on GitHub) <https://github.com/cognitum-octopus/cognipy>`__


Hello world program
-------------------

In Jupyter you write ::

    from cognipy.ontology import Ontology #the ontology processing class

    %%writefile hello.encnl
    World says Hello.
    Hello is a word.

    onto = Ontology("cnl/file","hello.encnl")
    print(onto.select_instances_of("a thing that says a word")[["says","Instance"]])


Related research papers
-----------------------

1. `Semantic rules representation in controlled natural language in FluentEditor <https://ieeexplore.ieee.org/document/6577807>`
2. `Collaborative Editing of Ontologies Using Fluent Editor and Ontorion <https://link.springer.com/chapter/10.1007/978-3-319-33245-1_5>`
3. `Semantic OLAP with FluentEditor and Ontorion Semantic Excel Toolchain <http://www.thinkmind.org/index.php?view=article&articleid=semapro_2015_3_30_30051>`
4. `Ontology-aided software engineering <https://www.semanticscholar.org/paper/Ontology-aided-software-engineering-Kaplanski/24100da2431d6f8a3cd9114c7d4a9050fb421d22?p2df>`
5. `Ontology of the Design Pattern Language for Smart Cities Systems <https://link.springer.com/chapter/10.1007/978-3-662-53580-6_6>`

How to cite CogniPy
-------------------
We would be grateful if scientific publications resulting from projects that make use of CogniPy would include the following sentence in the acknowledgments section: "This work was conducted using the CogniPy package, which is an open-source project maintained by [Cognitum Services S.A.](https://www.cognitum.eu)"

Output (Pandas DataFrame):

+--+-------+---------+
|  | says  | Instance|
+==+=======+=========+
|0 | Hello | World   |
+--+-------+---------+

.. toctree::
   :maxdepth: 5
   :caption: CogniPy:

   examples
   grammar
   dictonary
   modules

.. toctree::
    :hidden:
    :maxdepth: 0
    :titlesonly:


Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
