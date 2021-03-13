from cognipy.ontology import Ontology

def test_load1():
    Ontology("rdf/uri", "https://protege.stanford.edu/ontologies/pizza/pizza.owl")

def test_load_ACGT():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/ACGT/v1.0/00001.owl")

def test_load_AEO():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/AEO/v3.7/00002.owl")

def test_load_BAMS():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/BAMS/bams-from-swanson-98-4-5-07.from-web/2009-10-28/00003.owl")

def test_load_BAMS_neuron_ontology():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/BAMS/neuron-ontology/2009-10-28/00006.owl")

def test_load_BioPAX():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/BioPAX/biopax-level2/biopax-example-ecocyc-glycolysis/v1.0/00007.owl")

def test_load_biopax_level3():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/BioPAX/biopax-level3/v1.0/00012.owl")

def test_load_DOLCE_CommonSenseMapping():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/DOLCE/CommonSenseMapping/2008-02-08/00013.owl")

def test_load_DOLCE_DLP_397():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/DOLCE/DLP_397/2008-02-08/00014.owl")

def test_load_DOLCE_DOLCE_Lite():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/DOLCE/DOLCE-Lite/2008-02-08/00015.owl")

def test_load_DOLCE_ExtendedDnS():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/DOLCE/ExtendedDnS/2008-02-08/00016.owl")

def test_load_DOLCE_Plans():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/DOLCE/Plans/2008-02-08/00020.owl")

def test_load_Erlangen_CRM():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/Erlangen_CRM/v5.0.1/00025.owl")

def test_load_GALEN():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/GALEN/galen-module1/2008-02-05/00035.owl")

# 180M
# def test_load_GO_extensions():
#     Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/GO_extensions/x-anatomy-importer/2012-04-24/00040.owl")

def test_load_LUBM():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/LUBM/lubm/2009-05-29/00347.owl")

# 240M
# def test_load_NCI():
#     Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/NCI/v12.04e/00786.owl")

def test_load_OBI():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/OBI/2010-03-04/00350.owl")

def test_load_OBO_AERO():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/OBO/AERO/2012-10-12/00351.owl")

# lots of refrences
# def test_load_OBO_biological_process():
#     Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/OBO/biological_process/2009-02-12/00368.owl")

def test_load_ProPreO():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/ProPreO/2008-02-09/00772.owl")

def test_load_RKB_rna_with_individuals():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/RKB/rna-with-individuals/v0.2/00773.owl")

def test_load_RobertsFamily():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/RobertsFamily/2009-09-03/00775.owl")

def test_load_SEMINTEC():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/SEMINTEC/bigFile/2008-02-08/00776.owl")

def test_load_SNOMED():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/SNOMED/2014-09-01/00795.owl")

def test_load_VICODI():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/VICODI/vicodi_all/2008-02-08/00779.owl")

def test_load_WINE_food():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/WINE/food/2004-02-10/00781.owl")

def test_load_WINE_wine():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/WINE/wine/2004-02-10/00782.owl")

def test_load_PIZZA():
    Ontology("rdf/uri", "http://krr-nas.cs.ox.ac.uk/ontologies/lib/co-ode.org/PIZZA/2007-02-12/00793.owl")
