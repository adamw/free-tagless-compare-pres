package com.softwaremill.free.combine

import java.util.UUID

import cats.Monad
import cats.implicits._

import com.softwaremill.free.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.higherKinds

object CombineTagless {
  trait UserRepositoryAlg[F[_]] {
    def findUser(id: UUID): F[Option[User]]
    def updateUser(u: User): F[Unit]
  }

  trait EmailAlg[F[_]] {
    def sendEmail(email: String, subject: String, body: String): F[Unit]
  }

  class LoyaltyPoints[F[_]: Monad](ur: UserRepositoryAlg[F], es: EmailAlg[F]) {
    def addPoints(userId: UUID, pointsToAdd: Int): F[Either[String, Unit]] = {
      ur.findUser(userId).flatMap {
        case None => implicitly[Monad[F]].pure(Left("User not found"))
        case Some(user) =>
          val updated = user.copy(loyaltyPoints = user.loyaltyPoints + pointsToAdd)
          for {
            _ <- ur.updateUser(updated)
            _ <- es.sendEmail(user.email, "Points added!", s"You now have ${updated.loyaltyPoints}")
          } yield Right(())
      }
    }
  }

  trait FutureUserInterpreter extends UserRepositoryAlg[Future] {
    override def findUser(id: UUID): Future[Option[User]] =
      Future.successful(None) /* go and talk to a database */

    override def updateUser(u: User): Future[Unit] =
      Future.successful(()) /* as above */
  }

  trait FutureEmailInterpreter extends EmailAlg[Future] {
    override def sendEmail(email: String, subject: String, body: String): Future[Unit] =
      Future.successful(()) /* use smtp */
  }

  val result: Future[Either[String, Unit]] =
    new LoyaltyPoints(new FutureUserInterpreter {}, new FutureEmailInterpreter {}).addPoints(UUID.randomUUID(), 10)

}
