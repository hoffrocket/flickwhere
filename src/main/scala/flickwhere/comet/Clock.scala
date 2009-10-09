package flickwhere.comet

import _root_.net.liftweb._
import http._
import util._
import Helpers._
import js._
import JsCmds._
import _root_.scala.xml.Text

class Clock extends CometActor {
    override def defaultPrefix = Full("clk")
    
    def render = bind("time" -> timeSpan)
    
    private lazy val spanId = uniqueId+"_timespan"
     
    def timeSpan = (<span id={spanId}>{timeNow}</span>)
    
    def schedulePing = ActorPing.schedule(this, Tick, 10 seconds)

    schedulePing

    override def lowPriority : PartialFunction[Any, Unit] = { 
        case Tick => {
            partialUpdate(SetHtml(spanId, Text(timeNow.toString))) 
            // schedule an update in 10 seconds 
            schedulePing
        }
    }
    case object Tick
}