package com.submarinerich.service.school.model
import org.squeryl.PrimitiveTypeMode._
import org.squery.Schema
import org.squeryl.annotations.Column


class Edge(val a : Long, val b : Long ) extends KeyedEntity {
  def this() = this(-1,-1)

}

object Edge extends Schema{
  val edges = table[Edge]
  def exists( a1 : Long, b1 : Long ) : Boolean = {
  	false
  }

  def create( a1 : Long, b1 : Long ) : Option[Edge] = {
    None
  }

  def friends( a1 : Long ) : List[Long] = {
    List()
  }
  
  def followers( b1 : Long ) : List[Long] = {
    List()
  }


}

// vim: set ts=4 sw=4 et:
