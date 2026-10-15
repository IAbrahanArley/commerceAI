package br.com.commerceai.api.chat.memory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import static org.assertj.core.api.Assertions.*;

class TemporaryChatMemoryTests {

    private final MutableClock clock = new MutableClock();

    @Test
    void retainsOnlyRecentCompleteTurns() {
        var memory = memory(4, 10);
        for (int index = 1; index <= 3; index++) {
            store(memory, "s", "question-" + index, "answer-" + index);
        }
        assertThat(messages(memory, "s"))
                .containsExactly("question-2", "answer-2", "question-3", "answer-3");
    }

    @Test
    void isolatesSessionHistories() {
        var memory = memory(20, 10);
        store(memory, "first", "first-question", "first-answer");
        store(memory, "second", "second-question", "second-answer");
        assertThat(messages(memory, "first")).containsExactly("first-question", "first-answer");
        assertThat(messages(memory, "second")).containsExactly("second-question", "second-answer");
    }

    @Test
    void expiresAfterIdleTimeoutAndRefreshesExpirationOnActivity() {
        var memory = memory(20, 10);
        store(memory, "s", "question", "answer");
        clock.advance(Duration.ofMinutes(29));
        assertThat(messages(memory, "s")).hasSize(2);
        clock.advance(Duration.ofMinutes(29));
        assertThat(messages(memory, "s")).hasSize(2);
        clock.advance(Duration.ofMinutes(30));
        memory.cleanUpExpiredSessions();
        assertThat(messages(memory, "s")).isEmpty();
    }

    @Test
    void expiresOnAccessWithoutWaitingForScheduledCleanup() {
        var memory = memory(20, 10);
        store(memory, "s", "question", "answer");
        clock.advance(Duration.ofMinutes(30));
        assertThat(messages(memory, "s")).isEmpty();
    }

    @Test
    void evictsLeastRecentlyActiveIdleSessionAtCapacity() {
        var memory = memory(20, 2);
        store(memory, "first", "first-question", "first-answer");
        clock.advance(Duration.ofSeconds(1));
        store(memory, "second", "second-question", "second-answer");
        clock.advance(Duration.ofSeconds(1));
        assertThat(messages(memory, "first")).hasSize(2);
        clock.advance(Duration.ofSeconds(1));
        store(memory, "third", "third-question", "third-answer");
        assertThat(messages(memory, "first")).containsExactly("first-question", "first-answer");
        assertThat(messages(memory, "second")).isEmpty();
    }

    @Test
    void rejectsNewSessionWhenAllAreBusyAndDoesNotExpireAnActiveSession() throws Exception {
        var memory = memory(20, 1);
        store(memory, "busy", "question", "answer");
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var executor = Executors.newSingleThreadExecutor();
        try {
            var request = executor.submit(() -> memory.withSession("busy", session -> {
                entered.countDown();
                await(release);
                return session.get("busy");
            }));
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            clock.advance(Duration.ofMinutes(31));
            memory.cleanUpExpiredSessions();
            assertThatThrownBy(() -> memory.withSession("other", session -> "unexpected"))
                    .isInstanceOf(ChatMemoryCapacityException.class);
            release.countDown();
            assertThat(request.get(5, TimeUnit.SECONDS)).hasSize(2);
            assertThat(messages(memory, "busy")).hasSize(2);
            assertThat(messages(memory, "other")).isEmpty();
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void serializesCallsForSameSession() throws Exception {
        var memory = memory(20, 10);
        var entered = new CountDownLatch(1);
        var secondRequested = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var firstRunning = new AtomicBoolean(true);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = executor.submit(() -> memory.withSession("s", session -> {
                entered.countDown();
                await(release);
                session.add("s", turn("question", "answer"));
                firstRunning.set(false);
                return "done";
            }));
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> {
                secondRequested.countDown();
                return memory.withSession("s", session -> {
                    assertThat(firstRunning.get()).isFalse();
                    return session.get("s");
                });
            });
            assertThat(secondRequested.await(5, TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> second.get(200, TimeUnit.MILLISECONDS))
                    .isInstanceOf(TimeoutException.class);
            release.countDown();
            assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo("done");
            assertThat(second.get(5, TimeUnit.SECONDS)).hasSize(2);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void allowsDifferentSessionsToProceedWhileOneIsBlocked() throws Exception {
        var memory = memory(20, 10);
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var executor = Executors.newSingleThreadExecutor();
        try {
            var blocked = executor.submit(() -> memory.withSession("first", session -> {
                entered.countDown();
                await(release);
                return "done";
            }));
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();
            String result = memory.withSession("second", session -> "independent");
            assertThat(result).isEqualTo("independent");
            release.countDown();
            assertThat(blocked.get(5, TimeUnit.SECONDS)).isEqualTo("done");
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void releasesSessionAfterCallbackFailure() {
        var memory = memory(20, 1);
        assertThatThrownBy(() -> memory.withSession("failed", session -> {
            throw new IllegalStateException("failure");
        })).isInstanceOf(IllegalStateException.class);
        String result = memory.withSession("new", session -> "done");
        assertThat(result).isEqualTo("done");
    }

    private TemporaryChatMemory memory(int maxMessages, int maxSessions) {
        return new TemporaryChatMemory(new ChatMemoryProperties(maxMessages, maxSessions, Duration.ofMinutes(30)), clock);
    }

    private static List<Message> turn(String question, String answer) {
        return List.of(new UserMessage(question), new AssistantMessage(answer));
    }

    private static void store(TemporaryChatMemory memory, String id, String question, String answer) {
        memory.withSession(id, session -> {
            session.add(id, turn(question, answer));
            return null;
        });
    }

    private static List<String> messages(TemporaryChatMemory memory, String id) {
        return memory.withSession(id, session -> session.get(id).stream().map(Message::getText).toList());
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test synchronization");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private static class MutableClock extends Clock {

        private final AtomicReference<Instant> time = new AtomicReference<>(Instant.parse("2026-10-14T12:00:00Z"));

        void advance(Duration duration) {
            time.updateAndGet(instant -> instant.plus(duration));
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return time.get();
        }
    }
}
