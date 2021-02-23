/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.chainimport.internal;

import org.hyperledger.besu.crypto.EllipticCurveSignature;
import org.hyperledger.besu.crypto.EllipticCurveSignatureFactory;
import org.hyperledger.besu.crypto.KeyPair;
import org.hyperledger.besu.crypto.PrivateKey;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.Wei;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

@JsonIgnoreProperties("comment")
public class TransactionData {

  private final long gasLimit;
  private final Wei gasPrice;
  private final Bytes data;
  private final Wei value;
  private final Optional<Address> to;
  private final PrivateKey privateKey;

  private static final EllipticCurveSignature ELLIPTIC_CURVE_SIGNATURE =
      EllipticCurveSignatureFactory.getInstance();

  @JsonCreator
  public TransactionData(
      @JsonProperty("gasLimit") final String gasLimit,
      @JsonProperty("gasPrice") final String gasPrice,
      @JsonProperty("data") final Optional<String> data,
      @JsonProperty("value") final Optional<String> value,
      @JsonProperty("to") final Optional<String> to,
      @JsonProperty("secretKey") final String secretKey) {
    this.gasLimit = UInt256.fromHexString(gasLimit).toLong();
    this.gasPrice = Wei.fromHexString(gasPrice);
    this.data = data.map(Bytes::fromHexString).orElse(Bytes.EMPTY);
    this.value = value.map(Wei::fromHexString).orElse(Wei.ZERO);
    this.to = to.map(Address::fromHexString);
    this.privateKey = ELLIPTIC_CURVE_SIGNATURE.createPrivateKey(Bytes32.fromHexString(secretKey));
  }

  public Transaction getSignedTransaction(final NonceProvider nonceProvider) {
    final KeyPair keyPair = ELLIPTIC_CURVE_SIGNATURE.createKeyPair(privateKey);

    final Address fromAddress = Address.extract(keyPair.getPublicKey());
    final long nonce = nonceProvider.get(fromAddress);
    return Transaction.builder()
        .gasLimit(gasLimit)
        .gasPrice(gasPrice)
        .nonce(nonce)
        .payload(data)
        .value(value)
        .to(to.orElse(null))
        .guessType()
        .signAndBuild(keyPair);
  }

  @FunctionalInterface
  public interface NonceProvider {
    long get(final Address address);
  }
}
