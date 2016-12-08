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
import scalaz._
import Scalaz._

object FastParseImplicits {
  implicit class ParserOps[+T](val that: Parsed[T]) extends AnyVal {
    def validation = that match {
      case failure @ Parsed.Failure(lastParser, index, extra) =>
        // return the human-readable trace of the parse
        // showing the stack of parsers which were in progress
        // when the parse failed
        failure.extra.traced.trace.failureNel[(T, Int)]
      case Parsed.Success(value, index) =>
        (value, index).successNel[String]
    }
  }
}

class FastParseTest extends TestSpec {
  import FastParseImplicits._

  "fastparse" should "parse a single letter" in {
    // The simplest parser matches a single string like eg. "a"
    val letterA = P("a")
    letterA.parse("a").validation should beSuccess(((), 1))
    letterA.parse("b").validation should haveFailure("""letterA:1:1 / "a":1:1 ..."b"""")
  }

  it should "parse a single letter case insensitive" in {
    val letterA = P(IgnoreCase("a"))
    letterA.parse("A").validation should beSuccess(((), 1))
    letterA.parse("B").validation should haveFailure("""letterA:1:1 / "a":1:1 ..."B"""")
  }

  it should "parse a sequence of letters (and combine two parsers)" in {
    // You can combine two parsers with the ~ operator.
    // This creates a new parser that only succeeds
    // if both left and right parsers succeed one after another.
    val ab = P("a" ~ "b")
    ab.parse("ab").validation should beSuccess(((), 2))
    ab.parse("bb").validation should haveFailure("""ab:1:1 / "a":1:1 ..."bb"""")
  }

  it should "parse a given parser zero or more times" in {
    val ab = P("a".rep ~ "b")
    ab.parse("aaaaab").validation should beSuccess(((), 6))
    ab.parse("aaabaaa").validation should beSuccess(((), 4))

    val abc = P("a".rep(sep = "b") ~ "c")
    abc.parse("abababac").validation should beSuccess(((), 8))
    abc.parse("abaabac").validation should haveFailure("""abc:1:1 / ("c" | "b"):1:4 ..."abac"""")

    val ab4 = P("a".rep(min = 2, max = 4, sep = "b"))
    ab4.parse("ababababababa").validation should beSuccess(((), 7))

    val ab2exactly = P("ab".rep(exactly = 2))
    ab2exactly.parse("abab").validation should beSuccess(((), 4))

    val ab4c = P("a".rep(min = 2, max = 4, sep = "b") ~ "c")
    ab4c.parse("ac").validation should haveFailure("""ab4c:1:1 / "b":1:2 ..."c"""")
    ab4c.parse("abac").validation should beSuccess(((), 4))
    ab4c.parse("abababac").validation should beSuccess(((), 8))
    ab4c.parse("ababababac").validation should haveFailure("""ab4c:1:1 / "c":1:8 ..."bac"""")
  }
}
