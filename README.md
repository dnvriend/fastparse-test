# fastparse-test
A small study project on [Li Haoyi - FastParse](https://github.com/lihaoyi/fastparse), a parser-combinator library for Scala that lets you quickly and easily write recursive descent text- and binary data parsers in Scala.

## Introduction
To make sense of text, you need a way to parse it, and parser libraries such as fastparse can help you do that.

## Parsers
A parser is a function which takes an input stream of characters and yields a parse tree 
by applying the parser logic over sections of the character stream (called [lexemes](https://en.wikipedia.org/wiki/Lexeme)) 
to build up a composite data structure for the AST.

Running the (parser) function will result in traversing the stream of characters, 
yielding a value that represents the AST for the parsed expression, 
or failing with a parse error for malformed input, 
or failing by not consuming the entire stream of input. 

A more robust implementation would track the position information of failures for error reporting.

## Documentation

- [Understanding parser combinators](https://fsharpforfunandprofit.com/posts/understanding-parser-combinators/)
- [Combinator Parsing](http://www.artima.com/pins1ed/combinator-parsing.html)
- [Parsers](http://dev.stephendiehl.com/fun/002_parsers.html)

## Usage
In bash, type `sbt test`.

Have fun!