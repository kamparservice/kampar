package models

import com.typesafe.plugin._
import play.api.Play.current
import java.io._
import play.api._

class Mailer(var subject:String, var recipient:String, var body:String){
	
	def send() {
		val mail = use[MailerPlugin].email
		mail.setSubject(subject)
		mail.addRecipient(recipient)
		mail.addFrom(Configuration.load(new File("conf/application.conf")).getString("email.address.from").get)
		mail.sendHtml(body)
	}
}