package com.softwaremill.free.basic

import java.util.UUID

import cats.free.Free
import cats.~>
import com.softwaremill.free.User
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object BasicFree {

  sealed trait UserRepositoryAlg[T]
  case class FindUser(id: UUID) extends UserRepositoryAlg[Option[User]]
  case class UpdateUser(u: User) extends UserRepositoryAlg[Unit]

  type UserRepository[T] = Free[UserRepositoryAlg, T]

  def findUser(id: UUID): UserRepository[Option[User]] = Free.liftF(FindUser(id))
  def updateUser(u: User): UserRepository[Unit] = Free.liftF(UpdateUser(u))

  def addPoints(userId: UUID, pointsToAdd: Int): UserRepository[Either[String, Unit]] = {
    findUser(userId).flatMap {
      case None => Free.pure(Left("User not found"))
      case Some(user) =>
        val updated = user.copy(loyaltyPoints = user.loyaltyPoints + pointsToAdd)
        updateUser(updated).map(_ => Right(()))
    }
  }

  val futureInterpreter = new (UserRepositoryAlg ~> Future) {
    override def apply[A](fa: UserRepositoryAlg[A]): Future[A] = fa match {
      case FindUser(id) => /* go and talk to a database */ Future.successful(None)
      case UpdateUser(u) => /* as above */ Future.successful(())
    }
  }

  val result: Future[Either[String, Unit]] =
    addPoints(UUID.randomUUID(), 10).foldMap(futureInterpreter)
}
