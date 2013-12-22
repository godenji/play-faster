import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build with Transformers with Settings {

	lazy val superSettings = super.settings 

  val appName = "play-faster"
  val appVersion = "1.1"
	
  lazy val common = play.Project(
   appName + "-common", appVersion, path = file("module/common"), settings = _settings)
 
  lazy val foo = play.Project(
   appName + "-foo", appVersion, path = file("module/foo"), settings = _settings).
   dependsOn(common)

  lazy val bar = play.Project(
   appName + "-bar", appVersion, path = file("module/bar"), settings = _settings).
   dependsOn(common)

  lazy val aaMain = play.Project(appName + "-main", appVersion, settings = _settings).
  	settings(
  		
  		// disable built-in asset generators
  		lessEntryPoints := Nil, coffeescriptEntryPoints := Nil, javascriptEntryPoints := Nil,
  		
  		// prevent sbt from resolving deps on every clean/compile
			offline := true	
  	).dependsOn(common,foo,bar).aggregate(foo,bar)

}
