/*
 * Copyright 2007-2009 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
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

/**
 * A chat server.  It gets messages and returns them
 */

object ChatServer extends Actor with ListenerManager {
  private var chats: List[ChatLine] = List(ChatLine("System", Text("Welcome"), now))

  override def lowPriority = {
    case ChatServerMsg(user, msg) if msg.length > 0 =>
      chats ::= ChatLine(user, toHtml(msg), timeNow)
      chats = chats.take(50)
      updateListeners()

    case _ =>
  }

  def createUpdate = ChatServerUpdate(chats.take(15))

  /**
   * Convert an incoming string into XHTML using Textile Markup
   *
   * @param msg the incoming string
   *
   * @return textile markup for the incoming string
   */
  def toHtml(msg: String): NodeSeq = TextileParser.paraFixer(TextileParser.toHtml(msg, Empty))

  this.start
}

case class ChatLine(user: String, msg: NodeSeq, when: Date)
case class ChatServerMsg(user: String, msg: String)
case class ChatServerUpdate(msgs: List[ChatLine])

