import sbt.*

object Dependencies {
  object V {
    val bm4               = "0.3.1"
    val cats              = "2.13.0"
    val ctxApplied        = "0.1.4"
    val disciplineMunit   = "2.0.0"
    val fs2               = "3.12.0"
    val kp                = "0.11.2"
    val monocle           = "3.3.0"
    val munit             = "1.1.0"
    val munitCatsEffect   = "2.1.0"
    val scalaCheck        = "1.18.1"
    val scalaCheckEffect  = "1.0.4"
    val kittens           = "3.5.0"
    val catsEffectTestkit = "3.6.2"
  }

  val catsLaws              = "org.typelevel"  %% "cats-laws"               % V.cats
  val disciplineMunit       = "org.typelevel"  %% "discipline-munit"        % V.disciplineMunit
  val fs2Core               = "co.fs2"         %% "fs2-core"                % V.fs2
  val monocleCore           = "dev.optics"     %% "monocle-core"            % V.monocle
  val monocleMacro          = "dev.optics"     %% "monocle-macro"           % V.monocle
  val scalacheckEffect      = "org.typelevel"  %% "scalacheck-effect"       % V.scalaCheckEffect
  val kittens               = "org.typelevel"  %% "kittens"                 % V.kittens
  val munitCore             = "org.scalameta"  %% "munit"                   % V.munit
  val munitScalaCheck       = "org.scalameta"  %% "munit-scalacheck"        % V.munit
  val munitCatsEffect       = "org.typelevel"  %% "munit-cats-effect"       % V.munitCatsEffect
  val scalaCheck            = "org.scalacheck" %% "scalacheck"              % V.scalaCheck
  val scalacheckEffectMunit = "org.typelevel"  %% "scalacheck-effect-munit" % V.scalaCheckEffect
  val catsEffecTestkit      = "org.typelevel"  %% "cats-effect-testkit"     % V.catsEffectTestkit
}
