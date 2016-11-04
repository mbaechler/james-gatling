package org.apache.james.gatling.imap

import org.apache.james.gatling.imap.protocol.ImapProtocol

object PreDef {

  def imap = ImapProtocolBuilder.default
  implicit def imapProtocolBuilder2ImapProtocol(builder: ImapProtocolBuilder): ImapProtocol = builder.build()

  def imap(requestName: String) = new ImapActionBuilder(requestName)
}
