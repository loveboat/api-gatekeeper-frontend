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

package controllers

import connectors.{ApplicationConnector, AuthConnector}
import model.State.{State, _}
import model.{ApplicationWithUpliftRequest, Role}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.{GatekeeperAuthProvider, GatekeeperAuthWrapper}
import views.html.approvedApplication._
import views.html.dashboard._

import views.html.review.review

import scala.concurrent.Future

object DashboardController extends DashboardController {
  override def authProvider = GatekeeperAuthProvider

  override def authConnector = AuthConnector

  override val applicationConnector = ApplicationConnector
}

trait DashboardController extends FrontendController with GatekeeperAuthWrapper {

  val applicationConnector: ApplicationConnector

  val dashboardPage: Action[AnyContent] = requiresRole(Role.APIGatekeeper) {
    implicit request => implicit hc =>

      def applicationsForDashboard(apps: Seq[ApplicationWithUpliftRequest]): Map[String, Seq[ApplicationWithUpliftRequest]] = {
        val grouped: Map[State, Seq[ApplicationWithUpliftRequest]] = apps.groupBy(_.state)
        val pendingApproval = grouped.getOrElse(PENDING_GATEKEEPER_APPROVAL, Seq())
        val pendingVerification = grouped.getOrElse(PENDING_REQUESTER_VERIFICATION, Seq()) ++ grouped.getOrElse(PRODUCTION, Seq())

        Map("pendingApproval" -> pendingApproval.sortWith(ApplicationWithUpliftRequest.compareBySubmittedOn),
            "pendingVerification" -> pendingVerification.sortWith(ApplicationWithUpliftRequest.compareBySubmittedOn))
      }

      for {
        apps <- applicationConnector.fetchApplications()
        mappedApps = applicationsForDashboard(apps)
      } yield Ok(dashboard(mappedApps))
  }

  val reviewPage: Action[AnyContent] = requiresRole(Role.APIGatekeeper) {
    implicit request => implicit hc =>
      Future.successful(Ok(review()))
  }

  val approvedApplicationPage: Action[AnyContent] = requiresRole(Role.APIGatekeeper) {
    implicit request => implicit hc =>
      Future.successful(Ok(approved()))
  }

  def approveUplift(appId: String): Action[AnyContent] = requiresRole(Role.APIGatekeeper) {
    implicit request => implicit hc =>
      applicationConnector.approveUplift(appId, loggedIn.get) map {
        ApproveUpliftSuccessful => Redirect(controllers.routes.DashboardController.dashboardPage)
      }
  }

}