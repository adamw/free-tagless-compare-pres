package com.softwaremill.free

import java.util.UUID

import cats.free.Free
import cats.{InjectK, ~>}
import cats.implicits._
import cats.Monad
import cats.data.EitherK

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.higherKinds

object Original {

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

object UsingFree {

}

object UsingTagless {

}