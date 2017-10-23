package com.softwaremill.free.basic

import java.util.UUID

import cats.Monad
import cats.implicits._
import com.softwaremill.free.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.higherKinds

object BasicTagless {

  trait UserRepositoryAlg[F[_]] {
    def findUser(id: UUID): F[Option[User]]
    def updateUser(u: User): F[Unit]
  }

  class LoyaltyPoints[F[_]: Monad](ur: UserRepositoryAlg[F]) {
    def addPoints(userId: UUID, pointsToAdd: Int): F[Either[String, Unit]] = {
      ur.findUser(userId).flatMap {
        case None => implicitly[Monad[F]].pure(Left("User not found"))
        case Some(user) =>
          val updated = user.copy(loyaltyPoints = user.loyaltyPoints + pointsToAdd)
          ur.updateUser(updated).map(_ => Right(()))
      }
    }
  }

  trait FutureInterpreter extends UserRepositoryAlg[Future] {
    override def findUser(id: UUID): Future[Option[User]] =
      Future.successful(None) /* go and talk to a database */

    override def updateUser(u: User): Future[Unit] =
      Future.successful(()) /* as above */
  }

  val result: Future[Either[String, Unit]] =
    new LoyaltyPoints(new FutureInterpreter {}).addPoints(UUID.randomUUID(), 10)

}
