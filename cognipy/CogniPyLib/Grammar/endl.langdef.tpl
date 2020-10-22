<?xml version="1.0" encoding="utf-8"?>
<!--

Actipro Syntax Language Definition (.langdef)
  For use with Actipro SyntaxEditor and related products.
  http://www.actiprosoftware.com

'CNL' language created by Cognitum.
  Copyright (c) 2012 Cognitum.  All rights reserved.

-->
<LanguageDefinition LanguageKey="CNL" Creator="Cognitum" Copyright="Copyright (c) 2012 Cognitum.  All rights reserved." xmlns="http://schemas.actiprosoftware.com/langdef/1.0">
	<!-- Classification types -->
	<LanguageDefinition.ClassificationTypes>
		<ClassificationType Key="Comment" DefaultStyle="#FF008000" />
		<ClassificationType Key="Delimiter" />
		<ClassificationType Key="Identifier" />
		<ClassificationType Key="Keyword" DefaultStyle="#FF0000FF" />
		<ClassificationType Key="Number" />
		<ClassificationType Key="Operator" />
    <ClassificationType Key="NamespacePrefix" DefaultStyle="#FFB22222" />
    <ClassificationType Key="SingleQuote" DefaultStyle="#FF008000" />
	</LanguageDefinition.ClassificationTypes>
	<!-- Lexer -->
	<LanguageDefinition.Lexer>
		<DynamicLexer>
			<!-- Default state -->
			<State Id="1" Key="Default">
				<State.ChildStates>
					<StateRef Key="SingleLineComment" />
					<StateRef Key="MultiLineComment" />
				</State.ChildStates>
				<RegexPatternGroup TokenId="1" TokenKey="Whitespace" Pattern="{LineTerminatorWhitespace}+" />
        <RegexPatternGroup TokenId="19" TokenKey="SingleQuotes" ClassificationTypeKey="SingleQuote" Pattern="'[^'\n]+'" />
        <RegexPatternGroup TokenId="18" TokenKey="NamespacePrefix" ClassificationTypeKey="NamespacePrefix" Pattern="\[[^\[\]]+\]" />
        <ExplicitPatternGroup TokenId="2" TokenKey="OpenParenthesis" ClassificationTypeKey="Delimiter" Pattern="(" />
        <ExplicitPatternGroup TokenId="3" TokenKey="CloseParenthesis" ClassificationTypeKey="Delimiter" Pattern=")" />
				<ExplicitPatternGroup TokenId="4" TokenKey="OpenCurlyBrace" ClassificationTypeKey="Delimiter" Pattern="{" />
				<ExplicitPatternGroup TokenId="5" TokenKey="CloseCurlyBrace" ClassificationTypeKey="Delimiter" Pattern="}" />
				<ExplicitPatternGroup TokenId="6" TokenKey="Keyword" ClassificationTypeKey="Keyword" LookAheadPattern="{Whitespace}|\.|\,|\z" CaseSensitivity="Insensitive">
					<ExplicitPatterns>
            <![CDATA[
########## . , ?
					]]></ExplicitPatterns>
				</ExplicitPatternGroup>
				<RegexPatternGroup TokenId="7" TokenKey="Identifier" ClassificationTypeKey="Identifier" Pattern="(_ | {Alpha})({Word})*" />
				<ExplicitPatternGroup TokenId="8" TokenKey="Operator" ClassificationTypeKey="Operator">
					<ExplicitPatterns><![CDATA[
						== != = + - * /
					]]></ExplicitPatterns>
				</ExplicitPatternGroup>
				<ExplicitPatternGroup TokenId="9" TokenKey="Punctuation">
					<ExplicitPatterns><![CDATA[
						, ;
					]]></ExplicitPatterns>
				</ExplicitPatternGroup>
				<RegexPatternGroup TokenId="10" TokenKey="Number" ClassificationTypeKey="Number" Pattern="{Digit}* \. {Digit}+" LookAheadPattern="{NonWord}|\z" />
				<RegexPatternGroup TokenId="10" TokenKey="Number" ClassificationTypeKey="Number" Pattern="{Digit}+" LookAheadPattern="{NonWord}|\z" />
			</State>
			<!-- SingleLineComment state -->
			<State Id="2" Key="SingleLineComment" DefaultTokenId="11" DefaultTokenKey="SingleLineCommentText" DefaultClassificationTypeKey="Comment">
				<State.Scopes>
					<Scope>
						<Scope.StartPatternGroup>
							<ExplicitPatternGroup TokenId="12" TokenKey="SingleLineCommentStartDelimiter" Pattern="//" />
						</Scope.StartPatternGroup>
						<Scope.EndPatternGroup>
							<RegexPatternGroup TokenId="13" TokenKey="SingleLineCommentEndDelimiter" Pattern="\n" />
						</Scope.EndPatternGroup>
					</Scope>
				</State.Scopes>
				<RegexPatternGroup TokenId="11" TokenKey="SingleLineCommentText" Pattern="[^\n]+" />
			</State>
			<!-- MultiLineComment state -->
			<State Id="3" Key="MultiLineComment" DefaultTokenId="14" DefaultTokenKey="MultiLineCommentText" DefaultClassificationTypeKey="NamespacePrefix">
				<State.Scopes>
					<Scope>
						<Scope.StartPatternGroup>
							<RegexPatternGroup TokenId="15" TokenKey="MultiLineCommentStartDelimiter" Pattern="(References|Namespace):" />
						</Scope.StartPatternGroup>
						<Scope.EndPatternGroup>
							<ExplicitPatternGroup TokenId="16" TokenKey="MultiLineCommentEndDelimiter" Pattern="." />
						</Scope.EndPatternGroup>
					</Scope>
				</State.Scopes>
				<RegexPatternGroup TokenId="17" TokenKey="MultiLineCommentLineTerminator" Pattern="\n" />
        <RegexPatternGroup TokenKey="Namespace" ClassificationTypeKey="Comment" Pattern="'[^']+'" />
        <RegexPatternGroup TokenId="14" TokenKey="MultiLineCommentText" Pattern="[^\.\n']+" />        
			</State>
		</DynamicLexer>
	</LanguageDefinition.Lexer>
	<!-- Example text -->
	<LanguageDefinition.ExampleText><![CDATA[/*
*/
Every man is a woman.

]]></LanguageDefinition.ExampleText>
</LanguageDefinition>