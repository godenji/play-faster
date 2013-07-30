import sbt._
import Keys._

import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseTransformerFactory
import com.typesafe.sbteclipse.core._
import scala.xml.transform.RewriteRule
import scala.xml.{Node,Elem}

/*
 * sbt eclipse transformer factories (e.g. override compile target classpath)
 */
trait Transformers {
  
  val tmpfs = "TARGET_TMPFS" // linked resource, expands to abs path in eclipse  
  val f = java.io.File.separator
  val src_main = f + "src_managed" + f + "main"
  
  // add tmpfs src_managed/main classpathentry to project's .classpath
  lazy val addSourcesManaged = 
    new EclipseTransformerFactory[RewriteRule] {
      override def createTransformer(ref: ProjectRef, state: State): Validation[RewriteRule] = {
        setting(crossTarget in ref, state) map { ct =>
          new RewriteRule {
            override def transform(node: Node): Seq[Node] = node match {   
            	case el if(el.label == "classpath" && new java.io.File(ct + src_main).exists) =>
                val newChild = el.child ++ 
                	<classpathentry path={
                  	List(
                  	  tmpfs,
                  	  ct.getParent.split("/").reverse.head,
                  	  ct.getName
                  	).mkString(f) + src_main
                  } kind="src"></classpathentry>
                Elem(
                  el.prefix, "classpath", el.attributes, el.scope, newChild: _*
                )
              case other => other
            }
          }
        }
      }
    }
  
  // add linked resource to project's .project
  lazy val addLinkedResource = 
    new EclipseTransformerFactory[RewriteRule] {
      override def createTransformer(ref: ProjectRef, state: State): Validation[RewriteRule] = {
        setting(crossTarget in ref, state) map { ct =>
          new RewriteRule {
            override def transform(node: Node): Seq[Node] = node match {
              case el if el.label == "linkedResources" =>
                <linkedResources>
                	<link>
                		<name>{tmpfs}</name>
                		<type>2</type>
                		<locationURI>{tmpfs}</locationURI>
                	</link>
                </linkedResources>
              case other => other
            }
          }
        }
      }
    }
  
}
