package com.arangodb.spark

import com.arangodb.Protocol
import com.arangodb.entity.LoadBalancingStrategy
import com.arangodb.util.ArangoSerialization

trait ArangoOptions {

  def database: String = "_system"

  def hosts: Option[String] = None

  def user: Option[String] = None

  def password: Option[String] = None

  def useSsl: Option[Boolean] = None

  def sslKeyStoreFile: Option[String] = None

  def sslPassPhrase: Option[String] = None

  def sslProtocol: Option[String] = None

  def protocol: Option[Protocol] = None

  def maxConnections: Option[Int] = None

  def acquireHostList: Option[Boolean] = None

  def acquireHostListInterval: Option[Int] = None

  def loadBalancingStrategy: Option[LoadBalancingStrategy] = None

  @transient
  def serialization : Option[ArangoSerialization] = None
}