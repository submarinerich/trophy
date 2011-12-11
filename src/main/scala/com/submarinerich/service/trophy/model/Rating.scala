package com.submarinerich.service.trophy.model
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity


class Rating( val source : Long, val destination : Long, val rating : Int ) extends KeyedEntity[Long]
{
	val id : Long = -1
	def this() = this(-1,-1,0)
}
object Rating extends Schema {
	val ratings = table[Rating]("ratings")
	on(ratings)( b => declare( b.id is(autoIncremented("ratings_id_seq"))))
}

// vim: set ts=4 sw=4 et:
