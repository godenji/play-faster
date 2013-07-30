play-faster
===================

Description
-----------

Strips Play 2.x build process down to the essentials, reducing build times to absolute minimum. Provides basic guidelines for reducing build times via your own code, hardware, and jvm settings.


Motivation
----------

Isn't it obvious? The freedom of expression that Scala provides is not free, and some aspects of this mostly fantastic web framework leave something to be desired (e.g. routing and assets compilation)


Applies To
----------

Play 2 Scala applications on *nix systems, modify accordingly for Windows.


What Does It Do?
----------

1. Preserves sbt dependencies cache

moves sbt cache config & update dependencies directories out of clean's reach -- play> clean by default removes sbt deps dirs, which is why on EVERY clean/compile a deps update check is made, needlessly slowing down the build, particularly for sub projects where the deps check is made for each project.

2. Moves sbt compile target into RAM (optional*)

moves sbt compile target from project directory to tmpfs mounted directory (I use /tmp/sbt). Only shaves off a few seconds from the build time, but more importantly, it offloads I/O thrashing from your precious SSD into RAM.

3. Integrates sbteclipse

generates sbteclipse settings that allow for the above 2 steps to seamlessly occur with a simple play> eclipse

4. Disables built-in assets compilation (optional)

using a 3rd party assets build system (e.g. Bower + GruntJS) allows for rapid fire code-change/browser-refresh cycles, something that as of Play 2.1 is simply not happening (assets compilation continues to be _very_ slow). To re-enable built-in assets compilation just comment/remove the "lessEntryPoints" line from included Build.scala.


\* to keep compile target default, comment out "eclipseSettings" in Settings.scala; i.e.

		protected def _settings: Seq[Setting[_]] = { 
			superSettings ++ ivySettings //++ eclipseSettings
		}


How to Use
----------

Copy project/ Transformers.scala and Settings.scala to your main project/ directory. If moving sbt compile target to RAM is desired, modify "tmpfs" val in Transformers.scala with whatever variable name you set for the linked resource in Eclipse*, or keep "tmpfs" default value, "TARGET_TMPFS"

Compile target in RAM approach assumes that you have a tmpfs mounted directory; if not, add to your /etc/fstab (save, reboot):

		tmpfs	/dev/shm	tmpfs	defaults	0 0
		tmpfs	/tmp	tmpfs	defaults	0 0

Run a play> update to seed sbt deps cache (will be presevered across future play> clean-s); then do a clean/compile to populate tmpfs (or default) compile target. Finally, generate eclipse project settings: play> eclipse (skipParents=false if you want aggregator project included). Done, enjoy ;-)

\* to create a linked resource in Eclipse do the following:

1. create path variable: 

		preferences > (in search field) type "linked" > click Linked Resources > click New > 
		type a name (i.e. the value you provided for Transformers.scala "tmpfs" val)
		then browse to your tmpfs mounted directory (e.g. /tmp/sbt)

2. create linked resource: 

		right click any folder in your main (aggregator project) and do:
		file > new > advanced > check Link to filesystem > click Variables > 
		select path tmpfs variable you created in step 1.


How Not to Shoot yourself in Foot
-----------

1. Use sub projects (this is a huge WIN)
2. No need to .aggregate everything; for not-often-changing sub projects .dependsOn is enough
3. Consider replacing cake pattern with design by contract.
4. Avoid excessive use of self types
5. Specifying return type means less work for scalac to do
6. Implicits are quite useful, but don't go overboard
7. Be secretive; i.e. use private def and friends to hide dependent code from scalac (particularly helpful during incremental builds)
8. _ your nugget here _ (pull request)


JVM options
------------

For my setup (Dell Precision M4700 | 3840QM 3.6ghz 8mb cache | 256gb SSD | 32gb RAM) have the following java opts in ~/.bash_profile:

		JAVA_OPTS=-Xss8m -Xms512m -Xmx2048m -XX:MaxPermSize=512m -XX:ReservedCodeCacheSize=128m -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC

These settings (again, for my setup) result in optimal build times. -Xss option set to 1m,2m,4m were 10-20 seconds slower (4m, for some reason being the slowest). 

Setting a large -XX:ReservedCodeCacheSize also slowed down the build. Experiment, find out what works best for you.


Hardware Essentials
------------

1. Fast CPU (i7 extreme if you have the $$)
2. Fast disk
3. Enough RAM


Results
------------

After refactoring into a sub project enabled build and removing some fairly static core sub projects from play.Project .aggregate(...), cold clean/compile build times have been reduced from 140s to reproducable sub 60s (often in the low 50s). 

The big win in sub projects are of course incremental builds, where routes are dispersed across sub projects so you don't get hammered with the dreaded, compiling 1 scala source...followed by compiling 3/4 of entire project.

For warmed up jvm getting 13s clean/compile -- liking that ;-)


