    using System.Collections.Generic;
using Tools;
using System;

namespace CogniPy.CNL.EN
{
    public interface iaccept
    {
        object accept(IVisitor v);
    }

    public partial class PartialSymbol : TOKEN
    {
        public PartialSymbol(Parser yyp) : base(yyp) { }

        public override string yyname
        {
            get
            {
                return (string)this.GetType().GetProperty("yyname_" + this.yyps.GetType().Name).GetValue(this, new object[] { });
            }
        }
        public override int yynum
        {
            get
            {
                return (int)this.GetType().GetProperty("yynum_" + this.yyps.GetType().Name).GetValue(this, new object[] { });
            }
        }

        public virtual string yyname_endl { get { return null; } }
        public virtual int yynum_endl { get { return 0; } }
    }

    public partial class paragraph : iaccept
    {
        public paragraph(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<sentence> sentences;

        public paragraph(Parser yyp, sentence S)
            : base(yyp)
        { sentences = new System.Collections.Generic.List<sentence>(); sentences.Add(S); }
        public paragraph(Parser yyp, paragraph tu, sentence S)
            : base(yyp)
        { sentences = tu.sentences; sentences.Add(S); }

        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class sentence : iaccept
    {
        public sentence(Parser yyp) : base(yyp) { }
        public string modality;
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }

    public partial class subsumption : sentence
    {
        public subsumption(Parser yyp) : base(yyp) { }
        public subject c;
        public orloop d;

        public subsumption(Parser yyp, subject c_, string modality_, orloop d_) : base(yyp) { c = c_; d = d_; modality = modality_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class nosubsumption : sentence
    {
        public nosubsumption(Parser yyp) : base(yyp) { }
        public nosubject c;
        public orloop d;

        public nosubsumption(Parser yyp, nosubject c_, string modality_, orloop d_) : base(yyp) { c = c_; d = d_; modality = modality_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class annotation : sentence
    {
        public annotation(Parser yyp) : base(yyp) { }
        public string txt;

        public annotation(Parser yyp, string txt_) : base(yyp) { txt = txt_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class dlannotationassertion : sentence
    {
        public dlannotationassertion(Parser yyp):base(yyp) { }
        public dlannotationassertion(Parser yyp,string subject, string subjKind, W3CAnnotation w3cannot):base(yyp) { this.subject = subject; this.subjKind = subjKind; this.annotName = w3cannot.Type; this.value = (string)w3cannot.Value; this.language = w3cannot.Language; }
        public string subjKind;
        public string subject;
        public string annotName;
        public string value;
        public string language=null;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class equivalence2 : sentence
    {
        public equivalence2(Parser yyp) : base(yyp) { }
        public orloop c;
        public orloop d;

        public equivalence2(Parser yyp, orloop c_, string modality_, orloop d_) : base(yyp) { c = c_; d = d_; modality = modality_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class subsumption_if : sentence
    {
        public subsumption_if(Parser yyp) : base(yyp) { }
        public orloop c;
        public orloop d;

        public subsumption_if(Parser yyp, orloop c_, string modality_, orloop d_) : base(yyp) { c = c_; d = d_; modality = modality_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class datatypedef : sentence
    {
        public datatypedef(Parser yyp) : base(yyp) { }
        public string name;
        public abstractbound db;

        public datatypedef(Parser yyp, string name, abstractbound db) : base(yyp) { this.db = db.me(); this.name = name; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class exclusiveunion : sentence
    {
        public string name;
        public System.Collections.Generic.List<objectRoleExpr> objectRoleExprs;
        public exclusiveunion(Parser yyp) : base(yyp) { }
        public exclusiveunion(Parser yyp, string name_, orObjectRoleExprChain z_, string modality_)
            : base(yyp)
        {
            modality = modality_;
            name = name_;
            objectRoleExprs = z_.objectRoleExprs;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class exclusives : sentence
    {
        public System.Collections.Generic.List<objectRoleExpr> objectRoleExprs;
        public exclusives(Parser yyp) : base(yyp) { }
        public exclusives(Parser yyp, orObjectRoleExprChain z_, string modality_)
            : base(yyp)
        {
            modality = modality_;
            objectRoleExprs = z_.objectRoleExprs;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class rolesubsumption : sentence
    {
        public rolesubsumption(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<role> subChain;
        public roleWithXY superRole;


        public rolesubsumption(Parser yyp, role z_, roleWithXY s_)
            : base(yyp)
        {
            subChain = new System.Collections.Generic.List<role>();
            subChain.Add(z_);
            superRole = s_;
        }
        public rolesubsumption(Parser yyp, chain z_, roleWithXY s_)
            : base(yyp)
        {
            subChain = z_.roles;
            superRole = s_;
        }

        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class roleequivalence2 : sentence
    {
        public roleequivalence2(Parser yyp) : base(yyp) { }
        public role r;
        public roleWithXY s;
        public roleequivalence2(Parser yyp, role r_, roleWithXY s_) : base(yyp) { r = r_; s = s_; }

        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class roledisjoint2 : sentence
    {
        public roledisjoint2(Parser yyp) : base(yyp) { }
        public role r;
        public notRoleWithXY s;
        public roledisjoint2(Parser yyp, role r_, notRoleWithXY s_) : base(yyp) { r = r_; s = s_; }

        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class datarolesubsumption : sentence
    {
        public datarolesubsumption(Parser yyp) : base(yyp) { }
        public role subRole;
        public role superRole;

        public datarolesubsumption(Parser yyp, role z_, role s_) : base(yyp) { subRole = z_; superRole = s_; }

        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class dataroleequivalence2 : sentence
    {
        public dataroleequivalence2(Parser yyp) : base(yyp) { }
        public role r;
        public role s;
        public dataroleequivalence2(Parser yyp, role r_, role s_) : base(yyp) { r = r_; s = s_; }

        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class dataroledisjoint2 : sentence
    {
        public dataroledisjoint2(Parser yyp) : base(yyp) { }
        public role r;
        public role s;
        public dataroledisjoint2(Parser yyp, role r_, role s_) : base(yyp) { r = r_; s = s_; }

        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class haskey : sentence
    {
        public haskey(Parser yyp) : base(yyp) { }
        public objectRoleExpr s;
        public System.Collections.Generic.List<role> roles;
        public System.Collections.Generic.List<role> dataroles;

        public haskey(Parser yyp, objectRoleExpr s_, andanyrolechain x_)
            : base(yyp)
        {
            s = s_;
            roles = x_.chain;
            dataroles = x_.datachain;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }

    }

    public partial class subject : iaccept
    {
        public subject(Parser yyp) : base(yyp) {}
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }

    public partial class subjectEvery : subject
    {
        public subjectEvery(Parser yyp) : base(yyp) { }
        public single s;
        public subjectEvery(Parser yyp, single s_) : base(yyp) { s = s_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class subjectEverything : subject
    {
        public subjectEverything(Parser yyp) : base(yyp) { }
        public that t = null;
        public subjectEverything(Parser yyp, that t_) : base(yyp) { t = t_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class nosubject : iaccept
    {
        public nosubject(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }

    public partial class subjectNo : nosubject
    {
        public subjectNo(Parser yyp) : base(yyp) { }
        public single s;
        public subjectNo(Parser yyp, single s_) : base(yyp) { s = s_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class subjectNothing : nosubject
    {
        public subjectNothing(Parser yyp) : base(yyp) { }
        public that t = null;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class subjectBigName : subject
    {
        public subjectBigName(Parser yyp) : base(yyp) { }
        public string name;
        public subjectBigName(Parser yyp, string name_, bool very_) : base(yyp) { name = name_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class subjectThe : subject
    {
        public subjectThe(Parser yyp) : base(yyp) { }
        public single s;
        public bool only;
        public subjectThe(Parser yyp, bool only_, single s_) : base(yyp) { s = s_; only = only_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class objectRoleExpr : iaccept
    {
        public objectRoleExpr(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }
    public partial class objectRoleExpr1 : objectRoleExpr
    {
        public objectRoleExpr1(Parser yyp) : base(yyp) { }
        public bool Negated = false;
        public oobject s;
        public objectRoleExpr1(Parser yyp, bool Negated_, oobject s_) : base(yyp) { Negated = Negated_; s = s_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class objectRoleExpr2 : objectRoleExpr
    {
        public objectRoleExpr2(Parser yyp) : base(yyp) { }
        public bool Negated = false;
        public oobject s;
        public role r;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public objectRoleExpr2(Parser yyp, bool Negated_, oobject s_, role r_)
            : base(yyp)
        {
            Negated = Negated_; s = s_; r = r_;
        }
        public objectRoleExpr2(Parser yyp, bool Negated_, oobject s_, string rn, bool Inversed_ = false)
            : base(yyp)
        {
            Negated = Negated_; s = s_; r = new role(yyp, rn, Inversed_);
        }
    }

    public partial class objectRoleExpr3 : objectRoleExpr
    {
        public objectRoleExpr3(Parser yyp) : base(yyp) { }
        public that t;
        public role r;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public objectRoleExpr3(Parser yyp, that t_, role r_)
            : base(yyp)
        {
            t = t_; r = r_;
        }
    }

    public partial class modality
    {
        public modality(Parser yyp) : base(yyp) { }
    }
    
    public partial class modality2
    {
        public modality2(Parser yyp) : base(yyp) { }
    }

    public partial class isBeAre
    {
        public isBeAre(Parser yyp) : base(yyp) { }
    }

    public partial class aAn
    {
        public aAn(Parser yyp) : base(yyp) { }
    }

    public partial class instance : iaccept
    {
        public instance(Parser yyp) : base(yyp) {}
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class instanceThe : instance
    {
        public instanceThe(Parser yyp) : base(yyp) { }
        public single s;
        public bool only;
        public instanceThe(Parser yyp, bool only_, single s_) : base(yyp) { s = s_; only = only_; }

        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class instanceBigName : instance
    {
        public instanceBigName(Parser yyp) : base(yyp) { } 
        public string name;
        public bool very;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public instanceBigName(Parser yyp, string name_, bool very_) : base(yyp) { name = name_; very = very_; }
    }
    public partial class instanceList : iaccept
    {
        public instanceList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<instance> insts;

        public instanceList(Parser yyp, instance i, instance j)
            : base(yyp)
        { insts = new System.Collections.Generic.List<instance>(); insts.Add(i); insts.Add(j); }
        public instanceList(Parser yyp, instanceList il, instance i)
            : base(yyp)
        { insts = il.insts; insts.Add(i); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }

    public partial class chain : iaccept
    {
        public chain(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<role> roles;

        public chain(Parser yyp, role s)
            : base(yyp)
        { roles = new System.Collections.Generic.List<role>(); roles.Add(s); }

        public chain(Parser yyp, role s, role r)
            : base(yyp)
        { roles = new System.Collections.Generic.List<role>(); roles.Add(s); roles.Add(r); }
        public chain(Parser yyp, chain z, role r)
            : base(yyp)
        { roles = z.roles; roles.Add(r); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }

    public partial class orObjectRoleExprChain : iaccept
    {
        public orObjectRoleExprChain(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<objectRoleExpr> objectRoleExprs;

        public orObjectRoleExprChain(Parser yyp, objectRoleExpr r, objectRoleExpr s)
            : base(yyp)
        { objectRoleExprs = new System.Collections.Generic.List<objectRoleExpr>(); objectRoleExprs.Add(r); objectRoleExprs.Add(s); }
        public orObjectRoleExprChain(Parser yyp, orObjectRoleExprChain z, objectRoleExpr r)
            : base(yyp)
        { objectRoleExprs = z.objectRoleExprs; objectRoleExprs.Add(r); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }

    public partial class andanyrolechain : iaccept
    {
        public andanyrolechain(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<role> chain;
        public System.Collections.Generic.List<role> datachain;

        public andanyrolechain(Parser yyp, role r, bool isDataRole)
            : base(yyp)
        { chain = new System.Collections.Generic.List<role>(); datachain = new System.Collections.Generic.List<role>(); if (isDataRole)datachain.Add(r); else chain.Add(r); }
        public andanyrolechain(Parser yyp, andanyrolechain z, role r, bool isDataRole)
            : base(yyp)
        { chain = z.chain; datachain = z.datachain; if (isDataRole)datachain.Add(r); else chain.Add(r); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }
    public partial class oobject : iaccept
    {
        public oobject(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }
    public partial class oobjectA : oobject
    {
        public oobjectA(Parser yyp) : base(yyp) { }
        public single s;
        public oobjectA(Parser yyp, single s_) : base(yyp) { s = s_; }
        public oobjectA(Parser yyp, string name) : base(yyp) { s = new singleName(null, name); }
        public oobjectA(Parser yyp, string name, that t) : base(yyp) { s = new singleNameThat(null, name, t); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class oobjectInstance : oobject
    {
        public oobjectInstance(Parser yyp) : base(yyp) { }
        public instance i;
        public oobjectInstance(Parser yyp, instance i_) : base(yyp) { i = i_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class oobjectRelated : oobject
    {
        public oobjectRelated(Parser yyp) : base(yyp) { }
    }
    public partial class oobjectOnly : oobjectRelated
    {
        public oobjectOnly(Parser yyp) : base(yyp) { }
        public single s;
        public oobjectOnly(Parser yyp, single s_) : base(yyp) { s = s_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class oobjectOnlyInstance : oobjectRelated
    {
        public oobjectOnlyInstance(Parser yyp) : base(yyp) { }
        public instance i;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public oobjectOnlyInstance(Parser yyp, instance i_) : base(yyp) { i = i_; }
    }
    public partial class oobjectCardinal : oobjectRelated
    {
        public oobjectCardinal(Parser yyp) : base(yyp) { }
        public string Cmp;
        public string Cnt;
    }
    public partial class oobjectCmp : oobjectCardinal
    {
        public oobjectCmp(Parser yyp) : base(yyp) { }
        public single s;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public oobjectCmp(Parser yyp, string Cmp_, string Cnt_, single s_) : base(yyp) { s = s_; Cmp = Cmp_; Cnt = Cnt_; }
    }
    public partial class oobjectCmpInstance : oobjectCardinal
    {
        public oobjectCmpInstance(Parser yyp) : base(yyp) { }
        public instance i;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public oobjectCmpInstance(Parser yyp, string Cmp_, string Cnt_, instance i_) : base(yyp) { i = i_; Cmp = Cmp_; Cnt = Cnt_; }
    }
    public partial class oobjectBnd : oobjectRelated
    {
        public oobjectBnd(Parser yyp) : base(yyp) { }
        public abstractbound b;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public oobjectBnd(Parser yyp, abstractbound b_) : base(yyp) { b = b_.me(); }
    }
    public partial class oobjectOnlyBnd : oobjectRelated
    {
        public oobjectOnlyBnd(Parser yyp) : base(yyp) { }
        public abstractbound b;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public oobjectOnlyBnd(Parser yyp, abstractbound b_) : base(yyp) { b = b_.me(); }
    }
    public partial class oobjectCmpBnd : oobjectCardinal
    {
        public oobjectCmpBnd(Parser yyp) : base(yyp) { }
        public abstractbound b;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public oobjectCmpBnd(Parser yyp, string Cmp_, string Cnt_, abstractbound b_) : base(yyp) { Cnt = Cnt_; Cmp = Cmp_; b = b_.me(); }
    }

    public partial class oobjectSelf : oobjectRelated
    {
        public oobjectSelf(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class oobjectSomething : oobject
    {
        public oobjectSomething(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class oobjectNothing : oobject
    {
        public oobjectNothing(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class oobjectOnlyNothing : oobjectRelated
    {
        public oobjectOnlyNothing(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class oobjectSomethingThat : oobject
    {
        public oobjectSomethingThat(Parser yyp) : base(yyp) { }
        public that t;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public oobjectSomethingThat(Parser yyp, that t_) : base(yyp) { t = t_; }
    }
    public partial class oobjectOnlySomethingThat : oobjectRelated
    {
        public oobjectOnlySomethingThat(Parser yyp) : base(yyp) { }
        public that t;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public oobjectOnlySomethingThat(Parser yyp, that t_) : base(yyp) { t = t_; }
    }

    public partial class role : iaccept
    {
        public role(Parser yyp) : base(yyp) {}
        public string name;
        public bool inverse = false;
        public role(Parser yyp, string name_, bool inverse_) : base(yyp) { name = name_; inverse = inverse_; }

        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class roleWithXY : iaccept
    {
        public roleWithXY(Parser yyp) : base(yyp) { }
        public string name;
        public bool inverse = false;
        public roleWithXY(Parser yyp, string name_, bool inverse_) : base(yyp) { name = name_; inverse = inverse_;}

        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class notRoleWithXY : iaccept
    {
        public notRoleWithXY(Parser yyp) : base(yyp) { }
        public string name;
        public bool inverse = false;
        public notRoleWithXY(Parser yyp, string name_, bool inverse_) : base(yyp) { name = name_; inverse = inverse_; }

        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class single : iaccept
    {
        public single(Parser yyp) : base(yyp) {}
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class singleName : single
    {
        public singleName(Parser yyp) : base(yyp) {}
        public string name;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public singleName(Parser yyp, string name_) : base(yyp) { name = name_; }
    }
    public partial class singleThing : single
    {
        public singleThing(Parser yyp) : base(yyp) {}
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class singleNameThat : single
    {
        public singleNameThat(Parser yyp) : base(yyp) {}
        public string name;
        public that t;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public singleNameThat(Parser yyp, string name_, that t_) : base(yyp) { name = name_; t = t_; }
    }
    public partial class singleThingThat : single
    {
        public singleThingThat(Parser yyp) : base(yyp) {  }
        public that t;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public singleThingThat(Parser yyp, that t_) : base(yyp) { t = t_; }
    }

    public partial class that : iaccept
    {
        public that(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }
    public partial class thatOrLoop : that
    {
        public thatOrLoop(Parser yyp) : base(yyp) { }
        public orloop o;
        public thatOrLoop(Parser yyp, orloop o_) : base(yyp) { o = o_; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class singleOneOf : single
    {
        public singleOneOf(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<instance> insts;
        public singleOneOf(Parser yyp, instanceList il) : base(yyp) { insts = il.insts;}
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class andloop : iaccept
    {
        public andloop(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<objectRoleExpr> exprs;

        public andloop(Parser yyp, objectRoleExpr o)
            : base(yyp)
        { exprs = new System.Collections.Generic.List<objectRoleExpr>(); exprs.Add(o); }
        public andloop(Parser yyp, andloop l, objectRoleExpr o)
            : base(yyp)
        { exprs = l.exprs; exprs.Add(o); }
        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class orloop : iaccept
    {
        public orloop(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<andloop> exprs;

        public orloop(Parser yyp, andloop a)
            : base(yyp)
        { exprs = new System.Collections.Generic.List<andloop>(); exprs.Add(a); }
        public orloop(Parser yyp, orloop l, andloop a)
            : base(yyp)
        { exprs = l.exprs; exprs.Add(a); }
        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class comparer
    {
        public comparer(Parser yyp) : base(yyp) { }
    }
    public partial class comparer2
    {
        public comparer2(Parser yyp) : base(yyp) { }
    }
    public partial class equalTo
    {
        public equalTo(Parser yyp) : base(yyp) { }
    }
    public partial class unOp
    {
        public unOp(Parser yyp) : base(yyp) { }
    }
    public partial class unOp2
    {
        public unOp2(Parser yyp) : base(yyp) { }
    }
    public partial class binOp
    {
        public binOp(Parser yyp) : base(yyp) { }
    }
    public partial class word_number
    {
        public word_number(Parser yyp) : base(yyp) { }
    }

    public partial class dataval : iaccept
    {
        public dataval(Parser yyp) : base(yyp) { }

        public virtual object accept(IVisitor v) { return null; }
        public virtual string getVal() { return null; }

        public override string ToString()
        {
            return getVal().Substring(1, getVal().Length - 2).Replace("\'\'", "\'");
        }

        static System.Globalization.CultureInfo en_cult = new System.Globalization.CultureInfo("en-US");
        public double ToDouble()
        {
            return double.Parse(getVal(), en_cult.NumberFormat);
        }

        public int ToInt()
        {
            return int.Parse(getVal());
        }

        public bool ToBool()
        {
            return getVal() == "true";
        }
    }

    public partial class Number : dataval
    {
        public Number(Parser yyp) : base(yyp) { }
        public string val;
        public Number(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
    }

    public partial class StrData : dataval
    {
        public StrData(Parser yyp) : base(yyp) { }
        public string val;
        public StrData(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
    }

    public partial class DateTimeData : dataval
    {
        public DateTimeData(Parser yyp) : base(yyp) { }
        public string val;
        public DateTimeData(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
    }

    public partial class Duration : dataval
    {
        public Duration(Parser yyp) : base(yyp) { }
        public string val;
        public Duration(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
    }

    public partial class Float : dataval
    {
        public Float(Parser yyp) : base(yyp) { }
        public string val;
        public Float(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
    }
    public partial class Bool : dataval
    {
        public Bool(Parser yyp) : base(yyp) { }
        public string val;
        public Bool(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
    }

    public partial class abstractbound : iaccept
    {
        public abstractbound(Parser yyp) : base(yyp) { }
        public virtual bool isStrict() {return false;}
        public virtual dataval getStrictVal() { throw new InvalidOperationException(); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
        public virtual int priority() { return 0; }
        public abstractbound me()
        {
            return this is boundIdent ? (this as boundIdent).bnd : this;
        }
    }

    public partial class facet : iaccept
    {
        public string Cmp;
        public dataval V;

        public facet(Parser yyp) : base(yyp) { }
        public facet(Parser yyp, string Cmp_, dataval V_) : base(yyp) { Cmp = Cmp_; V = V_; }
        public facet(Parser yyp, string Cmp_, string V_)
            : base(yyp)
        {
            Cmp = Cmp_;
            if (Cmp == "#")
                V = new CNL.EN.StrData(null, V_);
            else if (Cmp.StartsWith("<->"))
                V = new CNL.EN.Number(null, V_);
            else
                throw new InvalidOperationException();
        }

        public virtual bool isStrict()
        {
            return Cmp == "=";
        }
        public virtual dataval getStrictVal()
        {
            return V;
        }
        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class facetList : iaccept
    {
        public List<facet> Facets;
        public facetList(Parser yyp) : base(yyp) { }
        public facetList(Parser yyp, facet f) : base(yyp) { Facets = new List<facet>() { f }; }
        public facetList(Parser yyp, facet f1, facet f2) : base(yyp) { Facets = new List<facet>() { f1, f2 }; }
        public facetList(Parser yyp, facetList l, facet f) : base(yyp) { Facets = l.Facets; Facets.Add(f); }
        public virtual bool isStrict() { return Facets.Count == 1 && Facets[0].isStrict(); }
        public virtual dataval getStrictVal()
        {
            if (Facets.Count == 1)
                return Facets[0].getStrictVal();
            throw new InvalidOperationException();
        }
        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class boundFacets : abstractbound
    {
        public boundFacets(Parser yyp) : base(yyp) { }
        public facetList l;
        public override bool isStrict()
        {
            return l.isStrict();
        }
        public override dataval getStrictVal()
        {
            return l.getStrictVal();
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public boundFacets(Parser yyp, facetList l_) : base(yyp) { l = l_; }
    }

    public partial class  boundAnd : abstractbound
    {
        public List<abstractbound> List;
        public override int priority() { return 3; }
        public boundAnd(Parser yyp) : base(yyp) { }
        public boundAnd(Parser yyp, abstractbound c, abstractbound d) : base(yyp) 
        {
            if (c.me() is boundAnd)
                List = (c.me() as boundAnd).List;
            else
                List = new System.Collections.Generic.List<abstractbound>() { c.me() };
            if (d.me() is boundAnd)
            {
                if (List == null)
                    List = new System.Collections.Generic.List<abstractbound>();
                List.AddRange((d.me() as boundAnd).List);
            }
            else
            {
                if (List == null)
                    List = new System.Collections.Generic.List<abstractbound>();
                List.Add(d.me());
            }
        }
        public override bool isStrict() { return List.Count == 1 && List[0].isStrict(); }
        public override dataval getStrictVal()
        {
            if (List.Count == 1)
                return List[0].getStrictVal();
            throw new InvalidOperationException();
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class boundOr : abstractbound
    {
        public List<abstractbound> List;
        public override int priority() { return 2; }
        public boundOr(Parser yyp) : base(yyp) { }
        public boundOr(Parser yyp, abstractbound c, abstractbound d)
            : base(yyp)
        {
            if (c.me() is boundOr)
                List = (c.me() as boundOr).List;
            else
                List = new System.Collections.Generic.List<abstractbound>() { c.me() };
            if (d.me() is boundOr)
            {
                if (List == null)
                    List = new System.Collections.Generic.List<abstractbound>();
                List.AddRange((d.me() as boundOr).List);
            }
            else
            {
                if (List == null)
                    List = new System.Collections.Generic.List<abstractbound>();
                List.Add(d.me());
            }
        }
        public override bool isStrict() { return List.Count == 1 && List[0].isStrict(); }
        public override dataval getStrictVal()
        {
            if (List.Count == 1)
                return List[0].getStrictVal();
            throw new InvalidOperationException();
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class boundNot : abstractbound
    {
        public override int priority() { return 5; }
        public abstractbound bnd;
        public boundNot(Parser yyp) : base(yyp) { }
        public boundNot(Parser yyp, abstractbound bnd) : base(yyp) { this.bnd = bnd.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class boundIdent : abstractbound
    {
        public abstractbound bnd;
        public boundIdent(Parser yyp) : base(yyp) { }
        public boundIdent(Parser yyp, abstractbound bnd) : base(yyp) { this.bnd = bnd.me(); }
        public override object accept(IVisitor v)
        {
            throw new InvalidOperationException();
        }
    }
    public partial class boundVal : abstractbound
    {
        public string Cmp;
        public dataval V;

        public boundVal(Parser yyp) : base(yyp) { }
        public boundVal(Parser yyp, string Cmp_, dataval V_) : base(yyp) { Cmp = Cmp_; V = V_; }
        public override bool isStrict()
        {
            return Cmp == "=";
        }
        public override dataval getStrictVal()
        {
            return V;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }

    }

    public partial class datavalList : iaccept
    {
        public datavalList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<dataval> vals;

        public datavalList(Parser yyp, dataval i, dataval j)
            : base(yyp)
        { vals = new System.Collections.Generic.List<dataval>(); vals.Add(i); vals.Add(j); }
        public datavalList(Parser yyp, datavalList il, dataval i)
            : base(yyp)
        { vals = il.vals; vals.Add(i); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }
    public partial class boundOneOf : abstractbound
    {
        public boundOneOf(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<dataval> vals;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public boundOneOf(Parser yyp, datavalList dl) : base(yyp) { vals = dl.vals; }
    }
    public partial class boundTop : abstractbound
    {
        public boundTop(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class boundTotal : abstractbound
    {
        public boundTotal(Parser yyp) : base(yyp) { }
        public string Kind;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public boundTotal(Parser yyp, string kind) : base(yyp) { Kind = kind; }
    }
    public partial class boundDataType : abstractbound
    {
        public boundDataType(Parser yyp) : base(yyp) { }
        public string name;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public boundDataType(Parser yyp, string name) : base(yyp) { this.name = name; }
    }

    public partial class andComma
    {
        public andComma(Parser yyp) : base(yyp) { }
    }

    public partial class orComma
    {
        public orComma(Parser yyp) : base(yyp) { }
    }

    public partial class beAre
    {
        public beAre(Parser yyp) : base(yyp) { }
    }

    public partial class doesNot
    {
        public doesNot(Parser yyp) : base(yyp) { }
    }

    public partial class doesNotBy
    {
        public doesNotBy(Parser yyp) : base(yyp) { }
    }

    public partial class valueOrThing
    {
        public valueOrThing(Parser yyp) : base(yyp) { }
    }

    ////////////////// SWRL //////////////////////////////////////

    public partial class swrlrule : sentence
    {
        public clause Predicate;
        public clause_result Result;

        public swrlrule(Parser yyp) : base(yyp) { }
        public swrlrule(Parser yyp, clause predicate, clause_result result, string modality)
            : base(yyp)
        {
            this.Predicate = predicate;
            this.Result = result;
            this.modality = modality;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class swrlrulefor : sentence
    {
        public clause Predicate;
        public clause_result Result;
        public exeargs Collection;

        public swrlrulefor(Parser yyp) : base(yyp) { }
        public swrlrulefor(Parser yyp, clause predicate, string vot, string n, datavaler col, clause_result result)
            : base(yyp)
        {
            this.Predicate = predicate;
            this.Result = result;

            iexevar ev;
            if (vot.ToLower() == "value")
                ev = new datavalvar(null, n);
            else
                ev = new identobject_name(null, null, n);

            this.Collection = new exeargs(null) { exevars = new List<iexevar>() { ev, col } };
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class clause : iaccept
    {
        public System.Collections.Generic.List<condition> Conditions;

        public clause(Parser yyp) : base(yyp) { }
        public clause(Parser yyp, condition condition)
            : base(yyp)
        {
            Conditions = new List<condition>(); Conditions.Add(condition);
        }
        public clause(Parser yyp, clause clause, condition condition)
            : base(yyp)
        {
            Conditions = clause.Conditions; Conditions.Add(condition);
        }
        public virtual object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class clause_result : iaccept
    {
        public System.Collections.Generic.List<condition_result> Conditions;

        public clause_result(Parser yyp) : base(yyp) { }
        public clause_result(Parser yyp, condition_result condition_result)
            : base(yyp)
        {
            Conditions = new List<condition_result>(); Conditions.Add(condition_result);
        }
        public clause_result(Parser yyp, clause_result clause_result, condition_result condition_result)
            : base(yyp)
        {
            Conditions = clause_result.Conditions; Conditions.Add(condition_result);
        }
        public virtual object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition : iaccept
    {
        public condition(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class condition_result : iaccept
    {
        public condition_result(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class duration : iaccept
    {
        public duration(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
        protected static datavaler zeroD = new datavalval(null, new Number(null, "0"));
    }

    public partial class duration_m : duration
    {
        public datavaler y;
        public datavaler M;
        public datavaler d;
        public datavaler h;
        public datavaler m;
        public datavaler s;

        public duration_m(Parser yyp) : base(yyp) { }
        public duration_m(Parser yyp, datavaler y, datavaler M = null, datavaler d = null, datavaler h = null, datavaler m = null, datavaler s = null)
            : base(yyp)
        {
            this.y = y ?? zeroD;
            this.M = M ?? zeroD;
            this.d = d ?? zeroD;
            this.h = h ?? zeroD;
            this.m = m ?? zeroD;
            this.s = s ?? zeroD;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class duration_w : duration
    {
        public datavaler y;
        public datavaler W;
        public datavaler d;
        public datavaler h;
        public datavaler m;
        public datavaler s;

        public duration_w(Parser yyp) : base(yyp) { }
        public duration_w(Parser yyp, datavaler y, datavaler W = null, datavaler d = null, datavaler h = null, datavaler m = null, datavaler s = null)
            : base(yyp)
        {
            this.y = y ?? zeroD;
            this.W = W ?? zeroD;
            this.d = d ?? zeroD;
            this.h = h ?? zeroD;
            this.m = m ?? zeroD;
            this.s = s ?? zeroD;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class datetime : iaccept
    {
        protected static datavaler zeroD = new datavalval(null, new Number(null, "0"));

        public datavaler y;
        public datavaler M;
        public datavaler d;
        public datavaler h;
        public datavaler m;
        public datavaler s;

        public datetime(Parser yyp) : base(yyp) { }
        public datetime(Parser yyp, datavaler y, datavaler M, datavaler d, datavaler h = null, datavaler m = null, datavaler s = null)
            : base(yyp)
        {
            this.y = y ?? zeroD;
            this.M = M ?? zeroD;
            this.d = d ?? zeroD;
            this.h = h ?? zeroD;
            this.m = m ?? zeroD;
            this.s = s ?? zeroD;
        }
        public virtual object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin : iaccept
    {
        public builtin(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class builtin_cmp : builtin
    {
        public datavaler a;
        public datavaler b;
        public string cmp;

        public builtin_cmp(Parser yyp) : base(yyp) { }
        public builtin_cmp(Parser yyp, datavaler a, string cmp, datavaler b)
            : base(yyp)
        {
            this.a = a;
            this.b = b;
            this.cmp = cmp;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin_alpha : builtin
    {
        public objectr a;
        public datavaler b;
        public string cmp;

        public builtin_alpha(Parser yyp) : base(yyp) { }
        public builtin_alpha(Parser yyp, objectr a, datavaler b)
            : base(yyp)
        {
            this.a = a;
            this.b = b;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin_annot : builtin
    {
        public objectr a;
        public datavaler prop;
        public datavaler lang;
        public datavaler b;

        public builtin_annot(Parser yyp) : base(yyp) { }
        public builtin_annot(Parser yyp, objectr a, datavaler prop, datavaler lang, datavaler b)
            : base(yyp)
        {
            this.a = a;
            this.b = b;
            this.prop = prop;
            this.lang = lang;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }
    public partial class builtin_exe : builtin
    {
        public string name;
        public datavaler a;
        public exeargs ea;

        public builtin_exe(Parser yyp) : base(yyp) { }
        public builtin_exe(Parser yyp, string name, exeargs ea, datavaler a)
            : base(yyp)
        {
            this.name = name;
            this.a = a;
            this.ea = ea;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }
    
    public partial class datavalerPlusList : iaccept
    {
        public datavalerPlusList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<datavaler> vals;

        public datavalerPlusList(Parser yyp, datavaler i, datavaler j)
            : base(yyp)
        { vals = new System.Collections.Generic.List<datavaler>(); vals.Add(i); vals.Add(j); }
        public datavalerPlusList(Parser yyp, datavalerPlusList il, datavaler i)
            : base(yyp)
        { vals = il.vals; vals.Add(i); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }

    public partial class datavalerTimesList : iaccept
    {
        public datavalerTimesList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<datavaler> vals;

        public datavalerTimesList(Parser yyp, datavaler i, datavaler j)
            : base(yyp)
        { vals = new System.Collections.Generic.List<datavaler>(); vals.Add(i); vals.Add(j); }
        public datavalerTimesList(Parser yyp, datavalerTimesList il, datavaler i)
            : base(yyp)
        { vals = il.vals; vals.Add(i); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }

    public partial class datavalerFollowedByList : iaccept
    {
        public datavalerFollowedByList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<datavaler> vals;

        public datavalerFollowedByList(Parser yyp, datavaler i, datavaler j)
            : base(yyp)
        { vals = new System.Collections.Generic.List<datavaler>(); vals.Add(i); vals.Add(j); }
        public datavalerFollowedByList(Parser yyp, datavalerFollowedByList il, datavaler i)
            : base(yyp)
        { vals = il.vals; vals.Add(i); }
        public virtual object accept(IVisitor v)
        {
            return null;
        }
    }
    
    public partial class builtin_list : builtin
    {
        public datavaler result;
        public System.Collections.Generic.List<datavaler> vals;
        public string tpy;

        public builtin_list(Parser yyp) : base(yyp) { }
        public builtin_list(Parser yyp, List<datavaler> dl, string tpy, datavaler result)
            : base(yyp)
        {
            this.result = result;
            this.vals = dl;
            this.tpy = tpy;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin_bin : builtin
    {
        public datavaler result;
        public datavaler b;
        public datavaler d;
        public string tpy;

        public builtin_bin(Parser yyp) : base(yyp) { }
        public builtin_bin(Parser yyp, datavaler b, string tpy, datavaler d, datavaler result)
            : base(yyp)
        {
            this.result = result;
            this.b = b;
            this.d = d;
            this.tpy = tpy;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin_unary_cmp : builtin
    {
        public datavaler result;
        public datavaler b;
        public string tpy;

        public builtin_unary_cmp(Parser yyp) : base(yyp) { }
        public builtin_unary_cmp(Parser yyp, string tpy, datavaler b, datavaler result)
            : base(yyp)
        {
            this.result = result;
            this.b = b;
            this.tpy = tpy;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin_unary_free : builtin
    {
        public datavaler a;
        public datavaler b;
        public string tpy;

        public builtin_unary_free(Parser yyp) : base(yyp) { }
        public builtin_unary_free(Parser yyp, datavaler a, string tpy, datavaler b)
            : base(yyp)
        {
            this.a = a;
            this.b = b;
            this.tpy = tpy;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }
    public partial class builtin_substr : builtin
    {
        public datavaler result;
        public datavaler b;
        public datavaler c;
        public datavaler d;
        public string tpy;

        public builtin_substr(Parser yyp) : base(yyp) { }
        public builtin_substr(Parser yyp, datavaler b, string tpy, datavaler c, datavaler result)
            : base(yyp)
        {
            this.result = result;
            this.b = b;
            this.c = c;
            this.d = null;
            this.tpy = tpy;
        }
        public builtin_substr(Parser yyp, datavaler b, string tpy, datavaler c, datavaler d, datavaler result)
            : base(yyp)
        {
            this.result = result;
            this.b = b;
            this.c = c;
            this.d = d;
            this.tpy = tpy;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin_trans : builtin
    {
        public datavaler result;
        public datavaler b;
        public datavaler c;
        public datavaler d;
        public string tpy;

        public builtin_trans(Parser yyp) : base(yyp) { }
        public builtin_trans(Parser yyp, string tpy, datavaler b, datavaler c, datavaler d, datavaler result)
            : base(yyp)
        {
            this.result = result;
            this.b = b;
            this.c = c;
            this.d = d;
            this.tpy = tpy;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin_duration : builtin
    {
        public datavaler a;
        public duration d;

        public builtin_duration(Parser yyp) : base(yyp) { }
        public builtin_duration(Parser yyp, duration d, datavaler a)
            : base(yyp)
        {
            this.a = a;
            this.d = d;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class builtin_datetime : builtin
    {
        public datavaler a;
        public datetime d;

        public builtin_datetime(Parser yyp) : base(yyp) { }
        public builtin_datetime(Parser yyp, datetime d, datavaler a)
            : base(yyp)
        {
            this.a = a;
            this.d = d;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_is : condition
    {
        public objectr objectA, objectB;
        public condition_kind condition_kind;

        public condition_is(Parser yyp) : base(yyp) { }
        public condition_is(Parser yyp, objectr objectA, objectr objectB, condition_kind condition_kind)
            : base(yyp)
        {
            this.condition_kind = condition_kind;
            this.objectA = objectA;
            this.objectB = objectB;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_exists : condition
    {
        public objectr objectA;

        public condition_exists(Parser yyp) : base(yyp) { }
        public condition_exists(Parser yyp, objectr objectA)
            : base(yyp)
        {
            this.objectA = objectA;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_definition : condition
    {
        public objectr objectA;
        public oobject objectClass;

        public condition_definition(Parser yyp) : base(yyp) { }
        public condition_definition(Parser yyp, objectr objectA, oobject objectClass)
            : base(yyp)
        {
            this.objectA = objectA;
            this.objectClass = objectClass;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    [Flags]
    public enum condition_kind { None = 0, Not = 0x1, Inv = 0x2, All = Not | Inv }

    public partial class condition_role : condition
    {
        public string role;
        public objectr objectA, objectB;
        public condition_kind condition_kind;

        public condition_role(Parser yyp) : base(yyp) { }
        public condition_role(Parser yyp, objectr objectA, string role, objectr objectB, condition_kind conditionKind)
            : base(yyp)
        {
            this.condition_kind = conditionKind;
            this.role = role;
            this.objectA = objectA;
            this.objectB = objectB;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_data_property : condition
    {
        public string property_name;
        public objectr objectA;
        public datavaler d_object;

        public condition_data_property(Parser yyp) : base(yyp) { }
        public condition_data_property(Parser yyp, objectr objectA, string property_name, string dvar)
            : base(yyp)
        {
            this.objectA = objectA;
            this.property_name = property_name;
            this.d_object = new datavalvar(yyp, dvar);
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_builtin : condition
    {
        public builtin bi;
        public condition_builtin(Parser yyp) : base(yyp) { }
        public condition_builtin(Parser yyp, builtin bi)
            : base(yyp)
        {
            this.bi = bi;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_result_builtin : condition_result
    {
        public builtin bi;
        public condition_result_builtin(Parser yyp) : base(yyp) { }
        public condition_result_builtin(Parser yyp, builtin bi)
            : base(yyp)
        {
            this.bi = bi;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }
    
    public partial class condition_data_property_bound : condition
    {
        public string property_name;
        public objectr objectA;
        public abstractbound bnd;

        public condition_data_property_bound(Parser yyp) : base(yyp) { }
        public condition_data_property_bound(Parser yyp, objectr objectA, string property_name, abstractbound bnd)
            : base(yyp)
        {
            this.objectA = objectA;
            this.property_name = property_name;
            this.bnd = bnd.me();
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_data_bound : condition
    {
        public string property_name;
        public datavaler d_object;
        public abstractbound bound;

        public condition_data_bound(Parser yyp) : base(yyp) { }
        public condition_data_bound(Parser yyp, datavaler d_object, abstractbound bound)
            : base(yyp)
        {
            this.d_object = d_object;
            this.bound = bound.me();
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_result_is : condition_result
    {
        public identobject objectA, objectB;
        public condition_kind condition_kind;

        public condition_result_is(Parser yyp) : base(yyp) { }
        public condition_result_is(Parser yyp, identobject objectA, identobject objectB, condition_kind condition_kind)
            : base(yyp)
        {
            this.condition_kind = condition_kind;
            this.objectA = objectA;
            this.objectB = objectB;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_result_definition : condition_result
    {
        public identobject objectA;
        public oobject objectClass;
        public condition_result_definition(Parser yyp) : base(yyp) { }
        public condition_result_definition(Parser yyp, identobject objectA, oobject objectClass)
            : base(yyp)
        {
            this.objectA = objectA;
            this.objectClass = objectClass;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class condition_result_role : condition_result
    {
        public string role;
        public identobject objectA, objectB;
        public condition_kind condition_kind;

        public condition_result_role(Parser yyp) : base(yyp) { }
        public condition_result_role(Parser yyp, identobject objectA, string role, identobject objectB, condition_kind condition_kind)
            : base(yyp)
        {
            this.condition_kind = condition_kind;
            this.role = role;
            this.objectA = objectA;
            this.objectB = objectB;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }



    public partial class condition_result_data_property : condition_result
    {
        public string property_name;
        public identobject objectA;
        public datavaler d_object;

        public condition_result_data_property(Parser yyp) : base(yyp) { }
        public condition_result_data_property(Parser yyp, identobject objectA, string property_name, datavaler d_object)
            : base(yyp)
        {
            this.objectA = objectA;
            this.property_name = property_name;
            this.d_object = d_object;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class objectr : iaccept
    {
        public objectr(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class objectr_nio : objectr
    {
        public notidentobject notidentobject;
        public objectr_nio(Parser yyp) : base(yyp) { }
        public objectr_nio(Parser yyp, notidentobject notidentobject) : base(yyp) { this.notidentobject = notidentobject; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class objectr_io : objectr
    {
        public identobject identobject;
        public objectr_io(Parser yyp) : base(yyp) { }
        public objectr_io(Parser yyp, identobject identobject) : base(yyp) { this.identobject = identobject; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class notidentobject : iaccept
    {
        public string name;
        public string num;
        public notidentobject(Parser yyp) : base(yyp) { }
        public notidentobject(Parser yyp, string name_, string num_ = null) : base(yyp) { name = name_; num = num_; }
        public virtual object accept(IVisitor v) { return v.Visit(this); }
    }

    public interface iexevar : iaccept
    {
    }

    public partial class identobject : iexevar
    {
        public identobject(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class datavaler : iexevar
    {
        public datavaler(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class datavalvar : datavaler
    {
        public string num;
        public datavalvar(Parser yyp) : base(yyp) { }
        public datavalvar(Parser yyp, string num_) : base(yyp) { num = num_; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class datavalval : datavaler
    {
        public dataval dv;
        public datavalval(Parser yyp) : base(yyp) { }
        public datavalval(Parser yyp, dataval dv) : base(yyp) { this.dv = dv; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class identobject_name : identobject
    {
        public string name;
        public string num;
        public identobject_name(Parser yyp) : base(yyp) { }
        public identobject_name(Parser yyp, string name_, string num_ = null) : base(yyp) { name = name_; num = num_; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class identobject_inst : identobject
    {
        public instancer i;
        public identobject_inst(Parser yyp) : base(yyp) { }
        public identobject_inst(Parser yyp, instancer i_) : base(yyp) { i = i_; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class instancer : iaccept
    {
        public string name;
        public bool very;
        public instancer(Parser yyp) : base(yyp) { }
        public instancer(Parser yyp, string name_, bool very_) : base(yyp) { name = name_; very = very_; }
        public virtual object accept(IVisitor v) { return v.Visit(this); }
    }

    ////////////////// SWRL //////////////////////////////////////

    // EXERULE
    public partial class exerule : sentence
    {
        public clause slp;
        public exeargs args;
        public string exe;

        public exerule(Parser yyp) : base(yyp) { }
        public exerule(Parser yyp, clause slp_, exeargs args_, string exe_)
            : base(yyp)
        {
            slp = slp_;
            args=args_;
            exe = exe_;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class exeargs : iaccept
    {
        public exeargs(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<iexevar> exevars;

        public exeargs(Parser yyp, iexevar s)
            : base(yyp)
        { exevars = new System.Collections.Generic.List<iexevar>(); exevars.Add(s); }

        public exeargs(Parser yyp, exeargs z, iexevar r)
            : base(yyp)
        { exevars = z.exevars; exevars.Add(r); }
        public virtual object accept(IVisitor v)
        {
            return v.Visit(this); 
        }
    }

    // CODE
    public partial class code : sentence
    {
        public string exe;

        public code(Parser yyp) : base(yyp) { }
        public code(Parser yyp, string exe_)
            : base(yyp)
        {
            exe = exe_;
        }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }


}
