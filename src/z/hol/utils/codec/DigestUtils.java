/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package z.hol.utils.codec;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Operations to simplifiy common {@link java.security.MessageDigest} tasks.  This
 * class is thread safe.
 *
 * @author Apache Software Foundation
 */
public class DigestUtils {

    /**
     * Returns a MessageDigest for the given <code>algorithm</code>.
     *
     * @param algorithm The MessageDigest algorithm name.
     * @return An MD5 digest instance.
     * @throws RuntimeException when a {@link java.security.NoSuchAlgorithmException} is caught,
     */
    static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Returns an MD5 MessageDigest.
     *
     * @return An MD5 digest instance.
     * @throws RuntimeException when a {@link java.security.NoSuchAlgorithmException} is caught,
     */
    private static MessageDigest getMd5Digest() {
        return getDigest("MD5");
    }

    /**
     * Returns an SHA digest.
     *
     * @return An SHA digest instance.
     * @throws RuntimeException when a {@link java.security.NoSuchAlgorithmException} is caught,
     */
    private static MessageDigest getShaDigest() {
        return getDigest("SHA");
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element 
     * <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return MD5 digest
     */
    public static byte[] md5(byte[] data) {
        return getMd5Digest().digest(data);
    }

    /**
     * Calculates the MD5 digest and returns the value as a 16 element 
     * <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return MD5 digest
     */
    public static byte[] md5(String data) {
        return md5(data.getBytes());
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character 
     * hex string.
     *
     * @param data Data to digest
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(byte[] data) {
        return new String(Hex.encodeHex(md5(data)));
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character 
     * hex string.
     *
     * @param data Data to digest
     * @return MD5 digest as a hex string
     */
    public static String md5Hex(String data) {
        return new String(Hex.encodeHex(md5(data)));
    }
    
    /**
     * 计算流的MD5值
     * @param in 输入流
     * @return MD5 字节列
     */
    public static byte[] md5(InputStream in){
    	MessageDigest md = getMd5Digest();
    	return streamDigest(in, md);
    }
    
    /**
     * 计算流的MD5值
     * @param in 输入流
     * @return MD5 HEX值
     */
    public static String md5Hex(InputStream in){
    	return new String(Hex.encodeHex(md5(in)));
    }
    
    /**
     * 计算流的散列值
     * @param in
     * @param md
     * @param autoClose 是否自动关闭流
     * @return
     */
    static byte[] streamDigest(InputStream in, MessageDigest md, boolean autoClose){
    	DigestInputStream din = new DigestInputStream(in, md);
    	byte[] buff = new byte[512];
    	try{
	    	while (din.read(buff) != -1){}
    	} catch (IOException e) {
			// TODO: handle exception
    		e.printStackTrace();
		}
    	if (autoClose){
	    	try {
				din.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	buff = null;
    	return md.digest();    	
    }
    
    /**
     * like streamDigest(in, md, true)
     * @param in
     * @param md
     * @return
     */
    static byte[] streamDigest(InputStream in, MessageDigest md){
    	return streamDigest(in, md, true);
    }

    /**
     * Calculates the SHA digest and returns the value as a 
     * <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return SHA digest
     */
    public static byte[] sha(byte[] data) {
        return getShaDigest().digest(data);
    }

    /**
     * Calculates the SHA digest and returns the value as a 
     * <code>byte[]</code>.
     *
     * @param data Data to digest
     * @return SHA digest
     */
    public static byte[] sha(String data) {
        return sha(data.getBytes());
    }
    
    /**
     * 计算流的SHA值
     * @param in
     * @return SHA 字节列
     */
    public static byte[] sha(InputStream in) {
        return streamDigest(in, getShaDigest());
    }

    /**
     * Calculates the SHA digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA digest as a hex string
     */
    public static String shaHex(byte[] data) {
        return new String(Hex.encodeHex(sha(data)));
    }

    /**
     * Calculates the SHA digest and returns the value as a hex string.
     *
     * @param data Data to digest
     * @return SHA digest as a hex string
     */
    public static String shaHex(String data) {
        return new String(Hex.encodeHex(sha(data)));
    }

    /**
     * 计算流的SHA
     * @param in
     * @return SHA 的值
     */
    public static String shaHex(InputStream in) {
        return new String(Hex.encodeHex(sha(in)));
    }
}
