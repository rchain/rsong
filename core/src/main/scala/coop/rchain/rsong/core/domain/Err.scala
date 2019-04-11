package coop.rchain.rsong.core.domain
 
object OpCode extends Enumeration {
  type OpCode = Value
  val grpcEval, grpcDeploy, grpcPropose, grpcShow, rholang, nameToPar,
      rsongHexConversion, rsongRetrival, rsongRevnetwork, contractFile,
      playCountConversion, nameNotFound, listenAtName, findName, cacheLayer, unregisteredUser,
      cachingSong, fileIO, unknown = Value
}

case class Err(code: OpCode.OpCode, msg: String) 
