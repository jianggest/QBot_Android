package com.happyfamliy.qbot.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.happyfamliy.qbot.data.local.dao.FactDao
import com.happyfamliy.qbot.data.local.dao.MessageDao
import com.happyfamliy.qbot.data.local.dao.SessionDao
import com.happyfamliy.qbot.data.local.entity.FactEntity
import com.happyfamliy.qbot.data.local.entity.MessageEntity
import com.happyfamliy.qbot.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], manifest = Config.NONE)
class QBotDatabaseTest {
    private lateinit var database: QBotDatabase
    private lateinit var sessionDao: SessionDao
    private lateinit var messageDao: MessageDao
    private lateinit var factDao: FactDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QBotDatabase::class.java
        ).allowMainThreadQueries().build()
        sessionDao = database.sessionDao()
        messageDao = database.messageDao()
        factDao = database.factDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testSessionInsertAndRetrieve() = runTest {
        val session = SessionEntity(title = "Test Session")
        val id = sessionDao.insertSession(session)
        assertTrue(id > 0)

        val retrieved = sessionDao.getSessionById(id)
        assertNotNull(retrieved)
        assertEquals("Test Session", retrieved?.title)
    }

    @Test
    fun testMessageInsertAndRetrieve() = runTest {
        // Create session first
        val sessionId = sessionDao.insertSession(SessionEntity(title = "Message Test"))

        // Insert message
        val message = MessageEntity(
            sessionId = sessionId,
            role = "user",
            content = "Hello there"
        )
        messageDao.insertMessage(message)

        // Retrieve messages for session
        val messages = messageDao.getMessagesForSession(sessionId).first()
        assertEquals(1, messages.size)
        assertEquals("Hello there", messages[0].content)
        assertEquals("user", messages[0].role)
    }

    @Test
    fun testFactInsertAndUpdate() = runTest {
        val fact = FactEntity(
            content = "Sky is blue",
            topic = "Nature",
            embedding = "[0.1, 0.2, 0.3]"
        )
        val id = factDao.insertFact(fact)

        var allFacts = factDao.getAllFacts().first()
        assertEquals(1, allFacts.size)

        // Update fact
        val retrieved = allFacts[0]
        factDao.updateFact(retrieved.copy(accessCount = 2))

        allFacts = factDao.getAllFactsSync()
        assertEquals(2, allFacts[0].accessCount)
    }

    @Test
    fun testCascadeDeleteSession() = runTest {
        val sessionId = sessionDao.insertSession(SessionEntity(title = "Delete Test"))
        messageDao.insertMessage(MessageEntity(sessionId = sessionId, role = "user", content = "msg 1"))
        messageDao.insertMessage(MessageEntity(sessionId = sessionId, role = "model", content = "msg 2"))

        var messages = messageDao.getMessagesForSession(sessionId).first()
        assertEquals(2, messages.size)

        // Delete session
        val session = sessionDao.getSessionById(sessionId)
        sessionDao.deleteSession(session!!)

        // Verify messages are deleted due to cascade
        messages = messageDao.getMessagesForSession(sessionId).first()
        assertTrue(messages.isEmpty())
    }
}
