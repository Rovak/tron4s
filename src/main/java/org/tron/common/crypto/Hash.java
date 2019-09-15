/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */

package org.tron.common.crypto;

import org.tron.common.crypto.cryptohash.Keccak256;
import org.tron.common.crypto.cryptohash.Keccak512;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.util.Arrays.copyOfRange;

public class Hash {

  private static final MessageDigest sha256digest;

  static {

    try {
      sha256digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e); // Can't happen.
    }

  }

  /**
   * @param input - data for hashing
   * @return - sha256 hash of the data
   */
  public static byte[] sha256(byte[] input) {
    return sha256digest.digest(input);
  }

  public static byte[] sha3(byte[] input) {
    MessageDigest digest;
    digest = new Keccak256();
    digest.update(input);
    return digest.digest();
  }

  public static byte[] sha3(byte[] input1, byte[] input2) {
    MessageDigest digest;
    digest = new Keccak256();
    digest.update(input1, 0, input1.length);
    digest.update(input2, 0, input2.length);
    return digest.digest();
  }

  /**
   * hashing chunk of the data
   *
   * @param input - data for hash
   * @param start - start of hashing chunk
   * @param length - length of hashing chunk
   * @return - keccak hash of the chunk
   */
  public static byte[] sha3(byte[] input, int start, int length) {
    MessageDigest digest = new Keccak256();
    digest.update(input, start, length);
    return digest.digest();
  }

  public static byte[] sha512(byte[] input) {
    MessageDigest digest = new Keccak512();
    digest.update(input);
    return digest.digest();
  }

  /**
   * Calculates RIGTMOST160(SHA3(input)). This is used in address calculations. *
   *
   * @param input - data
   * @return - add_pre_fix + 20 right bytes of the hash keccak of the data
   */
  public static byte[] sha3omit12(byte[] input) {
    byte[] hash = sha3(input);
    byte[] address = copyOfRange(hash, 11, hash.length);
    address[0] = 0x41;
    return address;
  }
}
