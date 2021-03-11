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

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.apache.tuweni.bytes.Bytes;
import org.bouncycastle.math.ec.ECPoint;

public interface SignatureAlgorithm {
  // needs to be known at compile time otherwise triggers InsecureCryptoUsage error
  String ALGORITHM = "ECDSA";

  void enableNative();

  SECPSignature sign(final Bytes dataHash, final KeyPair keyPair);

  boolean verify(final Bytes data, final SECPSignature signature, final SECPPublicKey pub);

  boolean verify(
      final Bytes data,
      final SECPSignature signature,
      final SECPPublicKey pub,
      final UnaryOperator<Bytes> preprocessor);

  SECPSignature normaliseSignature(
      final BigInteger nativeR,
      final BigInteger nativeS,
      final SECPPublicKey publicKey,
      final Bytes dataHash);

  Bytes calculateECDHKeyAgreement(final SECPPrivateKey privKey, final SECPPublicKey theirPubKey);

  BigInteger getHalfCurveOrder();

  String getProvider();

  String getCurveName();

  SECPPrivateKey createPrivateKey(final BigInteger key);

  SECPPrivateKey createPrivateKey(final Bytes key);

  SECPPublicKey createPublicKey(final SECPPrivateKey privateKey);

  SECPPublicKey createPublicKey(final BigInteger key);

  SECPPublicKey createPublicKey(final Bytes encoded);

  Optional<SECPPublicKey> recoverPublicKeyFromSignature(
      final Bytes dataHash, final SECPSignature signature);

  ECPoint publicKeyAsEcPoint(final SECPPublicKey publicKey);

  KeyPair createKeyPair(final SECPPrivateKey privateKey);

  KeyPair generateKeyPair();

  SECPSignature createSignature(final BigInteger r, final BigInteger s, final byte recId);

  SECPSignature decodeSignature(final Bytes bytes);

  int getPrivateKeyByteLength();

  int getPublicKeyByteLength();

  int getSignatureByteLength();
}