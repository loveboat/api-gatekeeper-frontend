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

package connectors

import config.WSHttp
import connectors.AuthConnector._
import model.{ApproveUpliftPreconditionFailed, ApproveUpliftSuccessful}
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, Upstream4xxResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ApplicationConnector extends ApplicationConnector {
  override val applicationBaseUrl: String = s"${baseUrl("third-party-application")}"
  override val http = WSHttp
}

trait ApplicationConnector {

  val applicationBaseUrl: String

  val http: HttpPost with HttpGet

  case class ApproveUpliftRequest(gatekeeperUserId: String)

  object ApproveUpliftRequest {
    implicit val format = Json.format[ApproveUpliftRequest]
  }

  def approveUplift(applicationId: String, gatekeeperUserId: String)(implicit hc: HeaderCarrier): Future[ApproveUpliftSuccessful] =
    http.POST(s"$applicationBaseUrl/application/$applicationId/approve-uplift", ApproveUpliftRequest(gatekeeperUserId), Seq("Content-Type" -> "application/json"))
      .map(_ => ApproveUpliftSuccessful)
      .recover {
        case e: Upstream4xxResponse if (e.upstreamResponseCode == 412) => throw new ApproveUpliftPreconditionFailed
      }


}
