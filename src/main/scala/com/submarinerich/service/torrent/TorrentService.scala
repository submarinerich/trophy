package com.submarinerich.service.torrent

import unfiltered.request._
import unfiltered.response._

import org.slf4j.{LoggerFactory,Logger}
import org.squeryl.SessionFactory
import org.squeryl.Session
import org.squeryl.adapters.MySQLAdapter
import javax.sql.DataSource
import com.mchange.v2.c3p0.DataSources
import org.squeryl.adapters.PostgreSqlAdapter
import com.submarinerich.service.Config

/** unfiltered plan */
class TorrentService extends unfiltered.filter.Plan {
  import QParams._

 /* setup logger */
  val logger = LoggerFactory.getLogger(getClass())

  /* setup database */
	// try{
	// 	var ds_unpooled : DataSource = DataSources.unpooledDataSource("jdbc:postgresql://localhost/torrent", 
	// 	                                                        "torrent","some000pass2d9workd");
	// 	var ds_pooled : DataSource = DataSources.pooledDataSource( ds_unpooled );
	// 	logger.info("datasource looks like: "+ds_pooled)
	// 	Class.forName("org.postgresql.Driver") 
	// 	SessionFactory.concreteFactory = Some( () => 
	// 		Session.create(ds_pooled.getConnection(), new PostgreSqlAdapter())
	// 	)
	// }catch{
	// 	case e : javax.naming.NameNotFoundException => {
	// 		Class.forName("org.postgresql.Driver") 
	// 		SessionFactory.concreteFactory = Some( () =>
	// 	    Session.create(
	// 	      java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/torrent", 
	// 				                                                        "torrent","some000pass2d9workd"),new PostgreSqlAdapter()))
	// 	}
	// 	case _ => { 
	// 		logger.info("uncaught error")
	// 			Class.forName("org.postgresql.Driver") 
	// 			SessionFactory.concreteFactory = Some( () =>
	// 		    Session.create(
	// 		      java.sql.DriverManager.getConnection("jdbc:postgresql://localhost/torrent", 
	// 					                                                        "torrent","some000pass2d9workd"),new PostgreSqlAdapter()))
	// 		}
	// }

  def intent = {
    case GET(Path("/")) =>
			load(config)
      logger.debug("GET /")
			
      Ok ~> Html(Config.indexPage)
    
  }

	def load( f : javax.servlet.FilterConfig ) : Unit = {
		if( !Config.loaded )
			Config.load(f)
	}
}

// vim: set ts=4 sw=4 et:
