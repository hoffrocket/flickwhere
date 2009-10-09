package flickwhere.comet

import _root_.scala.actors._
import Actor._
import _root_.net.liftweb._
import http._
import util._
import Helpers._
import _root_.scala.xml._
import S._
import SHtml._
import js._
import JsCmds._
import JE._
import net.liftweb.http.js.jquery.JqJsCmds._



class Flickr extends CometActor with CometListener {
    private var userName = ""
    private var chats: List[FlickrChatLine] = Nil
    private var photo:Box[Photo] = Empty
    private lazy val infoId = uniqueId + "_info"
    private lazy val infoIn = uniqueId + "_in"
    private lazy val photoId = uniqueId + "_photo"
    private lazy val inputArea = findKids(defaultXml, "chat", "input")
    private lazy val bodyArea = findKids(defaultXml, "chat", "body")
    private lazy val singleLine = deepFindKids(bodyArea, "chat", "list")

    // handle an update to the chat lists
    // by diffing the lists and then sending a partial update
    // to the browser
    override def lowPriority = {
      case FlickrChatServerUpdate(value, photoV) =>
        val update = (value -- chats).reverse.map(b => AppendHtml(infoId, line(b))) ++ photoV.map(p=> SetHtml(photoId,photoHtml(p)))
        partialUpdate(update)
        chats = value
        photo = photoV
        
    }
    
    def photoHtml(photo:Photo):NodeSeq = <img src={photo.src}/>

    // render the input area by binding the
    // appropriate dynamically generated code to the
    // view supplied by the template
    override lazy val fixedRender: Box[NodeSeq] =
    ajaxForm(After(100, SetValueAndFocus(infoIn, "")),
             bind("chat", inputArea,
                  "input" -> text("", sendMessage _, "id" -> infoIn)))

    // send a message to the chat server
    private def sendMessage(msg: String) = FlickrGameServer ! FlickrChatServerMsg(userName, msg.trim)

    // display a line
    private def line(c: FlickrChatLine) = bind("list", singleLine,
                                         "when" -> hourFormat(c.when),
                                         "who" -> c.user,
                                         "msg" -> c.msg)

    // display a list of chats
    private def displayList(in: NodeSeq): NodeSeq = chats.reverse.flatMap(line)

    // render the whole list of chats
    override def render =
    bind("chat", bodyArea,
         "name" -> userName,
         AttrBindParam("photoId", Text(photoId), "id"),
         AttrBindParam("id", Text(infoId), "id"),
         "photo" -> photo.map(photoHtml).openOr(NodeSeq.Empty),
         "list" -> displayList _)

    // setup the component
    override def localSetup {
      askForName
      super.localSetup
    }

    // register as a listener
    def registerWith = FlickrGameServer

    // ask for the user's name
    private def askForName {
      if (userName.length == 0) {
        ask(new AskName, "what's your username") {
          case s: String if (s.trim.length > 2) =>
            userName = s.trim
            reRender(true)

          case _ =>
            askForName
            reRender(false)
        }
      }
    }
}