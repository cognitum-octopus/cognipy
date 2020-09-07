Grammar
========

Overview
--------

**Semantic Web, Ontologies**

The Semantic Web encourages the inclusion of semantic content in web pages, databases and systems. The semantic technologies are standardized by the international standards body, the World Wide Web Consortium [W3C, http://www.w3.org/]. According to the W3C, "The Semantic Web provides a common framework that allows data to be shared and reused across application, enterprise, and community boundaries." Thus the Semantic Web plays an important role with respect to the advancement of information management, collaboration and information sharing [S. Staab, R. Studer, Handbook on Ontologies, 2nd ed., Springer 2009]. Ontologies and also all the aspects of semantic technologies (e.g. OWL, SWRL rules, reasoners) allow to represent static knowledge about given part of the world. Ontologies are used for sharing knowledge and common understanding of a particular domain of interest, which makes communication between various beings possible and unambiguous. The various actors may be human users with different levels of expertise or computer programs (agents). OWL stands for Web Ontology Language [http://www.w3.org/TR/owl2-semantics/]. It is developed by the W3C’s Web Ontology Working Group and intended to be the successor of DAML+OIL. OWL is the most expressive knowledge representation for the Semantic Web so far. It allows us to write explicit, formal conceptualizations of human endeavor. OWL can be used in a spectrum of tasks that appears in the semantic web applications. It allows describing the application domain (where formal semantic plays a crucial role) and application specification, database schema, database constraints as well as database content using common language. On the other hand OWL can be used as a language that allows domain experts (a person with special knowledge or skills in a particular area of endeavor) to express domain-specific knowledge (valid knowledge used to refer to an area of human endeavor). The importance of a well-defined, formal language is clear and known from the area of programming languages; it is a necessary condition for machine-processing of information; it describes the meaning of knowledge precisely; it does not refer to subjective intuitions, nor is it open to different interpretations by different people (or machines). On the other hand, it is questionable whether the XML-based syntax for OWL is very user-friendly.

Naming Conventions
------------------
**Naming in OWL**

OWL deals with entities that are related to each other. All OWL entities are represented using Internationalized Resource Identifiers (IRIs) [RFC3987]. Here we only assume that IRI has form: namespace#identifier e.g. IRI: ‘http://sample.org/ontologies/human.owl#woman’ is composed from namespace ‘http://sample.org/ontologies/human.owl’ and identifier ‘woman’. We assume that the following OWL naming convention is used for identifiers:

* Each owl class (concept) or named individual (instance) identifier is a noun or a name in singular form that is written using camel case, starting with a capital letter.
    * Example) VeryBeautifulGirl, JohnDow.
* Each owl object and data property (role, attribute) identifier is a verb in past-participle form in camel case starting with a small letter.
    * Example) isPartOf, hasAge.

This naming convention is not a part of the standard, it is a general naming recommendation, and therefore there exist OWL ontologies that do not fulfill above requirements. We say that OWL identifiers that do not fulfill above recommendation are non-standard in opposite to standard ones.

**Naming in Fluent Editor**

In FE grammar we use slightly different naming convention, as long as we want to be as close to the natural language as possible. Instead of camel case, we use dash to separate parts of names. However, to make the mapping possible we use the following rules:

All standard owl class identifiers are transformed into small-letter starting buzz-words. e.g. VeryBeautifulGirl → very-beautiful-girl
Example)