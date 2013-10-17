package z.hol.utils.codec;

import java.security.MessageDigest;
import java.util.zip.ZipInputStream;

/**
 * DigestUtils 扩展包<br>
 * 主要添加对zip流的直接散列计算
 * @author holmes
 *
 */
public class DigestUtilsEx {
	
    /**
     * Returns an MD5 MessageDigest.
     *
     * @return An MD5 digest instance.
     * @throws RuntimeException when a {@link java.security.NoSuchAlgorithmException} is caught,
     */
    private static MessageDigest getMd5Digest() {
        return DigestUtils.getDigest("MD5");
    }

    /**
     * Returns an SHA digest.
     *
     * @return An SHA digest instance.
     * @throws RuntimeException when a {@link java.security.NoSuchAlgorithmException} is caught,
     */
    private static MessageDigest getShaDigest() {
        return DigestUtils.getDigest("SHA");
    }

    /**
     * 对Zip流的直接MD5散列
     * @param zis
     * @return
     */
	public static byte[] zipMd5(ZipInputStream zis){
		MessageDigest md = getMd5Digest();
		return DigestUtils.streamDigest(zis, md, false);
	}
	
    /**
     * 对Zip流的直接MD5散列
     * @param zis
     * @return
     */
	public static String zipMd5Hex(ZipInputStream zis){
		return new String(Hex.encodeHex(zipMd5(zis)));
	}
	
    /**
     * 对Zip流的直接SHA散列
     * @param zis
     * @return
     */
	public static byte[] zipSha(ZipInputStream zis){
		MessageDigest md = getShaDigest();
		return DigestUtils.streamDigest(zis, md, false);
	}
	
    /**
     * 对Zip流的直接SHA散列
     * @param zis
     * @return
     */
	public static String zipShaHex(ZipInputStream zis){
		return new String(Hex.encodeHex(zipSha(zis)));
	}
}
