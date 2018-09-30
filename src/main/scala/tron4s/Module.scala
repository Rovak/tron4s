package tron4s

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.google.inject.name.{Named, Names}
import com.google.inject.util.Providers
import com.google.inject.{AbstractModule, Injector, Provider, Provides}
import com.typesafe.config.{Config, ConfigFactory}
import io.grpc.ManagedChannelBuilder
import javax.inject.{Inject, Singleton}
import org.tron.api.api.WalletGrpc.Wallet
import org.tron.api.api.{WalletGrpc, WalletSolidityGrpc}
import org.tron.api.api.WalletSolidityGrpc.WalletSolidity
import play.api.inject.ConfigurationProvider
import play.api.libs.concurrent.{Akka, DefaultFutures}
import tron4s.client.grpc.GrpcBalancer
import tron4s.grpc.GrpcPool
import tron4s.importer.ImportManager
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.reflect.ClassTag
//
//
//class AkkaProvider[T] @Inject() (injector: Injector, name: String) extends Provider[ActorRef] {
//  override def get(): ActorRef = {
//    val actorSystem = injector.getInstance(classOf[ActorSystem])
//    actorSystem.actorOf(Props(() => injector.getInstance(classOf[T])), name)
//  }
//}

class Module extends AbstractModule {

  override def configure = {
//    bindActor[ImportManager]("blockchain-importer")
//    bindActor[GrpcPool]("grpc-pool")
//    bindActor[GrpcBalancer]("grpc-balancer")

//    bind(classOf[WalletSolidity]).to(classOf[WalletSolidityGrpc.WalletSolidityStub])
    bind(classOf[play.api.libs.concurrent.Futures]).to(classOf[DefaultFutures])
  }

  @Provides
  @Singleton
  def actorSystem: ActorSystem = {
    ActorSystem()
  }

  @Provides
  @Singleton
  def buildConfig = {
    ConfigFactory.load()
  }

  @Provides
  @Singleton
  @Named("grpc-pool")
  def grpcPool(actorSystem: ActorSystem): ActorRef = {
    actorSystem.actorOf(Props(new GrpcPool))
  }

  @Provides
  @Singleton
  @Named("grpc-balancer")
  def grpcBalancer(actorSystem: ActorSystem, config: Config): ActorRef = {
    actorSystem.actorOf(Props(new GrpcBalancer(config)))
  }
//
//  def bindActor[T <: Actor: ClassTag](name: String, props: Props => Props = identity): Unit = {
//    bind(classOf[ActorRef])
//      .annotatedWith(Names.named(name))
//      .toProvider(Providers.guicify(Akka.providerOf[T](name, props)))
//      .asEagerSingleton()
//  }

  @Provides
  @Singleton
  @Inject
  def wsClient(implicit actorSystem: ActorSystem): StandaloneWSClient = {
    implicit val materializer = ActorMaterializer()
    StandaloneAhcWSClient()
  }

  @Provides
  @Singleton
  @Inject
  def buildGrpcClient(/*configurationProvider: ConfigurationProvider*/): WalletGrpc.Wallet = {
    val channel = ManagedChannelBuilder
      .forAddress("54.236.37.243", 50051)
      .usePlaintext(true)
      .build

    WalletGrpc.stub(channel)
  }

  @Provides
  @Singleton
  @Inject
  def buildSolidityClient(/*configurationProvider: ConfigurationProvider*/): WalletSolidityGrpc.WalletSolidityStub = {
    val channel = ManagedChannelBuilder
      .forAddress("39.105.66.80", 50051)
      .usePlaintext(true)
      .build

    WalletSolidityGrpc.stub(channel)
  }

}
