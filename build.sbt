name := "fastparse-test"

organization := "com.github.dnvriend"

version := "1.0.0"

scalaVersion := "2.10.6"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.8"
libraryDependencies += "org.apache.commons" % "commons-compress" % "1.12"
libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.4.1"
libraryDependencies += "org.typelevel" %% "scalaz-scalatest" % "1.1.0" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test

// for scala 2.11.x or higher you don't need the compiler plugin
addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

fork in Test := true
parallelExecution := false

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable scala code formatting //
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform

// Scalariform settings
SbtScalariform.autoImport.scalariformPreferences := SbtScalariform.autoImport.scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2016", "Dennis Vriend"),
  "conf" -> Apache2_0("2016", "Dennis Vriend", "#")
)

enablePlugins(AutomateHeaderPlugin, SbtScalariform)
