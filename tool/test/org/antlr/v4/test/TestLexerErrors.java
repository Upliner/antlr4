/*
 [The "BSD license"]
 Copyright (c) 2011 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.antlr.v4.test;

import org.junit.Test;

public class TestLexerErrors extends BaseTest {
	// TEST DETECTION
	@Test public void testInvalidCharAtStart() throws Exception {
		String grammar =
			"lexer grammar L;\n" +
			"A : 'a' 'b' ;\n";
		String tokens = execLexer("L.g", grammar, "L", "x");
		String expectingTokens =
			"[@0,1:0='<EOF>',<-1>,1:1]\n";
		assertEquals(expectingTokens, tokens);
		String expectingError = "line 1:0 token recognition error at: 'x'\n";
		String error = stderrDuringParse;
		assertEquals(expectingError, error);
	}

	@Test public void testInvalidCharAtStartAfterDFACache() throws Exception {
		String grammar =
			"lexer grammar L;\n" +
			"A : 'a' 'b' ;\n";
		String tokens = execLexer("L.g", grammar, "L", "abx");
		String expectingTokens =
			"[@0,0:1='ab',<3>,1:0]\n" +
			"[@1,3:2='<EOF>',<-1>,1:3]\n";
		assertEquals(expectingTokens, tokens);
		String expectingError = "line 1:2 token recognition error at: 'x'\n";
		String error = stderrDuringParse;
		assertEquals(expectingError, error);
	}

	@Test public void testInvalidCharInToken() throws Exception {
		String grammar =
			"lexer grammar L;\n" +
			"A : 'a' 'b' ;\n";
		String tokens = execLexer("L.g", grammar, "L", "ax");
		String expectingTokens =
			"[@0,2:1='<EOF>',<-1>,1:1]\n";
		assertEquals(expectingTokens, tokens);
		String expectingError = "line 1:0 token recognition error at: 'ax'\n";
		String error = stderrDuringParse;
		assertEquals(expectingError, error);
	}

	@Test public void testInvalidCharInTokenAfterDFACache() throws Exception {
		String grammar =
			"lexer grammar L;\n" +
			"A : 'a' 'b' ;\n";
		String tokens = execLexer("L.g", grammar, "L", "abax");
		String expectingTokens =
			"[@0,0:1='ab',<3>,1:0]\n" +
			"[@1,4:3='<EOF>',<-1>,1:4]\n";
		assertEquals(expectingTokens, tokens);
		String expectingError = "line 1:2 token recognition error at: 'ax'\n";
		String error = stderrDuringParse;
		assertEquals(expectingError, error);
	}

	@Test public void testDFAToATNThatFailsBackToDFA() throws Exception {
		String grammar =
			"lexer grammar L;\n" +
			"A : 'ab' ;\n"+
			"B : 'abc' ;\n";
		// The first ab caches the DFA then abx goes through the DFA but
		// into the ATN for the x, which fails. Must go back into DFA
		// and return to previous dfa accept state
		String tokens = execLexer("L.g", grammar, "L", "ababx");
		String expectingTokens =
			"[@0,0:1='ab',<3>,1:0]\n" +
			"[@1,2:3='ab',<3>,1:2]\n" +
			"[@2,5:4='<EOF>',<-1>,1:5]\n";
		assertEquals(expectingTokens, tokens);
		String expectingError = "line 1:4 token recognition error at: 'x'\n";
		String error = stderrDuringParse;
		assertEquals(expectingError, error);
	}

	@Test public void testDFAToATNThatMatchesThenFailsInATN() throws Exception {
		String grammar =
			"lexer grammar L;\n" +
			"A : 'ab' ;\n"+
			"B : 'abc' ;\n"+
			"C : 'abcd' ;\n";
		// The first ab caches the DFA then abx goes through the DFA but
		// into the ATN for the c.  It marks that hasn't except state
		// and then keeps going in the ATN. It fails on the x, but
		// uses the previous accepted in the ATN not DFA
		String tokens = execLexer("L.g", grammar, "L", "ababcx");
		String expectingTokens =
			"[@0,0:1='ab',<3>,1:0]\n" +
			"[@1,2:4='abc',<4>,1:2]\n" +
			"[@2,6:5='<EOF>',<-1>,1:6]\n";
		assertEquals(expectingTokens, tokens);
		String expectingError = "line 1:5 token recognition error at: 'x'\n";
		String error = stderrDuringParse;
		assertEquals(expectingError, error);
	}

	// TEST RECOVERY

}
