package models

import com.typesafe.plugin._
import play.api.Play.current

class Mailer(var subject:String, var recipient:String, var from:String, var body:String){
	
	def send() {
		val mail = use[MailerPlugin].email
		mail.setSubject(subject)
		mail.addRecipient(recipient)
		mail.addFrom(from)
		mail.send(body)
	}
}