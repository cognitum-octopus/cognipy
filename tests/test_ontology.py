import pytest
from cognipy.ontology import Ontology
from cognipy.ontology import CQL

@pytest.fixture()
def onto():
    return Ontology("cnl/string", "World says Hello. Hello is a word. Every impala is an animal. Every omnivore is an animal. Test is an impala.")

def test_get_load_error(onto):
    assert onto.get_load_error() == None

def test_as_rdf(onto):
    assert type(onto.as_rdf()) == str

def test_as_cnl(onto):
    cnl = onto.as_cnl()
    assert 'World says Hello.\nHello is a word.\nEvery impala is an animal.\nEvery omnivore is an animal.\nTest is an impala.\n' == cnl

def test_sub_concepts_of(onto):
    assert onto.sub_concepts_of("word") == []
    assert onto.sub_concepts_of("animal") == ['omnivore', 'impala']

def test_sparql_query_for_instances(onto):
    assert type(onto.sparql_query_for_instances("World")) is str

#TODO def test_annotations_for_subject():
#     assert onto.annotations_for_subject()

#TODO def test_constraints_for_subject():
#     assert onto.constrains_for_subject("World"))

def test_super_concepts_of(onto):
    assert onto.super_concepts_of("omnivore") == ['animal']

def test_instances_of(onto):
    assert onto.instances_of("word") == ['Hello']

def test_autocomplete(onto):
    assert onto.autocomplete("Hello is a ") == ['thing', 'word', 'impala', 'animal', 'omnivore']

def test_highlight(onto):
    assert onto.highlight("Hello is a word") == 'Hello **is** **a** word'

def test_insert_cnl(onto):
    onto.insert_cnl("Test says Hello.")
    assert onto.as_cnl() == 'World says Hello.\nHello is a word.\nEvery impala is an animal.\nEvery omnivore is an animal.\nTest is an impala.\nTest says Hello.\n'

#TODO def test_delete_cnl():

#TODO def test_delete_instance():

#TODO def setup_function

def test_sparql_query(onto):
    df = onto.sparql_query(CQL("""select ?a1 ?a2 {
                                ?a1 rdf:type <animal>. 
                                ?a2 rdf:type <word>. 
                            }""", "http://www.cognitum.eu/onto#"))
    assert df['a1'][0] == 'Test'
    assert df['a2'][0] == 'Hello'

def test_why(onto):
    assert type(onto.why("Test is an animal?")) == str

def test_reasoningInfo(onto):
    assert type(onto.reasoningInfo()) == str

def test_create_graph(onto):
    assert type(onto.create_graph()) == bytes
