package com.softwaremill.free.combine

import java.util.UUID

import cats.data.EitherK
import cats.free.Free
import cats.{InjectK, ~>}
import cats.implicits._

import com.softwaremill.free.User

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CombineFree {
  sealed trait UserRepositoryAlg[T]
  case class FindUser(id: UUID) extends UserRepositoryAlg[Option[User]]
  case class UpdateUser(u: User) extends UserRepositoryAlg[Unit]

  class Users[F[_]](implicit i: InjectK[UserRepositoryAlg, F]) {
    def findUser(id: UUID): Free[F, Option[User]] = Free.inject(FindUser(id))
    def updateUser(u: User): Free[F, Unit] = Free.inject(UpdateUser(u))
  }
  object Users {
    implicit def users[F[_]](implicit i: InjectK[UserRepositoryAlg, F]): Users[F] = new Users
  }

  //

  sealed trait EmailAlg[T]
  case class SendEmail(email: String, subject: String, body: String) extends EmailAlg[Unit]

  class Emails[F[_]](implicit i: InjectK[EmailAlg, F]) {
    def sendEmail(email: String, subject: String, body: String): Free[F, Unit] = Free.inject(SendEmail(email, subject, body))
  }
  object Emails {
    implicit def emails[F[_]](implicit i: InjectK[EmailAlg, F]): Emails[F] = new Emails
  }

  //

  type UserAndEmailAlg[T] = EitherK[UserRepositoryAlg, EmailAlg, T]

  def addPoints(userId: UUID, pointsToAdd: Int)(
    implicit ur: Users[UserAndEmailAlg], es: Emails[UserAndEmailAlg]): Free[UserAndEmailAlg, Either[String, Unit]] = {

    ur.findUser(userId).flatMap {
      case None => Free.pure(Left("User not found"))
      case Some(user) =>
        val updated = user.copy(loyaltyPoints = user.loyaltyPoints + pointsToAdd)

        for {
          _ <- ur.updateUser(updated)
          _ <- es.sendEmail(user.email, "Points added!", s"You now have ${updated.loyaltyPoints}")
        } yield Right(())
    }
  }

  val futureUserInterpreter = new (UserRepositoryAlg ~> Future) {
    override def apply[A](fa: UserRepositoryAlg[A]): Future[A] = fa match {
      case FindUser(id) => /* go and talk to a database */ Future.successful(None)
      case UpdateUser(u) => /* as above */ Future.successful(())
    }
  }

  val futureEmailInterpreter = new (EmailAlg ~> Future) {
    override def apply[A](fa: EmailAlg[A]): Future[A] = fa match {
      case SendEmail(email, subject, body) => /* use smtp */ Future.successful(())
    }
  }

  val futureUserOrEmailInterpreter = futureUserInterpreter or futureEmailInterpreter

  val result: Future[Either[String, Unit]] =
    addPoints(UUID.randomUUID(), 10).foldMap(futureUserOrEmailInterpreter)
}
