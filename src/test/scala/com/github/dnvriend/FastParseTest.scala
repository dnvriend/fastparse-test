/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend

import fastparse.all._
import fastparse.parsers.Intrinsics.ElemIn

import scalaz._
import Scalaz._

object FastParseImplicits {
  implicit class ParserOps[+T](val that: Parsed[T]) extends AnyVal {
    def validation = that match {
      case failure @ Parsed.Failure(lastParser, index, extra) =>
        // return the human-readable trace of the parse
        // showing the stack of parsers which were in progress
        // when the parse failed
        // It is a one-line snippet that tells you what the state of the parser was
        // when it failed. It contains the index where the parser was used and the index where the parser failed
        failure.extra.traced.trace.failureNel[(T, Int)]
      case Parsed.Success(value, index) =>
        // index = how much was consumed from the input
        (value, index).successNel[String]
    }
  }
}

class FastParseTest extends TestSpec {
  import FastParseImplicits._

  "fastparse" should "parse the input - a single letter - case sensitive" in {
    // The simplest parser matches a single string like eg. "a"
    val letterA: Parser[Unit] = P("a")
    letterA.parse("a").validation should beSuccess(((), 1))
    letterA.parse("b").validation should haveFailure("""letterA:1:1 / "a":1:1 ..."b"""")
  }

  it should "parse the input - a single letter - case insensitive - ignoring case" in {
    val letterA: Parser[Unit] = P(IgnoreCase("a"))
    letterA.parse("A").validation should beSuccess(((), 1))
    // parser 'letterA' was used at line: 1 index: 1
    // parser failed at line: 1 index: 1 and expected 'a' but got 'B'
    letterA.parse("B").validation should haveFailure("""letterA:1:1 / "a":1:1 ..."B"""")
  }

  it should "parse a sequence of letters (and combine two parsers)" in {
    // You can combine two parsers with the ~ operator.
    // This creates a new parser that only succeeds
    // if both left and right parsers succeed one after another.
    val ab: Parser[Unit] = P("a" ~ "b")
    ab.parse("ab").validation should beSuccess(((), 2))
    // parser 'ab' got applied at line: 1, index: 1
    // parser failed at line: 1, index: 2
    // parser expected 'b' at index: 2 but got 'c'
    ab.parse("ac").validation should haveFailure("""ab:1:1 / "b":1:2 ..."c"""")
    ab.parse("bb").validation should haveFailure("""ab:1:1 / "a":1:1 ..."bb"""")
  }

  it should "parse the input zero or more times" in {
    // The .rep method creates a new parser that attempts to parse
    // the given parser zero or more times.
    //
    // If you want to parse something a given number of times,
    // you can use .rep(min = 2, max = 4) o r the shorter .rep(1) for one or more times,
    // in addition there is exactly parameter that if it's defined behaves
    // like min and max equals to it.
    //
    // You can optionally provide an argument which acts as a separator between the usages
    // of the original parser, such as a comma in .rep(sep = ",").

    val ab: Parser[Unit] = P("a".rep ~ "b")
    ab.parse("aaaaab").validation should beSuccess(((), 6))
    ab.parse("aaabaaa").validation should beSuccess(((), 4))

    val abc: Parser[Unit] = P("a".rep(sep = "b") ~ "c")
    abc.parse("abababac").validation should beSuccess(((), 8))
    // parser 'abc' got applied at line: 1, index: 1
    // parser failed at line: 1, index: 4
    // parser expected either a 'c' or a 'b' but got 'abac'
    abc.parse("abaabac").validation should haveFailure("""abc:1:1 / ("c" | "b"):1:4 ..."abac"""")

    val ab4: Parser[Unit] = P("a".rep(min = 2, max = 4, sep = "b"))
    ab4.parse("ababababababa").validation should beSuccess(((), 7))

    val ab2exactly: Parser[Unit] = P("ab".rep(exactly = 2))
    ab2exactly.parse("abab").validation should beSuccess(((), 4))

    val ab4c: Parser[Unit] = P("a".rep(min = 2, max = 4, sep = "b") ~ "c")
    ab4c.parse("ac").validation should haveFailure("""ab4c:1:1 / "b":1:2 ..."c"""")
    ab4c.parse("abac").validation should beSuccess(((), 4))
    ab4c.parse("abababac").validation should beSuccess(((), 8))
    ab4c.parse("ababababac").validation should haveFailure("""ab4c:1:1 / "c":1:8 ..."bac"""")
  }

  it should "parse the input zero or one times" in {
    // Similar to .rep is .?,
    // which creates a new parser that attempts to parse
    // the given input zero or 1 times.

    val option: Parser[String] = P("c".? ~ "a".rep(sep = "b").! ~ End)
    option.parse("aba").validation should beSuccess(("aba", 3))
    option.parse("caba").validation should beSuccess(("aba", 4)) // did not capture the c in the parser
  }

  it should "parse the input according to an either parser" in {
    // The | operator tries the parser on the left,
    // and if that fails, tries the one on the right,
    // failing only if both parsers fail.

    val either: Parser[Unit] = P("a".rep ~ ("b" | "c" | "d") ~ End)
    either.parse("aaaaab").validation should beSuccess(((), 6))
    either.parse("aaaaac").validation should beSuccess(((), 6))
    either.parse("aaaaad").validation should beSuccess(((), 6))
    either.parse("aaaaae").validation should haveFailure("""either:1:1 / ("b" | "c" | "d" | "a"):1:6 ..."e"""")
  }

  it should "parse the input until the end or successfully stop in the middle" in {
    // The End parser only succeeds if at the end of the input string.
    // By default, a Parser does not need to consume the whole input,
    // and can succeed early consuming a portion of it
    // (exactly how much input was consumed is stored in the Success#index attribute).
    //
    // By using 'End', we can make the parse fail if it doesn't consume everything
    //
    // There is also a similar 'Start' parser, which only succeeds at the start of the input.

    val noEnd: Parser[Unit] = P("a".rep ~ "b")
    val withEnd: Parser[Unit] = P("a".rep ~ "b" ~ End)

    noEnd.parse("aaaba").validation should beSuccess(((), 4))
    // parser 'withEnd' expected not to parse more, but it got an extra 'a' at index: 5
    withEnd.parse("aaaba").validation should haveFailure("""withEnd:1:1 / End:1:5 ..."a"""")
  }

  it should "parse the input and always Pass or always Fail" in {
    // These two parsers always succeed, or always fail, respectively.
    // Neither consumes any input.
    Pass.parse("abcdefg").validation should beSuccess(((), 0))
    Fail.parse("abcdefg").validation should haveFailure("""Fail:1:1 ..."abcdefg"""")
  }

  it should "parse the input and provide the current index of the parse as its output as an integer" in {
    val finder: Parser[Int] = P("hay".rep ~ Index ~ "needle" ~ "hay".rep)
    // the 'Index' parser always succeeds, and provides the current index
    // of the parse into the input string.
    // which is useful for providing source locations
    // for AST nodes.
    //
    // "needle" ~ "hay".rep starts at index '9'
    // the parser 'finder' consumed 18 chars in total
    finder.parse("hayhayhayneedlehayhay").validation should beSuccess((9, 21))
  }

  it should "parse the input and capture the text" in {
    // So far, all the parsers go over the input text; they all return Unit which is '()'.
    //
    // In order to make the parsers, well, useful, you should use the '.!' operation
    // to capture the section of the input string the parser parsed.

    // Parser[String]: after capturing something with .!
    val capture1: Parser[String] = P("a".rep.! ~ "b" ~ End)
    capture1.parse("aaaaaab").validation should beSuccess(("aaaaaa", 7))

    // Parser[TupleN[String]]: capturing multiple things in series
    val capture2: Parser[(String, String)] = P("a".rep.! ~ "b".! ~ End)
    capture2.parse("aaaaaab").validation should beSuccess((("aaaaaa", "b"), 7))

    // Parser[TupleN[String]]: capturing multiple things in series
    val capture3: Parser[(String, String, String)] = P("a".rep.! ~ "b".! ~ "c".! ~ End)
    capture3.parse("aaabc").validation should beSuccess((("aaa", "b", "c"), 5))

    // Parser[Seq[String]]: after capturing things with .rep
    val captureRep: Parser[Seq[String]] = P("a".!.rep ~ "b" ~ End)
    captureRep.parse("aaab").validation should beSuccess((Seq("a", "a", "a"), 4))

    // Parser[Option[String]]: after capturing things with .?
    val captureOpt: Parser[Option[String]] = P("a".rep ~ "b".!.? ~ End)
    captureOpt.parse("aaab").validation should beSuccess((Option("b"), 4))
  }

  it should "parse any character input" in {
    // The 'AnyChar' parser parses any single character successfully.
    // It almost always succeeds,
    // except if there simply **aren't any characters left to parse**.
    //
    // There is also a plural 'AnyChars(count: Int)' version
    // that parses exactly count characters in a row, regardless of *what* they are.

    val ab = P("'" ~ AnyChar.! ~ "'")
    ab.parse("'-'").validation should beSuccess(("-", 3))
  }

  it should "map the parsed input to another type using 'map'" in {
    // .map lets you convert an arbitrary Parser[T]
    // into a Parser[V]
    // by providing a T => V function.
    //
    // This is useful for converting the strings
    // and tuples/seqs/options of strings
    // into more useful data-structures.

    val binary: Parser[String] = P(("0" | "1").rep.!)
    val binaryNum: Parser[Int] = P(binary.map(Integer.parseInt(_, 2)))
    binary.parse("1100").validation should beSuccess(("1100", 4))
    binaryNum.parse("1100").validation should beSuccess((12, 4))

    val hackagePackage: ElemIn[Char, String] = CharIn('a' to 'z', 'A' to 'Z', ".")
    val hackageVersion: ElemIn[Char, String] = CharIn('0' to '9', ".")
    val hackageParser: Parser[(String, String)] = P(hackagePackage.rep.! ~ "-" ~ hackageVersion.rep.! ~ End)
    hackageParser.parse("pretty-1.1.3.4").validation should beSuccess((("pretty", "1.1.3.4"), 14))
  }

  // Intrinsics are tools provided for convenience or performance
  "intrinsics" should "have a 'CharPred'(icate) parser that consumes a single character that satisfies the predicate" in {
    // 'CharPred'(icate) parser takes a Char => Boolean predicate
    // and creates a parser that parses any single character
    // that satisfies that predicate.
    //
    // e.g. you can use any of the helpful methods on scala.Char
    // to check if a Char isUpper, isDigit, isControl, etc. etc.

    val cp: Parser[String] = P(CharPred(_.isUpper).rep.! ~ "." ~ End)
    cp.parse("ABC.").validation should beSuccess(("ABC", 4))

    // parser 'cp' got applied at line: 1, index: 1
    // parser failed at line: 1, index: 3
    // parser expected either a '.' or something that satisfies the predicate but failed at 'c.'
    cp.parse("ABc.").validation should haveFailure("""cp:1:1 / ("." | CharPred(<function1>)):1:3 ..."c."""")
  }

  it should "have a CharIn parser that matches any sequence of characters its configured with" in {
    // The 'CharIn' parser is similar to the 'CharPred'(icate) parser,
    // except you pass in sequences of valid characters rather than a predicate.
    //
    // As a result, it's much faster to execute
    // than if you had used "a" | "b" | "c" | "d" | ...
    // to combine a bunch of single-character parsers together.
    //
    // The same warning as CharPred about the one time cost-of-construction applies.

    val ci: Parser[String] = P(CharIn("abc", "xyz").rep.! ~ End)
    ci.parse("aaabbccxyz").validation should beSuccess(("aaabbccxyz", 10))
    ci.parse("aaabbccdxyz.").validation should haveFailure("""ci:1:1 / (End | CharIn("abcxyz")):1:8 ..."dxyz."""")

    val digits: Parser[String] = P(CharIn('0' to '9').rep.!)
    digits.parse("12345abcde").validation should beSuccess(("12345", 5))
    digits.parse("123abcde45").validation should beSuccess(("123", 3))
  }

  it should "have a CharsWhile parser that continually parses characters that maches the given predicate" in {
    // The 'CharsWhile' parser is a repeated version of the 'CharPred'(icate) parser
    // that continually chomps away at characters
    // as long as they continue passes the given predicate.
    //
    // This is a very fast parser,
    // ideal for quickly consuming large numbers of characters.

    val cw: Parser[String] = P(CharsWhile(_ != ' ').!)
    cw.parse("12345").validation should beSuccess(("12345", 5))
    cw.parse("123 45").validation should beSuccess(("123", 3))
  }

  it should "have a StringIn parser that parses any number of strings you give it" in {
    // The 'StringIn' parser quickly parses one of any number of strings that you give it.
    // Behind the scenes, it converts the list of strings into a Trie
    // so it can attempt to parse all of them in a single pass.
    //
    // As a result, this is much faster to execute
    // than if you had combined the individual strings with "cow" | "cattle" | ....
    //
    // There is also a 'StringInIgnoreCase' parser you can use
    // if you want to match things case-insensitively.

    val si: Parser[Seq[String]] = P(StringIn("cow", "cattle").!.rep)
    si.parse("cowcattle").validation should beSuccess((Seq("cow", "cattle"), 9))
    si.parse("cowmoo").validation should beSuccess((Seq("cow"), 3))
  }
}
