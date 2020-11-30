import pytest
from cognipy.ontology import Ontology


onto = Ontology("cnl/string", "World says Hello. Hello is a word. Every impala is an animal. Every omnivore is an animal.")

def test_get_load_error():
    assert onto.get_load_error() == None

# def test_as_rdf():
#     assert

def test_as_cnl():
    cnl = onto.as_cnl()
    assert 'World says Hello.\nHello is a word.\nEvery impala is an animal.\nEvery omnivore is an animal.\n' == cnl

def test_sub_concepts_of():
    assert onto.sub_concepts_of("word") == []
    assert onto.sub_concepts_of("animal") == ['omnivore', 'impala']

# def test_sparql_query_for_instances():
#     assert onto.sparql_query_for_instances()

# def test_annotations_for_subject():
#     assert onto.annotations_for_subject()

# def test_constraints_for_subject():
#     assert onto.constrains_for_subject()

def test_super_concepts_of():
    assert onto.super_concepts_of("omnivore") == ['animal']

def test_instances_of():
    assert onto.instances_of("word") == ['Hello']

def test_autocomplete():
    assert onto.autocomplete("Every impala is an ") == ['thing']
    assert onto.autocomplete("Every impala is an") == ['animal']
    assert onto.autocomplete("Hello is a ") == ['thing', 'word', 'impala', 'animal', 'omnivore']

def test_highlight():
    assert onto.highlight("Hello is a word") == 'Hello **is** **a** word'

# def test_insert_cnl():
#     onto.insert_cnl("Test says Hello.")
#     assert onto.


# def test_delete_cnl():

# def test_delete_instance():

# def setup_function

# def sparql_query

# def test_why():
#
# def test_reasoningInfo():
#
#
# def test_create_graph():
