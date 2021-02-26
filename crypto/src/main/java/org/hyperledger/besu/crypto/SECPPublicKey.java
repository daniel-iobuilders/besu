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
package org.hyperledger.besu.crypto;

import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;

import org.apache.tuweni.bytes.Bytes;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.math.ec.ECPoint;

public class SECPPublicKey implements java.security.PublicKey {

  private final Bytes encoded;
  private final String algorithm;

  public static SECPPublicKey create(
      final BigInteger key, final String algorithm, final int byteLength) {
    checkNotNull(key);
    return create(SECPKeyUtil.toBytes(key, byteLength), algorithm);
  }

  public static SECPPublicKey create(final Bytes encoded, final String algorithm) {
    return new SECPPublicKey(encoded, algorithm);
  }

  public static SECPPublicKey create(
      final SECPPrivateKey privateKey, final ECDomainParameters curve, final String algorithm) {

    return SECPPublicKey.create(SECPKeyUtil.toPublicKey(privateKey, curve), algorithm);
  }

  private SECPPublicKey(final Bytes encoded, final String algorithm) {
    checkNotNull(encoded);
    checkNotNull(algorithm);

    this.encoded = encoded;
    this.algorithm = algorithm;
  }

  /**
   * Returns this public key as an {@link ECPoint} of Bouncy Castle, to facilitate cryptographic
   * operations.
   *
   * @return This public key represented as an Elliptic Curve point.
   */
  public ECPoint asEcPoint(final ECDomainParameters curve) {
    // 0x04 is the prefix for uncompressed keys.
    final Bytes val = Bytes.concatenate(Bytes.of(0x04), encoded);
    return curve.getCurve().decodePoint(val.toArrayUnsafe());
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof SECPPublicKey)) {
      return false;
    }

    final SECPPublicKey that = (SECPPublicKey) other;
    return this.encoded.equals(that.encoded) && this.algorithm.equals(that.algorithm);
  }

  @Override
  public byte[] getEncoded() {
    return encoded.toArrayUnsafe();
  }

  public Bytes getEncodedBytes() {
    return encoded;
  }

  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  @Override
  public String getFormat() {
    return null;
  }

  @Override
  public int hashCode() {
    return encoded.hashCode();
  }

  @Override
  public String toString() {
    return encoded.toString();
  }
}
