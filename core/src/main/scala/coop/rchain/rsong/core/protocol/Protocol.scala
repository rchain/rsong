package coop.rchain.rsong.core.protocol

import cats.Monoid
import coop.rchain.rsong.core.domain.DeParConverter

//object Protocol {
//
//  sealed trait ValueObject
//
//
//  implicit val DeParMonoid = new Monoid[DeParConverter] {
//    def empty: DeParConverter = DeParConverter()
//    def combine(d1: DeParConverter, d2: DeParConverter): DeParConverter =
//      DeParConverter(asInt = d1.asInt ::: d2.asInt,
//                     asString = d1.asString ::: d2.asString,
//                     asUri = d1.asUri ::: d2.asUri,
//                     asByteArray = d1.asByteArray ::: d2.asByteArray)
//  }
//}
