package com.submarinerich.service.blank

import unfiltered.request._
import unfiltered.response._

import org.slf4j.{LoggerFactory,Logger}
import org.squeryl.SessionFactory
import org.squeryl.Session
import org.squeryl.adapters.MySQLAdapter

/** unfiltered plan */
class BlankService extends unfiltered.filter.Plan {
  import QParams._

  val logger = LoggerFactory.getLogger(getClass())

  Class.forName("com.mysql.jdbc.Driver") 

  SessionFactory.concreteFactory = Some(()=>
    Session.create(
      java.sql.DriverManager.getConnection("jdbc://localhost:3306/servicename","username","superpass"),new MySQLAdapter))



  def intent = {
    case GET(Path("/")) =>
      logger.debug("GET /")
      Ok ~> view(Map.empty)(<p> What say you? </p>)
    case POST(Path("/") & Params(params)) =>
      logger.debug("POST /")
      val vw = view(params)_
      val expected = for {
        int <- lookup("int") is
          int { _ + " is not an integer" } is
          required("missing int")
        word <- lookup("palindrome") is
          trimmed is
          nonempty("Palindrome is empty") is
          pred(palindrome, { _ + " is not a palindrome" }) is
          required("missing palindrome")
      } yield vw(<p>Yup. { int.get } is an integer and { word.get } is a palindrome. </p>)
      expected(params) orFail { fails =>
        vw(<ul> { fails.map { f => <li>{f.error} </li> } } </ul>)
      }
  }
  def palindrome(s: String) = s.toLowerCase.reverse == s.toLowerCase
  def view(params: Map[String, Seq[String]])(body: scala.xml.NodeSeq) = {
    def p(k: String) = params.get(k).flatMap { _.headOption } getOrElse("")
    Html(
     <html>
      <head>
        <title>uf example</title>
        <link rel="stylesheet" type="text/css" href="/css/app.css"/>
      </head>
      <body>
       <div id="container">
       { body }
       <form method="POST">
         <div>Integer <input type="text" name="int" value={ p("int") } /></div>
         <div>Palindrome <input type="text" name="palindrome" value={ p("palindrome") } /></div>
         <input type="submit" />
       </form>
       </div>
     </body>
    </html>
   )
  }
}

// vim: set ts=4 sw=4 et: