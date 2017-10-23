package com.softwaremill.free.basic

import java.util.UUID

import com.softwaremill.free.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object BasicOriginal {

  trait UserRepository {
    def findUser(id: UUID): Future[Option[User]]

    def updateUser(u: User): Future[Unit]
  }

  class LoyaltyPoints(ur: UserRepository) {
    def addPoints(userId: UUID, pointsToAdd: Int): Future[Either[String, Unit]] = {
      ur.findUser(userId).flatMap {
        case None => Future.successful(Left("User not found"))
        case Some(user) =>
          val updated = user.copy(loyaltyPoints = user.loyaltyPoints + pointsToAdd)
          ur.updateUser(updated).map(_ => Right(()))
      }
    }
  }

}