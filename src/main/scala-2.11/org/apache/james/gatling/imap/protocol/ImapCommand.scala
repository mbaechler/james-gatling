package org.apache.james.gatling.imap.protocol

object ImapCommand {
  case class Select(userId:String, mailbox: String) extends ImapCommand
  case class Login(userId:String, username:String, password:String) extends ImapCommand
}

trait ImapCommand {
  def userId:String
}