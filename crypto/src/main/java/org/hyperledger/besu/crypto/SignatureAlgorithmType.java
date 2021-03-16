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

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class SignatureAlgorithmType {

  private static final Map<String, Supplier<SignatureAlgorithm>> TYPES =
      Map.of("secp256k1", SECP256K1::new,
             "secp256r1", SECP256K1::new);

  public static final Supplier<SignatureAlgorithm> DEFAULT_SIGNATURE_ALGORITHM_TYPE =
      TYPES.get("secp256k1");

  private final Supplier<SignatureAlgorithm> instantiator;

  private SignatureAlgorithmType(final Supplier<SignatureAlgorithm> instantiator) {
    this.instantiator = instantiator;
  }

  public static SignatureAlgorithmType create(final Optional<String> ecCurve) {
    if (ecCurve.isEmpty()) {
      return new SignatureAlgorithmType(DEFAULT_SIGNATURE_ALGORITHM_TYPE);
    }

    String ecCurveName = ecCurve.get();

    if (!isValidType(ecCurveName)) {
      throw new IllegalArgumentException(
          new StringBuilder()
              .append("Invalid genesis file configuration. Elliptic curve (ecCurve) ")
              .append(ecCurveName)
              .append(" is not in the list of valid elliptic curves ")
              .append(getEcCurvesListAsString())
              .toString());
    }

    return new SignatureAlgorithmType(TYPES.get(ecCurveName));
  }

  public SignatureAlgorithm getInstance() {
    return instantiator.get();
  }

  private static boolean isValidType(final String ecCurve) {
    return TYPES.containsKey(ecCurve);
  }

  private static String getEcCurvesListAsString() {
    Iterator<Map.Entry<String, Supplier<SignatureAlgorithm>>> it = TYPES.entrySet().iterator();

    StringBuilder ecCurveListBuilder = new StringBuilder();
    ecCurveListBuilder.append("[");

    while (it.hasNext()) {
      ecCurveListBuilder.append(it.next().getKey());

      if (it.hasNext()) {
        ecCurveListBuilder.append(", ");
      }
    }
    ecCurveListBuilder.append("]");

    return ecCurveListBuilder.toString();
  }
}
