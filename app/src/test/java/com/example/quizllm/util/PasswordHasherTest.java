package com.example.quizllm.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordHasherTest {
    @Test
    public void verifiesCorrectPasswordOnly() {
        String stored = PasswordHasher.hash("correct horse");
        assertTrue(PasswordHasher.verify("correct horse", stored));
        assertFalse(PasswordHasher.verify("wrong", stored));
    }

    @Test
    public void neverStoresThePasswordAndSaltsEachHash() {
        String a = PasswordHasher.hash("secret");
        String b = PasswordHasher.hash("secret");
        assertFalse(a.contains("secret"));
        assertNotEquals(a, b);
    }

    @Test
    public void rejectsMalformedStoredValues() {
        assertFalse(PasswordHasher.verify("x", null));
        assertFalse(PasswordHasher.verify("x", "secret"));          // old plain-text value
        assertFalse(PasswordHasher.verify("x", "pbkdf2$abc$!!$!!"));
    }
}
