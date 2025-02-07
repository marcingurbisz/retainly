// RetainlyTheme.kt
package com.example.retainly

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "Retainly"

// -------------------- Data Models --------------------
@Entity(tableName = "flashcard")
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val englishText: String,
    val polishTranslation: String,
    val context: String? = null,
    val created: Long = System.currentTimeMillis(),
    val nextReview: Long = System.currentTimeMillis(),
    val interval: Int = 0,
    val easeFactor: Float = 2.5f
)

// -------------------- Database --------------------
@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcard WHERE nextReview <= :currentTime ORDER BY nextReview")
    fun getDueCards(currentTime: Long): Flow<List<Flashcard>>

    @Insert
    suspend fun insertCard(flashcard: Flashcard)

    @Update
    suspend fun updateCard(flashcard: Flashcard)

    @Query("SELECT * FROM flashcard WHERE id = :id")
    suspend fun getCardById(id: Long): Flashcard?
}

@Database(entities = [Flashcard::class], version = 1)
abstract class RetainlyDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: RetainlyDatabase? = null

        fun getInstance(context: Context): RetainlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RetainlyDatabase::class.java,
                    "retainly.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// -------------------- Domain Logic --------------------
class ReviewManager @Inject constructor() {
    fun calculateNextReview(card: Flashcard, quality: Int): Flashcard {
        val newEaseFactor = when {
            quality < 3 -> card.easeFactor - 0.2f
            quality > 3 -> card.easeFactor + 0.1f
            else -> card.easeFactor
        }.coerceIn(1.3f, 2.5f)

        val newInterval = when {
            quality < 3 -> 1
            card.interval == 0 -> 1
            card.interval == 1 -> 6
            else -> (card.interval * card.easeFactor).toInt()
        }

        return card.copy(
            interval = newInterval,
            easeFactor = newEaseFactor,
            nextReview = System.currentTimeMillis() + (newInterval * 24 * 60 * 60 * 1000L)
        )
    }
}

// -------------------- Dependency Injection --------------------
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RetainlyDatabase = Room.databaseBuilder(
        context,
        RetainlyDatabase::class.java,
        "retainly.db"
    ).build()

    @Provides
    @Singleton
    fun provideFlashcardDao(database: RetainlyDatabase) = database.flashcardDao()

    @Provides
    @Singleton
    fun provideReviewManager() = ReviewManager()
}

// -------------------- ViewModel --------------------
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val flashcardDao: FlashcardDao,
    private val reviewManager: ReviewManager
) : ViewModel() {
    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val dueCards = flashcardDao.getDueCards(_currentTime.value)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        Log.d(TAG, "ReviewViewModel initialized")
    }

    fun processReview(card: Flashcard, quality: Int) {
        viewModelScope.launch {
            try {
                val updatedCard = reviewManager.calculateNextReview(card, quality)
                flashcardDao.updateCard(updatedCard)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing review", e)
            }
        }
    }
}

// -------------------- UI Components --------------------
@Composable
fun RetainlyApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ReviewScreen()
        }
    }
}

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val dueCards by viewModel.dueCards.collectAsState()
    var currentCardIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (dueCards.isEmpty()) {
            Text(
                text = "No cards due for review!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            val currentCard = dueCards[currentCardIndex]

            FlashcardReviewItem(
                card = currentCard,
                onResponse = { quality ->
                    viewModel.processReview(currentCard, quality)
                    if (currentCardIndex < dueCards.size - 1) {
                        currentCardIndex++
                    }
                }
            )
        }
    }
}

@Composable
fun FlashcardReviewItem(
    card: Flashcard,
    onResponse: (Int) -> Unit
) {
    var showAnswer by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.englishText,
                style = MaterialTheme.typography.headlineMedium
            )

            if (showAnswer) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = card.polishTranslation,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..4).forEach { quality ->
                        Button(
                            onClick = { onResponse(quality) }
                        ) {
                            Text(quality.toString())
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showAnswer = true }
                ) {
                    Text("Show Answer")
                }
            }
        }
    }
}

// -------------------- Broadcast Receiver --------------------
class RetainlyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.retainly.CREATE_CARD") {
            val englishText = intent.getStringExtra("text") ?: return
            val polishTranslation = intent.getStringExtra("translation") ?: return

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = RetainlyDatabase.getInstance(context)
                    database.flashcardDao().insertCard(
                        Flashcard(
                            englishText = englishText,
                            polishTranslation = polishTranslation
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating card", e)
                }
            }
        }
    }
}

// -------------------- Application --------------------
@HiltAndroidApp
class RetainlyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")

        try {
            setContent {
                RetainlyApp()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting content", e)
            throw e  // Rethrow to see the error in debugger
        }
    }
}