import pytest
from cognipy.ontology import Ontology
from cognipy.ontology import CQL
import xml.etree.ElementTree as ET
import pydot

@pytest.fixture()
def onto():
    return Ontology("cnl/string", "World says Hello. Hello is a word. Every impala is an animal."
                                  " Every omnivore is an animal. Test is an impala.")

def test_as_rdf(onto):
    rdf_xml = onto.as_rdf()
    tree = ET.fromstring(rdf_xml)
    test_list = ["http://www.cognitum.eu/onto#", "http://www.cognitum.eu/onto#says",
                 "http://www.cognitum.eu/onto#animal", "http://www.cognitum.eu/onto#impala",
                 "http://www.cognitum.eu/onto#omnivore", "http://www.cognitum.eu/onto#word",
                 "http://www.cognitum.eu/onto#Hello","http://www.cognitum.eu/onto#Test",
                 "http://www.cognitum.eu/onto#World"]
    for element in tree:
        assert list(element.attrib.values())[0] in test_list

def test_as_cnl(onto):
    cnl = onto.as_cnl()
    test_cnl = ['World says Hello.', 'Hello is a word.','Every impala is an animal.','Every omnivore is an animal.',
                'Test is an impala.']
    for str in test_cnl:
        assert str in cnl

def test_sub_concepts_of(onto):
    assert onto.sub_concepts_of("word") == []
    assert onto.sub_concepts_of("animal") == ['omnivore', 'impala']

#TODO
def test_sparql_query_for_instances(onto):
    assert type(onto.sparql_query_for_instances("World")) is str

#TODO def test_annotations_for_subject():
#     assert onto.annotations_for_subject()

#TODO def test_constraints_for_subject():
#     assert onto.constrains_for_subject("World"))

def test_super_concepts_of(onto):
    assert onto.super_concepts_of("omnivore") == ['animal']
    assert onto.super_concepts_of("Hello") == ['word']
    assert onto.super_concepts_of("impala") == ['animal']

def test_instances_of(onto):
    assert onto.instances_of("word") == ['Hello']
    assert onto.instances_of("impala") == ['Test']
    assert onto.instances_of("animal") == ['Test']

def test_autocomplete(onto):
    assert onto.autocomplete("Hello is a ") == ['thing', 'word', 'impala', 'animal', 'omnivore']

def test_highlight(onto):
    assert onto.highlight("Hello is a word") == 'Hello **is** **a** word'
    assert onto.highlight("Hello is a word.") == 'Hello **is** **a** word.'

def test_insert_cnl(onto):
    onto.insert_cnl("Test says Hello.")
    assert "Test is an impala" in onto.as_cnl()
    assert "Test says Hello." in onto.as_cnl()

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
    why = onto.why("Test is an animal?")
    assert '"concluded":"Test is an animal."' in why
    assert "Every impala is an animal." in why
    assert "Test is an impala." in why

def test_reasoningInfo(onto):
    assert type(onto.reasoningInfo()) == str

def test_create_graph(onto):
    test_graph = pydot.Dot(graph_type='graph')
    test_graph.set_K("1")
    test_graph.set_layout("dot")
    test_graph.set_splines("true")

    test_graph_svg = test_graph.create_svg(prog='dot')
    graph = onto.create_graph(show={})

    assert test_graph_svg == graph

def test_create_graph_subsumptions(onto):
    test_graph = pydot.Dot(graph_type='graph')

    for frm, to in [("omnivore", "animal"), ("impala", "animal")]:
        n = test_graph.get_node(frm)
        if len(n) == 0:
            frmN = pydot.Node(frm)
            test_graph.add_node(frmN)
        else:
            frmN = n[0]

        n = test_graph.get_node(to)
        if len(test_graph.get_node(to)) == 0:
            toN = pydot.Node(to)
            test_graph.add_node(toN)
        else:
            toN = n[0]
        e = pydot.Edge(to, frm)
        test_graph.add_edge(e)
        e.set_penwidth(0.5)

        e.set_fontsize(11)

        frmN.set_fontsize(11)
        frmN.set_shape('Mrecord')
        frmN.set_penwidth(0.5)

        toN.set_fontsize(11)
        toN.set_shape('Mrecord')
        toN.set_penwidth(0.5)

        e.set_dir("back")
        e.set_arrowtail("empty")
        e.set_style("solid")
        e.set_color("black")

        toN.set_style('filled')
        toN.set_fillcolor("aliceblue")

        frmN.set_style('filled')
        frmN.set_fillcolor("aliceblue")

    test_graph.set_K("1")
    test_graph.set_layout("dot")
    test_graph.set_splines("true")

    test_graph_svg = test_graph.create_svg(prog='dot')
    graph = onto.create_graph(show={"subsumptions"})

    assert graph == test_graph_svg

def test_create_graph_types(onto):
    test_graph = pydot.Dot(graph_type='graph')

    for frm, to in [['Test','impala'], ['Hello','word']]:
        frmN = pydot.Node(frm)
        test_graph.add_node(frmN)

        toN = pydot.Node(to)
        test_graph.add_node(toN)

        e = pydot.Edge(to, frm)
        test_graph.add_edge(e)
        e.set_penwidth(0.5)

        e.set_fontsize(11)

        frmN.set_fontsize(11)
        frmN.set_shape('Mrecord')
        frmN.set_penwidth(0.5)

        toN.set_fontsize(11)
        toN.set_shape('Mrecord')
        toN.set_penwidth(0.5)

        e.set_dir("back")
        e.set_arrowtail("empty")
        e.set_style("dashed")
        e.set_color("black")

        toN.set_style('filled')
        toN.set_fillcolor("aliceblue")

        frmN.set_style('filled')
        frmN.set_fillcolor("whitesmoke")

    test_graph.set_K("1")
    test_graph.set_layout("dot")
    test_graph.set_splines("true")

    test_graph_svg = test_graph.create_svg(prog='dot')
    graph = onto.create_graph(show={"types"})

    assert graph == test_graph_svg

def test_create_graph_relations(onto):
    test_graph = pydot.Dot(graph_type='graph')

    for frm, to, rel in [['World','Hello','says']]:

        frmN = pydot.Node(frm)
        test_graph.add_node(frmN)

        frmN.set_fontsize(11)
        frmN.set_shape('Mrecord')
        frmN.set_penwidth(0.5)


        toN = pydot.Node(to)
        test_graph.add_node(toN)

        toN.set_fontsize(11)
        toN.set_shape('Mrecord')
        toN.set_penwidth(0.5)

        e = pydot.Edge(frmN, toN)
        test_graph.add_edge(e)
        e.set_penwidth(0.5)
        e.set_fontsize(11)
        e.set_dir("forward")
        e.set_arrowhead("open")
        e.set_style("solid")
        e.set_color("black")
        e.set_label(rel)
        toN.set_style('filled')
        toN.set_fillcolor("whitesmoke")
        frmN.set_style('filled')
        frmN.set_fillcolor("whitesmoke")

        test_graph.set_K("1")
        test_graph.set_layout("dot")
        test_graph.set_splines("true")

    test_graph_svg = test_graph.create_svg(prog='dot')
    graph = onto.create_graph(show={"relations"})

    assert graph == test_graph_svg

def test_create_graph_attributes(onto):
    test_graph = pydot.Dot(graph_type='graph')

    onto.insert_cnl("Test has-name equal-to 'test_name'.")

    res = [['Test','test_name','has-name']]
    for frm, to, rel in res:
        to = onto._graph_attribute_formatter(to)

        frmN = pydot.Node(frm)
        test_graph.add_node(frmN)

        frmN.set_fontsize(11)
        frmN.set_shape('Mrecord')
        frmN.set_penwidth(0.5)
        frmN.set_style('filled')
        frmN.set_fillcolor("whitesmoke")
        lbl = "{{"+frm+"}|{{"+rel+"}|{"+to+"}}}"
        frmN.set_label(lbl)

        test_graph.set_K("1")
        test_graph.set_layout("dot")
        test_graph.set_splines("true")
        test_graph_svg = test_graph.create_svg(prog='dot')
        graph = onto.create_graph(show={"attributes"})

        assert graph == test_graph_svg



# def test_draw_graph(onto):
#     assert type(onto.draw_graph()) == class 'IPython.core.display.SVG'
