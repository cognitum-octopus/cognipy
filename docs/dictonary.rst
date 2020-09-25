.. role:: blue

Dictonary
=========

.. toctree::
    :hidden:

.. contents::


A / an
------
English indefinite article :blue:`a/an` indicates some unspecified individual of
a given concept. Individuals start with capital letter and are never
preceeded by :blue:`a/an`. Concepts start with small letter and can be preceeded
by :blue:`a/an`. Variables appear in semantic rules. When a variable occurs for
the first time it is preceeded by :blue:`a/an` and later by the.

Different use cases of variables in semantic rules are summed up in the
table below.

.. list-table::
    :widths: 25 25
    :header-rows: 1
    :align: center

    * - Variable pattern
      - Use case
    * - :blue:`a/the class-name`
      - denotes variables assigned to a class/concept
    * - :blue:`a/the thing`
      - represents an instance of the top concept "owl:Thing" and thus any variable in the SR-CE sentence.
    * - :blue:`a/the class-name(n)/thing(n)`
      - different numbers in parenthesis mark more variables of the same type.

.. container:: comment

    Example: Here concepts are preceeded by 'a', referring to an
    unspecified earlier individual of a given concept.

.. parsed-literal::

    Sophie :blue:`is a` giraffe.
    Leo :blue:`is a` lion.

.. container:: comment

    Example: Word 'every' is used to refer to all individuals of given concept, word 'a' refers to some unspecified individual of given concept.

.. parsed-literal::

    :blue:`Every` giraffe :blue:`is an` animal.
    :blue:`Every` lion :blue:`is an` animal.

.. container:: comment

    Example: In the semantic rule below, there is one variable 'person'.

.. parsed-literal::

    :blue:`If a` person has-age-in-years :blue:`greater-or-equal-to 18 then the` person :blue:`is an` adult-person.

.. container:: comment

    Example: In the semantic rule below, there is a variable which could be anything.

.. parsed-literal::

    :blue:`If a thing is a` person :blue:`then the thing` has-species-name :blue:`equal-to 'homo-sapiens'`.

.. container:: comment

    Example: In the semantic rule below, there are two variables 'person'.

.. parsed-literal::

    :blue:`If a` person(1) has-parent a person(2) :blue:`and the` person(2) :blue:`is a` female-person :blue:`then the` person(1) has-mother :blue:`the` person(2).


And
---
Conjunction :blue:`and` connects two phrases into complex phrase. It can be used
to make an intersection of concepts or to introduce intersection complex
conditions into semantic rules.

The examples show how to use conjuction :blue:`and`.

.. container:: comment

    The concept of a young male man is an intersection of concepts of a young thing, a male thing and a person.

.. parsed-literal::

      :blue:`Something is a` young-male-man :blue:`if-and-only-if-it is a` young-thing
      :blue:`that is a` male-thing :blue:`takand is a` person.

.. container:: comment

    In the 'if' statement below the antecedent clause is
    complex. It could also be the case for the consequent clause.

.. parsed-literal::

      :blue:`If a` person (1) has-parent :blue:`a` person (2) :blue:`and the` person (2) :blue:`is a`
      female-person :blue:`then the` person (1) has-mother :blue:`the` person (2) .

And-or
------

Conjunction :blue:`and-or` connects two phrases into complex phrase. It can be
used to make a union of concepts or to introduce complex conditions into
semantic rules. The complex condition is satisfied if at least one of
its conditions is fulfilled.

The examples show how to use conjuction :blue:`and-or`.

.. container:: comment

    The concept of a child is an intersection of concepts of boy
    and girl.

.. parsed-literal::

      :blue:`Something is a` child :blue:`if-and-only-if-it is a` boy :blue:`and-or is a` girl.


Anything
--------

:blue:`Anything` refers to the pairwise disjoint concept set. if is usedc in
pair with **or-something-else.**

The example shows how to use the keyword.

.. container:: comment

    Example: The concepts of cat, dog and human are pairwise disjoint.
    However, there may exist individuals that are none of them.

.. parsed-literal::

    :blue:`Anything either is a` cat or :blue:`is a` dog or :blue:`is a` human
    :blue:`or-something-else`.

Above sentence is equivalent to the following set of sentences:

.. parsed-literal::

      :blue:`No` cat :blue:`is a` dog.
      :blue:`No` cat :blue:`is a` human.
      :blue:`No` dog :blue:`is a` human.

As-well-as
----------

:blue:`As-well-as` allows to define complext data types facets.

The example shows how to use the keyword.

.. parsed-literal::

      :blue:`Every` top-model has-name :blue:`(some string value) as-well-as (some
      integer value)`.

Be (is/are)
-----------

The negated verb to :blue:`be` and its conjugated forms are used for concept
inclusion or concept equivalence. With that verb a hierarchy of concepts
is constructed or an individual is placed in the hierarchy of concept
(by saying what concepts the individual actually is). Concepts and
individuals are defined when their names are used in at least one
sentence.

The verb to :blue:`be` combined with the word :blue:`by` is used to create a role
inversion. It is conceptually equivalent to passive voice.

Those examples show how to use the verb to :blue:`be` to create a hierarchy of
concepts and individuals and make role inversion.

.. container:: comment

    Example: Individuals are named and placed in the hierarchy of concepts.

.. parsed-literal::

      Sophie :blue:`is a` giraffe.
      Leo :blue:`is a` lion.

.. container:: comment

    Example: A simple hierarchy of concepts. Lion and giraffe are inclusive of the concept of an animal.

.. parsed-literal::

      :blue:`Every` giraffe :blue:`is an` animal.
      :blue:`Every` lion :blue:`is an` animal.

.. container:: comment

    Example: A simple hierarchy of concepts. Concepts of a young male man and a boy are equivalent.

.. parsed-literal::

      :blue:`Something is a` boy :blue:`if-and-only-if-it is a` young-male-man.

.. container:: comment

    Example: A more complex hierarchy of concepts. The concept of a young
    male man is an intersection of concepts of a young thing, a male
    thing and a person.

.. parsed-literal::

      :blue:`Something is a` young-male-man :blue:`if-and-only-if-itis a` young-thing
      :blue:`that is a` male-thing :blue:`and is a` person.

.. container:: comment

    Example: Role inversion. The last two sentences are equivalent.

.. parsed-literal::

      :blue:`Every` child is loved by parent.
      Mary :blue:`is` loved :blue:`by` Tom.
      Tom loves Mary.


Be not (is not /are not)
------------------------

The negated verb :blue:`not to be` and its conjugated forms are used for
negating a concept or giving a complement concept.

Those examples show how to use the negated verb :blue:`not to be` to negate a
concept or to give a concept complement to it.

.. container:: comment

    Example: Everything in the world is an adult-thing or a young-thing.
    The concepts are complement.

.. parsed-literal::

      :blue:`Something is a` young-thing :blue:`if-and-only-if-it is not an`
      adult-thing.

.. container:: comment

    Example: The classes of young-thing and adult-thing are disjoint.
    However, there might exist a thing that is neither of them.

.. parsed-literal::

      :blue:`Every-single-thing that is a` young-thing :blue:`is not an` adult-thing.
      :blue:`Every` young-thing :blue:`is not an` adult-thing.

By
--

The verb to :blue:`be` and word :blue:`by` is used to create a role inversion. It is
conceptually equivalent to passive voice.

The example below shows how to use role inversion.

.. container:: comment

    Example: Role inversion. Last two sentences are equivalent.

.. parsed-literal::

      :blue:`Every` child :blue:`is` loved :blue:`by a` parent.
      Mary :blue:`is` loved :blue:`by` Tom.
      Tom loves Mary.

Less-than/more-than/at-least/at-most/differet-than/zero/one/two...
------------------------------------------------------------------

Keywords for indicating property cardinality restriction. They deal with
allowed and defined number of listed properties (roles). They can be
used to specify the number of individuals involved in the restriction.
Indeed, classes can be defined depending on the number of listed
properties. It is possible to declare a maximum, minimum or exact number
of listed properties in domain. Cardinality restrictions can also be
applied to the number of data attributes that a concept or instance has.

.. list-table::
    :widths: 25 25
    :header-rows: 1
    :align: center

    * - Keywords
      - Meaning
    * - :blue:`less-than`
      - **<**
    * - :blue:`more-than`
      - **>**
    * - :blue:`at-most`
      - **≤**
    * - :blue:`at-least`
      - **≥**
    * - :blue:`different-than`
      - **≠**

These examples show how to put restrictions on properties cardinality
numbers - maximum, minimum or exact.

.. container:: comment

    Example: The sentence defines the maximum number of allowed parents.

.. parsed-literal::

      :blue:`Every` person is-a-child-of :blue:`at-most two` parents.

.. container:: comment

    Example: Defines the minimum number of allowed parents.

.. parsed-literal::

      :blue:`Every` person is-a-child-of :blue:`at-least two` parents.

.. container:: comment

    Example: Defines the exact number of allowed parents.

.. parsed-literal::

      :blue:`Every` person :blue:`is-a-child-of two` parents.

.. container:: comment

    Example: Cardinality restriction about data attribute.

.. parsed-literal::

      :blue:`Every` cat has-name :blue:`at-most one (some string value)`.

Greater-than/lower-than/greater-or-equal-to/lower-or-equal-to/different-from
----------------------------------------------------------------------------

The construction :blue:`greater-than` ... assigns a range to a data attribute.
The names of the keywords are summmed up in the table below.

.. list-table::
    :widths: 25 25 25
    :header-rows: 1
    :align: center

    * - Keywords
      - Meaning
      -
    * - :blue:`greater-than`
      - **<**
      - maxExclusive
    * - :blue:`lower-than`
      - **>**
      - minExclusive
    * - :blue:`greater-or-equal-to`
      - **≤**
      - maxInclusive
    * - :blue:`lower-or-equal-to`
      - **≥**
      - minInclusive
    * - :blue:`different-from`
      - **≠**
      - minExclusive & maxExclusive
    * - :blue:`equal-to`
      - **=**
      - .

Those examples show how to assign data attributes.

.. parsed-literal::

      :blue:`Every` adult-person has-age :blue:`greater-or-equal-to 18`.

Do-not/does-not
---------------

The auxiliary verb :blue:`do-not` or :blue:`does-not` negates a role or data property.


Those examples show how to negate roles and data properties.

.. container:: comment

    Example: Mary loves nobody.

.. parsed-literal::

      Mary :blue:`does-not` love :blue:`a` thing.

Either ... or ...
-----------------

The construction :blue:`either ... or ...` is used to express a disjoint union
or enumerate individuals of a given concept.

Those examples show how to the :blue:`either ... or ...` construction.

.. container:: comment

    Example: The concepts of child, young thing, middle age thing and old
    thing are all pairwise disjoint. Their union is a person.

.. parsed-literal::

      :blue:`Something is a` person :blue:`if-and-only-if-it is a` child, :blue:`is a`
      young-thing, :blue:`is a` middle-age-thing :blue:`or is an` old-thing.

.. container:: comment

    Example: Enumerates individuals of a concept.

.. parsed-literal::

      Something :blue:`is a` my-birthday-guests :blue:`if-and-only-if-it is either`
      John, Mary :blue:`or` Bill.

Equal-to
--------

The construction :blue:`equal-to` assigns a data attribute. FluentEditor
currently supports the following data types: integer, real, boolean,
string and date-time.

Those examples show how to assign data attributes.

.. parsed-literal::

      John has-name :blue:`equal-to` 'John'.
      Lenka borns-on-date :blue:`equal-to` 1975-10-11.

Be (is/are)
-----------

The word :blue:`every` refers to all individuals of a given concept. Concepts
and individuals are defined when their names are used in at least one
sentence.

Those examples show how to use the word :blue:`every`.

.. container:: comment

    Example: A simple hierarchy of concepts. Lion and giraffe are
    inclusive of the concept of an animal.

.. parsed-literal::

      :blue:`Every` giraffe :blue:`is an` animal.
      :blue:`Every` lion :blue:`is an` animal.

.. container:: comment

    Example: A simple relation rule.

.. parsed-literal::

      :blue:`Every` giraffe eats :blue:`a` plant.


Every-single-thing
------------------

:blue:`Every-single-thing` indicates all individuals in the open-world
assumption. Individuals may be already defined in the ontology or not
defined but possibly existing. It is often used for writing axioms about
roles.

Those examples show how to use :blue:`every-single-thing` declaration to
indicate all existing individuals and specify more information about
them. Combined with :blue:`that` keyword, it can be used for writing more
complex rules.

.. container:: comment

    Example: All the individuals in the world are specified with the role
    'is-married-to' something.

.. parsed-literal::

      :blue:`Every-single-thing` is-married-to :blue:`something`.

.. container:: comment

    Example: If something has a wife, then it must be a person. Range of
    is-a-wife-of property is a person class

.. parsed-literal::

      :blue:`Every-single-thing` is-a-wife-of :blue:`nothing-but` persons.

.. container:: comment

    Example: Being someone's wife indicates being a woman. Domain of
    is-a-wife-of property is a woman class.

.. parsed-literal::

      :blue:`Every-single-thing that` is-a-wife-of :blue:`is a` woman.

.. container:: comment

    Example: Only people have names. If something has a data property
    value called name, then it is a person.

.. parsed-literal::

      :blue:`Every-single-thing that` has-name :blue:`(some value) is a` person.

.. container:: comment

    Example: Axiom on a role. 'to be part of' is reflexive.

.. parsed-literal::

      :blue:`Every-single-thing` is-part-of :blue:`itself`.

... for ... execute <? ... ?>
------------------------------

The construction :blue:`... for ... execute <? ... ?>` is used to express
`active rules <..\Index.html#activeRules>`__, which are a special case
of `SWRL rules <..\Grammar.html#swrl>`__. If an active rule is
fulfilled, some code/simple program can be executed. The concepts listed
after :blue:`for` are used as variables, while execute :blue:`<? ... ?>` is followed be
the code to be performed.

Different use cases of variables in semantic rules are summed up in the
table below.

Those examples show how to write active rules with the construction :blue:`...
for ... execute <? ... ?>`.

.. container:: comment

    Example: Active rule for an ontology monitoring the state of IT
    infrastructure.

.. parsed-literal::

      :blue:`If a` server :blue:`is` connected :blue:`by a thing and the thing` has-status
      Inoperable :blue:`then for the` server :blue:`and the thing execute`
      <?
      KnowledgeInsert(string.Format("{0} is connected by {1} and
      has-status Inoperable.", server, thing)); WriteMessage("[" +
      DateTime.Now + "] " + server + " is inoperable due to " + thing +
      ".");
      ?>.

Exists
------

:blue:`Exists` is equivalent to :blue:`is something` or :blue:`is a thing`. However, the first
may sound more natural sometimes. This is redundant in CNL, where a
concept is defined simply by using it. However, it might be usable in
translations from OWL to FE CNL.

Those examples show how to use the keyword :blue:`exists`.

.. parsed-literal::

      :blue:`If` John :blue:`exists then` John :blue:`is a` living-thing.

If-and-only-if
--------------

:blue:`If-and-only-if` expresses biconditional logical connective between two
statements. The statements should contain :blue:`X/Y` variables. It is used for
making axioms on properties/roles (such as **have-child** or
**have-sibling**).

Those examples show how make axioms on properties

.. container:: comment

    Example: has-child is reverse of has-parent.

.. parsed-literal::

      :blue:`X` has-child :blue:`Y if-and-only-if Y` has-parent :blue:`X`.

.. container:: comment

    Example: has-sibling is symmetric.

.. parsed-literal::

      :blue:`X` has-sibling :blue:`Y if-and-only-if Y` has-sibling :blue:`X`.

If-and-only-if-it
-----------------

:blue:`If-and-only-if-it` expresses biconditional logical connective between two
statements. In the construction :blue:`Something is ... if-and-only-if-it ...`
equivalent concepts are introduced. The concequent concept can be a
complex concept. It allows to define new concept as intersection, union
etc. of old ones. The construction can me modified to give complement
concept.

Those examples show how to define concepts.

.. container:: comment

    Example: Equivalent concepts.

.. parsed-literal::

      :blue:`Something is a` boy :blue:`if-and-only-if-it is a` young-male-man.

.. container:: comment

    Example: Concept intersection.

.. parsed-literal::

      :blue:`Something is a` young-male-man :blue:`if-and-only-if-it is a` young-thing
      :blue:`and is a` male-thing :blue:`and is a` person.

.. container:: comment

    Example: Everything in the world is an adult-thing or a young-thing.
    The concepts are complement.

.. parsed-literal::

      :blue:`Something is a` young-thing :blue:`if-and-only-if-it is not an`
      adult-thing.

If-and-only-if-it-either
------------------------

:blue:`If-and-only-if-it-either` expresses biconditional logical connective
between two statements. In the construction :blue:`Something is ...
if-and-only-if-it-either ...` a concept is defined as a disjoint union of
other concepts/individuals. It refers to OWL DisjointUnion.

The example shows how to define concepts as disjoint unions.

.. container:: comment

    Example: The classes: child, young, middle-age and old are disjoint
    and their sum creates a class person.

.. parsed-literal::

      :blue:`Something is a` person :blue:`**if-and-only-if-it-either** is a` child, :blue:`is
      a` young-thing, :blue:`is a` middle-age-thing :blue:`or is an` old-thing.

   This is equivalen to the following set of sentences:

.. parsed-literal::

      :blue:`Something is a` person :blue:`if-and-only-if-it is a` child :blue:`and-or is a`
      young-thing :blue:`and-or is a` middle-age-thing :blue:`and-or is an` old-thing.
      No child :blue:`is a` young-thing.
      No child :blue:`is a` middle-age-thing.
      No child :blue:`is an` old-thing.
      No young-thing :blue:`is a` middle-age-thing.
      No young-thing :blue:`is an` old-thing.
      No middle-age-thing :blue:`is` anold-thing.

.. container:: comment

    Example: We enumerate all individuals of a given concept. All
    individuals are disjoint.t.

.. parsed-literal::

      :blue:`Something is a` my-birthday-guest :blue:`if-and-only-if-it-either is`
      Monica, :blue:`is` Emilia, :blue:`is` Julia :blue:`or is` Anna.

.. container:: comment

    Example: A similar construction can be used for enumerating the
    individuals of a given concept. However, it does not imply that the
    individuals are disjoint. Concept cannot be enumerated in such way.

.. parsed-literal::

      :blue:`Something is a` my-birthday-guest :blue:`if-and-only-if-it is either`
      Monica, Emilia, Julia :blue:`or` Anna.
      Emilia :blue:`is` Julia.

If ... then ...
---------------

The construction :blue:`If ... then ...` is used for making an implication
(semantic rule). The construction can be used for two purposes: `SWRL
rules <..\Grammar.html#swrl>`__ and axioms about properties/roles. In
SWRL rules about concept and roles variables appear with a/the prefix.
There is a special class of semantic rules - axioms about roles in which
X/Y variables are used. Both antecedent and consequent clauses in the
implication can be complex sentences.

Different use cases of variables in semantic rules are summed up in the
table below.

.. list-table::
    :widths: 25 25
    :header-rows: 1
    :align: center

    * - Variable pattern
      - Use case
    * - :blue:`a/the class-name`
      - denotes variables assigned to a class/concept
    * - :blue:`a/the thing`
      - represents an instance of the top concept "owl:Thing" and thus any variable in the SR-CE sentence.
    * - :blue:`a/the class-name(n)/thing(n)`
      - different numbers in parenthesis mark more variables of the same type.


Those examples show how to write semantic rules with the construction :blue:`If
... then ....`

.. container:: comment

    Example: Axiom about role. Semantic rule for general role inclusion.

.. parsed-literal::

      :blue:`If X` is-proper-part-of :blue:`Y then X` is-part-of :blue:`Y`.

.. container:: comment

    Example: In the semantic rule below, there is one variable 'person'.

.. parsed-literal::

      :blue:`If a` person is-year-old :blue:`greater-or-equal-to 18 then the` person :blue:`is
      an` adult-person.

.. container:: comment

    Example: In the semantic rule below, there is one variable which
    could be anything.

.. parsed-literal::

      :blue:`If a thing is a` person :blue:`then the thing` has-name :blue:`(some string
      value)`.

Is-(not-)the-same-as
--------------------

:blue:`Is-the-same-as` expresses that two individuals are the same.
:blue:`Is-not-the-same-as` expresses that two individuals are different. It
refers to OWL sameAs axiom. It should not be replaced with expressions
is or :blue:`is not`.

Those examples show how to use the keywords and why they should not be
replaced with :blue:`is` or :blue:`is not`.

.. container:: comment

    Example: A cat and its owner are different.

.. parsed-literal::

      :blue:`If a` man has-pet :blue:`a` cat :blue:`then the` man is-not-the-same-as :blue:`the` cat.

.. container:: comment

    Example: Here, the person is the same individual as the human
    mentioned in the antecedent part of the rule.

.. parsed-literal::

      :blue:`If a` person has-synonym a human :blue:`then` the person :blue:`is-the-same-as the`
      human.

.. container:: comment

    Example: Here, the consequent clause the person is the human means
    that there exists (somewhere) some unspecified individual of human
    class which is the same individual as the person mentioned.

.. parsed-literal::

      :blue:`If a` person has-synonym :blue:`a` human :blue:`then the` person :blue:`is the` human.

Is-unique-if
------------

:blue:`Is-unique-if` defines a key for a class. Keys are for uniquely
identifying an individual. The open-world assumption does not imply that
some things (e.g. concepts, instances) are disjoint if they are named
differently. The uniqueness of two individuals can be inferred from
rules. If two named instances of the class coincide on values for each
of key properties, then these two individuals are the same. With more
complex construction, two individuals can be reasoned to be different
from each other.

OWL 1 does not provide a means to define keys. The OWL 2 construct
HasKey allows keys to be defined for a given class.

Those examples show how to define and use keys in CNL.

.. container:: comment

    Example: Tom and Mark can be theoretically the same individual.

.. parsed-literal::

      Tom :blue:`is a` man.
      Mark :blue:`is a` man.

.. container:: comment

    Example: Tom and Mark are reasoned to be different individuals.

.. parsed-literal::

      :blue:`Every X that is a` man :blue:`is-unique-if X` has-id :blue:`equal-to something`.
      :blue:`Every` man has-id :blue:`one (some integer value)`.
      Mark :blue:`is a` man :blue:`and` has-id :blue:`equal-to 11`.
      Tom :blue:`is a` man :blue:`and` has-id :blue:`equal-to 12`.

.. container:: comment

    Example: Tom and Mark are reasoned to be the same individuals.

.. parsed-literal::

    :blue:`Every X that is a` man :blue:`is-unique-if X` has-id :blue:`equal-to something`.
    :blue:`Every` man has-id :blue:`one (some integer value)`.
    Mark :blue:`is a` man :blue:`and` has-id :blue:`equal-to 11`.
    Tom :blue:`is a` man :blue:`and` has-id :blue:`equal-to 11`.

It
--

It Is a variable for :blue:`something` in semantic rules.

The example shows how to use the keyword.

.. container:: comment

    Example: Two sentences below are equivalent.

.. parsed-literal::

      :blue:`Every` cat :blue:`is an` animal.
      :blue:`If something is a` cat :blue:`then it is an` animal.

Itself
------

:blue:`Itself` is used when the subject and the object of the role are the same.
If every subject of a concept acts with a role on itself, then the role
is reflexive for the concept.

Those examples show how to use and how not to use the keyword :blue:`itself`.

.. parsed-literal::

      Leo likes :blue:`itself`.

.. container:: comment

    Example: The role 'to like' is reflexive for men.

.. parsed-literal::

      :blue:`Every` man likes :blue:`itself`.

.. container:: comment

    Example: The role of parent is irreflexive.

.. parsed-literal::

      :blue:`Nothing` is-parent-of :blue:`itself`.

Can/can-not/must/must-not/should/should-not
-------------------------------------------

Modal expressions are used for stating restrictions on the knowledge. In
open world assumption the validity of the statement should not change
after introducing additional knowledge. Therefore the validity of some
statements is unknown. However, additional requirements for the ontology
are stated with modal expressions. This allows user to express knowledge
about knowledge. Modal expressions do not have a direct representation
with OWL. They are an advanced feature supported by FluentEditor. FE now
supports only simple requirements, i.e. :blue:`Every (class-name) (modality)
(role/date-property) (value)`. The reasoner checks if all modal
expressions are fulfilled. Results of validation shall be highlighted in
different colors. Green means all requirements are fulfilled. Red means
an error, that some requirements are not fulfilled. It appears when
requirements with :blue:`must` or :blue:`can-not` expression is not fulfilled. Yellow
means a warning. It appears when requirements with :blue:`should, should-not,
can, or must-not` expression is not fulfilled.


The examples below show how to use modal expressions to specify
knowledge about knowledge.

.. parsed-literal::

      :blue:`Every` patient :blue:`must` have-age :blue:`(some integer value)`.
      :blue:`Every` patient :blue:`can` have-medical-history :blue:`(some string value)`.
      :blue:`Every` application :blue:`must` have-status :blue:`a thing that is either` Operable or Inoperable.
      :blue:`Every` patient :blue:`can-not` have-age :blue:`greater-than 200`.

No
--

The word :blue:`no` is used for negating a concept.

Those examples show how to use no to negate a concept.

.. container:: comment

    Example: The classes of young-thing and adult-thing are disjoint.
    However, there might exist a thing that is neither of them.

.. parsed-literal::

      :blue:`No` young-thing :blue:`is an` adult-thing.

None
----

:blue:`None` is a short version of :blue:`nothing-but things that are nothing`.

The example shows how to use the keyword.

.. container:: comment

    Example: Two sentences below are equivalent.

.. parsed-literal::

      :blue:`Every` root-folder has-parent :blue:`none`.
      :blue:`Every` root-folder has-parent :blue:`nothing-but things that are nothing`.

Nothing
-------

:blue:`Nothing` refers to the bottom concept (empty set). It describes concepts
that cannot have any individuals. It is used for specifying restrictions
on the roles.

Those examples show how to use the keyword :blue:`nothing`.

.. container:: comment

    Example: The role of parent is irreflexive.

.. parsed-literal::

      :blue:`Nothing` is-parent-of :blue:`itself`.

.. container:: comment

    Example: Two sentences below are equivalent.

.. parsed-literal::

      :blue:`Nothing` is-part-of :blue:`at-least two things`.
      :blue:`Every-single-thing` is-part-of :blue:`at-most one thing`.

.. container:: comment

    Example: A more complex restriction on roles.

.. parsed-literal::

      :blue:`Nothing is a` dead-body :blue:`that` has-inside a living-thing.

Nothing-but
-----------

:blue:`Nothing-but` gives restriction on range of a property (role) or data type
of a data property. It is so called universal role restriction.

Those examples show how to restrict range of a role or data type of a
data property.

.. container:: comment

    Example: If something has a wife, then it must be a person. Range of
    is-a-wife-of property is a person class

.. parsed-literal::

      :blue:`Every-single-thing` is-a-wife-of :blue:`nothing-but` persons.

.. container:: comment

    Example: Names are words. The name property can be only of type
    string.

.. parsed-literal::

      :blue:`Every-single-thing` has-name :blue:`nothing-but (some string value)`.

Or-something-else
-----------------

:blue:`Or-something-else` refers to the disjoint concept statement that starts
with :blue:`Anything` keyword. This statement allows to enumerate pairwise
disjoint concepts.

The example shows how to use the keyword.

.. container:: comment

    Example: The concepts of cat, dog and human are pairwise disjoint.
    However, there may exist individuals that are none of them.

.. parsed-literal::

      :blue:`Anything either is a` cat :blue:`or is a` dog or :blue:`is a` human
      :blue:`or-something-else`.

Something
---------

The word :blue:`something` has different meanings depending on the context. It
represents all individuals (top concept). It represents any data value
of any data type. It starts a general rule about all things. It can be
used for defining a new concept as an intersection or union of other
concepts or for stating that two concepts are equivalent.

This example shows how to use the keyword :blue:`something` in different ways.

.. container:: comment

    Example: Top concept, equivalent to a thing.

.. parsed-literal::

      John :blue:`is something`.
      John :blue:`is a thing`.

.. container:: comment

    Example: Any data value of any data type.

.. parsed-literal::

      :blue:`Every X that is a` man :blue:`is-unique-if X` has-id :blue:`equal-to something`.

.. container:: comment

    Example: Concept equivalence.

.. parsed-literal::

      :blue:`Something is a` boy :blue:`if-and-only-if-it is a` young-male-man.

.. container:: comment

    Example: Concept intersection.

.. parsed-literal::

      :blue:`Something is a` young-male-man :blue:`if-and-only-if-it is a` young-thing
      and :blue:`is a` male-thing and :blue:`is a` person.

.. container:: comment

    Example: Concept union.

.. parsed-literal::

      :blue:`Something is a` child :blue:`if-and-only-if-it is a` boy :blue:`and-or is a` girl.

(some boolean/datetime/integer/string value)
--------------------------------------------

These keywords refer to a data property of a specified type (see table).

.. list-table::
    :widths: 25 25
    :header-rows: 1
    :align: center

    * - Keywords
      - Meaning
    * - :blue:`(some value)`
      - equivalent to rdfs:Literal. This can take any data type but without knowing what type this data is.
    * - :blue:`(some integer value)`
      - equivalent to xsd:int.
    * - :blue:`(some real value)`
      - equivalent to xsd:double.
    * - :blue:`(some boolean value)`
      - equivalent to xsd:boolean.
    * - :blue:`(some string value)`
      - equivalent to xsd:string.
    * - :blue:`(some datetime value)`
      - equivalent to xsd:datetime.

The example shows how to specify data type of a data property.

.. container:: comment

    Example: We specify the data type of name to be string and the data
    type of age to be integer.

.. parsed-literal::

      :blue:`Every` person has-name :blue:`nothing-but (some string value)`.
      :blue:`Every` person has-age :blue:`nothing-but (some integer value)`.

That
----

The word :blue:`that` starts further specification (restriction) of a concept.
Restriction can be arbitrary complex. :blue:`Every-single-thing that ...`
construction is used frequently and means the whole class of things
satisfying the restriction.

This example shows how to use the construction :blue:`that` ... in CNL
sentences.

.. container:: comment

    Example: Restrictions on the concept of application.

.. parsed-literal::

      :blue:`Every` application :blue:`must have-status a thing that is either` Operable
      :blue:`or` Inoperable.

.. container:: comment

    Example: Every-single-thing that ... construction occurs quite often
    in CNL. Here, it refers to the whole class of young things.

.. parsed-literal::

      :blue:`Every-single-thing that is a` young-thing :blue:`is not an` adult-thing.

that-has-length
---------------

The construction that-has-length restricts length of a data attribute.

The example below shows how to restrict length of a data attribute.

.. parsed-literal::

      :blue:`Every` cat has-name :blue:`that-has-length lower-or-equal-to 10`.

That-matches-pattern
--------------------

The construction :blue:`that-matches-pattern` defines string attributes as
regular expression patterns, both in ontology and questions. It is a new
feature in CogniPy. This functionality allows to specify not
only one particular string as attribute, but also a whole set (or class)
of strings defined by regular expression. This keyword may appear insted
of :blue:`equal-to` or :blue:`(some value)` keyword. It can be used in questions.

The example below shows how to use regular expressions in CNL.

.. container:: comment

    Example: Rule for female names in Polish language - it must end with
    'a' sound.

.. parsed-literal::

      :blue:`Every-single-thing that` has-name :blue:`that-matches-pattern` '.*a' :blue:`is a`
      female-person.

The
---

English definite article :blue:`the` indicates some specified individual of a
given concept. An individual of a given concept is specified by
mentioning it earlier in the sentence. Variables appear in semantic
rules. When a variable occurs for the first time it is preceeded by :blue:`a/an`
and later by :blue:`the`.

Different use cases of variables in semantic rules are summed up in the
table below.

:blue:`THE-" "` is used to define an instance with a custom identifier, other
that combination of words starting with capital letter and numbers
separated by dashes.

.. list-table::
    :widths: 25 25
    :header-rows: 1
    :align: center

    * - Variable pattern
      - Use case
    * - :blue:`a/the class-name`
      - denotes variables assigned to a class/concept
    * - :blue:`a/the thing`
      - represents an instance of the top concept "owl:Thing" and thus any variable in the SR-CE sentence.
    * - :blue:`a/the class-name(n)/thing(n)`
      - different number in parenthesis marks more variables of the same type.

This example shows how to use definite article the while introducing
variables into semantic rules.

.. container:: comment

    Example: In the semantic rule below, there is one variable 'person'.

.. parsed-literal::

      :blue:`If a` person is-year-old :blue:`greater-or-equal-to 18 then the` person :blue:`is
      an` adult-person.

.. container:: comment

    Example: In the semantic rule below, there is one variable which
    could be anything.

.. parsed-literal::

      :blue:`If a` thing is a` person :blue:`then the thing` has-name :blue:`(some string
      value)`.

.. container:: comment

    Example: In the semantic rule below, there are two variables
    'person'.

.. parsed-literal::

      :blue:`If a` person(1) has-parent :blue:`a` person(2) :blue:`and the` person(2) :blue:`is a`
      female-person :blue:`then the` person(1) has-mother the person(2).

.. container:: comment

    Example: Custom instance identifier.

.. parsed-literal::

      :blue:`THE-"K22 P2"`

The-one-and-the-only
--------------------

:blue:`The-one-and-the-only` is used to define a class which has only one
instance. It refers to an instance of an concept that defines the
concept and is the only instance of the concept.

The example shows how to use the keyword.

.. container:: comment

    Example: The first sentence is equivalent to the combination of the
    second and the third sentence.

.. parsed-literal::

      :blue:`The-one-and-only` singlethon :blue:`is a` cat.
      :blue:`The` singlethon :blue:`is a` cat.
      :blue:`Every` singlethon :blue:`is the` singlethon.

Thing/things
------------

The word :blue:`thing` represents an instance of the top concept "owl:Thing" and
thus any variable in the SR-CE sentence. Used as a variable in semantic
rules. If more variables of type :blue:`thing` appear, they can be enumerated by
thing(n).

Those examples show how to use the word :blue:`thing` in semantic rules.

.. container:: comment

    Example: In the semantic rule below, there is one variable which
    could be anything.

.. parsed-literal::

      :blue:`If a thing is a` person :blue:`then the thing` has-name :blue:`(some string
      value)`.

.. container:: comment

    Example: In the semantic rule below, there are two variables 'thing'.

.. parsed-literal::

      :blue:`If a thing(1)` hosts :blue:`a thing(2) and the thing(2)` hosts :blue:`an`
      application :blue:`then the thing(1)` hosts :blue:`the` application.
