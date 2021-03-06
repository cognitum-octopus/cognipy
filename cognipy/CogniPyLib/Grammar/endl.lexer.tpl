%lexer
%namespace CogniPy.CNL.EN
%declare{
	public string str;
}
%encoding UTF-8

%token STR;
%token ID;
%token NAT;
%token NUM;
%token DBL;
%token DEC;
%token BOL;
%token CODE;
%token COMMENT;

%define SP			(" "|"\t"|"\v"|"\n"|"\r"|"\f")

%define DOT			("\."|"\?")
%define DDOT		"\:"
%define SLASH		"/"
%define COMMA		","


%define BOP			"("
%define BCL			")"
%define DOL			"$"

%define UPPERL      [A-Z]
%define LOWERL		[a-z]
%define DIGIT		[0-9]
%define NATURAL		{DIGIT}+
%define NUMBER		("+"|"-")?{DIGIT}+
%define FLOAT		("+"|"-")?{DIGIT}*"\."?{DIGIT}+([Ee]("+"|"-")?{DIGIT}+)?
%define DECIMAL		{DOL}{FLOAT}
//%define STRING		("\'"([^\'\r\n]|"\'\'"|"\|")*"\'")
%define DATETIME	[1-9][0-9][0-9][0-9]"-"[0-1][0-9]"-"[0-3][0-9]("T"[0-2][0-9]":"[0-5][0-9](":"[0-5][0-9]("."[0-9]([0-9]([0-9])?)?)?)?("Z"|(("+"|"-")[0-2][0-9]":"[0-5][0-9]))?)?
//%define DURATION	P({NATURAL}Y)?({NATURAL}M)?({NATURAL}D)?(T({NATURAL}H)?({NATURAL}M)?({NATURAL}(("."[0-9]+)?S))?)?
%define DURATION	P({NATURAL}D)?(T({NATURAL}H)?({NATURAL}M)?({NATURAL}(("."[0-9]([0-9]([0-9])?)?)?S))?)?
%define NINT		("\""([^\"\r\n]|"\"\"")+"\"")
%define NINT2		(The-"\""([^\"\r\n]|"\"\"")*"\"")
%define NINT3		(THE-"\""([^\"\r\n]|"\"\"")*"\"")
%define BIGNAME		{UPPERL}{LOWERL}*("-"(({UPPERL}{LOWERL}*)|({DIGIT}+)))*
%define VERYBIGNAME	{UPPERL}{UPPERL}+("-"(({UPPERL}{UPPERL}+)|({DIGIT}+)))*
%define NAME		{LOWERL}+("-"(({LOWERL}+)|({DIGIT}+)))*
%define URI			(([^:\/?#>]+):)?(\/\/([^\/?#>]*))?([^?#>]*)(\?([^#>]*))?(#([^>]*))?
%define TERMS		"["(({Letter}|{DIGIT}|"*"|"-"|"+"|"%"|"_")+|(\<{URI}\>)|"")"]"
//%define VAR			{UPPERL}


##########

{BOP}		%OPEN
{BCL}		%CLOSE
{NATURAL}           %NAT 
{NUMBER}			%NUM 
{FLOAT}				%DBL
{DECIMAL}			%DEC
//{STRING}			%STR    
{DATETIME}			%DTM
{DURATION}			%DUR

({NINT}|{NAME})({SP}*{TERMS})?			%NAME     
({NINT2}|{BIGNAME})({SP}*{TERMS})?		%BIGNAME     
({NINT3}|{VERYBIGNAME})({SP}*{TERMS})?	%VERYBIGNAME


{COMMA}				%COMMA		
{DOT}				%END		

{BIGNAME}\^?{DDOT}						{ yybegin("COMMENT"); yyl.str=yytext; }
<COMMENT>			\'					{ yyl.str += yytext; yybegin("COMMENTSTR");}
<COMMENTSTR>		\'\'				{ yyl.str += yytext;}
<COMMENTSTR>		\'					{ yyl.str += yytext; yybegin("COMMENT");}
<COMMENTSTR>		([^']|\n|\r)+		{ yyl.str += yytext;}
<COMMENT>			\"					{ yyl.str += yytext; yybegin("COMMENTSTR2");}
<COMMENTSTR2>		\"\"				{ yyl.str += yytext;}
<COMMENTSTR2>		\"					{ yyl.str += yytext; yybegin("COMMENT");}
<COMMENTSTR2>		([^"]|\n|\r)+		{ yyl.str += yytext;}
<COMMENT>			\[					{ yyl.str += yytext; yybegin("COMMENTSB");}
<COMMENTSB>			\]					{ yyl.str += yytext; yybegin("COMMENT");}
<COMMENTSB>			([^\[\]]|\n|\r)+	{ yyl.str += yytext;}
<COMMENT>			\.\.				{ yyl.str += yytext;}
<COMMENT>			\.					{ yyl.yytext = yyl.str+yytext; yybegin("YYINITIAL"); return new COMMENT();}
<COMMENT>			([^'"\[.]|\n|\r)+	{ yyl.str += yytext;}

\'						{ yybegin("STR"); yyl.str=yytext; }
<STR>	\'\'			{ yyl.str += yytext;}
<STR>	\'				{ yyl.yytext = yyl.str+yytext; yybegin("YYINITIAL"); return new STR();}
<STR>	([^']|\n|\r)+	{ yyl.str += yytext;}

"<?"	{ yybegin("CODE"); yyl.str=yytext; }
<CODE>	"??>"			{ yyl.str += yytext;}
<CODE>	"?>"			{ yyl.yytext = yyl.str+yytext; yybegin("YYINITIAL"); return new CODE();}
<CODE>	([^?]|\n|\r)+	{ yyl.str += yytext;}


{SP}				{ }		

