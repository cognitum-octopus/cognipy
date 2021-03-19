csc /debug+ /out:Tools.dll /target:library /define:GENTIME dfa.cs lexer.cs parser.cs olist.cs serialise.cs genbase0.cs
csc /debug+ /r:Tools.dll lg.cs
lg pg.lexer
csc /debug+ /r:Tools.dll pg.cs pg.lexer.cs
lg cs0.lexer
pg cs0.parser
csc /debug+ /out:Tools.dll /target:library /define:GENTIME dfa.cs lexer.cs parser.cs olist.cs serialise.cs genbase.cs cs0.lexer.cs cs0.parser.cs
csc /debug+ /r:Tools.dll lg.cs
csc /debug+ /r:Tools.dll pg.cs pg.lexer.cs 
csc /debug+ /out:runtime/Tools.dll /target:library dfa.cs lexer.cs parser.cs olist.cs serialise.cs

