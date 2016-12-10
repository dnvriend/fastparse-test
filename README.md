# fastparse-test
A small study project on [Li Haoyi - FastParse](https://github.com/lihaoyi/fastparse), a parser-combinator library for Scala that lets you quickly and easily write recursive descent text- and binary data parsers in Scala.

## Introduction
To make sense of text, you need a way to parse it, and parser libraries such as fastparse can help you do that.

## Parsers
In functional programming, a parser combinator is a higher-order function that accepts several parsers as input 
and returns a new parser as its output.

A parser is a function which takes an input stream of characters and yields a parse tree 
by applying the parser logic over sections of the character stream (called [lexemes](https://en.wikipedia.org/wiki/Lexeme)) 
to build up a composite data structure for the AST.

Running the (parser) function will result in traversing the stream of characters, 
yielding a value that represents the AST for the parsed expression, 
or failing with a parse error for malformed input, 
or failing by not consuming the entire stream of input. 

A more robust implementation would track the position information of failures for error reporting.

## FastParse
Fastparse revolves around __Parser objects__. A Parser[T, Elem, Repr] object is a parser that 
_can attempt_ to parse a value T from an input sequence of elements of type Elem. 
The Repr type-parameter is responsible for output type in Capture, 
since input is converted to the _IndexedSeq[Elem]_ or _Iterator[IndexedSeq[Elem]]_ during all parsing operations.

There are two main cases: 
- for __String Parsers__, you are looking at _Parser[T, Char, String]_, 
- for __Byte Parsers__, you would be dealing with _Parser[T, Byte, Bytes]_

FastParse contains self-contained immutable parser objects that can handle streams of _Char_ or _Byte_ which is the Elem type.
The primary method of these parser objects is `.parse()`, which returns a [T] on success and a stack trace on failure.

Parser objects have the following methods:

- parse(input: Repr)
- parseIterator(input: Iterator[Repr])

The main external API is `.parse()` for parsing regular arrays of data 
and `.parseIterator()` for parsing streaming data. 

## Documentation

- [FastParse](http://www.lihaoyi.com/fastparse/)
- [Understanding parser combinators](https://fsharpforfunandprofit.com/posts/understanding-parser-combinators/)
- [Combinator Parsing](http://www.artima.com/pins1ed/combinator-parsing.html)
- [Parsers](http://dev.stephendiehl.com/fun/002_parsers.html)

## Usage
In bash, type `sbt test`.

Have fun!