package com.suryawebstudio.mark;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.suryawebstudio.mark.executor.ConfirmationHasher;

import org.junit.Test;

public class ConfirmationHasherTest {
    @Test public void differentPayloadsProduceDifferentHashes() {
        String first = ConfirmationHasher.sha256("send_message", "{\"text\":\"one\"}");
        String second = ConfirmationHasher.sha256("send_message", "{\"text\":\"two\"}");
        assertNotNull(first);
        assertNotEquals(first, second);
    }
}
