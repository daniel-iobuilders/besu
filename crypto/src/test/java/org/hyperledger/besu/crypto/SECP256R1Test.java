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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hyperledger.besu.crypto.Hash.keccak256;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SECP256R1Test {

  protected SECP256R1 secp256R1;

  protected static String suiteStartTime = null;
  protected static String suiteName = null;

  @BeforeClass
  public static void setTestSuiteStartTime() {
    suiteStartTime =
        LocalDateTime.now(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    suiteName(SECP256K1Test.class);
    ;
  }

  @Before
  public void setUp() {
    secp256R1 = new SECP256R1();
  }

  public static void suiteName(final Class<?> clazz) {
    suiteName = clazz.getSimpleName() + "-" + suiteStartTime;
  }

  public static String suiteName() {
    return suiteName;
  }

  @Test
  public void recoverPublicKeyFromSignature() {
    final SECPPrivateKey privateKey =
        secp256R1.createPrivateKey(
            new BigInteger("c85ef7d79691fe79573b1a7064c19c1a9819ebdbd1faaab1a8ec92344438aaf4", 16));
    final KeyPair keyPair = secp256R1.createKeyPair(privateKey);

    final Bytes data = Bytes.wrap("This is an example of a signed message.".getBytes(UTF_8));
    final Bytes32 dataHash = keccak256(data);
    final SECPSignature signature = secp256R1.sign(dataHash, keyPair);

    final SECPPublicKey recoveredPublicKey =
        secp256R1.recoverPublicKeyFromSignature(dataHash, signature).get();
    assertThat(recoveredPublicKey.toString()).isEqualTo(keyPair.getPublicKey().toString());
  }

  @Test
  public void signatureGeneration() {
    final SECPPrivateKey privateKey =
        secp256R1.createPrivateKey(
            new BigInteger("80efca15d2f582aa65a7359158f58f699ff3b3c3d5a169c33323dcddce58c1c8", 16));
    final KeyPair keyPair = secp256R1.createKeyPair(privateKey);

    final Bytes data = Bytes.wrap("This is an example of a signed message.".getBytes(UTF_8));
    final Bytes32 dataHash = keccak256(data);
    final SECPSignature expectedSignature =
        secp256R1.createSignature(
            new BigInteger(
                "02203c4a8a272588165c52a18d926ee4368d1448ce576a7dda6848ef841948981227", 16),
            new BigInteger(
                "0220344ddb43192c20076cc0de45f41f43358a9116124308798849c55fb650a27062", 16),
            (byte) 1);

    final SECPSignature actualSignature = secp256R1.sign(dataHash, keyPair);
    assertThat(actualSignature).isEqualTo(expectedSignature);
  }

  @Test
  public void signatureVerification() {
    final SECPPrivateKey privateKey =
        secp256R1.createPrivateKey(
            new BigInteger("a7e8b16ad7ffa26fce80be2b0e00008018aadf1b16dea4ecc913b8c1c4f18531", 16));
    final KeyPair keyPair = secp256R1.createKeyPair(privateKey);

    final Bytes data = Bytes.wrap("This is an example of a signed message.".getBytes(UTF_8));
    final Bytes32 dataHash = keccak256(data);

    final SECPSignature signature = secp256R1.sign(dataHash, keyPair);
    assertThat(secp256R1.verify(data, signature, keyPair.getPublicKey(), Hash::keccak256)).isTrue();
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidFileThrowsInvalidKeyPairException() throws Exception {
    final File tempFile = Files.createTempFile(suiteName(), ".keypair").toFile();
    tempFile.deleteOnExit();
    Files.write(tempFile.toPath(), "not valid".getBytes(UTF_8));
    KeyPairUtil.load(tempFile);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidMultiLineFileThrowsInvalidIdException() throws Exception {
    final File tempFile = Files.createTempFile(suiteName(), ".keypair").toFile();
    tempFile.deleteOnExit();
    Files.write(tempFile.toPath(), "not\n\nvalid".getBytes(UTF_8));
    KeyPairUtil.load(tempFile);
  }
}
