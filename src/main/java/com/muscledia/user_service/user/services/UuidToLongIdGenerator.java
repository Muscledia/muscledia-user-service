package com.muscledia.user_service.user.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class UuidToLongIdGenerator {

    private final JdbcTemplate jdbcTemplate;
    private static final int MAX_COLLISION_ATTEMPTS = 10;

    // JavaScript safe integer range: -(2^53 - 1) to (2^53 - 1)
    // Using positive range: 1 to 9007199254740991 (15-16 digits max)
    private static final long MAX_SAFE_INTEGER = 9007199254740991L; // JavaScript Number.MAX_SAFE_INTEGER

    @Autowired
    public UuidToLongIdGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Generates a unique Long ID based on UUID with collision handling
     * @return UuidLongPair containing both the UUID string and Long ID
     */
    @Transactional
    public UuidLongPair generateUniqueId() {
        for (int attempt = 0; attempt < MAX_COLLISION_ATTEMPTS; attempt++) {
            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();
            Long longId = convertUuidToSafeLong(uuid);

            if (!isIdExists(longId) && !isUuidExists(uuidString)) {
                return new UuidLongPair(uuidString, longId);
            }
        }

        throw new RuntimeException("Unable to generate unique ID after " + MAX_COLLISION_ATTEMPTS + " attempts");
    }

    /**
     * Converts UUID to a JavaScript-safe Long (within Number.MAX_SAFE_INTEGER)
     */
    private Long convertUuidToSafeLong(UUID uuid) {
        try {
            // Use SHA-256 hash for better distribution
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(uuid.toString().getBytes());

            // Take first 8 bytes and convert to Long
            ByteBuffer buffer = ByteBuffer.wrap(hash, 0, 8);
            long rawLong = buffer.getLong();

            // Ensure it's positive and within JavaScript safe range
            long safeLong = Math.abs(rawLong) % MAX_SAFE_INTEGER;

            // Ensure it's not too small (at least 6 digits)
            return safeLong < 100000L ? safeLong + 100000L : safeLong;

        } catch (NoSuchAlgorithmException e) {
            // Fallback to simpler method
            return convertUuidToLongFallback(uuid);
        }
    }

    /**
     * Fallback method for UUID to Long conversion
     */
    private Long convertUuidToLongFallback(UUID uuid) {
        long mostSigBits = Math.abs(uuid.getMostSignificantBits());
        long leastSigBits = Math.abs(uuid.getLeastSignificantBits());

        // XOR and ensure within safe range
        long combined = (mostSigBits ^ leastSigBits) % MAX_SAFE_INTEGER;

        return combined < 100000L ? combined + 100000L : combined;
    }

    /**
     * Checks if the Long ID already exists in the database
     */
    private boolean isIdExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    /**
     * Checks if the UUID string already exists in the database
     */
    private boolean isUuidExists(String uuidString) {
        String sql = "SELECT COUNT(*) FROM users WHERE uuid_string = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, uuidString);
        return count != null && count > 0;
    }

    /**
     * Finds a user ID by UUID string
     */
    @Transactional(readOnly = true)
    public Long findUserIdByUuid(String uuidString) {
        String sql = "SELECT user_id FROM users WHERE uuid_string = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, uuidString);
    }

    /**
     * Finds a UUID string by user ID
     */
    @Transactional(readOnly = true)
    public String findUuidByUserId(Long userId) {
        String sql = "SELECT uuid_string FROM users WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, userId);
    }

    /**
     * Validates that a given string is a valid UUID
     */
    public boolean isValidUuid(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Converts a UUID string back to Long ID (if you know the UUID)
     */
    public Long convertUuidStringToLong(String uuidString) {
        if (!isValidUuid(uuidString)) {
            throw new IllegalArgumentException("Invalid UUID format");
        }
        UUID uuid = UUID.fromString(uuidString);
        return convertUuidToSafeLong(uuid);
    }

    /**
     * Inner class to hold both UUID string and Long ID
     */
    public static class UuidLongPair {
        private final String uuidString;
        private final Long longId;

        public UuidLongPair(String uuidString, Long longId) {
            this.uuidString = uuidString;
            this.longId = longId;
        }

        public String getUuidString() {
            return uuidString;
        }

        public Long getLongId() {
            return longId;
        }

        @Override
        public String toString() {
            return "UuidLongPair{" +
                    "uuidString='" + uuidString + '\'' +
                    ", longId=" + longId +
                    '}';
        }
    }
}
