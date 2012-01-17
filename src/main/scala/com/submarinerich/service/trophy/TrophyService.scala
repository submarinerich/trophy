package com.submarinerich.service.trophy

import unfiltered.request._
import unfiltered.request.{ & => ampersand }
import unfiltered.response._

import com.submarinerich.json.JSONUtil.generate
import com.submarinerich.display.DisplayMarkdown
import com.submarinerich.util.Settings
import com.submarinerich.service.SubmarineRichService
import org.squeryl.PrimitiveTypeMode._

import grizzled.slf4j.Logger

import actors._

import com.twitter.ostrich.stats.Stats

import com.submarinerich.service.trophy.model.{Favorite,Rating}

//http://xuwei-k.github.com/unfiltered-sxr/unfiltered-2.9.1-0.5.0/unfiltered/response/statuses.scala.html

class TrophyService extends SubmarineRichService
{
  import QParams._

	/* setup logger */
  val logger = Logger(getClass())

  Settings.setFile("/submarinerich/service/trophy.yaml")
  
  conf = Settings.shared

	/* setup ostrich */
  startOstrich

  log.info("ostrich running on port: "+service.address.getPort)

  /* setup database */
  setupPostgresConnection


  def intent = {
    case GET(Path("/")) =>
			Stats.incr("/ called")
      logger.debug("GET /")
      Ok ~> Html(DisplayMarkdown("indexPage.mkd")) 
    case POST(Path("/fav") ampersand Params(params)) => 
			Stats.incr("/fav called")
			var source : Long = -1
			var dest : Long = -1
			var category : Int = 0
			
			if( params.contains("source") && params("source").size > 0 )
				source = params("source")(0).toLong
				
			if( params.contains("destination") && params("destination").size > 0 )
				dest = params("destination")(0).toLong	
				
			if( params.contains("category") && params("category").size > 0 )
				category = params("category")(0).toInt
				
			var i : Option[Favorite] = None
			var favs : List[Favorite] = List()
			transaction{
				favs = Favorite.favorites.where( a => a.source === source and a.destination === dest and a.category === category ).toList
			}
			if( favs.size > 0 ){
				i = Some( favs(0) )
			}else if(source > -1 && dest > -1 ){
				transaction{
					i = Some( Favorite.favorites.insert( new Favorite(source,dest,category)))
				}
			}
			i match {
				case Some( f : Favorite ) => 
					Ok ~> ResponseString( generate( Map( "id" -> f.id, "source" -> f.source, "destination" -> f.destination, "cat" -> f.category )))
				case _ => BadRequest ~> ResponseString( generate(Map("error" -> "something wrong")))				
			}
		case POST(Path("/unfav") ampersand Params(params)) => 
			Stats.incr("/unfav called")
			var source : Long = -1
			var dest : Long = -1
			var category : Int = 0
			
			if( params.contains("source") && params("source").size > 0 )
				source = params("source")(0).toLong
				
			if( params.contains("destination") && params("destination").size > 0 )
				dest = params("destination")(0).toLong	
				
			if( params.contains("category") && params("category").size > 0 )
				category = params("category")(0).toInt
				
			if( source > -1 && dest > -1 ){
				transaction{
					Favorite.favorites.deleteWhere( a => a.source === source and a.destination === dest and a.category === category)
				}
			}
			Ok ~> ResponseString(generate(Map("success" -> "deleted favorite")))

		case POST(Path("/rate") ampersand Params(params)) => 
			Stats.incr("/rate called")
			var source : Long = -1
			var dest : Long = -1
			var rating : Int = 0
		
			if( params.contains("source") && params("source").size > 0 )
				source = params("source")(0).toLong
			
			if( params.contains("destination") && params("destination").size > 0 )
				dest = params("destination")(0).toLong	
			
			if( params.contains("rating") && params("rating").size > 0 )
				rating = params("rating")(0).toInt
			
			var i : Option[Rating] = None
			var favs : List[Rating] = List()
			transaction{
				favs = Rating.ratings.where( a => a.source === source and a.destination === dest ).toList
			}
			if( favs.size > 0 ){
				transaction{
					update( Rating.ratings )( r => 
						where(r.id === favs(0).id )
						set( r.rating := rating )
						)	
				}
				transaction{
					favs = Rating.ratings.where( a => a.id === favs(0).id).toList
				}
				i = Some(favs(0))

			}else if(source > -1 && dest > -1 ){
				transaction{
					i = Some( Rating.ratings.insert( new Rating(source,dest,rating)))
				}
			}
			i match {
				case Some( f : Rating ) => 
					Ok ~> ResponseString( generate( Map( "id" -> f.id, "source" -> f.source, "destination" -> f.destination, "rating" -> f.rating )))
				case _ => BadRequest ~> ResponseString( generate(Map("error" -> "something wrong")))				
			}

    case GET(Path(Seg("favorites"::source::Nil)) ampersand Params(params)) =>
      Stats.incr("/favorites/<source> called")
      try{
        var s : Long = source.toLong
        var favs : List[Favorite] = List()
        transaction{
          favs = Favorite.favorites.where( a => a.source === s ).toList 
        }
        var items : scala.collection.mutable.ArrayBuffer[Long] = new scala.collection.mutable.ArrayBuffer[Long]()
        favs.map( a => items += a.destination )
        Ok ~> ResponseString( generate( Map("source" -> s, "favorites" -> items.toArray ) ))
      }catch{
        case e : Exception => BadRequest ~> ResponseString( generate(Map("error" -> "no source")))
        case _ => BadRequest ~> ResponseString( generate(Map("error" -> "no source")))
      }
		case GET(Path(Seg("favorites"::item::"count"::Nil)) ampersand Params(params)) =>
		 	var cnt : Long = 0
			Stats.incr("/favorites/item/count called")
			if( item.toLong > -1 )
			transaction{
				cnt = from(Favorite.favorites)(p => 
				        where(p.destination === item.toLong) 
				        compute(count)
				      )
			}
		 Ok ~> ResponseString(generate(Map("id" -> item.toLong, "count" -> cnt)))
		case GET(Path(Seg("ratings"::item::"count"::Nil)) ampersand Params(params)) =>
			Stats.incr("/ratings/item/count called")
			var cnt : Long = 0
			if( item.toLong > -1 )
			transaction{
				cnt = from(Rating.ratings)(p => 
				        where(p.destination === item.toLong) 
				        compute(count)
				      )
			}
		 Ok ~> ResponseString(generate(Map("id" -> item.toLong, "count" -> cnt)))
		case GET(Path(Seg("ratings"::item::"average"::Nil)) ampersand Params(params)) =>
			Stats.incr("/ratings/item/average called")
		 	var ratings : List[Rating] = List()
			if( item.toLong > -1 )
			transaction{
				ratings = Rating.ratings.where(p => p.destination === item.toLong).toList
			}
			var total : Double = 0.0
			ratings.map( a => total += a.rating.toDouble )
			var avg : Double = 0.0
			if( ratings.size > 0 )
				avg = total/ratings.size 
		 Ok ~> ResponseString(generate(Map("id" -> item.toLong, "count" -> ratings.size, "average" -> avg )))
  }

	
}

// vim: set ts=2 sw=2 et:
