package flickwhere.comet

import _root_.scala.actors.Actor
import Actor._
import _root_.net.liftweb._
import http._
import util._
import Helpers._
import _root_.scala.xml.{NodeSeq, Text}
import textile.TextileParser
import _root_.java.util.Date
import _root_.net.liftweb.json._
import JsonAST._
import JsonDSL._
import JsonParser._
import _root_.java.net.{HttpURLConnection, URL, URLEncoder}


object FlickrGameServer extends Actor with ListenerManager {
  private def key = Props.get("flickr.key","")

  private var chats: List[FlickrChatLine] = List(FlickrChatLine("System", Text("Welcome"), now))
  private var game:Box[FlickrGame] = Empty
  private var photos:List[Photo] = Nil

  override def lowPriority = {
    case FlickrChatServerMsg(user, msg) if msg.length > 0 =>
      chats ::= FlickrChatLine(user, toHtml(msg), timeNow)
      chats = chats.take(50)
      updateListeners()
    case NewGame => 
        photos match {
            case x::xs => 
                Log.info("Starting new game with " + photos.length + " photos")
                Log.info("First photo: " + x.src)
                game = Full(new FlickrGame(Nil,x))
                photos = xs
            case Nil => doFeedUpdate
        }
        scheduleNewGame
        updateListeners()
    case f:FlickrGuess => 
        game.map(_.addGuess(f))
    case FeedUpdate =>
        doFeedUpdate
    case _ =>
  }
  
  def scheduleNewGame = ActorPing.schedule(this, NewGame, 30 seconds)
  
  def createUpdate = FlickrChatServerUpdate(chats.take(15), game.map(_.photo))
  
  def getJson(params:String) = new URL("http://api.flickr.com/services/rest/?method=" + params + "&format=json&nojsoncallback=1&api_key="+key).openConnection match {
      case conn: HttpURLConnection => {
        conn.setRequestMethod("GET")
        conn.connect
        import org.apache.commons.io.IOUtils
        parse(IOUtils.toString(conn.getInputStream,"UTF-8"))
      }
    }
  
  def doFeedUpdate{
      var allPhotos:List[Photo] = Nil
      (1 to 10).foreach(n=>{
          val json = getJson("flickr.photos.search&has_geo=1&media=photos&per_page=500&page="+n) 
          val results = for (JArray(photos) <- json\"photos"\"photo";
              photo <- photos;
              JString(id) <- photo\"id";
              JInt(farm) <- photo\"farm";
              JString(server) <- photo\"server";
              JString(secret) <- photo\"secret") yield Photo(id,farm.toString,server,secret)
          allPhotos = allPhotos ++ results.toList          
      })
      Log.info("Got feed photo count " + allPhotos.length)
      photos = Shuffle.shuffle(allPhotos)
      this ! NewGame
  }
  
  def lookupLocation(photo:Photo):Box[(Double,Double)] = {
      val json = getJson("flickr.photos.geo.getLocation&photo_id="+photo.id)
      val location = json\"photo"\"location"
      val result = for(JDouble(latitude) <- location\"latitude"; JDouble(longitude)<-location\"longitude") yield (latitude, longitude)
      result.firstOption
  }



  def toHtml(msg: String): NodeSeq = TextileParser.paraFixer(TextileParser.toHtml(msg, Empty))

  this.start
  this ! FeedUpdate
  case object FeedUpdate
  case object NewGame
}

case class FlickrGuess(user:Flickr, coord:Tuple2[Double,Double])
class FlickrGame(var guesses:List[FlickrGuess], val photo:Photo){
    val startTime = new java.util.Date()
    def addGuess(guess:FlickrGuess) { guesses ::= guess }
}
case class FlickrChatLine(user: String, msg: NodeSeq, when: Date)
case class FlickrChatServerMsg(user: String, msg: String)
case class FlickrChatServerUpdate(msgs: List[FlickrChatLine], photo:Box[Photo])
case class Photo(id:String, farm:String, server:String, secret:String){
    def src = "http://farm%s.static.flickr.com/%s/%s_%s_m.jpg".format(farm, server,id,secret)
}

object Shuffle {
    private lazy val rand = new scala.util.Random();
    def shuffle[T](xs: List[T]): List[T] = xs match {
          case Nil => Nil
          case xs => { 
              val i = rand.nextInt(xs.size);
              xs(i) :: shuffle(xs.take(i) ++ xs.drop(i+1))
          }
    }
}