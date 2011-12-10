package com.submarinerich.service
import scala.collection.mutable.HashMap
import javax.servlet.FilterConfig
import org.yaml.snakeyaml.Yaml
import scala.collection.Map
import scala.collection.JavaConversions._
import com.tristanhunt.knockoff.DefaultDiscounter.{knockoff,toXHTML}

object Config
{
  def loaded : Boolean = files.size > 0
  var files : HashMap[String,String] = new HashMap[String,String]()
	var attributes : HashMap[String,Any] = new HashMap[String,Any]()

  def load( f : FilterConfig) : Unit = {
    import scala.io.Source.fromFile
  	if( !files.contains("index") ){
      var configFile : scala.io.BufferedSource = fromFile(f.getServletContext().getResource("/WEB-INF/indexPage.mkd").toURI)//f.getServletContext().getResource($$$).toU
      var tl : String = ""
      for( l <- configFile.getLines() )
        tl += l + "\n"
      files += ("index" -> tl)
    }
    if( !files.contains("config") ){
      var configFile : scala.io.BufferedSource = fromFile(f.getServletContext().getResource("/WEB-INF/config.yaml").toURI)
      var tl : String = ""
      for( l <- configFile.getLines() )
        tl += l + "\n"
      files += ("config" -> tl)

		 	var yaml : Yaml = new Yaml()
		  val javamap : java.util.Map[String,Any] = (yaml.load(tl).asInstanceOf[java.util.Map[String,Any]])
		  val keyMap : Map[String,Any] = javamap
		
			if( !attributes.contains("redisHost") && keyMap.contains("redis_server") )
				attributes += ("redisHost" -> keyMap("redis_server"))
				
			if( !attributes.contains("redisPort") && keyMap.contains("redis_port"))
				attributes += ("redisPort" -> keyMap("redis_port"))
			attributes("title") = "UNTITLED"
			if( keyMap.contains("site_name"))
				attributes("title") = keyMap("site_name").toString
    }
  }
	import scala.xml.NodeSeq
	
  def getIndex : NodeSeq = {
		val item1 = knockoff( files("index") )
    val readmehtml = toXHTML( item1 )
		readmehtml
  }
	
	def indexPage : NodeSeq = {
		<html xmlns:lift="http://liftweb.net/" xmlns="http://www.w3.org/1999/xhtml">
		<head>
		    <meta content="text/html; charset=utf-8" http-equiv="Content-Type" />
		    <link type="image/png" href="http://cdn.submarinerich.com/images/favicon.png" rel="icon" />
		    <meta content="" name="description" />
		    <meta content="" name="keywords" />
			<title>{attributes("title")}</title>
			<link media="screen, projection" type="text/css" href="http://cdn.submarinerich.com/css/screen.css" rel="stylesheet" />
			<link media="print" type="text/css" href="http://cdn.submarinerich.com/css/print.css" rel="stylesheet" /> 
			<!--[if lt IE 8]><link rel="stylesheet" href="http://cdn.submarinerich.com/css/ie.css" type="text/css" media="screen, projection"/><![endif]-->
			<!-- Date: 2011-08-14 -->
			<link media="screen, projection" type="text/css" href="http://submarine-cdn.s3.amazonaws.com/school/css/index.css" rel="stylesheet"/>
		</head>
		<body>
			<div class="container">
		  	<div class="span-24 last">
				<img src="http://cdn.submarinerich.com/images/sr-tiny.png" id="sr_logo" />
				<span>
				{getIndex}
				</span>
			</div>
			</div>
			<script src="http://cdn.submarinerich.com/js/modernizr-2.0.6.min.js"></script> 
			</body>
		</html>
	}


}


// vim: set ts=4 sw=4 et:
