/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.cluster;

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;

import com.google.common.collect.Lists;
import io.atomix.cluster.discovery.NodeDiscoveryProvider;
import io.atomix.utils.Builder;
import io.atomix.utils.net.Address;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for an {@link AtomixCluster} instance.
 * <p>
 * This builder is used to configure an {@link AtomixCluster} instance programmatically. To create a new builder, use
 * one of the {@link AtomixCluster#builder()} static methods.
 * <pre>
 *   {@code
 *   AtomixClusterBuilder builder = AtomixCluster.builder();
 *   }
 * </pre>
 * The instance is configured by calling the {@code with*} methods on this builder. Once the instance has been
 * configured, call {@link #build()} to build the instance:
 * <pre>
 *   {@code
 *   AtomixCluster cluster = AtomixCluster.builder()
 *     .withMemberId("member-1")
 *     .withAddress("localhost", 5000)
 *     .build();
 *   }
 * </pre>
 * Backing the builder is an {@link ClusterConfig} which is loaded when the builder is initially constructed. To load
 * a configuration from a file, use {@link AtomixCluster#builder(String)}.
 */
public abstract class AbstractClusterBuilder<T> implements Builder<T> {
  protected final ClusterConfig config;

  protected AbstractClusterBuilder() {
    this(new ClusterConfig());
  }

  protected AbstractClusterBuilder(ClusterConfig config) {
    this.config = checkNotNull(config);
  }

  /**
   * Sets the cluster identifier.
   * <p>
   * The cluster identifier is used to verify intra-cluster communication is taking place between nodes that are intended
   * to be part of the same cluster, e.g. if multicast discovery is used. It only needs to be configured if multiple
   * Atomix clusters are running within the same network.
   *
   * @param clusterId the cluster identifier
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withClusterId(String clusterId) {
    config.setClusterId(clusterId);
    return this;
  }

  /**
   * Sets the local member identifier.
   * <p>
   * The member identifier is an optional attribute that can be used to identify and send messages directly to this
   * node. If no member identifier is provided, a {@link java.util.UUID} based identifier will be generated.
   *
   * @param localMemberId the local member identifier
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withMemberId(String localMemberId) {
    config.getNodeConfig().setId(localMemberId);
    return this;
  }

  /**
   * Sets the local member identifier.
   * <p>
   * The member identifier is an optional attribute that can be used to identify and send messages directly to this
   * node. If no member identifier is provided, a {@link java.util.UUID} based identifier will be generated.
   *
   * @param localMemberId the local member identifier
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withMemberId(MemberId localMemberId) {
    config.getNodeConfig().setId(localMemberId);
    return this;
  }

  /**
   * Sets the member host.
   *
   * @param host the member host
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withHost(String host) {
    config.getNodeConfig().setHost(host);
    return this;
  }

  /**
   * Sets the member port.
   *
   * @param port the member port
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withPort(int port) {
    config.getNodeConfig().setPort(port);
    return this;
  }

  /**
   * Sets the member address.
   * <p>
   * The constructed {@link AtomixCluster} will bind to the given address for intra-cluster communication. The format
   * of the address can be {@code host:port} or just {@code host}.
   *
   * @param address a host:port tuple
   * @return the cluster builder
   * @throws io.atomix.utils.net.MalformedAddressException if a valid {@link Address} cannot be constructed from the arguments
   * @deprecated since 3.1. Use {@link #withHost(String)} and/or {@link #withPort(int)} instead
   */
  @Deprecated
  public AbstractClusterBuilder<T> withAddress(String address) {
    return withAddress(Address.from(address));
  }

  /**
   * Sets the member host/port.
   * <p>
   * The constructed {@link AtomixCluster} will bind to the given host/port for intra-cluster communication. The
   * provided host should be visible to other nodes in the cluster.
   *
   * @param host the host name
   * @param port the port number
   * @return the cluster builder
   * @throws io.atomix.utils.net.MalformedAddressException if a valid {@link Address} cannot be constructed from the arguments
   * @deprecated since 3.1. Use {@link #withHost(String)} and {@link #withPort(int)} instead
   */
  @Deprecated
  public AbstractClusterBuilder<T> withAddress(String host, int port) {
    return withAddress(Address.from(host, port));
  }

  /**
   * Sets the member address using local host.
   * <p>
   * The constructed {@link AtomixCluster} will bind to the given port for intra-cluster communication.
   *
   * @param port the port number
   * @return the cluster builder
   * @throws io.atomix.utils.net.MalformedAddressException if a valid {@link Address} cannot be constructed from the arguments
   * @deprecated since 3.1. Use {@link #withPort(int)} instead
   */
  @Deprecated
  public AbstractClusterBuilder<T> withAddress(int port) {
    return withAddress(Address.from(port));
  }

  /**
   * Sets the member address.
   * <p>
   * The constructed {@link AtomixCluster} will bind to the given address for intra-cluster communication. The
   * provided address should be visible to other nodes in the cluster.
   *
   * @param address the member address
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withAddress(Address address) {
    config.getNodeConfig().setAddress(address);
    return this;
  }

  /**
   * Sets the zone to which the member belongs.
   * <p>
   * The zone attribute can be used to enable zone-awareness in replication for certain primitive protocols. It is an
   * arbitrary string that should be used to group multiple nodes together by their physical location.
   *
   * @param zoneId the zone to which the member belongs
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withZoneId(String zoneId) {
    config.getNodeConfig().setZoneId(zoneId);
    return this;
  }

  /**
   * Sets the zone to which the member belongs.
   * <p>
   * The zone attribute can be used to enable zone-awareness in replication for certain primitive protocols. It is an
   * arbitrary string that should be used to group multiple nodes together by their physical location.
   *
   * @param zone the zone to which the member belongs
   * @return the cluster builder
   * @deprecated since 3.1. Use {@link #withZoneId(String)} instead
   */
  @Deprecated
  public AbstractClusterBuilder<T> withZone(String zone) {
    config.getNodeConfig().setZoneId(zone);
    return this;
  }

  /**
   * Sets the rack to which the member belongs.
   * <p>
   * The rack attribute can be used to enable rack-awareness in replication for certain primitive protocols. It is an
   * arbitrary string that should be used to group multiple nodes together by their physical location.
   *
   * @param rackId the rack to which the member belongs
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withRackId(String rackId) {
    config.getNodeConfig().setRackId(rackId);
    return this;
  }

  /**
   * Sets the rack to which the member belongs.
   * <p>
   * The rack attribute can be used to enable rack-awareness in replication for certain primitive protocols. It is an
   * arbitrary string that should be used to group multiple nodes together by their physical location.
   *
   * @param rack the rack to which the member belongs
   * @return the cluster builder
   * @deprecated since 3.1. Use {@link #withRackId(String)} instead
   */
  @Deprecated
  public AbstractClusterBuilder<T> withRack(String rack) {
    config.getNodeConfig().setRackId(rack);
    return this;
  }

  /**
   * Sets the host to which the member belongs.
   * <p>
   * The host attribute can be used to enable host-awareness in replication for certain primitive protocols. It is an
   * arbitrary string that should be used to group multiple nodes together by their physical location. Typically this
   * attribute only applies to containerized clusters.
   *
   * @param hostId the host to which the member belongs
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withHostId(String hostId) {
    config.getNodeConfig().setHostId(hostId);
    return this;
  }

  /**
   * Sets the member properties.
   * <p>
   * The properties are arbitrary settings that will be replicated along with this node's member information. Properties
   * can be used to enable other nodes to determine metadata about this node.
   *
   * @param properties the member properties
   * @return the cluster builder
   * @throws NullPointerException if the properties are null
   */
  public AbstractClusterBuilder<T> withProperties(Properties properties) {
    config.getNodeConfig().setProperties(properties);
    return this;
  }

  /**
   * Sets a property of the member.
   * <p>
   * The properties are arbitrary settings that will be replicated along with this node's member information. Properties
   * can be used to enable other nodes to determine metadata about this node.
   *
   * @param key   the property key to set
   * @param value the property value to set
   * @return the cluster builder
   * @throws NullPointerException if the property is null
   */
  public AbstractClusterBuilder<T> withProperty(String key, String value) {
    config.getNodeConfig().setProperty(key, value);
    return this;
  }

  /**
   * Sets the interface to which to bind the instance.
   *
   * @param iface the interface to which to bind the instance
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withMessagingInterface(String iface) {
    return withMessagingInterfaces(Lists.newArrayList(iface));
  }

  /**
   * Sets the interface(s) to which to bind the instance.
   *
   * @param ifaces the interface(s) to which to bind the instance
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withMessagingInterfaces(String... ifaces) {
    return withMessagingInterfaces(Lists.newArrayList(ifaces));
  }

  /**
   * Sets the interface(s) to which to bind the instance.
   *
   * @param ifaces the interface(s) to which to bind the instance
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withMessagingInterfaces(Collection<String> ifaces) {
    config.getMessagingConfig().setInterfaces(Lists.newArrayList(ifaces));
    return this;
  }

  /**
   * Sets the local port to which to bind the node.
   *
   * @param bindPort the local port to which to bind the node
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withMessagingPort(int bindPort) {
    config.getMessagingConfig().setPort(bindPort);
    return this;
  }

  /**
   * Sets the messaging connection pool size.
   * <p>
   * The node will create {@code connectionPoolSize} connections to each peer with which it regularly communicates
   * over TCP. Periodic heartbeats from cluster membership protocols will not consume pool connections. Thus, if
   * a node does not communicate with one of its peers for replication or application communication, the pool for
   * that peer should remain empty.
   *
   * @param connectionPoolSize the connection pool size
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withConnectionPoolSize(int connectionPoolSize) {
    config.getMessagingConfig().setConnectionPoolSize(connectionPoolSize);
    return this;
  }

  /**
   * Sets the cluster membership provider.
   * <p>
   * The membership provider determines how peers are located and the cluster is bootstrapped.
   *
   * @param locationProvider the membership provider
   * @return the cluster builder
   * @see io.atomix.cluster.discovery.BootstrapDiscoveryProvider
   */
  public AbstractClusterBuilder<T> withMembershipProvider(NodeDiscoveryProvider locationProvider) {
    config.setDiscoveryConfig(locationProvider.config());
    return this;
  }

  /**
   * Sets whether to broadcast member updates to all peers.
   *
   * @param broadcastUpdates whether to broadcast member updates to all peers
   * @return the protocol builder
   */
  public AbstractClusterBuilder<T> withBroadcastUpdates(boolean broadcastUpdates) {
    config.getMembershipConfig().setBroadcastUpdates(broadcastUpdates);
    return this;
  }

  /**
   * Sets whether to broadcast disputes to all peers.
   *
   * @param broadcastDisputes whether to broadcast disputes to all peers
   * @return the protocol builder
   */
  public AbstractClusterBuilder<T> withBroadcastDisputes(boolean broadcastDisputes) {
    config.getMembershipConfig().setBroadcastDisputes(broadcastDisputes);
    return this;
  }

  /**
   * Sets whether to notify a suspect node on state changes.
   *
   * @param notifySuspect whether to notify a suspect node on state changes
   * @return the protocol builder
   */
  public AbstractClusterBuilder<T> withNotifySuspect(boolean notifySuspect) {
    config.getMembershipConfig().setNotifySuspect(notifySuspect);
    return this;
  }

  /**
   * Sets the gossip interval.
   *
   * @param gossipInterval the gossip interval
   * @return the protocol builder
   */
  public AbstractClusterBuilder<T> withGossipInterval(Duration gossipInterval) {
    config.getMembershipConfig().setGossipInterval(gossipInterval);
    return this;
  }

  /**
   * Sets the gossip fanout.
   *
   * @param gossipFanout the gossip fanout
   * @return the protocol builder
   */
  public AbstractClusterBuilder<T> withGossipFanout(int gossipFanout) {
    config.getMembershipConfig().setGossipFanout(gossipFanout);
    return this;
  }

  /**
   * Sets the probe interval.
   *
   * @param probeInterval the probe interval
   * @return the protocol builder
   */
  public AbstractClusterBuilder<T> withProbeInterval(Duration probeInterval) {
    config.getMembershipConfig().setProbeInterval(probeInterval);
    return this;
  }

  /**
   * Sets the number of probes to perform on suspect members.
   *
   * @param suspectProbes the number of probes to perform on suspect members
   * @return the protocol builder
   */
  public AbstractClusterBuilder<T> withSuspectProbes(int suspectProbes) {
    config.getMembershipConfig().setSuspectProbes(suspectProbes);
    return this;
  }

  /**
   * Sets the failure timeout to use prior to phi failure detectors being populated.
   *
   * @param failureTimeout the failure timeout
   * @return the protocol builder
   */
  public AbstractClusterBuilder<T> withFailureTimeout(Duration failureTimeout) {
    config.getMembershipConfig().setFailureTimeout(failureTimeout);
    return this;
  }

  /**
   * Enables TLS for the Atomix messaging service.
   * <p>
   * The messaging service is the service through which all Atomix protocols communicate with their peers. Enabling
   * TLS for the messaging service enables TLS for all internal Atomix communication.
   * When TLS is enabled, Atomix will look for an {@code atomix.jks} file in the {@code /conf} directory unless
   * a keystore/truststore is provided.
   *
   * @return the cluster builder
   * @see #withKeyPath(String)
   * @see #withCertPath(String)
   */
  public AbstractClusterBuilder<T> withTlsEnabled() {
    return withTlsEnabled(true);
  }

  /**
   * Sets whether TLS is enabled for the Atomix messaging service.
   * <p>
   * The messaging service is the service through which all Atomix protocols communicate with their peers. Enabling
   * TLS for the messaging service enables TLS for all internal Atomix communication.
   * When TLS is enabled, Atomix will look for an {@code atomix.jks} file in the {@code /conf} directory unless
   * a keystore/truststore is provided.
   *
   * @param tlsEnabled whether to enable TLS
   * @return the cluster builder
   * @see #withKeyPath(String)
   * @see #withCertPath(String)
   */
  public AbstractClusterBuilder<T> withTlsEnabled(boolean tlsEnabled) {
    config.getMessagingConfig().getTlsConfig().setEnabled(tlsEnabled);
    return this;
  }

  /**
   * Sets the path to the certificate chain.
   *
   * @param certPath the path to the certificate chain
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withCertPath(String certPath) {
    config.getMessagingConfig().getTlsConfig().setCertPath(certPath);
    return this;
  }

  /**
   * Sets the path to the private key.
   *
   * @param keyPath the path to the private key
   * @return the cluster builder
   */
  public AbstractClusterBuilder<T> withKeyPath(String keyPath) {
    config.getMessagingConfig().getTlsConfig().setKeyPath(keyPath);
    return this;
  }
}