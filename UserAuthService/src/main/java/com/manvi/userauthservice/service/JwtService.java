package  com.manvi.userauthservice.service;

import com.manvi.userauthservice.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirySeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry-seconds}") long expirySeconds) {

        /*
         * STEP 1: Generate a cryptographically secure secret using OpenSSL.
         *
         * Command:
         *     openssl rand -base64 48
         *
         * - OpenSSL generates 48 cryptographically random bytes.
         * - 48 bytes = 384 bits of key material.
         * - Base64 converts every 3 bytes into 4 printable characters.
         * - Therefore, 48 random bytes become 64 Base64 characters.
         *
         * IMPORTANT:
         * Base64 is encoding, NOT encryption.
         * It does not increase the key's randomness or security.
         *
         * Store the generated Base64 string in an environment variable:
         *
         *     JWT_SECRET=<generated-base64-secret>
         *
         * application.properties:
         *
         *     jwt.secret=${JWT_SECRET}
         *     jwt.expiry-seconds=3600
         */

        /*
         * STEP 2: Decode Base64 to recover the ORIGINAL random bytes.
         *
         * OpenSSL originally generated 48 random bytes, but we stored
         * their 64-character Base64 representation as a String.
         *
         * Decoders.BASE64.decode(secret):
         *
         *     64 Base64 characters -> 48 original random bytes
         *
         * Why not secret.getBytes(StandardCharsets.UTF_8)?
         *
         * UTF-8 conversion does NOT decode Base64.
         * It converts the 64 Base64 characters themselves into bytes.
         *
         * Since Base64 characters are ASCII:
         *
         *     secret.getBytes(UTF_8) -> 64 bytes
         *
         * These 64 bytes are DIFFERENT from the original 48 bytes.
         *
         * Therefore, when a secret is stored as Base64,
         * always decode it to recover the intended key material.
         *
        * byte[] keyBytes = Decoders.BASE64.decode(secret);  it decode the original material resulting into 48 bytes--automatic JWT algo - HS384--
        * for HS512 JJWT algo we need atleast 64 bytes so convert 64 Base64 into UT8-8 (ASCII)
       *  Because all 64 Base64 characters are ASCII characters, and each ASCII character occupies exactly one byte in UTF-8.
         * One important distinction: UTF-8 does not always produce one byte per character. Non-ASCII characters can require multiple bytes.
        */

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        /*
         * STEP 3: Convert raw key bytes into a cryptographic SecretKey.
         *
         * HMAC algorithms operate on binary key material.
         * Java cryptographic APIs represent this material using
         * the SecretKey interface.
         *
         * Keys.hmacShaKeyFor(keyBytes):
         *
         * 1. Validates that the key is long enough for HMAC-SHA JWT signing.
         * 2. Wraps the supplied bytes in a SecretKey object.
         * 3. Selects an HMAC key algorithm based on the byte length.
         *
         * It does NOT generate a new random secret.
         * It does NOT change the length of the key material.
         *
         * JJWT key algorithm selection:
         *
         *     32-47 bytes -> HmacSHA256
         *     48-63 bytes -> HmacSHA384
         *     64+ bytes   -> HmacSHA512
         *
         * Our decoded key contains 48 bytes = 384 bits.
         *
         * Therefore:
         *     key.getAlgorithm() -> "HmacSHA384"
         *     key.getEncoded().length -> 48 bytes
         *
         * With JJWT 0.12.x:
         *
         *     Jwts.builder().signWith(key)
         *
         * automatically selects HS384 for this key.
         *
         * HS384 generates a fixed 384-bit (48-byte) signature.
         * JWT then Base64URL-encodes that signature into 64 characters.
         *
         * JWT structure:
         *
         *     Base64URL(header).Base64URL(payload).Base64URL(signature)
         *
         * The same key is required to verify the HS384 signature.
         */
        this.key = Keys.hmacShaKeyFor(keyBytes);

        /*
         * STEP 4: Store the JWT expiration duration.
         *
         * Example:
         *     jwt.expiry-seconds=3600
         *
         * This means the JWT expires 3600 seconds (1 hour)
         * after its issuance time, assuming token creation uses
         * this value to calculate the expiration claim.
         *
         * A short-lived access token limits how long a stolen token
         * can be used, but expiration alone does not provide
         * immediate token revocation.
         */
        this.expirySeconds = expirySeconds;
    }


    public String generateToken(User user){

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .issuer("UserAuthService")
                .claim("role", user.getRole().name())
                .expiration(new Date(System.currentTimeMillis() + expirySeconds *1000))
                .signWith(key)
                .compact();


    }

    /*


     * Parses and validates a signed JWT and returns its claims.
     *
     * JWT structure:
     *     Base64URL(header).Base64URL(payload).Base64URL(signature)

      * IMPORTANT:
     * Decoding a JWT is NOT the same as verifying it.
     * Anyone can Base64URL-decode the header and payload.
     * Verification ensures that the JWT was signed using the expected key
     * and that its signed contents have not been modified.

     What parseSignedClaims checks, and what it throws.
     It checks the shape, the signature, and the time claims (exp, and nbf if present).
      Each failure is a subclass of JwtException: a wrong seal, an expired token, a malformed string. That is why our controller can catch one type.
       One edge it does not cover: an empty token string is refused with an IllegalArgumentException, which is not a JwtException .
        If two servers' clocks differ by a few seconds, a fresh token can look expired on one of them; the parser builder has a clockSkewSeconds(...) setting for a small allowance.
     *
     * KEY GENERATION IN OUR APPLICATION:
     *
     * 1. Generate the secret using:
     *
     *        openssl rand -base64 48
     *
     *    - OpenSSL generates 48 random bytes (384 bits).
     *    - Base64 encodes those bytes into 64 characters.
     *
     * 2. Convert the Base64 string to UTF-8 bytes:
     *
     *        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
     *
     *    - We are NOT decoding the Base64 string.
     *    - Each Base64 character occupies 1 byte in UTF-8.
     *    - Therefore, 64 characters become 64 bytes (512 bits).
     *    - These bytes differ from the original 48 random bytes.
     *
     * 3. Create the HMAC key:
     *
     *        this.key = Keys.hmacShaKeyFor(keyBytes);
     *
     *    - The resulting key contains 64 bytes (512 bits).
     *    - JJWT selects HmacSHA512 as the key algorithm.
     *    - When using .signWith(key), JJWT automatically selects HS512.
     *
     * 4. HS512 produces:
     *
     *    - A 512-bit (64-byte) HMAC signature.
     *    - An 86-character Base64URL-encoded signature.
     *
     * IMPORTANT:
     * The same key material must be used for signing and verification.
     * Base64 encoding is not encryption.
     */
    public Claims parseToken(String token) {

        return Jwts.parser()

                /*
                 * STEP 1: Configure the verification key.
                 *
                 * verifyWith(key):
                 * - Supplies our 64-byte HmacSHA512 SecretKey.
                 * - HS512 is a symmetric signing algorithm.
                 * - The same secret key is used to sign and verify JWTs.
                 * - Does not verify the token yet.
                 *
                 * During verification, JJWT calculates the expected
                 * HMAC-SHA512 signature using:
                 *
                 *   Base64URL(header) + "." + Base64URL(payload)
                 *
                 * and our SecretKey.
                 *
                 * It compares the calculated signature with the
                 * signature provided in the JWT.
                 */
                .verifyWith(key)

                /*
                 * STEP 2: Build the configured JWT parser.
                 *
                 * build():
                 * - Creates the JwtParser using the supplied key.
                 * - Does not parse or validate the JWT yet.
                 */
                .build()

                /*
                 * STEP 3: Parse and validate the signed JWT.
                 *
                 * parseSignedClaims(token):
                 * - Parses the three JWT components.
                 * - Base64URL-decodes the header, payload, and signature.
                 * - Verifies the signature using our SecretKey.
                 * - Validates expiration (exp) and not-before (nbf)
                 *   claims when present.
                 *
                 * Throws an exception if validation fails:
                 *
                 * - ExpiredJwtException: Token has expired.
                 * - SignatureException: Signature is invalid.
                 * - MalformedJwtException: Invalid JWT structure.
                 * - UnsupportedJwtException: Unsupported JWT format.
                 *
                 * Note:
                 * This does not automatically verify whether the user
                 * still exists, is active, or has been revoked.
                 */
                .parseSignedClaims(token)

                /*
                 * STEP 4: Extract the verified payload.
                 *
                 * getPayload():
                 * - Returns the Claims object.
                 *
                 * Example:
                 * {
                 *   "sub": "user123",
                 *   "iat": 1793600000,
                 *   "exp": 1793603600
                 * }
                 *
                 * Access claims using:
                 *
                 * claims.getSubject();
                 * claims.getIssuedAt();
                 * claims.getExpiration();
                 */
                .getPayload();
    }







}