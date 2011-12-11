package com.submarinerich.service.trophy

import unfiltered.request._
import unfiltered.request.{ & => ampersand }
import unfiltered.response._

import org.slf4j.{LoggerFactory,Logger}
import org.squeryl.SessionFactory
import org.squeryl.Session
import org.squeryl.PrimitiveTypeMode._
import javax.sql.DataSource
import com.mchange.v2.c3p0.DataSources
import org.squeryl.adapters.PostgreSqlAdapter
import com.submarinerich.service.Config
import com.codahale.jerkson.Json._


import com.submarinerich.service.trophy.model.{Favorite,Rating}

//http://xuwei-k.github.com/unfiltered-sxr/unfiltered-2.9.1-0.5.0/unfiltered/response/statuses.scala.html

/** unfiltered plan */
class TrophyService extends unfiltered.filter.Plan {
  import QParams._

 /* setup logger */
  val logger = LoggerFactory.getLogger(getClass())

  /* setup database */
	try{
		var ds_unpooled : DataSource = DataSources.unpooledDataSource("jdbc:postgresql://localhost/trophy", 
		                                                        "trophy","trophy48284sdwrervivce");
		var ds_pooled : DataSource = DataSources.pooledDataSource( ds_unpooled );
		logger.info("datasource looks like: "+ds_pooled)
		Class.forName("org.postgresql.Driver") 
		SessionFactory.concreteFactory = Some( () => 
			Session.create(ds_pooled.getConnection(), new PostgreSqlAdapter())
		)
	}catch{
		case e : javax.naming.NameNotFoundException => {
			Class.forName("org.postgresql.Driver") 
			SessionFactory.concreteFactory = Some( () =>
		    Session.create(
		      java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/trophy", 
					                                                        "trophy","trophy48284sdwrervivce"),new PostgreSqlAdapter()))
		}
		case _ => { 
			logger.info("uncaught error")
				Class.forName("org.postgresql.Driver") 
				SessionFactory.concreteFactory = Some( () =>
			    Session.create(
			      java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/trophy", 
						                                                        "trophy","trophy48284sdwrervivce"),new PostgreSqlAdapter()))
			}
	}
	

  def intent = {
    case GET(Path("/")) =>
			load(config)
      logger.debug("GET /")
      Ok ~> Html(Config.indexPage)
    case POST(Path("/fav") ampersand Params(params)) => 
			load(config)
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
			load(config)
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
			load(config)
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
		case GET(Path(Seg("favorites"::item::"count"::Nil)) ampersand Params(params)) =>
		 	var cnt : Long = 0
			if( item.toLong > -1 )
			transaction{
				cnt = from(Favorite.favorites)(p => 
				        where(p.destination === item.toLong) 
				        compute(count)
				      )
			}
		 Ok ~> ResponseString(generate(Map("id" -> item.toLong, "count" -> cnt)))
		case GET(Path(Seg("ratings"::item::"count"::Nil)) ampersand Params(params)) =>
			load(config)
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
			load(config)
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

	def load( f : javax.servlet.FilterConfig ) : Unit = {
		if( !Config.loaded )
			Config.load(f)
	}
}

// vim: set ts=4 sw=4 et:
