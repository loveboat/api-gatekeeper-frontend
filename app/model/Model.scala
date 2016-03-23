/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package model

import org.joda.time.DateTime
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.json.{JsonDecryptor, JsonEncryptor}
import uk.gov.hmrc.crypto.{ApplicationCrypto, Protected}


case class LoginDetails(userName: String, password: Protected[String])

object LoginDetails {
  implicit val crypto = ApplicationCrypto.JsonCrypto

  object JsonStringEncryption extends JsonEncryptor[String]

  object JsonStringDecryption extends JsonDecryptor[String]

  implicit val encryptedStringFormats = JsonStringEncryption
  implicit val decryptedStringFormats = JsonStringDecryption

  implicit val formats = Json.format[LoginDetails]

  def make(userName: String, password: String) = LoginDetails(userName, Protected(password))

  def unmake(user: LoginDetails) = Some((user.userName, user.password.decryptedValue))
}


case class Role(scope: String, name: String)

object Role {
  implicit val format = Json.format[Role]
  val APIGatekeeper = Role("api", "gatekeeper")
}

case class BearerToken(authToken: String, expiry: DateTime) {
  override val toString = authToken
}

object BearerToken {
  implicit val format = Json.format[BearerToken]
}

case class SuccessfulAuthentication(access_token: BearerToken, userName: String, roles: Option[Set[Role]])

object GatekeeperSessionKeys {
  val LoggedInUser = "LoggedInUser"
}

class ApproveUpliftPreconditionFailed extends Throwable

sealed trait ApproveUpliftSuccessful
case object ApproveUpliftSuccessful extends ApproveUpliftSuccessful