package controllers.v1

import java.time.{ DateTimeException }
import javax.inject.Inject

import play.api.mvc.{ Action, AnyContent, Result, Controller }
import play.api.libs.json._

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scala.util.Try

import utils.Utilities._
import models._
import services._

/**
 * Created by ChiuA on 23/10/2017.
 */

class SearchController @Inject() (data: HBaseData) extends Controller {

  def getBusiness(period: String, id: String): Action[AnyContent] = {
    Action.async { implicit request =>
      id match {
        case id if validateUbrn(id) => Try(periodToYearMonth(period)) match {
          case Success(validPeriod) =>
            Try(Business.toJson(data.getOutput(period, id))) match {
              case Success(results) => ResultsMatcher(results)
              case Failure(_) => NotFound(errAsJson(404, "Not Found", s"Could not find UBRN ${id} in period ${validPeriod}")).future
            }
          case Failure(_: DateTimeException) => UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please ensure the period is in the following format: YYYYMM")).future
        }
        case _ => UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please Ensure that UBRN is 12 characters long")).future
      }
    }
  }

  def ResultsMatcher(businessModel: JsValue): Future[Result] = {
    Ok(businessModel).future
  }
}