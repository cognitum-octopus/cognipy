﻿/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, The University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
using System;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using org.semanticweb.owlapi.apibinding;
using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.util;
using org.semanticweb.owlapi.io;
using org.coode.owlapi.manchesterowlsyntax;
using org.semanticweb.owlapi.vocab;
using System.Collections.Generic;
using org.semanticweb.owlapi.reasoner;
using org.semanticweb.owlapi.reasoner.structural;
using uk.ac.manchester.cs.owlapi.modularity;


/**
 * @author Matthew Horridge, The University Of Manchester, Bio-Health Informatics
 *         Group, Date: 11-Jan-2007
 */

/**
 * ADAPTED FOR .NET BY
 * @author Alessandro Seganti, a.seganti@cognitum.eu, Cognitum Poland
 *         , Date: 18-Aug-2014
 */

namespace Cognitum.OwlApi.Net.Test{
public class Examples {

    private static String PIZZA_IRI = "http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/pizza.owl";

    /**
     * The examples here show how to load ontologies.
     * 
     * @throws OWLOntologyCreationException
     */
    public void shouldLoad(){
        // Get hold of an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // Let's load an ontology from the web
        string pizzaIRI = "http://130.88.198.11/co-ode-files/ontologies/pizza.owl";
        IRI iri = IRI
                .create(pizzaIRI);
        OWLOntology pizzaOntology = manager
                .loadOntologyFromOntologyDocument(iri);
        Console.WriteLine("Loaded ontology: " + pizzaOntology);
        // Remove the ontology so that we can load a local copy.
        manager.removeOntology(pizzaOntology);
        // We can also load ontologies from files. Download the pizza ontology
        // from http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/pizza.owl
        // and put it
        // somewhere on your hard drive Create a file object that points to the
        // local copy
        java.io.File file = new java.io.File(System.IO.Directory.GetCurrentDirectory()+@"\Ontologies\pizza.owl");
        // Now load the local copy
        OWLOntology localPizza = manager.loadOntologyFromOntologyDocument(file);
        Console.WriteLine("Loaded ontology: " + localPizza);
        // We can always obtain the location where an ontology was loaded from
        IRI documentIRI = manager.getOntologyDocumentIRI(localPizza);
        Console.WriteLine("    from: " + documentIRI);
        // Remove the ontology again so we can reload it later
        manager.removeOntology(pizzaOntology);
        // In cases where a local copy of one of more ontologies is used, an
        // ontology IRI mapper can be used to provide a redirection mechanism.
        // This means that ontologies can be loaded as if they were located on
        // the web. In this example, we simply redirect the loading from
        // http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/pizza.owl to
        // our local copy
        // above.
        manager.addIRIMapper(new SimpleIRIMapper(iri, IRI.create(file)));
        // Load the ontology as if we were loading it from the web (from its
        // ontology IRI)
        IRI pizzaOntologyIRI = IRI
                .create(pizzaIRI);
        OWLOntology redirectedPizza = manager.loadOntology(pizzaOntologyIRI);
        Console.WriteLine("Loaded ontology: " + redirectedPizza);
        Console.WriteLine("    from: "
                + manager.getOntologyDocumentIRI(redirectedPizza));
        // Note that when imports are loaded an ontology manager will be
        // searched for mappings
    }

    /**
     * This example shows how an ontology can be saved in various formats to
     * various locations and streams.
     * 
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     * @throws IOException
     */
    public void shouldSaveOntologies(){
        // Get hold of an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // Let's load an ontology from the web. We load the ontology from a
        // document IRI
        IRI documentIRI = IRI
                .create("http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/pizza.owl");
        OWLOntology pizzaOntology = manager
                .loadOntologyFromOntologyDocument(documentIRI);
        Console.WriteLine("Loaded ontology: " + pizzaOntology);
        // Now save a local copy of the ontology. (Specify a path appropriate to
        // your setup)
        java.io.File file = java.io.File.createTempFile("owlapiexamples", "saving");
        manager.saveOntology(pizzaOntology, IRI.create(file.toURI()));
        // By default ontologies are saved in the format from which they were
        // loaded. In this case the ontology was loaded from an rdf/xml file We
        // can get information about the format of an ontology from its manager
        OWLOntologyFormat format = manager.getOntologyFormat(pizzaOntology);
        // We can save the ontology in a different format Lets save the ontology
        // in owl/xml format
        OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
        // Some ontology formats support prefix names and prefix IRIs. In our
        // case we loaded the pizza ontology from an rdf/xml format, which
        // supports prefixes. When we save the ontology in the new format we
        // will copy the prefixes over so that we have nicely abbreviated IRIs
        // in the new ontology document
        if (format.isPrefixOWLOntologyFormat()) {
            owlxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        manager.saveOntology(pizzaOntology, owlxmlFormat,
                IRI.create(file.toURI()));
        // We can also dump an ontology to System.out by specifying a different
        // OWLOntologyOutputTarget Note that we can write an ontology to a
        // stream in a similar way using the StreamOutputTarget class
        OWLOntologyDocumentTarget documentTarget = new SystemOutDocumentTarget();
        // Try another format - The Manchester OWL Syntax
        ManchesterOWLSyntaxOntologyFormat manSyntaxFormat = new ManchesterOWLSyntaxOntologyFormat();
        if (format.isPrefixOWLOntologyFormat()) {
            manSyntaxFormat
                    .copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        manager.saveOntology(pizzaOntology, manSyntaxFormat,
                new StreamDocumentTarget(new java.io.ByteArrayOutputStream()));
        file.delete();
    }

    /**
     * This example shows how to get access to objects that represent entities.
     * 
     * @throws OWLOntologyCreationException
     */
    public void shouldAccessEntities(){
        // In order to get access to objects that represent entities we need a
        // data factory.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // We can get a reference to a data factory from an OWLOntologyManager.
        OWLDataFactory factory = manager.getOWLDataFactory();
        // In OWL, entities are named objects that are used to build class
        // expressions and axioms. They include classes, properties (object,
        // data and annotation), named individuals and datatypes. All entities
        // may be obtained from an OWLDataFactory. Let's create an object to
        // represent a class. In this case, we'll choose
        // http://www.semanticweb.org/owlapi/ontologies/ontology#A as the IRI
        // for our class. There are two ways we can create classes (and other
        // entities). The first is by specifying the full IRI. First we create
        // an IRI object:
        IRI iri = IRI
                .create("http://www.semanticweb.org/owlapi/ontologies/ontology#A");
        // Now we create the class
        OWLClass clsAMethodA = factory.getOWLClass(iri);
        // The second is to use a prefix manager and specify abbreviated IRIs.
        // This is useful for creating lots of entities with the same prefix
        // IRIs. First create our prefix manager and specify that the default
        // prefix IRI (bound to the empty prefix name) is
        // http://www.semanticweb.org/owlapi/ontologies/ontology#
        PrefixManager pm = new DefaultPrefixManager(
                "http://www.semanticweb.org/owlapi/ontologies/ontology#");
        // Now we use the prefix manager and just specify an abbreviated IRI
        OWLClass clsAMethodB = factory.getOWLClass(":A", pm);
        // Note that clsAMethodA will be equal to clsAMethodB because they are
        // both OWLClass objects and have the same IRI. Creating entities in the
        // above manner does not "add them to an ontology". They are merely
        // objects that allow us to reference certain objects (classes etc.) for
        // use in class expressions, and axioms (which can be added to an
        // ontology). Lets create an ontology, and add a declaration axiom to
        // the ontology that declares the above class
        OWLOntology ontology = manager
                .createOntology(IRI
                        .create("http://www.semanticweb.org/owlapi/ontologies/ontology"));
        // We can add a declaration axiom to the ontology, that essentially adds
        // the class to the signature of our ontology.
        OWLDeclarationAxiom declarationAxiom = factory
                .getOWLDeclarationAxiom(clsAMethodA);
        manager.addAxiom(ontology, declarationAxiom);
        // Note that it isn't necessary to add declarations to an ontology in
        // order to use an entity. For some ontology formats (e.g. the
        // Manchester Syntax), declarations will automatically be added in the
        // saved version of the ontology.
    }

    /**
     * This example shows how to create dataranges.
     * 
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public void shouldBuildDataRanges(){
        // OWLDataRange is the superclass of all data ranges in the OWL API.
        // Data ranges are used as the types of literals, as the ranges for data
        // properties, as filler for data reatrictions. Get hold of a manager to
        // work with
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        // OWLDatatype represents named datatypes in OWL. These are a bit like
        // classes whose instances are data values OWLDatatype objects are
        // obtained from a data factory. The OWL2Datatype enum defines built in
        // OWL 2 Datatypes Get hold of the integer datatype
        OWLDatatype integer = factory.getOWLDatatype(OWL2Datatype.XSD_INTEGER
                .getIRI());
        // For common data types there are some convenience methods of
        // OWLDataFactory. For example
        OWLDatatype integerDatatype = factory.getIntegerOWLDatatype();
        OWLDatatype floatDatatype = factory.getFloatOWLDatatype();
        OWLDatatype doubleDatatype = factory.getDoubleOWLDatatype();
        OWLDatatype booleanDatatype = factory.getBooleanOWLDatatype();
        // The top datatype (analgous to owl:Thing) is rdfs:Literal, which can
        // be obtained from the data factory
        OWLDatatype rdfsLiteral = factory.getTopDatatype();
        // Custom data ranges can be built up from these basic datatypes. For
        // example, it is possible to restrict a datatype using facets from XML
        // Schema Datatypes. For example, lets create a data range that
        // describes integers that are greater or equal to 18 To do this, we
        // restrict the xsd:integer datatype using the xsd:minInclusive facet
        // with a value of 18. Get hold of a literal that is an integer value 18
        OWLLiteral eighteen = factory.getOWLLiteral(18);
        // Now create the restriction. The OWLFacet enum provides an enumeration
        // of the various facets that can be used
        OWLDatatypeRestriction integerGE18 = factory.getOWLDatatypeRestriction(
                integer, OWLFacet.MIN_INCLUSIVE, eighteen);
        // We could use this datatype in restriction, as the range of data
        // properties etc. For example, if we want to restrict the range of the
        // :hasAge data property to 18 or more we specify its range as this data
        // range
        PrefixManager pm = new DefaultPrefixManager(
                "http://www.semanticweb.org/ontologies/dataranges#");
        OWLDataProperty hasAge = factory.getOWLDataProperty(":hasAge", pm);
        OWLDataPropertyRangeAxiom rangeAxiom = factory
                .getOWLDataPropertyRangeAxiom(hasAge, integerGE18);
        OWLOntology ontology = manager.createOntology(IRI
                .create("http://www.semanticweb.org/ontologies/dataranges"));
        // Add the range axiom to our ontology
        manager.addAxiom(ontology, rangeAxiom);
        // For creating datatype restrictions on integers or doubles there are
        // some convenience methods on OWLDataFactory For example: Create a data
        // range of integers greater or equal to 60
        OWLDatatypeRestriction integerGE60 = factory
                .getOWLDatatypeMinInclusiveRestriction(60);
        // Create a data range of integers less than 16
        OWLDatatypeRestriction integerLT16 = factory
                .getOWLDatatypeMaxExclusiveRestriction(18);
        // In OWL 2 it is possible to represent the intersection, union and
        // complement of data types For example, we could create a union of data
        // ranges of the data range integer less than 16 or integer greater or
        // equal to 60
        OWLDataUnionOf concessionaryAge = factory.getOWLDataUnionOf(
                integerLT16, integerGE60);
        // We can also coin names for custom data ranges. To do this we use an
        // OWLDatatypeDefintionAxiom Get hold of a named datarange (datatype)
        // that will be used to assign a name to our above datatype
        OWLDatatype concessionaryAgeDatatype = factory.getOWLDatatype(
                ":ConcessionaryAge", pm);
        // Now create a datatype definition axiom
        OWLDatatypeDefinitionAxiom datatypeDef = factory
                .getOWLDatatypeDefinitionAxiom(concessionaryAgeDatatype,
                        concessionaryAge);
        // Add the definition to our ontology
        manager.addAxiom(ontology, datatypeDef);
        // Dump our ontology
        manager.saveOntology(ontology, new StreamDocumentTarget(
                new java.io.ByteArrayOutputStream()));
    }

    /**
     * This example shows how to work with dataranges. OWL 1.1 allows data
     * ranges to be created by taking a base datatype e.g. int, string etc. and
     * then by applying facets to restrict the datarange. For example, int
     * greater than 18
     * 
     * @throws OWLOntologyCreationException
     */
    public void shouldUseDataranges(){
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        String baseUri = "http://org.semanticweb.datarangeexample";
        OWLOntology ont = man.createOntology(IRI.create(baseUri));
        // We want to add an axiom to our ontology that states that adults have
        // an age greater than 18. To do this, we will create a restriction
        // along a hasAge property, with a filler that corresponds to the set of
        // integers greater than 18. First get a reference to our hasAge
        // property
        OWLDataFactory factory = man.getOWLDataFactory();
        OWLDataProperty hasAge = factory.getOWLDataProperty(IRI.create(baseUri
                + "hasAge"));
        // For completeness, we will make hasAge functional by adding an axiom
        // to state this
        OWLFunctionalDataPropertyAxiom funcAx = factory
                .getOWLFunctionalDataPropertyAxiom(hasAge);
        man.applyChange(new AddAxiom(ont, funcAx));
        // Now create the data range which correponds to int greater than 18. To
        // do this, we get hold of the int datatype and then restrict it with a
        // minInclusive facet restriction.
        OWLDatatype intDatatype = factory.getIntegerOWLDatatype();
        // Create the value "18", which is an int.
        OWLLiteral eighteenConstant = factory.getOWLLiteral(18);
        // Now create our custom datarange, which is int greater than or equal
        // to 18. To do this, we need the minInclusive facet
        OWLFacet facet = OWLFacet.MIN_INCLUSIVE;
        // Create the restricted data range by applying the facet restriction
        // with a value of 18 to int
        OWLDataRange intGreaterThan18 = factory.getOWLDatatypeRestriction(
                intDatatype, facet, eighteenConstant);
        // Now we can use this in our datatype restriction on hasAge
        OWLClassExpression thingsWithAgeGreaterOrEqualTo18 = factory
                .getOWLDataSomeValuesFrom(hasAge, intGreaterThan18);
        // Now we want to say all adults have an age that is greater or equal to
        // 18 - i.e. Adult is a subclass of hasAge some int[>= 18] Obtain a
        // reference to the Adult class
        OWLClass adult = factory.getOWLClass(IRI.create(baseUri + "#Adult"));
        // Now make adult a subclass of the things that have an age greater to
        // or equal to 18
        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(adult,
                thingsWithAgeGreaterOrEqualTo18);
        // Add our axiom to the ontology
        man.applyChange(new AddAxiom(ont, ax));
    }

    /**
     * 
     */
    public void shouldInstantiateLiterals() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        // Get an plain literal with an empty language tag
        OWLLiteral literal1 = factory.getOWLLiteral("My string literal", "");
        // Get an untyped string literal with a language tag
        OWLLiteral literal2 = factory.getOWLLiteral("My string literal", "en");
        // Typed literals are literals that are typed with a datatype Create a
        // typed literal to represent the integer 33
        OWLDatatype integerDatatype = factory
                .getOWLDatatype(OWL2Datatype.XSD_INTEGER.getIRI());
        OWLLiteral literal3 = factory.getOWLLiteral("33", integerDatatype);
        // There is are short cut methods on OWLDataFactory for creating typed
        // literals with common datatypes Internallym these methods create
        // literals as above Create a literal to represent the integer 33
        OWLLiteral literal4 = factory.getOWLLiteral(33);
        // Create a literal to represent the double 33.3
        OWLLiteral literal5 = factory.getOWLLiteral(33.3);
        // Create a literal to represent the boolean value true
        OWLLiteral literal6 = factory.getOWLLiteral(true);
    }

    /**
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     * @throws IOException
     */
    public void shouldLoadAndSave(){
        // A simple example of how to load and save an ontology We first need to
        // obtain a copy of an OWLOntologyManager, which, as the name suggests,
        // manages a set of ontologies. An ontology is unique within an ontology
        // manager. Each ontology knows its ontology manager. To load multiple
        // copies of an ontology, multiple managers would have to be used.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // We load an ontology from a document IRI - in this case we'll load the
        // pizza ontology.
        IRI documentIRI = IRI.create(PIZZA_IRI);
        // Now ask the manager to load the ontology
        OWLOntology ontology = manager
                .loadOntologyFromOntologyDocument(documentIRI);
        // Print out all of the classes which are contained in the signature of
        // the ontology. These are the classes that are referenced by axioms in
        // the ontology.
        var clsIt = ontology.getClassesInSignature().iterator();
        while(clsIt.hasNext()){
            OWLClass cls = (OWLClass)clsIt.next();
            Console.WriteLine(cls.ToString());
        }
        // Now save a copy to another location in OWL/XML format (i.e. disregard
        // the format that the ontology was loaded in).
        java.io.File f = java.io.File.createTempFile("owlapiexample", "example1.xml");
        IRI documentIRI2 = IRI.create(f);
        manager.saveOntology(ontology, new OWLXMLOntologyFormat(), documentIRI2);
        // Remove the ontology from the manager
        manager.removeOntology(ontology);
        f.delete();
    }

    /**
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public void shouldAddAxiom(){
        // Create the manager that we will use to load ontologies.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // Ontologies can have an IRI, which is used to identify the ontology.
        // You should think of the ontology IRI as the "name" of the ontology.
        // This IRI frequently resembles a Web address (i.e. http://...), but it
        // is important to realise that the ontology IRI might not necessarily
        // be resolvable. In other words, we can't necessarily get a document
        // from the URL corresponding to the ontology IRI, which represents the
        // ontology. In order to have a concrete representation of an ontology
        // (e.g. an RDF/XML file), we MAP the ontology IRI to a PHYSICAL URI. We
        // do this using an IRIMapper Let's create an ontology and name it
        // "http://www.co-ode.org/ontologies/testont.owl" We need to set up a
        // mapping which points to a concrete file where the ontology will be
        // stored. (It's good practice to do this even if we don't intend to
        // save the ontology).
        IRI ontologyIRI = IRI
                .create("http://www.co-ode.org/ontologies/testont.owl");
        // Create the document IRI for our ontology
        IRI documentIRI = IRI.create("file:/tmp/MyOnt.owl");
        // Set up a mapping, which maps the ontology to the document IRI
        SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
        manager.addIRIMapper(mapper);
        // Now create the ontology - we use the ontology IRI (not the physical
        // URI)
        OWLOntology ontology = manager.createOntology(ontologyIRI);
        // Now we want to specify that A is a subclass of B. To do this, we add
        // a subclass axiom. A subclass axiom is simply an object that specifies
        // that one class is a subclass of another class. We need a data factory
        // to create various object from. Each manager has a reference to a data
        // factory that we can use.
        OWLDataFactory factory = manager.getOWLDataFactory();
        // Get hold of references to class A and class B. Note that the ontology
        // does not contain class A or classB, we simply get references to
        // objects from a data factory that represent class A and class B
        OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
        OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));
        // Now create the axiom
        OWLAxiom axiom = factory.getOWLSubClassOfAxiom(clsA, clsB);
        // We now add the axiom to the ontology, so that the ontology states
        // that A is a subclass of B. To do this we create an AddAxiom change
        // object. At this stage neither classes A or B, or the axiom are
        // contained in the ontology. We have to add the axiom to the ontology.
        AddAxiom addAxiom = new AddAxiom(ontology, axiom);
        // We now use the manager to apply the change
        manager.applyChange(addAxiom);
        // The ontology will now contain references to class A and class B -
        // that is, class A and class B are contained within the SIGNATURE of
        // the ontology let's print them out
        var clsIt = ontology.getClassesInSignature().iterator();
        while(clsIt.hasNext()){
            OWLClass cls = (OWLClass)clsIt.next();
            Console.WriteLine("Referenced class: "+ cls.ToString());
        }
        // We should also find that B is an ASSERTED superclass of A
        var superClassesIt = clsA.getSuperClasses(ontology).iterator();
        Console.WriteLine("Asserted superclasses of " + clsA + ":");
        while(superClassesIt.hasNext()) {
            OWLClassExpression desc = (OWLClassExpression) superClassesIt.next();
            Console.WriteLine(desc);
        }
        // Now save the ontology. The ontology will be saved to the location
        // where we loaded it from, in the default ontology format
        manager.saveOntology(ontology);
    }

    /**
     * These examples show how to create new ontologies.
     * 
     * @throws OWLOntologyCreationException
     */
    public void shouldCreateOntology(){
        // We first need to create an OWLOntologyManager, which will provide a
        // point for creating, loading and saving ontologies. We can create a
        // default ontology manager with the OWLManager class. This provides a
        // common setup of an ontology manager. It registers parsers etc. for
        // loading ontologies in a variety of syntaxes
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // In OWL 2, an ontology may be named with an IRI (Internationalised
        // Resource Identifier) We can create an instance of the IRI class as
        // follows:
        IRI ontologyIRI = IRI
                .create("http://www.semanticweb.org/ontologies/myontology");
        // Here we have decided to call our ontology
        // "http://www.semanticweb.org/ontologies/myontology" If we publish our
        // ontology then we should make the location coincide with the ontology
        // IRI Now we have an IRI we can create an ontology using the manager
        OWLOntology ontology = manager.createOntology(ontologyIRI);
        Console.WriteLine("Created ontology: " + ontology);
        // In OWL 2 if an ontology has an ontology IRI it may also have a
        // version IRI The OWL API encapsulates ontology IRI and possible
        // version IRI information in an OWLOntologyID Each ontology knows about
        // its ID
        OWLOntologyID ontologyID = ontology.getOntologyID();
        // In this case our ontology has an IRI but does not have a version IRI
        Console.WriteLine("Ontology IRI: " + ontologyID.getOntologyIRI());
        // Our version IRI will be null to indicate that we don't have a version
        // IRI
        Console.WriteLine("Ontology Version IRI: "
                + ontologyID.getVersionIRI());
        // An ontology may not have a version IRI - in this case, we count the
        // ontology as an anonymous ontology. Our ontology does have an IRI so
        // it is not anonymous:
        Console.WriteLine("Anonymous Ontology: " + ontologyID.isAnonymous());
        // Once an ontology has been created its ontology ID (Ontology IRI and
        // version IRI can be changed) to do this we must apply a SetOntologyID
        // change through the ontology manager. Lets specify a version IRI for
        // our ontology. In our case we will just "extend" our ontology IRI with
        // some version information. We could of course specify any IRI for our
        // version IRI.
        IRI versionIRI = IRI.create(ontologyIRI + "/version1");
        // Note that we MUST specify an ontology IRI if we want to specify a
        // version IRI
        OWLOntologyID newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);
        // Create the change that will set our version IRI
        SetOntologyID setOntologyID = new SetOntologyID(ontology, newOntologyID);
        // Apply the change
        manager.applyChange(setOntologyID);
        Console.WriteLine("Ontology: " + ontology);
        // We can also just specify the ontology IRI and possibly the version
        // IRI at ontology creation time Set up our ID by specifying an ontology
        // IRI and version IRI
        IRI ontologyIRI2 = IRI
                .create("http://www.semanticweb.org/ontologies/myontology2");
        IRI versionIRI2 = IRI
                .create("http://www.semanticweb.org/ontologies/myontology2/newversion");
        OWLOntologyID ontologyID2 = new OWLOntologyID(ontologyIRI2, versionIRI2);
        // Now create the ontology
        OWLOntology ontology2 = manager.createOntology(ontologyID2);
        Console.WriteLine("Created ontology: " + ontology2);
        // Finally, if we don't want to give an ontology an IRI, in OWL 2 we
        // don't have to
        OWLOntology anonOntology = manager.createOntology();
        Console.WriteLine("Created ontology: " + anonOntology);
        // This ontology is anonymous
        Console.WriteLine("Anonymous: " + anonOntology.isAnonymous());
    }

    /**
     * This example shows how to specify various property assertions for
     * individuals.
     * 
     * @throws OWLOntologyStorageException
     * @throws OWLOntologyCreationException
     */
    public void shouldCreatePropertyAssertions(){
        // We can specify the properties of an individual using property
        // assertions Get a copy of an ontology manager
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI ontologyIRI = IRI.create("http://example.com/owl/families/");
        OWLOntology ontology = manager.createOntology(ontologyIRI);
        // Get hold of a data factory from the manager and set up a prefix
        // manager to make things easier
        OWLDataFactory factory = manager.getOWLDataFactory();
        PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
        // Let's specify the :John has a wife :Mary Get hold of the necessary
        // individuals and object property
        OWLNamedIndividual john = factory.getOWLNamedIndividual(":John", pm);
        OWLNamedIndividual mary = factory.getOWLNamedIndividual(":Mary", pm);
        OWLObjectProperty hasWife = factory
                .getOWLObjectProperty(":hasWife", pm);
        // To specify that :John is related to :Mary via the :hasWife property
        // we create an object property assertion and add it to the ontology
        OWLObjectPropertyAssertionAxiom propertyAssertion = factory
                .getOWLObjectPropertyAssertionAxiom(hasWife, john, mary);
        manager.addAxiom(ontology, propertyAssertion);
        // Now let's specify that :John is aged 51. Get hold of a data property
        // called :hasAge
        OWLDataProperty hasAge = factory.getOWLDataProperty(":hasAge", pm);
        // To specify that :John has an age of 51 we create a data property
        // assertion and add it to the ontology
        OWLDataPropertyAssertionAxiom dataPropertyAssertion = factory
                .getOWLDataPropertyAssertionAxiom(hasAge, john, 51);
        manager.addAxiom(ontology, dataPropertyAssertion);
        // Note that the above is a shortcut for creating a typed literal and
        // specifying this typed literal as the value of the property assertion.
        // That is, Get hold of the xsd:integer datatype
        OWLDatatype integerDatatype = factory
                .getOWLDatatype(OWL2Datatype.XSD_INTEGER.getIRI());
        // Create a typed literal. We type the literal "51" with the datatype
        OWLLiteral literal = factory.getOWLLiteral("51", integerDatatype);
        // Create the property assertion and add it to the ontology
        OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(hasAge, john,
                literal);
        manager.addAxiom(ontology, ax);
        // Dump the ontology to System.out
        manager.saveOntology(ontology, new StreamDocumentTarget(
                new java.io.ByteArrayOutputStream()));
    }

    /**
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public void shouldAddClassAssertion(){
        // For more information on classes and instances see the OWL 2 Primer
        // http://www.w3.org/TR/2009/REC-owl2-primer-20091027/#Classes_and_Instances
        // In order to say that an individual is an instance of a class (in an
        // ontology), we can add a ClassAssertion to the ontology. For example,
        // suppose we wanted to specify that :Mary is an instance of the class
        // :Person. First we need to obtain the individual :Mary and the class
        // :Person Create an ontology manager to work with
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        // The IRIs used here are taken from the OWL 2 Primer
        String baseUri = "http://example.com/owl/families/";
        PrefixManager pm = new DefaultPrefixManager(baseUri);
        // Get the reference to the :Person class (the full IRI will be
        // <http://example.com/owl/families/Person>)
        OWLClass person = dataFactory.getOWLClass(":Person", pm);
        // Get the reference to the :Mary class (the full IRI will be
        // <http://example.com/owl/families/Mary>)
        OWLNamedIndividual mary = dataFactory
                .getOWLNamedIndividual(":Mary", pm);
        // Now create a ClassAssertion to specify that :Mary is an instance of
        // :Person
        OWLClassAssertionAxiom classAssertion = dataFactory
                .getOWLClassAssertionAxiom(person, mary);
        // We need to add the class assertion to the ontology that we want
        // specify that :Mary is a :Person
        OWLOntology ontology = manager.createOntology(IRI.create(baseUri));
        // Add the class assertion
        manager.addAxiom(ontology, classAssertion);
        // Dump the ontology to stdout
        manager.saveOntology(ontology, new StreamDocumentTarget(
                new java.io.ByteArrayOutputStream()));
    }

    /**
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public void shouldCreateAndSaveOntology(){
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // Let's create an ontology and name it
        // "http://www.co-ode.org/ontologies/testont.owl" We need to set up a
        // mapping which points to a concrete file where the ontology will be
        // stored. (It's good practice to do this even if we don't intend to
        // save the ontology).
        IRI ontologyIRI = IRI
                .create("http://www.co-ode.org/ontologies/testont.owl");
        // Create a document IRI which can be resolved to point to where our
        // ontology will be saved.
        IRI documentIRI = IRI.create("file:/tmp/SWRLTest.owl");
        // Set up a mapping, which maps the ontology to the document IRI
        SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
        manager.addIRIMapper(mapper);
        // Now create the ontology - we use the ontology IRI (not the physical
        // IRI)
        OWLOntology ontology = manager.createOntology(ontologyIRI);
        OWLDataFactory factory = manager.getOWLDataFactory();
        // Get hold of references to class A and class B. Note that the ontology
        // does not contain class A or classB, we simply get references to
        // objects from a data factory that represent class A and class B
        OWLClass clsA = factory.getOWLClass(IRI.create(ontologyIRI + "#A"));
        OWLClass clsB = factory.getOWLClass(IRI.create(ontologyIRI + "#B"));
        SWRLVariable var = factory.getSWRLVariable(IRI.create(ontologyIRI
                + "#x"));
        SWRLRule rule = factory.getSWRLRule(
                java.util.Collections.singleton(factory.getSWRLClassAtom(clsA, var)),
                java.util.Collections.singleton(factory.getSWRLClassAtom(clsB, var)));
        manager.applyChange(new AddAxiom(ontology, rule));
        OWLObjectProperty prop = factory.getOWLObjectProperty(IRI
                .create(ontologyIRI + "#propA"));
        OWLObjectProperty propB = factory.getOWLObjectProperty(IRI
                .create(ontologyIRI + "#propB"));
        SWRLObjectPropertyAtom propAtom = factory.getSWRLObjectPropertyAtom(
                prop, var, var);
        SWRLObjectPropertyAtom propAtom2 = factory.getSWRLObjectPropertyAtom(
                propB, var, var);
        HashSet<SWRLAtom> antecedent = new HashSet<SWRLAtom>();
        antecedent.Add(propAtom);
        antecedent.Add(propAtom2);
        SWRLRule rule2 = factory.getSWRLRule((java.util.Set)(antecedent),
                java.util.Collections.singleton(propAtom));
        manager.applyChange(new AddAxiom(ontology, rule2));
        // Now save the ontology. The ontology will be saved to the location
        // where we loaded it from, in the default ontology format
        manager.saveOntology(ontology);
    }

    /**
     * This example shows how add an object property assertion (triple) of the
     * form prop(subject, object) for example hasPart(a, b).
     * 
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public void shouldAddObjectPropertyAssertions(){
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        String baseUri = "http://www.semanticweb.org/ontologies/individualsexample";
        OWLOntology ont = man.createOntology(IRI.create(baseUri));
        OWLDataFactory dataFactory = man.getOWLDataFactory();
        // In this case, we would like to state that matthew has a father who is
        // peter. We need a subject and object - matthew is the subject and
        // peter is the object. We use the data factory to obtain references to
        // these individuals
        OWLIndividual matthew = dataFactory.getOWLNamedIndividual(IRI
                .create(baseUri + "#matthew"));
        OWLIndividual peter = dataFactory.getOWLNamedIndividual(IRI.create(baseUri
                + "#peter"));
        // We want to link the subject and object with the hasFather property,
        // so use the data factory to obtain a reference to this object
        // property.
        OWLObjectProperty hasFather = dataFactory.getOWLObjectProperty(IRI
                .create(baseUri + "#hasFather"));
        // Now create the actual assertion (triple), as an object property
        // assertion axiom matthew --> hasFather --> peter
        OWLObjectPropertyAssertionAxiom assertion = dataFactory
                .getOWLObjectPropertyAssertionAxiom(hasFather, matthew, peter);
        // Finally, add the axiom to our ontology and save
        AddAxiom addAxiomChange = new AddAxiom(ont, assertion);
        man.applyChange(addAxiomChange);
        // We can also specify that matthew is an instance of Person. To do this
        // we use a ClassAssertion axiom. First we need a reference to the
        // person class
        OWLClass personClass = dataFactory.getOWLClass(IRI.create(baseUri
                + "#Person"));
        // Now we will create out Class Assertion to specify that matthew is an
        // instance of Person (or rather that Person has matthew as an instance)
        OWLClassAssertionAxiom ax = dataFactory.getOWLClassAssertionAxiom(
                personClass, matthew);
        // Add this axiom to our ontology. We can use a short cut method -
        // instead of creating the AddAxiom change ourselves, it will be created
        // automatically and the change applied
        man.addAxiom(ont, ax);
        // Save our ontology
        man.saveOntology(ont, IRI.create("file:/tmp/example.owl"));
    }

    /**
     * An example which shows how to "delete" entities, in this case
     * individuals, from and ontology.
     * 
     * @throws OWLOntologyCreationException
     */
    public void shouldDeleteIndividuals(){
        // The pizza ontology contains several individuals that represent
        // countries, which describe the country of origin of various pizzas and
        // ingredients. In this example we will delete them all. First off, we
        // start by loading the pizza ontology.
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI
                .create(PIZZA_IRI));
        // We can't directly delete individuals, properties or classes from an
        // ontology because ontologies don't directly contain entities -- they
        // are merely referenced by the axioms that the ontology contains. For
        // example, if an ontology contained a subclass axiom SubClassOf(A, B)
        // which stated A was a subclass of B, then that ontology would contain
        // references to classes A and B. If we essentially want to "delete"
        // classes A and B from this ontology we have to remove all axioms that
        // contain class A and class B in their SIGNATURE (in this case just one
        // axiom SubClassOf(A, B)). To do this, we can use the OWLEntityRemove
        // utility class, which will remove an entity (class, property or
        // individual) from a set of ontologies. Create the entity remover - in
        // this case we just want to remove the individuals from the pizza
        // ontology, so pass our reference to the pizza ontology in as a
        // singleton set.
        OWLEntityRemover remover = new OWLEntityRemover(man,
                java.util.Collections.singleton(ont));
        Console.WriteLine("Number of individuals: "
                + ont.getIndividualsInSignature().size());
        // Loop through each individual that is referenced in the pizza
        // ontology, and ask it to accept a visit from the entity remover. The
        // remover will automatically accumulate the changes which are necessary
        // to remove the individual from the ontologies (the pizza ontology)
        // which it knows about
        var indIt = ont.getIndividualsInSignature().iterator();
        while(indIt.hasNext()) {
            OWLNamedIndividual ind =(OWLNamedIndividual) indIt.next();
            ind.accept(remover);
        }
        // Now we get all of the changes from the entity remover, which should
        // be applied to remove all of the individuals that we have visited from
        // the pizza ontology. Notice that "batch" deletes can essentially be
        // performed - we simply visit all of the classes, properties and
        // individuals that we want to remove and then apply ALL of the changes
        // after using the entity remover to collect them
        man.applyChanges(remover.getChanges());
        Console.WriteLine("Number of individuals: "
                + ont.getIndividualsInSignature().size());
        // At this point, if we wanted to reuse the entity remover, we would
        // have to reset it
        remover.reset();
    }

    /**
     * An example which shows how to create restrictions and add them as
     * superclasses of a class (i.e. "adding restrictions to classes")
     * 
     * @throws OWLOntologyCreationException
     */
    
    public void shouldCreateRestrictions(){
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        String baseUri = "http://org.semanticweb.restrictionexample";
        OWLOntology ont = man.createOntology(IRI.create(baseUri));
        // In this example we will add an axiom to state that all Heads have
        // parts that are noses (in fact, here we merely state that a Head has
        // at least one nose!). We do this by creating an existential (some)
        // restriction to describe the class of things which have a part that is
        // a nose (hasPart some Nose), and then we use this restriction in a
        // subclass axiom to state that Head is a subclass of things that have
        // parts that are Noses SubClassOf(Head, hasPart some Nose) -- in other
        // words, Heads have parts that are noses! First we need to obtain
        // references to our hasPart property and our Nose class
        OWLDataFactory factory = man.getOWLDataFactory();
        OWLObjectProperty hasPart = factory.getOWLObjectProperty(IRI
                .create(baseUri + "#hasPart"));
        OWLClass nose = factory.getOWLClass(IRI.create(baseUri + "#Nose"));
        // Now create a restriction to describe the class of individuals that
        // have at least one part that is a kind of nose
        OWLClassExpression hasPartSomeNose = factory
                .getOWLObjectSomeValuesFrom(hasPart, nose);
        // Obtain a reference to the Head class so that we can specify that
        // Heads have noses
        OWLClass head = factory.getOWLClass(IRI.create(baseUri + "#Head"));
        // We now want to state that Head is a subclass of hasPart some Nose, to
        // do this we create a subclass axiom, with head as the subclass and
        // "hasPart some Nose" as the superclass (remember, restrictions are
        // also classes - they describe classes of individuals -- they are
        // anonymous classes).
        OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(head,
                hasPartSomeNose);
        // Add the axiom to our ontology
        AddAxiom addAx = new AddAxiom(ont, ax);
        man.applyChange(addAx);
    }

    /**
     * An example which shows how to interact with a reasoner. In this example
     * Pellet is used as the reasoner. You must get hold of the pellet libraries
     * from pellet.owldl.com.
     * 
     * @throws OWLOntologyCreationException
     */
    
    public void shouldUseReasoner(){
        String DOCUMENT_IRI = "http://owl.cs.manchester.ac.uk/repository/download?ontology=file:/Users/seanb/Desktop/Cercedilla2005/hands-on/people.owl&format=RDF/XML";
        // Create our ontology manager in the usual way.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // Load a copy of the people+pets ontology. We'll load the ontology from
        // the web (it's acutally located in the TONES ontology repository).
        IRI docIRI = IRI.create(DOCUMENT_IRI);
        // We load the ontology from a document - our IRI points to it directly
        OWLOntology ont = manager.loadOntologyFromOntologyDocument(docIRI);
        Console.WriteLine("Loaded " + ont.getOntologyID());
        // We need to create an instance of OWLReasoner. An OWLReasoner provides
        // the basic query functionality that we need, for example the ability
        // obtain the subclasses of a class etc. To do this we use a reasoner
        // factory. Create a reasoner factory. In this case, we will use HermiT,
        // but we could also use FaCT++ (http://code.google.com/p/factplusplus/)
        // or Pellet(http://clarkparsia.com/pellet) Note that (as of 03 Feb
        // 2010) FaCT++ and Pellet OWL API 3.0.0 compatible libraries are
        // expected to be available in the near future). For now, we'll use
        // HermiT HermiT can be downloaded from http://hermit-reasoner.com Make
        // sure you get the HermiT library and add it to your class path. You
        // can then instantiate the HermiT reasoner factory: Comment out the
        // first line below and uncomment the second line below to instantiate
        // the HermiT reasoner factory. You'll also need to import the
        // org.semanticweb.HermiT.Reasoner package.
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        // OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
        // We'll now create an instance of an OWLReasoner (the implementation
        // being provided by HermiT as we're using the HermiT reasoner factory).
        // The are two categories of reasoner, Buffering and NonBuffering. In
        // our case, we'll create the buffering reasoner, which is the default
        // kind of reasoner. We'll also attach a progress monitor to the
        // reasoner. To do this we set up a configuration that knows about a
        // progress monitor. Create a console progress monitor. This will print
        // the reasoner progress out to the console.
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        // Specify the progress monitor via a configuration. We could also
        // specify other setup parameters in the configuration, and different
        // reasoners may accept their own defined parameters this way.
        OWLReasonerConfiguration config = new SimpleConfiguration(
                progressMonitor);
        // Create a reasoner that will reason over our ontology and its imports
        // closure. Pass in the configuration.
        OWLReasoner reasoner = reasonerFactory.createReasoner(ont, config);
        // Ask the reasoner to do all the necessary work now
        reasoner.precomputeInferences();
        // We can determine if the ontology is actually consistent (in this
        // case, it should be).
        bool consistent = reasoner.isConsistent();
        Console.WriteLine("Consistent: " + consistent);
        Console.WriteLine("\n");
        // We can easily get a list of unsatisfiable classes. (A class is
        // unsatisfiable if it can't possibly have any instances). Note that the
        // getUnsatisfiableClasses method is really just a convenience method
        // for obtaining the classes that are equivalent to owl:Nothing. In our
        // case there should be just one unsatisfiable class - "mad_cow" We ask
        // the reasoner for the unsatisfiable classes, which returns the bottom
        // node in the class hierarchy (an unsatisfiable class is a subclass of
        // every class).
        Node bottomNode = reasoner.getUnsatisfiableClasses();
        // This node contains owl:Nothing and all the classes that are
        // equivalent to owl:Nothing - i.e. the unsatisfiable classes. We just
        // want to print out the unsatisfiable classes excluding owl:Nothing,
        // and we can used a convenience method on the node to get these
        HashSet<OWLClass> unsatisfiable = (HashSet<OWLClass>)bottomNode.getEntitiesMinusBottom();
        if (unsatisfiable.Count != 0) {
            Console.WriteLine("The following classes are unsatisfiable: ");
            foreach (OWLClass cls in unsatisfiable) {
                Console.WriteLine("    " + cls);
            }
        } else {
            Console.WriteLine("There are no unsatisfiable classes");
        }
        Console.WriteLine("\n");
        // Now we want to query the reasoner for all descendants of vegetarian.
        // Vegetarians are defined in the ontology to be animals that don't eat
        // animals or parts of animals.
        OWLDataFactory fac = manager.getOWLDataFactory();
        // Get a reference to the vegetarian class so that we can as the
        // reasoner about it. The full IRI of this class happens to be:
        // <http://owl.man.ac.uk/2005/07/sssw/people#vegetarian>
        OWLClass vegPizza = fac.getOWLClass(IRI
                .create("http://owl.man.ac.uk/2005/07/sssw/people#vegetarian"));
        // Now use the reasoner to obtain the subclasses of vegetarian. We can
        // ask for the direct subclasses of vegetarian or all of the (proper)
        // subclasses of vegetarian. In this case we just want the direct ones
        // (which we specify by the "true" flag).
        NodeSet subClses = reasoner.getSubClasses(vegPizza, true);
        // The reasoner returns a NodeSet, which represents a set of Nodes. Each
        // node in the set represents a subclass of vegetarian pizza. A node of
        // classes contains classes, where each class in the node is equivalent.
        // For example, if we asked for the subclasses of some class A and got
        // back a NodeSet containing two nodes {B, C} and {D}, then A would have
        // two proper subclasses. One of these subclasses would be equivalent to
        // the class D, and the other would be the class that is equivalent to
        // class B and class C. In this case, we don't particularly care about
        // the equivalences, so we will flatten this set of sets and print the
        // result
        HashSet<OWLClass> clses = (HashSet<OWLClass>)subClses.getFlattened();
        Console.WriteLine("Subclasses of vegetarian: ");
        foreach (OWLClass cls in clses) {
            Console.WriteLine("    " + cls);
        }
        Console.WriteLine("\n");
        // In this case, we should find that the classes, cow, sheep and giraffe
        // are vegetarian. Note that in this ontology only the class cow had
        // been stated to be a subclass of vegetarian. The fact that sheep and
        // giraffe are subclasses of vegetarian was implicit in the ontology
        // (through other things we had said) and this illustrates why it is
        // important to use a reasoner for querying an ontology. We can easily
        // retrieve the instances of a class. In this example we'll obtain the
        // instances of the class pet. This class has a full IRI of
        // <http://owl.man.ac.uk/2005/07/sssw/people#pet> We need to obtain a
        // reference to this class so that we can ask the reasoner about it.
        OWLClass country = fac.getOWLClass(IRI
                .create("http://owl.man.ac.uk/2005/07/sssw/people#pet"));
        // Ask the reasoner for the instances of pet
        NodeSet individualsNodeSet = reasoner.getInstances(
                country, true);
        // The reasoner returns a NodeSet again. This time the NodeSet contains
        // individuals. Again, we just want the individuals, so get a flattened
        // set.
        HashSet<OWLNamedIndividual> individuals =(HashSet<OWLNamedIndividual>) individualsNodeSet.getFlattened();
        Console.WriteLine("Instances of pet: ");
        foreach (OWLNamedIndividual ind in individuals) {
            Console.WriteLine("    " + ind);
        }
        Console.WriteLine("\n");
        // Again, it's worth noting that not all of the individuals that are
        // returned were explicitly stated to be pets. Finally, we can ask for
        // the property values (property assertions in OWL speak) for a given
        // individual and property. Let's get the property values for the
        // individual Mick, the full IRI of which is
        // <http://owl.man.ac.uk/2005/07/sssw/people#Mick> Get a reference to
        // the individual Mick
        OWLNamedIndividual mick = fac.getOWLNamedIndividual(IRI
                .create("http://owl.man.ac.uk/2005/07/sssw/people#Mick"));
        // Let's get the pets of Mick Get hold of the has_pet property which has
        // a full IRI of <http://owl.man.ac.uk/2005/07/sssw/people#has_pet>
        OWLObjectProperty hasPet = fac.getOWLObjectProperty(IRI
                .create("http://owl.man.ac.uk/2005/07/sssw/people#has_pet"));
        // Now ask the reasoner for the has_pet property values for Mick
        NodeSet petValuesNodeSet = reasoner.getObjectPropertyValues(mick, hasPet);
        HashSet<OWLNamedIndividual> values = (HashSet<OWLNamedIndividual>) petValuesNodeSet.getFlattened();
        Console.WriteLine("The has_pet property values for Mick are: ");
        foreach (OWLNamedIndividual ind in values) {
            Console.WriteLine("    " + ind);
        }
        // Notice that Mick has a pet Rex, which wasn't asserted in the
        // ontology. Finally, let's print out the class hierarchy. Get hold of
        // the top node in the class hierarchy (containing owl:Thing) Now print
        // the hierarchy out
        Node topNode = reasoner.getTopClassNode();
        print(topNode, reasoner, 0);
    }

    private static void print(Node parent, OWLReasoner reasoner,
            int depth) {
        // We don't want to print out the bottom node (containing owl:Nothing
        // and unsatisfiable classes) because this would appear as a leaf node
        // everywhere
        if (parent.isBottomNode()) {
            return;
        }
        // Print an indent to denote parent-child relationships
        printIndent(depth);
        // Now print the node (containing the child classes)
        printNode(parent);
        var itNodes = reasoner.getSubClasses((OWLClassExpression)parent.getRepresentativeElement(), true).iterator();

        while(itNodes.hasNext()) {
            Node child = (Node)itNodes.next();
            // Recurse to do the children. Note that we don't have to worry
            // about cycles as there are non in the inferred class hierarchy
            // graph - a cycle gets collapsed into a single node since each
            // class in the cycle is equivalent.
            print(child, reasoner, depth + 1);
        }
    }

    private static void printIndent(int depth) {
        for (int i = 0; i < depth; i++) {
            Console.Write("    ");
        }
    }

    private static void printNode(org.semanticweb.owlapi.reasoner.Node node) {
        DefaultPrefixManager pm = new DefaultPrefixManager(
                "http://owl.man.ac.uk/2005/07/sssw/people#");
        // Print out a node as a list of class names in curly brackets
        Console.Write("{");
        var it = node.getEntities().iterator();
        while(it.hasNext()){
            OWLClass cls = (OWLClass)it.next();
            // User a prefix manager to provide a slightly nicer shorter name
            Console.Write(pm.getShortForm(cls));
            if (it.hasNext()) {
                Console.Write(" ");
            }
        }
        Console.WriteLine("}");
    }

    /**
     * This example shows how to examine the restrictions on a class.
     * 
     * @throws OWLOntologyCreationException
     */
    
    public void shouldLookAtRestrictions(){
        // Create our manager
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        // Load the pizza ontology
        OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI
                .create(PIZZA_IRI));
        Console.WriteLine("Loaded: " + ont.getOntologyID());
        // We want to examine the restrictions on margherita pizza. To do this,
        // we need to obtain a reference to the margherita pizza class. In this
        // case, we know the URI for margherita pizza (it happens to be the
        // ontology URI - the base URI plus #Margherita - note that this isn't
        // always the case. A class may have a URI that bears no resemblance to
        // the ontology URI which contains axioms about the class).
        IRI margheritaPizzaIRI = IRI.create(ont.getOntologyID()
                .getOntologyIRI() + "#Margherita");
        OWLClass margheritaPizza = man.getOWLDataFactory().getOWLClass(
                margheritaPizzaIRI);
        // Now we want to collect the properties which are used in existential
        // restrictions on the class. To do this, we will create a utility class
        // - RestrictionVisitor, which acts as a filter for existential
        // restrictions. This uses the Visitor Pattern (google Visitor Design
        // Pattern for more information on this design pattern, or see
        // http://en.wikipedia.org/wiki/Visitor_pattern)
        RestrictionVisitor restrictionVisitor = new RestrictionVisitor(new HashSet<OWLOntology>(){ont});

        // In this case, restrictions are used as (anonymous) superclasses, so
        // to get the restrictions on margherita pizza we need to obtain the
        // subclass axioms for margherita pizza.
        var ontIt = ont.getSubClassAxiomsForSubClass(margheritaPizza).iterator();
        while(ontIt.hasNext()) {
            OWLSubClassOfAxiom ax=(OWLSubClassOfAxiom) ontIt.next();
            OWLClassExpression superCls = ax.getSuperClass();
            // Ask our superclass to accept a visit from the RestrictionVisitor
            // - if it is an existential restiction then our restriction visitor
            // will answer it - if not our visitor will ignore it
            superCls.accept(restrictionVisitor);
        }
        // Our RestrictionVisitor has now collected all of the properties that
        // have been restricted in existential restrictions - print them out.
        Console.WriteLine("Restricted properties for " + margheritaPizza
                + ": " + restrictionVisitor.getRestrictedProperties().Count);
        foreach (OWLObjectPropertyExpression prop in restrictionVisitor.getRestrictedProperties()) {
            Console.WriteLine("    " + prop);
        }
    }

    /**
     * Visits existential restrictions and collects the properties which are
     * restricted.
     */
    private class RestrictionVisitor :
            OWLClassExpressionVisitorAdapter {

        private HashSet<OWLClass> processedClasses;
        private HashSet<OWLObjectPropertyExpression> restrictedProperties;
        private HashSet<OWLOntology> onts;

        public RestrictionVisitor(HashSet<OWLOntology> onts) {
            restrictedProperties = new HashSet<OWLObjectPropertyExpression>();
            processedClasses = new HashSet<OWLClass>();
            this.onts = onts;
        }

        public HashSet<OWLObjectPropertyExpression> getRestrictedProperties() {
            return restrictedProperties;
        }

        public void visit(OWLClass desc) {
            if (!processedClasses.Contains(desc)) {
                // If we are processing inherited restrictions then we
                // recursively visit named supers. Note that we need to keep
                // track of the classes that we have processed so that we don't
                // get caught out by cycles in the taxonomy
                processedClasses.Add(desc);
                foreach (OWLOntology ont in onts) {
                    var ontIt = ont.getSubClassAxiomsForSubClass(desc).iterator();
                    while(ontIt.hasNext()) {
                        OWLSubClassOfAxiom ax = (OWLSubClassOfAxiom) ontIt.next();
                        ax.getSuperClass().accept(this);
                    }
                }
            }
        }

        public void visit(OWLObjectSomeValuesFrom desc) {
            // This method gets called when a class expression is an existential
            // (someValuesFrom) restriction and it asks us to visit it
            restrictedProperties.Add((OWLObjectPropertyExpression)desc.getProperty());
        }
    }

    /**
     * This example shows how to create and read annotations.
     * 
     * @throws OWLOntologyCreationException
     */
    
    public void shouldCreateAndReadAnnotations(){
        // Create our manager
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        // Load the pizza ontology
        OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI
                .create(PIZZA_IRI));
        Console.WriteLine("Loaded: " + ont.getOntologyID());
        // We want to add a comment to the pizza class. First, we need to obtain
        // a reference to the pizza class
        OWLDataFactory df = man.getOWLDataFactory();
        OWLClass pizzaCls = df.getOWLClass(IRI.create(ont.getOntologyID()
                .getOntologyIRI().toString()
                + "#Pizza"));
        // Now we create the content of our comment. In this case we simply want
        // a plain string literal. We'll attach a language to the comment to
        // specify that our comment is written in English (en).
        OWLAnnotation commentAnno = df.getOWLAnnotation(df.getRDFSComment(),
                df.getOWLLiteral("A class which represents pizzas", "en"));
        // Specify that the pizza class has an annotation - to do this we attach
        // an entity annotation using an entity annotation axiom (remember,
        // classes are entities)
        OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(pizzaCls.getIRI(),
                commentAnno);
        // Add the axiom to the ontology
        man.applyChange(new AddAxiom(ont, ax));
        // Now lets add a version info annotation to the ontology. There is no
        // 'standard' OWL 1.1 annotation object for this, like there is for
        // comments and labels, so the creation of the annotation is a bit more
        // involved. First we'll create a constant for the annotation value.
        // Version info should probably contain a version number for the
        // ontology, but in this case, we'll add some text to describe why the
        // version has been updated
        OWLLiteral lit = df.getOWLLiteral("Added a comment to the pizza class");
        // The above constant is just a plain literal containing the version
        // info text/comment we need to create an annotation, which pairs a URI
        // with the constant
        OWLAnnotation anno = df.getOWLAnnotation(df
                .getOWLAnnotationProperty(OWLRDFVocabulary.OWL_VERSION_INFO
                        .getIRI()), lit);
        // Now we can add this as an ontology annotation Apply the change in the
        // usual way
        man.applyChange(new AddOntologyAnnotation(ont, anno));
        // The pizza ontology has labels attached to most classes which are
        // translations of class names into Portuguese (pt) we can access these
        // and print them out. At this point, it is worth noting that constants
        // can be typed or untyped. If constants are untyped then they can have
        // language tags, which are optional - typed constant cannot have
        // language tags. For each class in the ontology, we retrieve its
        // annotations and sift through them. If the annotation annotates the
        // class with a constant which is untyped then we check the language tag
        // to see if it is Portugeuse. Firstly, get the annotation property for
        // rdfs:label
        OWLAnnotationProperty label = df
                .getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
        var clsIt = ont.getClassesInSignature().iterator();
        while(clsIt.hasNext()) {
            OWLClass cls = (OWLClass)clsIt.next();
            // Get the annotations on the class that use the label property
            var annotIt = cls.getAnnotations(ont, label).iterator();
            while(annotIt.hasNext()) {
                OWLAnnotation annotation = (OWLAnnotation) annotIt.next();
                OWLAnnotationValue annotVal = annotation.getValue();
                if (annotVal is OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    if (val.hasLang("pt")) {
                        Console.WriteLine(cls + " -> " + val.getLiteral());
                    }
                }
            }
        }
    }

    /**
     * This example shows how to generate an ontology containing some inferred
     * information.
     * 
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    
    public void shouldCreateInferredAxioms(){
        // Create a reasoner factory. In this case, we will use pellet, but we
        // could also use FaCT++ using the FaCTPlusPlusReasonerFactory. Pellet
        // requires the Pellet libraries (pellet.jar, aterm-java-x.x.jar) and
        // the XSD libraries that are bundled with pellet: xsdlib.jar and
        // relaxngDatatype.jar make sure these jars are on the classpath
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        // Uncomment the line below reasonerFactory = new
        // PelletReasonerFactory(); Load an example ontology - for the purposes
        // of the example, we will just load the pizza ontology.
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI
                .create(PIZZA_IRI));
        // Create the reasoner and classify the ontology
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        // To generate an inferred ontology we use implementations of inferred
        // axiom generators to generate the parts of the ontology we want (e.g.
        // subclass axioms, equivalent classes axioms, class assertion axiom
        // etc. - see the org.semanticweb.owlapi.util package for more
        // implementations). Set up our list of inferred axiom generators
        List<InferredAxiomGenerator> gens = new List<InferredAxiomGenerator>();
        gens.Add(new InferredSubClassAxiomGenerator());
        // Put the inferred axioms into a fresh empty ontology - note that there
        // is nothing stopping us stuffing them back into the original asserted
        // ontology if we wanted to do this.
        OWLOntology infOnt = man.createOntology();
        // Now get the inferred ontology generator to generate some inferred
        // axioms for us (into our fresh ontology). We specify the reasoner that
        // we want to use and the inferred axiom generators that we want to use.
        InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner,(java.util.List)gens);
        iog.fillOntology(man, infOnt);
        // Save the inferred ontology. (Replace the URI with one that is
        // appropriate for your setup)
        man.saveOntology(infOnt, new StringDocumentTarget());
    }

    /**
     * This example shows how to merge to ontologies (by simply combining axioms
     * from one ontology into another ontology).
     * 
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    
    public void shouldMergeOntologies(){
        // Just load two arbitrary ontologies for the purposes of this example
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        man.loadOntologyFromOntologyDocument(IRI.create(PIZZA_IRI));
        man.loadOntologyFromOntologyDocument(IRI
                .create("http://www.co-ode.org/ontologies/amino-acid/2006/05/18/amino-acid.owl"));
        // Create our ontology merger
        OWLOntologyMerger merger = new OWLOntologyMerger(man);
        // We merge all of the loaded ontologies. Since an OWLOntologyManager is
        // an OWLOntologySetProvider we just pass this in. We also need to
        // specify the URI of the new ontology that will be created.
        IRI mergedOntologyIRI = IRI
                .create("http://www.semanticweb.com/mymergedont");
        OWLOntology merged = merger
                .createMergedOntology(man, mergedOntologyIRI);
        // Print out the axioms in the merged ontology.
        var axIt = merged.getAxioms().iterator();
        while(axIt.hasNext()) {
            OWLAxiom ax = (OWLAxiom) axIt.next();
            Console.WriteLine(ax);
        }
        // Save to RDF/XML
        man.saveOntology(merged, new RDFXMLOntologyFormat(),
                IRI.create("file:/tmp/mergedont.owlapi"));
    }

    /** @throws OWLOntologyCreationException */
    
    public void shouldWalkOntology(){
        // This example shows how to use an ontology walker to walk the asserted
        // structure of an ontology. Suppose we want to find the axioms that use
        // a some values from (existential restriction) we can use the walker to
        // do this. We'll use the pizza ontology as an example. Load the
        // ontology from the web:
        IRI documentIRI = IRI.create(PIZZA_IRI);
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(documentIRI);
        // Create the walker. Pass in the pizza ontology - we need to put it
        // into a set though, so we just create a singleton set in this case.
        OWLOntologyWalker walker = new OWLOntologyWalker(
                java.util.Collections.singleton(ont));
        // Now ask our walker to walk over the ontology. We specify a visitor
        // who gets visited by the various objects as the walker encounters
        // them. We need to create out visitor. This can be any ordinary
        // visitor, but we will extend the OWLOntologyWalkerVisitor because it
        // provides a convenience method to get the current axiom being visited
        // as we go. Create an instance and override the
        // visit(OWLObjectSomeValuesFrom) method, because we are interested in
        // some values from restrictions.


        /// HOW TO TRANSLATE THIS?
        //OWLOntologyWalkerVisitor visitor = new OWLOntologyWalkerVisitor(walker) {
        //    public Object visit(OWLObjectSomeValuesFrom desc) {
        //        // Print out the restriction
        //        Console.WriteLine(desc);
        //        // Print out the axiom where the restriction is used
        //        Console.WriteLine("         " + getCurrentAxiom());
        //        Console.WriteLine();
        //        // We don't need to return anything here.
        //        return null;
        //    }
        //};
        //// Now ask the walker to walk over the ontology structure using our
        //// visitor instance.
        //walker.walkStructure(visitor);
    }

    /** @throws OWLOntologyCreationException */
    
    public void shouldQueryWithReasoner(){
        // We will load the pizza ontology and query it using a reasoner
        IRI documentIRI = IRI.create(PIZZA_IRI);
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(documentIRI);
        // For this particular ontology, we know that all class, properties
        // names etc. have URIs that is made up of the ontology IRI plus # plus
        // the local name
        String prefix = ont.getOntologyID().getOntologyIRI() + "#";
        // Create a reasoner. We will use Pellet in this case. Make sure that
        // the latest version of the Pellet libraries are on the runtime class
        // path
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        // Uncomment the line below reasonerFactory = new
        // PelletReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ont);
        // Now we can query the reasoner, suppose we want to determine the
        // properties that instances of Marghertia pizza must have
        OWLClass margheritaPizza = man.getOWLDataFactory().getOWLClass(
                IRI.create(prefix + "Margherita"));
        printProperties(man, ont, reasoner, margheritaPizza);
        // Let's do the same for JalapenoPepperTopping
        OWLClass vegTopping = man.getOWLDataFactory().getOWLClass(
                IRI.create(prefix + "JalapenoPepperTopping"));
        printProperties(man, ont, reasoner, vegTopping);
        // We can also ask if the instances of a class must have a property
        OWLClass mozzarellaTopping = man.getOWLDataFactory().getOWLClass(
                IRI.create(prefix + "MozzarellaTopping"));
        OWLObjectProperty hasOrigin = man
                .getOWLDataFactory()
                .getOWLObjectProperty(IRI.create(prefix + "hasCountryOfOrigin"));
        if (hasProperty(man, reasoner, mozzarellaTopping, hasOrigin)) {
            Console.WriteLine("Instances of " + mozzarellaTopping
                    + " have a country of origin");
        }
    }

    /**
     * Prints out the properties that instances of a class expression must have.
     * 
     * @param man
     *        The manager
     * @param ont
     *        The ontology
     * @param reasoner
     *        The reasoner
     * @param cls
     *        The class expression
     */
    private static void printProperties(OWLOntologyManager man,
            OWLOntology ont, OWLReasoner reasoner, OWLClass cls) {
        if (!ont.containsClassInSignature(cls.getIRI())) {
            throw new Exception("Class not in signature of the ontology");
        }
        // Note that the following code could be optimised... if we find that
        // instances of the specified class do not have a property, then we
        // don't need to check the sub properties of this property
        Console.WriteLine("Properties of " + cls);
        var propIt = ont.getObjectPropertiesInSignature().iterator();
        while(propIt.hasNext()) {
            OWLObjectPropertyExpression prop = (OWLObjectPropertyExpression) propIt.next();
            bool sat = hasProperty(man, reasoner, cls, prop);
            if (sat) {
                Console.WriteLine("Instances of " + cls
                        + " necessarily have the property " + prop);
            }
        }
    }

    private static bool
            hasProperty(OWLOntologyManager man, OWLReasoner reasoner,
                    OWLClass cls, OWLObjectPropertyExpression prop) {
        // To test whether the instances of a class must have a property we
        // create a some values from restriction and then ask for the
        // satisfiability of the class interesected with the complement of this
        // some values from restriction. If the intersection is satisfiable then
        // the instances of the class don't have to have the property,
        // otherwise, they do.
        OWLDataFactory dataFactory = man.getOWLDataFactory();
        OWLClassExpression restriction = dataFactory
                .getOWLObjectSomeValuesFrom(prop, dataFactory.getOWLThing());
        // Now we see if the intersection of the class and the complement of
        // this restriction is satisfiable
        OWLClassExpression complement = dataFactory
                .getOWLObjectComplementOf(restriction);
        OWLClassExpression intersection = dataFactory
                .getOWLObjectIntersectionOf(cls, complement);
        return !reasoner.isSatisfiable(intersection);
    }

    /**
     * This example shows how to use IRI mappers to redirect imports and
     * loading.
     * 
     * @throws OWLOntologyCreationException
     */
    
    public void shouldUseIRIMappers(){
        IRI MGED_ONTOLOGY_IRI = IRI
                .create("http://mged.sourceforge.net/ontologies/MGEDOntology.owl");
        IRI PROTEGE_ONTOLOGY_IRI = IRI
                .create("http://protege.stanford.edu/plugins/owl/protege");
        IRI TONES_REPOSIITORY_IRI = IRI
                .create("http://owl.cs.manchester.ac.uk/repository/download");
        // Create a manager to work with
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // Load the MGED ontology. There is a copy of the MGED ontology located
        // at the address pointed to by its ontology IRI (this is good practice
        // and is recommended in the OWL 2 spec).
        OWLOntology ontology = manager.loadOntology(MGED_ONTOLOGY_IRI);
        // Print out the ontology IRI and its imported ontology IRIs
        printOntologyAndImports(manager, ontology);
        // We'll load the MGED ontology again, but this time, we'll get the
        // Protege ontology (that it imports) from the TONES repository. To tell
        // the ontology manager to do this we need to add an IRI mapper. We need
        // an implementation of OWLOntologyIRIMapper. Given and IRI and
        // OWLOntologyIRIMapper simply returns some other IRI. There are quite a
        // few implementations of IRI mapper in the OWL API, here we will just
        // use a really basic implementation that maps a specific IRI to another
        // specific IRI. Create a mapper that maps the Protege ontology IRI to
        // the document IRI that points to a copy in the TONES ontology
        // repository.
        IRI protegeOntologyDocumentIRI = getTONESRepositoryDocumentIRI(
                PROTEGE_ONTOLOGY_IRI, TONES_REPOSIITORY_IRI);
        OWLOntologyIRIMapper iriMapper = new SimpleIRIMapper(
                PROTEGE_ONTOLOGY_IRI, protegeOntologyDocumentIRI);
        Console.WriteLine();
        Console.WriteLine();
        // Create a new manager that we will use to load the MGED ontology
        OWLOntologyManager manager2 = OWLManager.createOWLOntologyManager();
        // Register our mapper with the manager
        manager2.addIRIMapper(iriMapper);
        // Now load our MGED ontology
        OWLOntology ontology2 = manager2.loadOntology(MGED_ONTOLOGY_IRI);
        // Print out the details
        printOntologyAndImports(manager2, ontology2);
        // Notice that the document IRI of the protege ontology is different to
        // the document IRI of the ontology when it was loaded the first time.
        // This is due to the mapper redirecting the ontology loader. For
        // example, AutoIRIMapper: An AutoIRIMapper finds ontologies in a local
        // folder and maps their IRIs to their locations in this folder We
        // specify a directory/folder where the ontologies are located. In this
        // case we've just specified the tmp directory.
        java.io.File file = new java.io.File("/tmp");
        // We can also specify a flag to indicate whether the directory should
        // be searched recursively.
        OWLOntologyIRIMapper autoIRIMapper = new AutoIRIMapper(file, false);
        // We can now use this mapper in the usual way, i.e.
        manager2.addIRIMapper(autoIRIMapper);
        // Of course, applications (such as Protege) usually implement their own
        // mappers to deal with specific application requirements.
    }

    private static void printOntologyAndImports(OWLOntologyManager manager,
            OWLOntology ontology) {
        Console.WriteLine("Loaded ontology:");
        // Print ontology IRI and where it was loaded from (they will be the
        // same)
        printOntology(manager, ontology);
        // List the imported ontologies
        var importIt = ontology.getImports().iterator();
        while(importIt.hasNext()) {
            OWLOntology importedOntology = (OWLOntology)importIt.next();
            Console.WriteLine("Imports:");
            printOntology(manager, importedOntology);
        }
    }

    /**
     * Prints the IRI of an ontology and its document IRI.
     * 
     * @param manager
     *        The manager that manages the ontology
     * @param ontology
     *        The ontology
     */
    private static void printOntology(OWLOntologyManager manager,
            OWLOntology ontology) {
        IRI ontologyIRI = ontology.getOntologyID().getOntologyIRI();
        IRI documentIRI = manager.getOntologyDocumentIRI(ontology);
        Console.WriteLine(ontologyIRI == null ? "anonymous" : ontologyIRI
                .toQuotedString());
        Console.WriteLine("    from " + documentIRI.toQuotedString());
    }

    /**
     * Convenience method that obtains the document IRI of an ontology contained
     * in the TONES ontology repository given the ontology IRI. The TONES
     * repository contains various ontologies of interest to reasoner developers
     * and tools developers. Ontologies in the repository may be accessed in a
     * RESTful way (see http://owl.cs.manchester.ac.uk/repository/) for more
     * details). We basically get an ontology by specifying the repository IRI
     * with an ontology query parameter that has the ontology IRI that we're
     * after as its value.
     * 
     * @param ontologyIRI
     *        The IRI of the ontology.
     * @param Tones
     *        tones iri
     * @return The document IRI of the ontology in the TONES repository.
     */
    private static IRI
            getTONESRepositoryDocumentIRI(IRI ontologyIRI, IRI Tones) {
        StringBuilder sb = new StringBuilder();
        sb.Append(Tones);
        sb.Append("?ontology=");
        sb.Append(ontologyIRI);
        return IRI.create(sb.ToString());
    }

    /**
     * This example shows how to extract modules.
     * 
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    
    public void shouldExtractModules(){
        String DOCUMENT_IRI = "http://owl.cs.manchester.ac.uk/co-ode-files/ontologies/pizza.owl";
        // Create our manager
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        // Load the pizza ontology
        OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI
                .create(DOCUMENT_IRI));
        Console.WriteLine("Loaded: " + ont.getOntologyID());
        // We want to extract a module for all toppings. We therefore have to
        // generate a seed signature that contains "PizzaTopping" and its
        // subclasses. We start by creating a signature that consists of
        // "PizzaTopping".
        OWLDataFactory df = man.getOWLDataFactory();
        OWLClass toppingCls = df.getOWLClass(IRI.create(ont.getOntologyID()
                .getOntologyIRI().toString()
                + "#PizzaTopping"));
        HashSet<OWLEntity> sig = new HashSet<OWLEntity>();
        sig.Add(toppingCls);
        // We now add all subclasses (direct and indirect) of the chosen
        // classes. Ideally, it should be done using a DL reasoner, in order to
        // take inferred subclass relations into account. We are using the
        // structural reasoner of the OWL API for simplicity.
        HashSet<OWLEntity> seedSig = new HashSet<OWLEntity>();
        OWLReasoner reasoner = new StructuralReasoner(ont,
                new SimpleConfiguration(), BufferingMode.NON_BUFFERING);
        foreach (OWLEntity ent in sig) {
            seedSig.Add(ent);
            /// HOW TO TRANSLATE THIS?
            //if (OWLClass.class.isAssignableFrom(ent.getClass())) {
            //    NodeSet<OWLClass> subClasses = reasoner.getSubClasses(
            //            (OWLClass) ent, false);
            //    seedSig.addAll(subClasses.getFlattened());
            //}
        }
        // Output for debugging purposes
        Console.WriteLine();
        Console.WriteLine("Extracting the module for the seed signature consisting of the following entities:");
        foreach (OWLEntity ent in seedSig) {
            Console.WriteLine("  " + ent);
        }
        Console.WriteLine();
        Console.WriteLine("Some statistics of the original ontology:");
        Console.WriteLine("  " + ont.getSignature(true).size() + " entities");
        Console.WriteLine("  " + ont.getLogicalAxiomCount()
                + " logical axioms");
        Console.WriteLine("  "
                + (ont.getAxiomCount() - ont.getLogicalAxiomCount())
                + " other axioms");
        Console.WriteLine();
        // We now extract a locality-based module. For most reuse purposes, the
        // module type should be STAR -- this yields the smallest possible
        // locality-based module. These modules guarantee that all entailments
        // of the original ontology that can be formulated using only terms from
        // the seed signature or the module will also be entailments of the
        // module. In easier words, the module preserves all knowledge of the
        // ontology about the terms in the seed signature or the module.
        
        SyntacticLocalityModuleExtractor sme = new SyntacticLocalityModuleExtractor(
                man, ont, ModuleType.STAR);
        IRI moduleIRI = IRI.create("file:/tmp/PizzaToppingModule.owl");
        OWLOntology mod = sme.extractAsOntology((java.util.Set)seedSig, moduleIRI);
        // Output for debugging purposes
        Console.WriteLine("Some statistics of the module:");
        Console.WriteLine("  " + mod.getSignature(true).size() + " entities");
        Console.WriteLine("  " + mod.getLogicalAxiomCount()
                + " logical axioms");
        Console.WriteLine("  "
                + (mod.getAxiomCount() - mod.getLogicalAxiomCount())
                + " other axioms");
        Console.WriteLine();
        // And we save the module.
        Console.WriteLine("Saving the module as "
                + mod.getOntologyID().getOntologyIRI());
        man.saveOntology(mod);
    }

    /**
     * The following example uses entities and axioms that are used in the OWL
     * Primer. The purpose of this example is to illustrate some of the methods
     * of creating class expressions and various types of axioms. Typically, an
     * ontology wouldn't be constructed programmatically in a long drawn out
     * fashion like this, it would be constructe in an ontology editor such as
     * Protege 4, or Swoop. The OWL API would then be used to examine the
     * asserted structure of the ontology, and in conjunction with an OWL
     * reasoner such as FaCT++ or Pellet used to query the inferred ontology.
     * 
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    
    public void owlPrimer(){
        // The OWLOntologyManager is at the heart of the OWL API, we can create
        // an instance of this using the OWLManager class, which will set up
        // commonly used options (such as which parsers are registered etc.
        // etc.)
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        // We want to create an ontology that corresponds to the ontology used
        // in the OWL Primer. Every ontology has a URI that uniquely identifies
        // the ontology. The URI is essentially a name for the ontology. Note
        // that the URI doesn't necessarily point to a location on the web - in
        // this example, we won't publish the ontology at the URL corresponding
        // to the ontology URI below.
        IRI ontologyIRI = IRI.create("http://example.com/owlapi/families");
        // Now that we have a URI for out ontology, we can create the actual
        // ontology. Note that the create ontology method throws an
        // OWLOntologyCreationException if there was a problem creating the
        // ontology.
        OWLOntology ont = manager.createOntology(ontologyIRI);
        // We can use the manager to get a reference to an OWLDataFactory. The
        // data factory provides a point for creating OWL API objects such as
        // classes, properties and individuals.
        OWLDataFactory factory = manager.getOWLDataFactory();
        // We first need to create some references to individuals. All of our
        // individual must have URIs. A common convention is to take the URI of
        // an ontology, append a # and then a local name. For example we can
        // create the individual 'John', using the ontology URI and appending
        // #John. Note however, that there is no reuqirement that a URI of a
        // class, property or individual that is used in an ontology have a
        // correspondance with the URI of the ontology.
        OWLIndividual john = factory.getOWLNamedIndividual(IRI
                .create(ontologyIRI + "#John"));
        OWLIndividual mary = factory.getOWLNamedIndividual(IRI
                .create(ontologyIRI + "#Mary"));
        OWLIndividual susan = factory.getOWLNamedIndividual(IRI
                .create(ontologyIRI + "#Susan"));
        OWLIndividual bill = factory.getOWLNamedIndividual(IRI
                .create(ontologyIRI + "#Bill"));
        // The ontologies that we created aren't contained in any ontology at
        // the moment. Individuals (or classes or properties) can't directly be
        // added to an ontology, they have to be used in axioms, and then the
        // axioms are added to an ontology. We now want to add some facts to the
        // ontology. These facts are otherwise known as property assertions. In
        // our case, we want to say that John has a wife Mary. To do this we
        // need to have a reference to the hasWife object property (object
        // properties link an individual to an individual, and data properties
        // link and individual to a constant - here, we need an object property
        // because John and Mary are individuals).
        OWLObjectProperty hasWife = factory.getOWLObjectProperty(IRI
                .create(ontologyIRI + "#hasWife"));
        // Now we need to create the assertion that John hasWife Mary. To do
        // this we need an axiom, in this case an object property assertion
        // axiom. This can be thought of as a "triple" that has a subject, john,
        // a predicate, hasWife and an object Mary
        OWLObjectPropertyAssertionAxiom axiom1 = factory
                .getOWLObjectPropertyAssertionAxiom(hasWife, john, mary);
        // We now need to add this assertion to our ontology. To do this, we
        // apply an ontology change to the ontology via the OWLOntologyManager.
        // First we create the change object that will tell the manager that we
        // want to add the axiom to the ontology
        AddAxiom addAxiom1 = new AddAxiom(ont, axiom1);
        // Now we apply the change using the manager.
        manager.applyChange(addAxiom1);
        // Now we want to add the other facts/assertions to the ontology John
        // hasSon Bill Get a refernece to the hasSon property
        OWLObjectProperty hasSon = factory.getOWLObjectProperty(IRI
                .create(ontologyIRI + "#hasSon"));
        // Create the assertion, John hasSon Bill
        OWLAxiom axiom2 = factory.getOWLObjectPropertyAssertionAxiom(hasSon,
                john, bill);
        // Apply the change
        manager.applyChange(new AddAxiom(ont, axiom2));
        // John hasDaughter Susan
        OWLObjectProperty hasDaughter = factory.getOWLObjectProperty(IRI
                .create(ontologyIRI + "#hasDaughter"));
        OWLAxiom axiom3 = factory.getOWLObjectPropertyAssertionAxiom(
                hasDaughter, john, susan);
        manager.applyChange(new AddAxiom(ont, axiom3));
        // John hasAge 33 In this case, hasAge is a data property, which we need
        // a reference to
        OWLDataProperty hasAge = factory.getOWLDataProperty(IRI
                .create(ontologyIRI + "#hasAge"));
        // We create a data property assertion instead of an object property
        // assertion
        OWLAxiom axiom4 = factory.getOWLDataPropertyAssertionAxiom(hasAge,
                john, 33);
        manager.applyChange(new AddAxiom(ont, axiom4));
        // In the above code, 33 is an integer, so we can just pass 33 into the
        // data factory method. Behind the scenes the OWL API will create a
        // typed constant that it will use as the value of the data property
        // assertion. We could have manually created the constant as follows:
        OWLDatatype intDatatype = factory.getIntegerOWLDatatype();
        OWLLiteral thirtyThree = factory.getOWLLiteral("33", intDatatype);
        // We would then create the axiom as follows:
        factory.getOWLDataPropertyAssertionAxiom(hasAge, john, thirtyThree);
        // However, the convenice method is much shorter! We can now create the
        // other facts/assertion for Mary. The OWL API uses a change object
        // model, which means we can stack up changes (or sets of axioms) and
        // apply the changes (or add the axioms) in one go. We will do this for
        // Mary
        HashSet<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        axioms.Add(factory.getOWLObjectPropertyAssertionAxiom(hasSon, mary,
                bill));
        axioms.Add(factory.getOWLObjectPropertyAssertionAxiom(hasDaughter,
                mary, susan));
        axioms.Add(factory.getOWLDataPropertyAssertionAxiom(hasAge, mary, 31));
        // Add facts/assertions for Bill and Susan
        axioms.Add(factory.getOWLDataPropertyAssertionAxiom(hasAge, bill, 13));
        axioms.Add(factory.getOWLDataPropertyAssertionAxiom(hasAge, mary, 8));
        // Now add all the axioms in one go - there is a convenience method on
        // OWLOntologyManager that will automatically generate the AddAxiom
        // change objects for us. We need to specify the ontology that the
        // axioms should be added to and the axioms to add.
        manager.addAxioms(ont, (java.util.Set)axioms);
        // Now specify the genders of John, Mary, Bill and Susan. To do this we
        // need the male and female individuals and the hasGender object
        // property.
        OWLIndividual male = factory.getOWLNamedIndividual(IRI
                .create(ontologyIRI + "#male"));
        OWLIndividual female = factory.getOWLNamedIndividual(IRI
                .create(ontologyIRI + "#female"));
        OWLObjectProperty hasGender = factory.getOWLObjectProperty(IRI
                .create(ontologyIRI + "#hasGender"));
        HashSet<OWLAxiom> genders = new HashSet<OWLAxiom>();
        genders.Add(factory.getOWLObjectPropertyAssertionAxiom(hasGender, john,
                male));
        genders.Add(factory.getOWLObjectPropertyAssertionAxiom(hasGender, mary,
                female));
        genders.Add(factory.getOWLObjectPropertyAssertionAxiom(hasGender, bill,
                male));
        genders.Add(factory.getOWLObjectPropertyAssertionAxiom(hasGender,
                susan, female));
        // Add the facts about the genders
        manager.addAxioms(ont, (java.util.Set)genders);
        // Domain and Range Axioms //At this point, we have an ontology
        // containing facts about several individuals. We now want to specify
        // more information about the various properties that we have used. We
        // want to say that the domains and ranges of hasWife, hasSon and
        // hasDaughter are the class Person. To do this we need various domain
        // and range axioms, and we need a reference to the class Person First
        // get a reference to the person class
        OWLClass person = factory.getOWLClass(IRI.create(ontologyIRI
                + "#Person"));
        // Now we add the domain and range axioms that specify the domains and
        // ranges of the various properties that we are interested in.
        HashSet<OWLAxiom> domainsAndRanges = new HashSet<OWLAxiom>();
        // Domain and then range of hasWife
        domainsAndRanges.Add(factory.getOWLObjectPropertyDomainAxiom(hasWife,
                person));
        domainsAndRanges.Add(factory.getOWLObjectPropertyRangeAxiom(hasWife,
                person));
        // Domain and range of hasSon and also hasDaugher
        domainsAndRanges.Add(factory.getOWLObjectPropertyDomainAxiom(hasSon,
                person));
        domainsAndRanges.Add(factory.getOWLObjectPropertyRangeAxiom(hasSon,
                person));
        domainsAndRanges.Add(factory.getOWLObjectPropertyDomainAxiom(
                hasDaughter, person));
        domainsAndRanges.Add(factory.getOWLObjectPropertyRangeAxiom(
                hasDaughter, person));
        // We also have the domain of the data property hasAge as Person, and
        // the range as integer. We need the integer datatype. The XML Schema
        // Datatype URIs are used for data types. The OWL API provide a built in
        // set via the XSDVocabulary enum.
        domainsAndRanges.Add(factory.getOWLDataPropertyDomainAxiom(hasAge,
                person));
        OWLDatatype integerDatatype = factory.getIntegerOWLDatatype();
        domainsAndRanges.Add(factory.getOWLDataPropertyRangeAxiom(hasAge,
                integerDatatype));
        // Now add all of our domain and range axioms
        manager.addAxioms(ont, (java.util.Set)domainsAndRanges);
        // Class assertion axioms //We can also explicitly say than an
        // individual is an instance of a given class. To do this we use a Class
        // assertion axiom.
        OWLClassAssertionAxiom classAssertionAx = factory
                .getOWLClassAssertionAxiom(person, john);
        // Add the axiom directly using the addAxiom convenience method on
        // OWLOntologyManager
        manager.addAxiom(ont, classAssertionAx);
        // Inverse property axioms //We can specify the inverse property of
        // hasWife as hasHusband We first need a reference to the hasHusband
        // property.
        OWLObjectProperty hasHusband = factory.getOWLObjectProperty(IRI
                .create(ont.getOntologyID().getOntologyIRI() + "#hasHusband"));
        // The full URI of the hasHusband property will be
        // http://example.com/owlapi/families#hasHusband since the URI of our
        // ontology is http://example.com/owlapi/families Create the inverse
        // object properties axiom and add it
        manager.addAxiom(ont,
                factory.getOWLInverseObjectPropertiesAxiom(hasWife, hasHusband));
        // Sub property axioms //OWL allows a property hierarchy to be
        // specified. Here, hasSon and hasDaughter will be specified as
        // hasChild.
        OWLObjectProperty hasChild = factory.getOWLObjectProperty(IRI
                .create(ont.getOntologyID().getOntologyIRI() + "#hasChild"));
        OWLSubObjectPropertyOfAxiom hasSonSubHasChildAx = factory
                .getOWLSubObjectPropertyOfAxiom(hasSon, hasChild);
        // Add the axiom
        manager.addAxiom(ont, hasSonSubHasChildAx);
        // And hasDaughter, which is also a sub property of hasChild
        manager.addAxiom(ont,
                factory.getOWLSubObjectPropertyOfAxiom(hasDaughter, hasChild));
        // Property characteristics //Next, we want to say that the hasAge
        // property is Functional. This means that something can have at most
        // one hasAge property. We can do this with a functional data property
        // axiom First create the axiom
        OWLFunctionalDataPropertyAxiom hasAgeFuncAx = factory
                .getOWLFunctionalDataPropertyAxiom(hasAge);
        // Now add it to the ontology
        manager.addAxiom(ont, hasAgeFuncAx);
        // The hasWife property should be Functional, InverseFunctional,
        // Irreflexive and Asymmetric. Note that the asymmetric property axiom
        // used to be called antisymmetric - older versions of the OWL API may
        // refer to antisymmetric property axioms
        HashSet<OWLAxiom> hasWifeAxioms = new HashSet<OWLAxiom>();
        hasWifeAxioms.Add(factory.getOWLFunctionalObjectPropertyAxiom(hasWife));
        hasWifeAxioms.Add(factory
                .getOWLInverseFunctionalObjectPropertyAxiom(hasWife));
        hasWifeAxioms
                .Add(factory.getOWLIrreflexiveObjectPropertyAxiom(hasWife));
        hasWifeAxioms.Add(factory.getOWLAsymmetricObjectPropertyAxiom(hasWife));
        // Add all of the axioms that specify the characteristics of hasWife
        manager.addAxioms(ont, (java.util.Set)hasWifeAxioms);
        // SubClass axioms //Now we want to start specifying something about
        // classes in our ontology. To begin with we will simply say something
        // about the relationship between named classes Besides the Person class
        // that we already have, we want to say something about the classes Man,
        // Woman and Parent. To say something about these classes, as usual, we
        // need references to them:
        OWLClass man = factory.getOWLClass(IRI.create(ontologyIRI + "#Man"));
        OWLClass woman = factory
                .getOWLClass(IRI.create(ontologyIRI + "#Woman"));
        OWLClass parent = factory.getOWLClass(IRI.create(ontologyIRI
                + "#Parent"));
        // It is important to realise that simply getting references to a class
        // via the data factory does not add them to an ontology - only axioms
        // can be added to an ontology. Now say that Man, Woman and Parent are
        // subclasses of Person
        manager.addAxiom(ont, factory.getOWLSubClassOfAxiom(man, person));
        manager.addAxiom(ont, factory.getOWLSubClassOfAxiom(woman, person));
        manager.addAxiom(ont, factory.getOWLSubClassOfAxiom(parent, person));
        // Restrictions //Now we want to say that Person has exactly 1 Age,
        // exactly 1 Gender and, only has gender that is male or female. We will
        // deal with these restrictions one by one and then combine them as a
        // superclass (Necessary conditions) of Person. All anonymous class
        // expressions extend OWLClassExpression. First, hasAge exactly 1
        OWLDataExactCardinality hasAgeRestriction = factory
                .getOWLDataExactCardinality(1, hasAge);
        // Now the hasGender exactly 1
        OWLObjectExactCardinality hasGenderRestriction = factory
                .getOWLObjectExactCardinality(1, hasGender);
        // And finally, the hasGender only {male female} To create this
        // restriction, we need an OWLObjectOneOf class expression since male
        // and female are individuals We can just list as many individuals as we
        // need as the argument of the method.
        OWLObjectOneOf maleOrFemale = factory.getOWLObjectOneOf(male, female);
        // Now create the actual restriction
        OWLObjectAllValuesFrom hasGenderOnlyMaleFemale = factory
                .getOWLObjectAllValuesFrom(hasGender, maleOrFemale);
        // Finally, we bundle these restrictions up into an intersection, since
        // we want person to be a subclass of the intersection of them
        OWLObjectIntersectionOf intersection = factory
                .getOWLObjectIntersectionOf(hasAgeRestriction,
                        hasGenderRestriction, hasGenderOnlyMaleFemale);
        // And now we set this anonymous intersection class to be a superclass
        // of Person using a subclass axiom
        manager.addAxiom(ont,
                factory.getOWLSubClassOfAxiom(person, intersection));
        // Restrictions and other anonymous classes can also be used anywhere a
        // named class can be used. Let's set the range of hasSon to be Person
        // and hasGender value male. This requires an anonymous class that is
        // the intersection of Person, and also, hasGender value male. We need
        // to create the hasGender value male restriction - this describes the
        // class of things that have a hasGender relationship to the individual
        // male.
        OWLObjectHasValue hasGenderValueMaleRestriction = factory
                .getOWLObjectHasValue(hasGender, male);
        // Now combine this with Person in an intersection
        OWLClassExpression personAndHasGenderValueMale = factory
                .getOWLObjectIntersectionOf(person,
                        hasGenderValueMaleRestriction);
        // Now specify this anonymous class as the range of hasSon using an
        // object property range axioms
        manager.addAxiom(ont, factory.getOWLObjectPropertyRangeAxiom(hasSon,
                personAndHasGenderValueMale));
        // We can do a similar thing for hasDaughter, by specifying that
        // hasDaughter has a range of Person and hasGender value female. This
        // time, we will make things a little more compact by not using so many
        // variables
        OWLClassExpression rangeOfHasDaughter = factory
                .getOWLObjectIntersectionOf(person,
                        factory.getOWLObjectHasValue(hasGender, female));
        manager.addAxiom(ont, factory.getOWLObjectPropertyRangeAxiom(
                hasDaughter, rangeOfHasDaughter));
        // Data Ranges and Equivalent Classes axioms //In OWL 2, we can specify
        // expressive data ranges. Here, we will specify the classes Teenage,
        // Adult and Child by saying something about individuals ages. First we
        // take the class Teenager, all of whose instance have an age greater or
        // equal to 13 and less than 20. In Manchester Syntax this is written as
        // Person and hasAge some int[>=13, <20] We create a data range by
        // taking the integer datatype and applying facet restrictions to it.
        // Note that we have statically imported the data range facet vocabulary
        // OWLFacet
        OWLFacetRestriction geq13 = factory.getOWLFacetRestriction(
                org.semanticweb.owlapi.vocab.OWLFacet.MIN_INCLUSIVE, factory.getOWLLiteral(13));
        // We don't have to explicitly create the typed constant, there are
        // convenience methods to do this
        OWLFacetRestriction lt20 = factory.getOWLFacetRestriction(
                org.semanticweb.owlapi.vocab.OWLFacet.MAX_EXCLUSIVE, 20);
        // Restrict the base type, integer (which is just an XML Schema
        // Datatype) with the facet restrictions.
        OWLDataRange dataRng = factory.getOWLDatatypeRestriction(
                integerDatatype, geq13, lt20);
        // Now we have the data range of greater than equal to 13 and less than
        // 20 we can use this in a restriction.
        OWLDataSomeValuesFrom teenagerAgeRestriction = factory
                .getOWLDataSomeValuesFrom(hasAge, dataRng);
        // Now make Teenager equivalent to Person and hasAge some int[>=13, <20]
        // First create the class Person and hasAge some int[>=13, <20]
        OWLClassExpression teenagePerson = factory.getOWLObjectIntersectionOf(
                person, teenagerAgeRestriction);
        OWLClass teenager = factory.getOWLClass(IRI.create(ontologyIRI
                + "#Teenager"));
        OWLEquivalentClassesAxiom teenagerDefinition = factory
                .getOWLEquivalentClassesAxiom(teenager, teenagePerson);
        manager.addAxiom(ont, teenagerDefinition);
        // Do the same for Adult that has an age greater than 21
        OWLDataRange geq21 = factory.getOWLDatatypeRestriction(integerDatatype,
                factory.getOWLFacetRestriction(org.semanticweb.owlapi.vocab.OWLFacet.MIN_INCLUSIVE, 21));
        OWLClass adult = factory
                .getOWLClass(IRI.create(ontologyIRI + "#Adult"));
        OWLClassExpression adultAgeRestriction = factory
                .getOWLDataSomeValuesFrom(hasAge, geq21);
        OWLClassExpression adultPerson = factory.getOWLObjectIntersectionOf(
                person, adultAgeRestriction);
        OWLAxiom adultDefinition = factory.getOWLEquivalentClassesAxiom(adult,
                adultPerson);
        manager.addAxiom(ont, adultDefinition);
        // And finally Child
        OWLDataRange notGeq21 = factory.getOWLDataComplementOf(geq21);
        OWLClass child = factory
                .getOWLClass(IRI.create(ontologyIRI + "#Child"));
        OWLClassExpression childAgeRestriction = factory
                .getOWLDataSomeValuesFrom(hasAge, notGeq21);
        OWLClassExpression childPerson = factory.getOWLObjectIntersectionOf(
                person, childAgeRestriction);
        OWLAxiom childDefinition = factory.getOWLEquivalentClassesAxiom(child,
                childPerson);
        manager.addAxiom(ont, childDefinition);
        // Different individuals //In OWL, we can say that individuals are
        // different from each other. To do this we use a different individuals
        // axiom. Since John, Mary, Bill and Susan are all different
        // individuals, we can express this using a different individuals axiom.
        OWLDifferentIndividualsAxiom diffInds = factory
                .getOWLDifferentIndividualsAxiom(john, mary, bill, susan);
        manager.addAxiom(ont, diffInds);
        // Male and Female are also different
        manager.addAxiom(ont,
                factory.getOWLDifferentIndividualsAxiom(male, female));
        // Disjoint classes //Two say that two classes do not have any instances
        // in common we use a disjoint classes axiom:
        OWLDisjointClassesAxiom disjointClassesAxiom = factory
                .getOWLDisjointClassesAxiom(man, woman);
        manager.addAxiom(ont, disjointClassesAxiom);
        // Ontology Management //Having added axioms to out ontology we can now
        // save it (in a variety of formats). RDF/XML is the default format
        /// HOW TO TRANSLATE THIS?
        //Console.WriteLine("RDF/XML: ");
        //manager.saveOntology(ont, new StreamDocumentTarget();
        //// OWL/XML
        //Console.WriteLine("OWL/XML: ");
        //manager.saveOntology(ont, new OWLXMLOntologyFormat(),
        //        new StreamDocumentTarget(System.out));
        //// Manchester Syntax
        //Console.WriteLine("Manchester syntax: ");
        //manager.saveOntology(ont, new ManchesterOWLSyntaxOntologyFormat(),
        //        new StreamDocumentTarget(System.out));
        //// Turtle
        //Console.WriteLine("Turtle: ");
        //manager.saveOntology(ont, new TurtleOntologyFormat(),
        //        new StreamDocumentTarget(System.out));
    }
}
}