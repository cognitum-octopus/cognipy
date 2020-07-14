from IPython.display import display, Markdown, Latex
import pandas as pd
import textwrap
import re

import pydot
from io import BytesIO
import IPython

from cognipy.interop import cognipy_create,cognipy_delete,cognipy_call

def CQL(sparql,ns='http://www.cognitum.eu/onto#'):
    def CONC(str):
        return '<'+ns+ str.split('-')[0]+"".join( [x.title() for x in str.split('-')[1:]])+">"
    def my_replace(match):
        match = match.group()
        return CONC(match[1:-1])
    return re.sub(r'\<[^\>:/]+\>', my_replace, sparql)

def encode_string_for_graph_label(str):
    return str.replace('{', '&#123;').replace('|', '&#124;').replace('}', '&#125;').replace('<', '&#60;').replace('>', '&#62;')

class Ontology:
    def _resolve_str(self,v):
        if(isinstance(v,str) and len(v)>0 and v[0]=='`' and v[-1]=='`'):
            return self._evaluator(v[1:-1])
        else:
            return v

    def _resolve_value(self,col):
        if(isinstance(col,list)):
            return list([self._resolve_value(y) for y in col])
        elif(isinstance(col,set)):
            return set({self._resolve_value(y) for y in col})
        elif(isinstance(col,dict)):
            return dict({x:self._resolve_value(y) for x,y in col.items()})
        else:
            return self._resolve_str(col)

    def __init__(self,source, arg, verbose = False,evaluator = None, graph_attribute_formatter = lambda val:encode_string_for_graph_label(textwrap.fill(str(val),40)),stop_on_error=True):
        """Constructor for the ontology.

        Args:
            source (str one of: 'cnl/file'|'cnl/string'|'rdf/uri'|'rdf/string')
            arg (str): path/string/url
            verbose(bool) : should the content of the ontology be displayed
        """
        loadAnnotations=True
        passParamsAsCnl=True
        modalCheck=True
        materialized=True
        self._verbose=verbose
        self._evaluator = evaluator

        self._materialized= materialized
        self._graph_attribute_formatter = graph_attribute_formatter

        self._uid = cognipy_create()
        if source == "cnl/file":
            cognipy_call(self._uid,"LoadCnl",arg,loadAnnotations,modalCheck,passParamsAsCnl,stop_on_error)
        elif source == "cnl/string":
            cognipy_call(self._uid,"LoadCnlFromString",arg,loadAnnotations,modalCheck,passParamsAsCnl,stop_on_error)
        elif source == "rdf/uri":
            cognipy_call(self._uid,"LoadRdf",arg,loadAnnotations,modalCheck,passParamsAsCnl,stop_on_error)
        elif source == "rdf/string":
            cognipy_call(self._uid,"LoadRdfFromString",arg,loadAnnotations,modalCheck,passParamsAsCnl,stop_on_error)

        if self._verbose:
            cnl = self.as_cnl()
            markdown = self.highlight(cnl).replace('**a**','a').replace('**X**','X').replace('**Y**','Y').replace('\r\n','<br>').replace("<br><br>","<br>")
            display(Markdown(markdown))

    def get_load_error(self):
        return cognipy_call(self._uid,"GetLoadError")
        
    def __del__(self):
        cognipy_delete(self._uid)

    def as_rdf(self,conclusions=False):
        """Returns the content of the knowledge base in OWL/RDF format."""
        return cognipy_call(self._uid,"ToRDF",conclusions)

    def as_cnl(self,conclusions=False):
        """Returns the content of the knowledge base in OCNL format."""
        return cognipy_call(self._uid,"ToCNL",conclusions,True)

    def sub_concepts_of(self, cnl, direct=False):
        """Get all the sub-concepts of the given concept specification

        Args:
            cnl (str): the cnl expression that evaluates to the concept definition

        Returns:
            Pandas DataFrame congaing all the sub-concepts of the given concept expression
        """
        return cognipy_call(self._uid,"GetSubConceptsOf",cnl,direct)

    def sparql_query_for_instances(self, cnl):
        return cognipy_call(self._uid,"SelectInstancesSPARQL",cnl,False)

#TODO
    def annotations_for_subject(self,subject,prop="", lang=""):
        return self._to_pandas(robjects.r('ontorion.annotations.for.subject')(self._onto,subject,prop,lang))

#TODO
    def constrains_for_subject(self,concept):
        return self._to_pandas(robjects.r('ontorion.constrains.for.subject')(self._onto,concept))

    def super_concepts_of(self, cnl,direct=False):
        """Get all the super-concepts of the given concept specification

        Args:
            cnl (str): the cnl expression that evaluates to the concept definition

        Returns:
            Pandas DataFrame congaing all the super-concepts of the given concept expression
        """
        return cognipy_call(self._uid,"GetSuperConceptsOf",cnl,direct)

    def instances_of(self, cnl, direct=False):
        return cognipy_call(self._uid,"GetInstancesOf",cnl,direct)

    def _to_pandas(self, vals,cols):
        if self._evaluator is not None:
            vals = [ [ self._resolve_value(item) for item in row  ] for row in vals ]
        return pd.DataFrame(vals,columns=cols)

    def select_instances_of(self, cnl, set_resolver=lambda s:s):
        """Get all the instances of the given concept specification

        Args:
            cnl (str): the cnl expression that evaluates to the concept definition

        Returns:
            Pandas DataFrame congaing all the instances of the given concept expression together with all their attributes and relations
        """
        val = cognipy_call(self._uid,"SparqlQueryForInstancesWithDetails",cnl)
        return self._to_pandas(val["Item2"],val["Item1"])

    def autocomplete(self, str):
        """Gives the autocompletion lists for a given string

        Args:
            cnl (str): the partially defined cnl expression

        Returns:
            str: autocompletion
        """
        return list(cognipy_call(self._uid,"AutoComplete",str))

    def highlight(self, cnl):
        """Gives the Markdown of the given cnl

        Args:
            cnl (str): the cnl string

        Returns:
            str: highlighted cnl string
        """
        return cognipy_call(self._uid,"Highlight",cnl)

    def insert_cnl(self,cnl):
        """Inserts new knowledge into the ontology

        Args:
            cnl (str): the cnl string
        """
        cognipy_call(self._uid,"KnowledgeInsert",cnl, True, True)
        if self._verbose:
            markdown = self.highlight(cnl).replace('**a**','a').replace('**X**','X').replace('**Y**','Y').replace('\r\n','<br>').replace("<br><br>","<br>")
            display(Markdown(markdown))

    def delete_cnl(self,cnl):
        """Deletes the specified knowledge from the ontology

        Args:
            cnl (str): the cnl string
        """
        cognipy_call(self._uid,"KnowledgeDelete",cnl, True)
        if self._verbose:
            markdown = self.highlight(cnl).replace('**a**','a').replace('**X**','X').replace('**Y**','Y').replace('\r\n','<br>').replace("<br><br>","<br>")
            display(Markdown(markdown))

    def delete_instance(self,inst):
        """Deletes the specified instance  from the ontology including all the conncetions it has to other instances and concepts

        Args:
            inst (str): the cnl name of the instance
        """
        cognipy_call(self._uid,"RemoveInstance",inst)
        if self._verbose:
            markdown = "("+inst+")"
            display(Markdown(markdown))

#TODO
    def setup_function(self,name, func):
        """Sets the function up so it can be called from within the complex cnl rule

        Args:
            name (str): the name of the function
            func : the function
        """
        robjects.globalenv[name]=func

    def sparql_query(self,query, asCNL = True, column_names=None):
        """Executes the SPARQL query

        Args:
            query: the SPARQL query. YOu can directly use prefixes like: [rdf:,rdfs,owl:]
            asCNL : should the result names be automatically converted  back to their CNL representation (defult) of they should remain being rdf identifiers.

        Returns:
            Pandas Dataframe containing all the results of the query
        """

        val= cognipy_call(self._uid,"SparqlQuery",query,True,asCNL)
        return self._to_pandas(val["Item2"],val["Item1"] if column_names is None else column_names)

    def why(self,cnl):
        """Explains why

        Args:
            cnl (str): the cnl string
        """
        return cognipy_call(self._uid,"Why",cnl,True)

    def reasoningInfo(self):
        self.super_concepts_of("a thing")
        return cognipy_call(self._uid,"GetReasoningInfo")

    def create_graph(self, layout="hierarchical", show= {"subsumptions","types","relations","attributes"} , include = {}, exclude = {}, constrains=[], format="svg",filename=None, fontname=None,fontsize=11):
        """Creates the ontology diagram as an image

        Args:
            layout(str): type of layout (one of: "hierarchical"(default), "force directed")
            show(set(str)): one or more of strings {"subsumptions","types","relations","attributes"}
            include(set(str)): one or more names of entities that must be included (even if show argument says no)
            exclude(set(str)): one or more names of entities that must be excluded (even if show argument says yes)
            format(str): format of the output image(one of: "svg"(default) ,"png")
        """
        showCnc = "subsumptions" in show
        showInst = "types" in show
        showRels = "relations" in show
        showVals = "attributes" in show

        SubsumptionEdgeColor = "black"
        RelationEdgeColor = "black"
        ConceptColor = "aliceblue"
        InstanceColor = "whitesmoke"

        def addEdge(graph,frm,to):
            n=graph.get_node(frm)
            if len(n)==0:
                frmN = pydot.Node(frm)
                graph.add_node(frmN)
            else:
                frmN = n[0]

            n=graph.get_node(to)
            if len(graph.get_node(to))==0:
                toN = pydot.Node(to)
                graph.add_node(toN)
            else:
                toN = n[0]

            e = pydot.Edge(to, frm)
            graph.add_edge(e)

            e.set_penwidth(0.5)
            if fontname is not None:
                e.set_fontname(fontname)
            e.set_fontsize(fontsize)

            if fontname is not None:
                frmN.set_fontname(fontname)
            frmN.set_fontsize(fontsize)
            frmN.set_shape('Mrecord')
            frmN.set_penwidth(0.5)

            if fontname is not None:
                toN.set_fontname(fontname)
            toN.set_fontsize(fontsize)
            toN.set_shape('Mrecord')
            toN.set_penwidth(0.5)

            return frmN,e,toN

        def addNode(graph,frm):
            n=graph.get_node(frm)
            if len(n)==0:
                frmN = pydot.Node(frm)
                graph.add_node(frmN)
            else:
                frmN = n[0]

            if fontname is not None:
                frmN.set_fontname(fontname)
            frmN.set_fontsize(fontsize)
            frmN.set_shape('Mrecord')
            frmN.set_penwidth(0.5)

            return frmN

        def addInstanceOf(graph,frm,to):
            frmN,e,toN = addEdge(graph,frm,to)

            e.set_dir("back")
            e.set_arrowtail("empty")
            e.set_style("dashed")
            e.set_color(SubsumptionEdgeColor)

            toN.set_style('filled')
            toN.set_fillcolor(ConceptColor)

            frmN.set_style('filled')
            frmN.set_fillcolor(InstanceColor)

        def addSubsumption(graph,frm,to):
            frmN,e,toN = addEdge(graph,frm,to)

            e.set_dir("back")
            e.set_arrowtail("empty")
            e.set_style("solid")
            e.set_color(SubsumptionEdgeColor)

            toN.set_style('filled')
            toN.set_fillcolor(ConceptColor)

            frmN.set_style('filled')
            frmN.set_fillcolor(ConceptColor)

        def addLabel(labels,frmN,frm,str):
            if(frm not in labels.keys()):
                lbl = "{"+frm+"}"
            else:
                lbl = labels[frm]

            lbl = lbl[:-1]+"|{"+str+"}}"
            frmN.set_label(lbl)
            labels[frm]=lbl


        def addAssertion(graph,labels,frm,to,etx):
            frmN = addNode(graph,frm)
            toN= addNode(graph,to)

            e = pydot.Edge(frmN,toN)
            graph.add_edge(e)

            e.set_penwidth(0.5)
            if fontname is not None:
                e.set_fontname(fontname)
            e.set_fontsize(fontsize)

            e.set_dir("forward")
            e.set_arrowhead("open")
            e.set_style("solid")
            e.set_color(RelationEdgeColor)
            e.set_label(etx)

            toN.set_style('filled')
            toN.set_fillcolor(InstanceColor)

            frmN.set_style('filled')
            frmN.set_fillcolor(InstanceColor)

        def addDataValue(graph,labels,frm,val,etx):
            frmN = addNode(graph,frm)
            frmN.set_style('filled')
            frmN.set_fillcolor(InstanceColor)
            addLabel(labels,frmN,frm,etx+"|"+val)

        graph = pydot.Dot(graph_type='graph')
        negsubsumptions = set([])
        negtypes = set([])
        subsumptions = set([])
        instances = set([])
        relations = set([])
        datavalues = set([])
        labels={}

        def canAdd(shw,frm,to):
            return (shw and (frm not in exclude) and (to not in exclude)) or (not shw and (frm not in exclude) and (to not in exclude) and ((frm in include ) or (to in include)))

        def canAddR(shw,frm,r,to):
            return (shw and (frm not in exclude) and (r not in exclude) and (to not in exclude)) or (not shw and (frm not in exclude) and (r not in exclude) and (to not in exclude)and ((frm in include) or (r in include) or (to in include)))

        def constrainsUnionSparql(constrains,v):
            if len(constrains)==0:
                return ""
            def singSpqr(x):
                q=self.sparql_query_for_instances(x)
                q=q[q.find("WHERE")+7:]
                q=q[:len(q)-1]
                q=q.replace("?x0",v).replace("?z0",v)
                return q
            return "{SELECT "+v+" {{"+str.join("} UNION {",[singSpqr(x) for x in constrains])+"}}}"

        if showCnc or len(include)>0:
            negres = self.sparql_query("select ?x ?y ?z {?x rdfs:subClassOf ?y. ?y rdfs:subClassOf ?z. ?x rdf:type owl:Class. ?y rdf:type owl:Class. ?z rdf:type owl:Class. filter (?x!=?y && ?y !=?z && ?x!=?z)}")
            for frm,mid,to in negres.values:
                if frm not in exclude and to not in exclude and mid not in exclude:
                    if not frm+":"+to in negsubsumptions:
                        negsubsumptions.add(frm+":"+to)

            res = self.sparql_query("select ?x ?y {?x rdfs:subClassOf ?y. ?x rdf:type owl:Class. ?y rdf:type owl:Class.  filter (?x!=?y)}")
            for frm,to in res.values:
                if canAdd(showCnc,frm,to):
                    if not frm+":"+to in negsubsumptions:
                        if not frm+":"+to in subsumptions:
                            subsumptions.add(frm+":"+to)
                            addSubsumption(graph,frm,to)

        xcspq=constrainsUnionSparql(constrains,"?x")

        if showInst or len(include)>0:
            negres = self.sparql_query("select ?x ?y ?z {?x rdf:type ?y. ?y rdfs:subClassOf ?z. ?x rdf:type owl:NamedIndividual. ?y rdf:type owl:Class. ?z rdf:type owl:Class. filter (?x!=?y && ?y !=?z && ?x!=?z)."+xcspq+" }")
            for frm,mid,to in negres.values:
                if frm not in exclude and to not in exclude and mid not in exclude:
                    if not frm+":"+to in negtypes:
                        negtypes.add(frm+":"+to)

            res = self.sparql_query("select ?x ?y {?x rdf:type ?y. ?x rdf:type owl:NamedIndividual. ?y rdf:type owl:Class."+xcspq+"}")
            for frm,to in res.values:
                if canAdd(showInst,frm,to):
                    if not frm+":"+to in negtypes:
                        if not frm+":"+to in instances:
                            instances.add(frm+":"+to)
                            addInstanceOf(graph,frm,to)

        if showRels or len(include)>0:
            res = self.sparql_query("select ?x ?y ?r {?x ?r ?y. ?x rdf:type owl:NamedIndividual. ?y rdf:type owl:NamedIndividual. ?r rdf:type owl:ObjectProperty. "+xcspq+"}");
            for frm,to,rel in res.values:
                if canAddR(showRels,frm,rel,to):
                    if not frm+":"+rel+":"+to in relations:
                        relations.add(frm+":"+rel+":"+to)
                        addAssertion(graph,labels,frm,to,rel)

        if showVals or len(include)>0:
            res = self.sparql_query("select ?x ?y ?r {?x ?r ?y. ?x rdf:type owl:NamedIndividual. ?r rdf:type owl:DatatypeProperty. "+xcspq+"}");
            datavaldic = {}
            for frm,to,rel in res.values:
                if canAdd(showVals,frm,rel):
                    to = self._graph_attribute_formatter(to)
                    if not frm+":"+rel+":"+to in datavalues:
                        datavalues.add(frm+":"+rel+":"+to)
                        if(frm+":"+rel in datavaldic.keys()):
                            datavaldic[frm+":"+rel]=datavaldic[frm+":"+rel]+"|"+to
                        else:
                            datavaldic[frm+":"+rel]=to

            for k in datavaldic.keys():
                frm,rel = k.split(':')
                addDataValue(graph,labels,frm,"{"+datavaldic[k]+"}",rel)

        graph.set_K("1")
        #graph.set_layout("circo")
        if layout=="hierarchical":
            graph.set_layout("dot")
        elif layout=="force directed":
            graph.set_layout("fdp")
        else:
            raise ValueError("unknown layout type")
        graph.set_splines("true")

        #graph.set_layout("neato")
        #graph.set_layout("osage")
        #graph.set_layout("patchwork")
        #graph.set_layout("sfdp")
        #graph.set_layout("twopi")

        if filename is None:
            if format=="svg":
                return graph.create_svg(prog='dot')
            elif format=="png":
                return graph.create_png(prog='dot')
            else:
                raise ValueError("unknown image format")
        else:
            if format=="svg":
                return graph.write_svg(filename,prog='dot')
            elif format=="png":
                return graph.write_png(filename,prog='dot')
            else:
                raise ValueError("unknown image format")

    def draw_graph(self, layout="hierarchical", show= {"subsumptions","types","relations","attributes"} , include = {}, exclude = {}, constrains=[],fontname=None,fontsize=11):
        """Draws the ontology

        Args:
            layout(str): type of layout (one of: "hierarchical", "force directed")
            show(set(str)): one or more of strings {"concepts","instances","relations","values"}
            include(set(str)): one or more names of entities that must be included (even if show argument says no)
            exclude(set(str)): one or more names of entities that must be excluded (even if show argument says yes)
        """
        return IPython.display.SVG(data=self.create_graph(layout,show,include,exclude,constrains,fontname=fontname,fontsize=fontsize))

class ABoxBatch:
    def __init__(self):
        self._rb = []
        self._insts = []

    def has_type(self,inst,cls):
        self._rb.append("type")
        self._rb.append(inst)
        self._rb.append("")
        self._rb.append(cls)
        return self

    def same_as(self,inst,inst2):
        self._rb.append("==")
        self._rb.append(inst)
        self._rb.append("")
        self._rb.append(inst2)
        return self

    def different_from(self,inst,inst2):
        self._rb.append("!=")
        self._rb.append(inst)
        self._rb.append("")
        self._rb.append(inst2)
        return self

    def relates(self,inst,prop,inst2):
        self._rb.append("R")
        self._rb.append(inst)
        self._rb.append(prop)
        self._rb.append(inst2)
        return self

    def value(self,inst,prop,v):
        def tos(v):
            if(isinstance(v,int)):
                return "I:"+str(v)
            elif(isinstance(v,float)):
                return "F:"+str(v)
            elif(isinstance(v,bool)):
                return "B:"+"[1]" if v else "[0]"
            else:
                return "S:'"+str(v)+"'"

        self._rb.append("D")
        self._rb.append(inst)
        self._rb.append(prop)
        self._rb.append(tos(v))
        return self

    def delete_instance(self,name):
        self._insts.append(name)

    def insert(self,onto):
        cognipy_call(onto._uid,"AssertionsInsert", self._rb)
        if onto._verbose:
            markdown = ''
            for i in range(0,len(self._rb),4):
                if self._rb[i]=="type":
                    markdown+= self._rb[i+3]+"("+self._rb[i+1]+")"
                elif self._rb[i]=="==":
                    markdown+= self._rb[i+1]+"=="+self._rb[i+3]
                elif self._rb[i]=="!=":
                    markdown+= self._rb[i+1]+"!="+self._rb[i+3]
                elif self._rb[i]=="R":
                    markdown+= self._rb[i+2]+"("+self._rb[i+1]+","+self._rb[i+3]+")"
                else:
                    markdown+= self._rb[i+2]+"("+self._rb[i+1]+","+self._rb[i+3]+")"
                markdown+="<br>"
            markdown+=""
            display(Markdown(markdown))

    def delete(self,onto):
        cognipy_call(onto._uid,"AssertionsDelete", self._rb)
        for inst in self._insts:
            cognipy_call(onto._uid,"RemoveInstance", inst)

        if onto._verbose:
            markdown = ''
            for i in range(0,len(self._rb),4):
                if self._rb[i]=="type":
                    markdown+= self._rb[i+3]+"("+self._rb[i+1]+")"
                elif self._rb[i]=="==":
                    markdown+= self._rb[i+1]+"=="+self._rb[i+3]
                elif self._rb[i]=="!=":
                    markdown+= self._rb[i+1]+"!="+self._rb[i+3]
                elif self._rb[i]=="R":
                    markdown+= self._rb[i+2]+"("+self._rb[i+1]+","+self._rb[i+3]+")"
                else:
                    markdown+= self._rb[i+2]+"("+self._rb[i+1]+","+self._rb[i+3]+")"
                markdown+="<br>"

            for inst in self._insts:
                markdown+= "*("+inst+")"
                markdown+="<br>"

            markdown+=""
            display(Markdown(markdown))

#TODO
from functools import wraps

def custom_predicate(onto):
    def custom_inner_pred(func):
        @wraps(func)
        def wrapper(*args,**kwargs):
            return func(*[onto._resolve_value(m) for m in args],**kwargs)

        robjects.globalenv[func.__name__]=wrapper
        return wrapper
    return custom_inner_pred
