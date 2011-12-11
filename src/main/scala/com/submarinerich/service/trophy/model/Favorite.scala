package com.submarinerich.service.trophy.model
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.annotations.Column
import org.squeryl.KeyedEntity

class Favorite( val source : Long, val destination : Long, val category : Int ) extends KeyedEntity[Long] {
	val id : Long = -1
	def this() = this(-1,-1,0)
}

object Favorite extends Schema{
	val favorites = table[Favorite]("favorites")
	on(favorites)( b => declare(b.id is(autoIncremented("favorites_id_seq"))))
}

// vim: set ts=4 sw=4 et:
