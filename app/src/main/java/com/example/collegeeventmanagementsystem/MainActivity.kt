package com.example.collegeeventmanagementsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import java.time.temporal.ChronoUnit
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.collegeeventmanagementsystem.ui.theme.CollegeEventManagementSystemTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CollegeEventManagementSystemTheme {
                var currentScreen by remember { mutableStateOf("login") }
                var userRole by remember { mutableStateOf("Student") }
                var userName by remember { mutableStateOf("User") }
                var userEmail by remember { mutableStateOf("") }
                var userId by remember { mutableLongStateOf(-1L) }
                
                val allEvents = remember { mutableStateListOf<Event>() }
                val coroutineScope = rememberCoroutineScope()
                
                // Fetch events from Java backend on startup or when screen changes to dashboard
                LaunchedEffect(currentScreen) {
                    if (currentScreen == "dashboard") {
                        try {
                            val apiEvents = RetrofitClient.instance.getEvents()
                            allEvents.clear()
                            allEvents.addAll(apiEvents.map { apiEvent ->
                                    Event(
                                        id = apiEvent.id?.toIntOrNull() ?: 0,
                                        title = apiEvent.title,
                                        date = try { 
                                            LocalDate.parse(apiEvent.date ?: LocalDate.now().toString()) 
                                        } catch (e: Exception) { 
                                            LocalDate.now() 
                                        },
                                        location = apiEvent.location,
                                        description = apiEvent.description,
                                        category = apiEvent.category,
                                        imageUrl = apiEvent.imageUrl
                                    )
                            })
                        } catch (e: Exception) {
                            // Fallback to sample data if server is down
                            if (allEvents.isEmpty()) {
                                allEvents.addAll(sampleEvents)
                            }
                        }
                    }
                }

                val registeredEventIds = remember { mutableStateListOf<Int>() }
                val pendingPaymentEventIds = remember { mutableStateListOf<Int>() }
                val notifications = remember { mutableStateListOf<String>() }

                // Fetch registrations for the user
                LaunchedEffect(userId) {
                    if (userId != -1L) {
                        try {
                            val registrations = RetrofitClient.instance.getUserRegistrations(userId)
                            registeredEventIds.clear()
                            pendingPaymentEventIds.clear()
                            registrations.forEach { reg ->
                                if (reg.status == "CONFIRMED") {
                                    registeredEventIds.add(reg.eventId.toInt())
                                } else {
                                    pendingPaymentEventIds.add(reg.eventId.toInt())
                                }
                            }
                        } catch (e: Exception) {
                            // Silently fail or log
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "login" -> LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            onLoginSuccess = { role, email, name, id ->
                                userRole = role
                                userEmail = email
                                userName = name
                                userId = id
                                currentScreen = "dashboard"
                            }
                        )
                        "dashboard" -> DashboardScreen(
                            modifier = Modifier.padding(innerPadding),
                            userRole = userRole,
                            userName = userName,
                            userEmail = userEmail,
                            events = allEvents,
                            registeredEventIds = registeredEventIds,
                            pendingPaymentEventIds = pendingPaymentEventIds,
                            notifications = notifications,
                            onRegister = { id -> 
                                if (!registeredEventIds.contains(id) && !pendingPaymentEventIds.contains(id)) {
                                    coroutineScope.launch {
                                        try {
                                            RetrofitClient.instance.addRegistration(ApiRegistration(
                                                userId = userId,
                                                eventId = id.toLong(),
                                                userName = userName,
                                                userEmail = userEmail,
                                                status = "PENDING"
                                            ))
                                            pendingPaymentEventIds.add(id)
                                            val eventTitle = allEvents.find { it.id == id }?.title ?: "Event"
                                            notifications.add(0, "Pending Payment for $eventTitle")
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onConfirmPayment = { id ->
                                // Note: In this app, we'll assume confirm happens locally for the prototype, 
                                // but we should ideally update the status in the backend.
                                if (pendingPaymentEventIds.contains(id)) {
                                    coroutineScope.launch {
                                        try {
                                            // Simulated status update: In a real app we'd have an update-status endpoint
                                            // For now we re-add as CONFIRMED or let Admin do it.
                                            // Let's assume onConfirmPayment is a student action (e.g. after "I have paid")
                                            // For the prototype we just update local state.
                                            pendingPaymentEventIds.remove(id)
                                            registeredEventIds.add(id)
                                            val eventTitle = allEvents.find { it.id == id }?.title ?: "Event"
                                            notifications.add(0, "Payment Submitted for $eventTitle")
                                        } catch (e: Exception) {}
                                    }
                                }
                            },
                            onCancelRegistration = { id ->
                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.instance.cancelRegistration(userId, id.toLong())
                                        registeredEventIds.remove(id)
                                        pendingPaymentEventIds.remove(id)
                                        Toast.makeText(this@MainActivity, "Registration Cancelled", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(this@MainActivity, "Cancel Failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onAddEvent = { newEvent -> 
                                coroutineScope.launch {
                                    try {
                                        RetrofitClient.instance.addEvent(ApiEvent(
                                            title = newEvent.title,
                                            location = newEvent.location,
                                            description = newEvent.description,
                                            date = newEvent.date.toString()
                                        ))
                                        // Refresh list after adding
                                        val apiEvents = RetrofitClient.instance.getEvents()
                                        allEvents.clear()
                                        allEvents.addAll(apiEvents.map { apiEvent ->
                                            Event(
                                                id = apiEvent.id?.toIntOrNull() ?: 0,
                                                title = apiEvent.title,
                                                date = try { 
                                                    LocalDate.parse(apiEvent.date ?: LocalDate.now().toString()) 
                                                } catch (e: Exception) { 
                                                    LocalDate.now() 
                                                },
                                                location = apiEvent.location,
                                                description = apiEvent.description,
                                                category = apiEvent.category,
                                                imageUrl = apiEvent.imageUrl
                                            )
                                        })
                                        notifications.add(0, "New Event Created: ${newEvent.title}")
                                    } catch (e: Exception) {
                                        // Local fallback if server fails
                                        allEvents.add(newEvent)
                                        notifications.add(0, "Created locally (Server error): ${newEvent.title}")
                                    }
                                }
                            },
                            onDeleteEvent = { event -> allEvents.remove(event) },
                            onEditEvent = { oldEvent, newEvent ->
                                val index = allEvents.indexOf(oldEvent)
                                if (index != -1) allEvents[index] = newEvent
                            },
                            onLogout = { currentScreen = "login" },
                            onUpdateProfile = { newName -> userName = newName },
                            userId = userId
                        )
                    }
                }
            }
        }
    }
}

data class Event(
    val id: Int,
    val title: String,
    val date: LocalDate,
    val location: String,
    val description: String,
    val category: String? = null,
    val imageUrl: String? = null
)

val sampleEvents = listOf(
    // --- TECHNICAL (20 events) ---
    // 10 ARCHIVE
    Event(1, "Advanced Java Workshop", LocalDate.parse("2026-04-10"), "Main Auditorium - Slot 101", "Advanced Java concepts.", "Technical"),
    Event(2, "Python for Data Science", LocalDate.parse("2026-04-11"), "Seminar Hall A - Slot 102", "Data analysis with Python.", "Technical"),
    Event(3, "Android App Development", LocalDate.parse("2026-04-12"), "Seminar Hall B - Slot 103", "Mobile dev bootcamp.", "Technical"),
    Event(4, "Machine Learning Seminar", LocalDate.parse("2026-04-13"), "Tech Hub - Slot 104", "AI and ML intro.", "Technical"),
    Event(5, "Ethical Hacking 101", LocalDate.parse("2026-04-14"), "Innovation Lab - Slot 105", "Cybersecurity basics.", "Technical"),
    Event(6, "Cloud Computing Bootcamp", LocalDate.parse("2026-04-15"), "Student Center - Slot 106", "AWS and Azure.", "Technical"),
    Event(7, "Blockchain Fundamentals", LocalDate.parse("2026-04-16"), "College Green - Slot 107", "Crypto and Web3.", "Technical"),
    Event(8, "Competitive Programming", LocalDate.parse("2026-04-17"), "Sports Complex - Slot 108", "Algorithm training.", "Technical"),
    Event(9, "Full Stack Web Dev", LocalDate.parse("2026-04-18"), "Open Air Theater - Slot 109", "MERN stack.", "Technical"),
    Event(10, "UI/UX Design Sprint", LocalDate.parse("2026-04-19"), "Conference Room - Slot 110", "Figma and design.", "Technical"),
    // 10 CURRENT (on or after 2026-04-23)
    Event(11, "IoT Project Expo", LocalDate.parse("2026-04-23"), "Main Auditorium - Slot 111", "Smart devices.", "Technical"),
    Event(12, "AR/VR Hands-on", LocalDate.parse("2026-04-24"), "Seminar Hall A - Slot 112", "Unity and VR.", "Technical"),
    Event(13, "DevOps with Docker", LocalDate.parse("2026-04-25"), "Seminar Hall B - Slot 113", "CI/CD pipelines.", "Technical"),
    Event(14, "Cybersecurity Drill", LocalDate.parse("2026-04-26"), "Tech Hub - Slot 114", "Network defense.", "Technical"),
    Event(15, "Big Data Analytics", LocalDate.parse("2026-04-27"), "Innovation Lab - Slot 115", "Hadoop and Spark.", "Technical"),
    Event(16, "Embedded Systems Lab", LocalDate.parse("2026-04-28"), "Student Center - Slot 116", "Arduino and Pi.", "Technical"),
    Event(17, "Natural Language Processing", LocalDate.parse("2026-04-29"), "College Green - Slot 117", "NLP with Python.", "Technical"),
    Event(18, "Game Dev Workshop", LocalDate.parse("2026-04-30"), "Sports Complex - Slot 118", "Unreal Engine.", "Technical"),
    Event(19, "Linux Kernel Internals", LocalDate.parse("2026-05-01"), "Open Air Theater - Slot 119", "OS deep dive.", "Technical"),
    Event(20, "Tech Paper Presentation", LocalDate.parse("2026-05-02"), "Conference Room - Slot 120", "Research showcase.", "Technical"),

    // --- FUN (20 events) ---
    // 10 ARCHIVE
    Event(21, "LAN Gaming - CS:GO", LocalDate.parse("2026-04-10"), "Main Auditorium - Slot 121", "Esports tourney.", "Fun"),
    Event(22, "LAN Gaming - Valorant", LocalDate.parse("2026-04-11"), "Seminar Hall A - Slot 122", "FPS competition.", "Fun"),
    Event(23, "Tech Trivia Night", LocalDate.parse("2026-04-12"), "Seminar Hall B - Slot 123", "Geeky quiz.", "Fun"),
    Event(24, "Code-Golf Challenge", LocalDate.parse("2026-04-13"), "Tech Hub - Slot 124", "Shortest code wins.", "Fun"),
    Event(25, "Geeky Scavenger Hunt", LocalDate.parse("2026-04-14"), "Innovation Lab - Slot 125", "Campus clues.", "Fun"),
    Event(26, "Retro Console Night", LocalDate.parse("2026-04-15"), "Student Center - Slot 126", "Classic games.", "Fun"),
    Event(27, "Board Game Bonanza", LocalDate.parse("2026-04-16"), "College Green - Slot 127", "Strategy games.", "Fun"),
    Event(28, "Movie Night: The Social Network", LocalDate.parse("2026-04-17"), "Sports Complex - Slot 128", "Coding biopic.", "Fun"),
    Event(29, "Meme-making Contest", LocalDate.parse("2026-04-18"), "Open Air Theater - Slot 129", "Digital humor.", "Fun"),
    Event(30, "Photography Walk", LocalDate.parse("2026-04-19"), "Conference Room - Slot 130", "Snap the campus.", "Fun"),
    // 10 CURRENT
    Event(31, "Karaoke Lounge", LocalDate.parse("2026-04-23"), "Main Auditorium - Slot 131", "Sing your heart out.", "Fun"),
    Event(32, "Magic & Illusion Show", LocalDate.parse("2026-04-24"), "Seminar Hall A - Slot 132", "Mind-bending magic.", "Fun"),
    Event(33, "Escape Room Challenge", LocalDate.parse("2026-04-25"), "Seminar Hall B - Slot 133", "Solve the room.", "Fun"),
    Event(34, "Blind Coding Contest", LocalDate.parse("2026-04-26"), "Tech Hub - Slot 134", "Screen-off challenge.", "Fun"),
    Event(35, "Pixel Art Workshop", LocalDate.parse("2026-04-27"), "Innovation Lab - Slot 135", "Digital drawing.", "Fun"),
    Event(36, "Standup Comedy Hour", LocalDate.parse("2026-04-28"), "Student Center - Slot 136", "Evening of laughs.", "Fun"),
    Event(37, "VR Gaming Tournament", LocalDate.parse("2026-04-29"), "College Green - Slot 137", "Immersive gaming.", "Fun"),
    Event(38, "Dum Charades (Tech Edition)", LocalDate.parse("2026-04-30"), "Sports Complex - Slot 138", "Acting tech terms.", "Fun"),
    Event(39, "Open Mic Night", LocalDate.parse("2026-05-01"), "Open Air Theater - Slot 139", "Show your talent.", "Fun"),
    Event(40, "Campus Photography", LocalDate.parse("2026-05-02"), "Conference Room - Slot 140", "Photography event.", "Fun"),

    // --- SPORTS (20 events) ---
    // 10 ARCHIVE
    Event(41, "Inter-branch Cricket", LocalDate.parse("2026-04-10"), "College Green - Slot 141", "Cricket match.", "Sports"),
    Event(42, "Football Finals", LocalDate.parse("2026-04-11"), "Sports Complex - Slot 142", "Soccer tournament.", "Sports"),
    Event(43, "Basketball Shootout", LocalDate.parse("2026-04-12"), "Student Center - Slot 143", "Hoops challenge.", "Sports"),
    Event(44, "Badminton Singles", LocalDate.parse("2026-04-13"), "Sports Complex - Slot 144", "Smash it.", "Sports"),
    Event(45, "Table Tennis Clash", LocalDate.parse("2026-04-14"), "Common Room - Slot 145", "Ping pong battle.", "Sports"),
    Event(46, "Rapid Chess Tournament", LocalDate.parse("2026-04-15"), "Library Hall - Slot 146", "Mind sports.", "Sports"),
    Event(47, "Volleyball League", LocalDate.parse("2026-04-16"), "Sports Court - Slot 147", "Team spikes.", "Sports"),
    Event(48, "Kabaddi Championship", LocalDate.parse("2026-04-17"), "Open Field - Slot 148", "Traditional sport.", "Sports"),
    Event(49, "Lawn Tennis Open", LocalDate.parse("2026-04-18"), "Tennis Court - Slot 149", "Racket battle.", "Sports"),
    Event(50, "Athletics: 100m Sprint", LocalDate.parse("2026-04-19"), "Track Field - Slot 150", "Fastest student.", "Sports"),
    // 10 CURRENT
    Event(51, "Marathon for Tech", LocalDate.parse("2026-04-23"), "Campus Perimeter - Slot 151", "5K Run.", "Sports"),
    Event(52, "Swimming Gala", LocalDate.parse("2026-04-24"), "Pool Area - Slot 152", "Water races.", "Sports"),
    Event(53, "Carrom Tournament", LocalDate.parse("2026-04-25"), "Student Center - Slot 153", "Board battle.", "Sports"),
    Event(54, "Powerlifting Meet", LocalDate.parse("2026-04-26"), "Gym Hall - Slot 154", "Strength test.", "Sports"),
    Event(55, "Archery Contest", LocalDate.parse("2026-04-27"), "East Wing - Slot 155", "Aim and shoot.", "Sports"),
    Event(56, "Cycling Expedition", LocalDate.parse("2026-04-28"), "Green Track - Slot 156", "Nature ride.", "Sports"),
    Event(57, "Tug of War (CE vs IT)", LocalDate.parse("2026-04-29"), "College Green - Slot 157", "Branch rivalry.", "Sports"),
    Event(58, "E-Sports: FIFA 24", LocalDate.parse("2026-04-30"), "Gaming Den - Slot 158", "Virtual football.", "Sports"),
    Event(59, "E-Sports: Dota 2", LocalDate.parse("2026-05-01"), "Tech Hub - Slot 159", "MOBA clash.", "Sports"),
    Event(60, "Yoga & Wellness", LocalDate.parse("2026-05-02"), "Innovation Lab - Slot 160", "Mindfulness.", "Sports"),

    // --- CULTURAL (20 events) ---
    // 10 ARCHIVE
    Event(61, "Classical Dance Night", LocalDate.parse("2026-04-10"), "Main Auditorium - Slot 161", "Graceful performance.", "Cultural"),
    Event(62, "Battle of Bands", LocalDate.parse("2026-04-11"), "Open Air Theater - Slot 162", "Rock it out.", "Cultural"),
    Event(63, "Street Play (Nukkad Natak)", LocalDate.parse("2026-04-12"), "Entrance Plaza - Slot 163", "Social awareness.", "Cultural"),
    Event(64, "Poetry Slam Session", LocalDate.parse("2026-04-13"), "Seminar Hall A - Slot 164", "Spoken word.", "Cultural"),
    Event(65, "Folk Dance Showcase", LocalDate.parse("2026-04-14"), "Main Stage - Slot 165", "Traditional rhythms.", "Cultural"),
    Event(66, "Singing Idol (Solo)", LocalDate.parse("2026-04-15"), "Music Room - Slot 166", "Vocal talent.", "Cultural"),
    Event(67, "Annual Fashion Show", LocalDate.parse("2026-04-16"), "Grand Hall - Slot 167", "Style on ramp.", "Cultural"),
    Event(68, "Canvas Painting Contest", LocalDate.parse("2026-04-17"), "Art Studio - Slot 168", "Brush and colors.", "Cultural"),
    Event(69, "Clay Sculpting", LocalDate.parse("2026-04-18"), "Design Lab - Slot 169", "Shape it up.", "Cultural"),
    Event(70, "Heritage Walk", LocalDate.parse("2026-04-19"), "City Center - Slot 170", "Local history.", "Cultural"),
    // 10 CURRENT
    Event(71, "Instrumental Music Solo", LocalDate.parse("2026-04-23"), "Auditorium - Slot 171", "Melody and strings.", "Cultural"),
    Event(72, "Mime Act Performance", LocalDate.parse("2026-04-24"), "Black Box - Slot 172", "Silent drama.", "Cultural"),
    Event(73, "Rangoli Design Comp", LocalDate.parse("2026-04-25"), "Main Foyer - Slot 173", "Colorful art.", "Cultural"),
    Event(74, "Mehendi Art Workshop", LocalDate.parse("2026-04-26"), "Common Room - Slot 174", "Henna designs.", "Cultural"),
    Event(75, "Skit & Drama Night", LocalDate.parse("2026-04-27"), "Main Stage - Slot 175", "Theatrical plays.", "Cultural"),
    Event(76, "Traditional Attire Day", LocalDate.parse("2026-04-28"), "Campus Wide - Slot 176", "Ethical wear.", "Cultural"),
    Event(77, "Cultural Fusion Fest", LocalDate.parse("2026-04-29"), "Grand Plaza - Slot 177", "Mix of traditions.", "Cultural"),
    Event(78, "Beatboxing Battle", LocalDate.parse("2026-04-30"), "Tech Hub - Slot 178", "Vocal percussion.", "Cultural"),
    Event(79, "Photography Exhibition", LocalDate.parse("2026-05-01"), "Art Gallery - Slot 179", "Visual stories.", "Cultural"),
    Event(80, "Lit-Fest Debate", LocalDate.parse("2026-05-02"), "Seminar Hall B - Slot 180", "Word war.", "Cultural")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    userRole: String = "Student",
    userName: String = "User",
    userEmail: String = "",
    events: List<Event>,
    registeredEventIds: List<Int>,
    pendingPaymentEventIds: List<Int>,
    notifications: List<String>,
    onRegister: (Int) -> Unit,
    onConfirmPayment: (Int) -> Unit,
    onCancelRegistration: (Int) -> Unit,
    onAddEvent: (Event) -> Unit,
    onDeleteEvent: (Event) -> Unit,
    onEditEvent: (Event, Event) -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (String) -> Unit,
    userId: Long
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<Event?>(null) }
    var showProfile by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showOnlinePaymentForm by remember { mutableStateOf(false) }
    var showQRPayment by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var selectedEventId by remember { mutableStateOf<Int?>(null) }
    var showCertificateDialog by remember { mutableStateOf<ApiRegistration?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    val tabs = if (userRole == "Student") {
        listOf("all event", "current event", "past event", "my registration", "rating board")
    } else {
        listOf("all event", "current event", "past event", "rating board")
    }

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // --- SIDEBAR ---
            Column(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF1A1C1E))
                    .padding(20.dp)
            ) {
                Text(
                    text = "CollegeEvents",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // User Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2F33))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Active account", fontSize = 10.sp, color = Color.Gray)
                        Text(text = userEmail, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        TextButton(
                            onClick = { showProfile = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Profile", color = Color(0xFF764BA2), fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "STUDENT PORTAL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Menu Items
                Surface(
                    onClick = { selectedTab = 0 },
                    color = if (selectedTab == 0) Color(0xFF2C2F33) else Color.Transparent,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Browse Events", color = Color.White, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // Enrollment Stats
                if (userRole == "Student") {
                    Text(text = "My Enrollment Stats", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2F33))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Confirmed", fontSize = 12.sp, color = Color.White)
                                Badge(containerColor = Color(0xFF4CAF50)) { Text(registeredEventIds.size.toString()) }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Pending", fontSize = 12.sp, color = Color.White)
                                Badge(containerColor = Color(0xFFFF9800)) { Text(pendingPaymentEventIds.size.toString()) }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Total Amount", fontSize = 11.sp, color = Color.Gray)
                                Text(text = "₹${(registeredEventIds.size + pendingPaymentEventIds.size) * 100}", fontSize = 12.sp, color = Color(0xFF03DAC5), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Sign Out
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2F33)),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Sign Out", color = Color.White)
                }
            }

            // --- MAIN CONTENT ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFF5F7FA))
            ) {
                // Top Bar with Search
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search events by name or location...") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF764BA2)
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    BadgedBox(badge = { if (notifications.isNotEmpty()) Badge { Text(notifications.size.toString()) } }) {
                        IconButton(onClick = { showNotifications = true }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Gray)
                        }
                    }
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF764BA2)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    text = if (title == "rating board") "⭐ $title" else title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                // Categories
                var selectedCategory by remember { mutableStateOf("All Types") }
                val categories = listOf("All Types", "Technical", "Fun", "Sports", "Cultural")

                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        val cat = categories[index]
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (cat == "Technical") Color(0xFF6200EE) else Color(0xFF764BA2),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                val pivotDate = LocalDate.parse("2026-04-23")

                val filteredEvents = when (selectedTab) {
                    0 -> events // All
                    1 -> events.filter { !it.date.isBefore(pivotDate) } // Current
                    2 -> events.filter { it.date.isBefore(pivotDate) } // Archive
                    3 -> if (userRole == "Student") events.filter { registeredEventIds.contains(it.id) || pendingPaymentEventIds.contains(it.id) } else emptyList() // My Events
                    else -> emptyList()
                }.filter { 
                    (it.title.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true)) &&
                    (selectedCategory == "All Types" || it.category == selectedCategory)
                }.sortedBy { it.date.isBefore(pivotDate) }

                if (selectedTab == (tabs.size - 1)) {
                    RatingBoardScreen(userName = userName)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredEvents) { event ->
                            val isRegistered = registeredEventIds.contains(event.id)
                            val isPending = pendingPaymentEventIds.contains(event.id)
                            val isArchive = event.date.isBefore(pivotDate)
                            EventItem(
                                event = event,
                                isAdmin = userRole == "Admin",
                                showRegister = userRole == "Student" && !isArchive && !isRegistered && !isPending,
                                isRegistered = isRegistered,
                                isPending = isPending,
                                showCancel = userRole == "Student" && !isArchive && (isRegistered || isPending),
                                showCertificate = isRegistered && isArchive,
                                onRegister = { 
                                    selectedEventId = event.id
                                    showPaymentDialog = true 
                                },
                                onCancel = { onCancelRegistration(event.id) },
                                onEdit = { editingEvent = event },
                                onDelete = { onDeleteEvent(event) },
                                onDownloadCertificate = { 
                                    coroutineScope.launch {
                                        try {
                                            val regs = RetrofitClient.instance.getUserRegistrations(userId)
                                            val reg = regs.find { it.eventId == event.id.toLong() }
                                            showCertificateDialog = reg
                                        } catch (e: Exception) {}
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showCertificateDialog != null) {
            val reg = showCertificateDialog!!
            val event = events.find { it.id == reg.eventId.toInt() }
            CertificateDialog(
                registration = reg,
                eventTitle = event?.title ?: "Event",
                onDismiss = { showCertificateDialog = null }
            )
        }

    // Dialogs ... (rest of the code remains same)

        if (userRole == "Admin") {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Event")
            }
        }

        if (showCreateDialog) {
            CreateEventDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { title, desc, loc ->
                    onAddEvent(Event(events.size + 1, title, LocalDate.now().plusDays(1), loc, desc))
                    showCreateDialog = false
                }
            )
        }

        editingEvent?.let { event ->
            EditEventDialog(
                event = event,
                onDismiss = { editingEvent = null },
                onConfirm = { title, desc, loc ->
                    onEditEvent(event, event.copy(title = title, description = desc, location = loc))
                    editingEvent = null
                }
            )
        }

        if (showProfile) {
            ProfileDialog(
                id = userId,
                name = userName,
                email = userEmail,
                role = userRole,
                onDismiss = { showProfile = false },
                onLogout = { 
                    showProfile = false
                    onLogout() 
                },
                onUpdate = { newName, newEmail -> 
                    userName = newName
                    userEmail = newEmail
                }
            )
        }

        if (showNotifications) {
            NotificationsDialog(
                notifications = notifications,
                onDismiss = { showNotifications = false }
            )
        }

        if (showPaymentDialog) {
            PaymentDialog(
                onDismiss = { showPaymentDialog = false },
                onPaymentSelected = { method ->
                    when (method) {
                        "Online" -> {
                            showPaymentDialog = false
                            showOnlinePaymentForm = true
                        }
                        "QR" -> {
                            showPaymentDialog = false
                            showQRPayment = true
                        }
                        else -> {
                            showPaymentDialog = false
                            showReceiptDialog = true
                        }
                    }
                }
            )
        }

        if (showQRPayment) {
            QRPaymentDialog(
                onDismiss = { showQRPayment = false },
                onPaymentSuccess = {
                    selectedEventId?.let { onRegister(it) }
                    showQRPayment = false
                }
            )
        }

        if (showOnlinePaymentForm) {
            val event = events.find { it.id == selectedEventId }
            OnlinePaymentDialog(
                eventTitle = event?.title ?: "Event",
                onDismiss = { showOnlinePaymentForm = false },
                onPaymentSuccess = {
                    selectedEventId?.let { onRegister(it) }
                    showOnlinePaymentForm = false
                }
            )
        }

        if (showReceiptDialog) {
            val event = events.find { it.id == selectedEventId }
            ReceiptDialog(
                eventTitle = event?.title ?: "Event",
                onDismiss = { showReceiptDialog = false },
                onConfirm = {
                    selectedEventId?.let { onRegister(it) }
                    showReceiptDialog = false
                }
            )
        }
    }
}

@Composable
fun OnlinePaymentDialog(eventTitle: String, onDismiss: () -> Unit, onPaymentSuccess: () -> Unit) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFF6200EE))
                Spacer(Modifier.width(8.dp))
                Text("Secure Online Payment") 
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Registering for: $eventTitle", fontWeight = FontWeight.Bold)
                Text("Amount: ₹100.00", color = Color.DarkGray)
                
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { if (it.length <= 16) cardNumber = it },
                    label = { Text("Card Number (16 digits)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null) }
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expiry,
                        onValueChange = { if (it.length <= 5) expiry = it },
                        label = { Text("MM/YY") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 3) cvv = it },
                        label = { Text("CVV") },
                        modifier = Modifier.weight(1f),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Processing transaction...", fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cardNumber.length == 16 && cvv.length == 3) {
                        isLoading = true
                        // Simulate a network delay for the transaction
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isLoading = false
                            Toast.makeText(context, "Payment Successful!", Toast.LENGTH_LONG).show()
                            onPaymentSuccess()
                        }, 2000)
                    } else {
                        Toast.makeText(context, "Please enter valid card details", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Pay Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
        }
    )
}

@Composable
fun QRPaymentDialog(onDismiss: () -> Unit, onPaymentSuccess: () -> Unit) {
    val context = LocalContext.current
    val upiId = "7862869695@upi"
    val upiUrl = "upi://pay?pa=$upiId&pn=CollegeEventSystem&am=100.00&cu=INR"
    val qrUrl = "https://chart.googleapis.com/chart?chs=500x500&cht=qr&chl=${android.net.Uri.encode(upiUrl)}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan & Pay with UPI") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Scan this QR using GPay, PhonePe, or Paytm", fontSize = 14.sp)
                
                // Using AsyncImage or similar would be better, but for simulation we use a placeholder 
                // In a real app, you'd use a library like Coil to load the URL
                Card(
                    modifier = Modifier.size(200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        // Simulating the QR Image load
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Gray)
                        Text("QR: $upiId", modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp), fontSize = 10.sp)
                    }
                }
                
                Text("Amount: ₹100.00", fontWeight = FontWeight.Bold)
                Text("UPI ID: $upiId", fontSize = 12.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = {
                Toast.makeText(context, "Verifying Payment...", Toast.LENGTH_SHORT).show()
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    onPaymentSuccess()
                }, 1500)
            }) {
                Text("I have paid")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PaymentDialog(onDismiss: () -> Unit, onPaymentSelected: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Payment Method") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onPaymentSelected("Online") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pay Online (Card)")
                }
                Button(
                    onClick = { onPaymentSelected("QR") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("Pay via UPI (QR Code)", color = Color.White)
                }
                Button(
                    onClick = { onPaymentSelected("Cash") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF764BA2))
                ) {
                    Text("Pay at Event Desk (Cash)", color = Color.White)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReceiptDialog(eventTitle: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registration Receipt") },
        text = {
            Column {
                Text("Event: $eventTitle", fontWeight = FontWeight.Bold)
                Text("Status: Payment Pending (Cash)")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please download this receipt and show it at the event desk.")
            }
        },
        confirmButton = {
            Button(onClick = {
                Toast.makeText(context, "Receipt Downloaded to Gallery", Toast.LENGTH_SHORT).show()
                onConfirm()
            }) { 
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Download Receipt") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Red) }
        }
    )
}

@Composable
fun ProfileDialog(
    id: Long,
    name: String,
    email: String,
    role: String,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    onUpdate: (String) -> Unit
) {
    var editName by remember { mutableStateOf(name) }
    var editEmail by remember { mutableStateOf(email) }
    var editPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isUpdating by remember { mutableStateOf(false) }

    // Use a custom dialog style or wrap content in a scrollable column to ensure visibility
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f),
        title = { Text("Profile Settings & Certificates", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(0xFFF3E5F5),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF6200EE))
                        Spacer(Modifier.width(8.dp))
                        Text("Institute: SSASIT", fontWeight = FontWeight.Bold, color = Color(0xFF6200EE))
                    }
                }

                Text("Role: $role", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                
                OutlinedTextField(
                    value = editName, 
                    onValueChange = { editName = it }, 
                    label = { Text("Full Name") }, 
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                
                OutlinedTextField(
                    value = editEmail, 
                    onValueChange = { editEmail = it }, 
                    label = { Text("Email Address") }, 
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                )

                OutlinedTextField(
                    value = editPassword, 
                    onValueChange = { editPassword = it }, 
                    label = { Text("New Password (optional)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                )
                
                if (isUpdating) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isUpdating = true
                            try {
                                RetrofitClient.instance.updateProfile(ApiUser(
                                    id = id,
                                    name = editName,
                                    email = editEmail,
                                    password = if (editPassword.isNotEmpty()) editPassword else null
                                ))
                                onUpdate(editName)
                                Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Update Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isUpdating = false
                            }
                        }
                    }, 
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdating,
                    shape = MaterialTheme.shapes.medium
                ) { 
                    Text("Save Changes") 
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 2.dp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF4CAF50))
                    Spacer(Modifier.width(8.dp))
                    Text("SSASIT Certificates (12)", fontWeight = FontWeight.Bold, color = Color.Black)
                }
                
                // Using Column instead of LazyColumn inside a scrollable Column to avoid nested scrolling issues
                val ssasitEvents = listOf(
                    "Web-A-Thon 2024", "C-Programming Quiz", "SSASIT Hackathon", 
                    "Data Structures Masterclass", "Tech Expo 2023", "Bridge Design Challenge",
                    "UI/UX Workshop", "Python Automation", "AI Ethics Seminar",
                    "Cloud Infra Training", "IoT Innovation Lab", "Java Backend Dev"
                )
                
                ssasitEvents.forEach { eventTitle ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(eventTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Verified by SSASIT Authority", fontSize = 10.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { 
                                Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF6200EE), modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onLogout) { Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CertificateDialog(registration: ApiRegistration, eventTitle: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val certId = registration.certificateId ?: "CERT-${System.currentTimeMillis() % 100000}"
    val role = registration.eventRole ?: "Participant"
    val qrUrl = "https://chart.googleapis.com/chart?chs=300x300&cht=qr&chl=SSASIT_VERIFIED_${certId}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("E-Certificate", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp).border(2.dp, Color(0xFF764BA2)).padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SSASIT", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF764BA2))
                        Text("CERTIFICATE OF ACHIEVEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Text("This is to certify that", fontSize = 10.sp)
                        Text(registration.userName ?: "Student", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("has successfully served as", fontSize = 10.sp)
                        Text(role, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text("in $eventTitle", fontSize = 12.sp)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("ID: $certId", fontSize = 10.sp, color = Color.Gray)
                        Text("Date: ${LocalDate.now()}", fontSize = 10.sp, color = Color.Gray)
                    }
                    // QR Placeholder
                    Box(modifier = Modifier.size(60.dp).background(Color.White).border(1.dp, Color.LightGray)) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.fillMaxSize().padding(4.dp), tint = Color.Black)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                Text("Scan to verify authenticity", fontSize = 8.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(onClick = {
                Toast.makeText(context, "Certificate Saved to Downloads", Toast.LENGTH_SHORT).show()
                onDismiss()
            }) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Download PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun RatingBoardScreen(userName: String) {
    val memories = remember { mutableStateListOf<ApiMemory>() }
    var showAddDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val fetched = RetrofitClient.instance.getMemories()
            memories.clear()
            memories.addAll(fetched)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load memories", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (memories.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Text("No memories shared yet. Be the first!", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(memories) { memory ->
                    MemoryCard(memory)
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFF764BA2),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Memory")
        }

        if (showAddDialog) {
            AddMemoryDialog(
                userName = userName,
                onDismiss = { showAddDialog = false },
                onAdded = { newMemory ->
                    coroutineScope.launch {
                        try {
                            val saved = RetrofitClient.instance.addMemory(newMemory)
                            memories.add(0, saved)
                            showAddDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to post memory", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MemoryCard(memory: ApiMemory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color(0xFFF0F0F0))) {
                // In a real app, use Coil to load Base64 image
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.align(Alignment.Center), tint = Color.LightGray)
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(memory.eventTitle ?: "Event Memory", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("by ${memory.userName}", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (i < memory.rating) Color(0xFFFFD700) else Color.LightGray
                        )
                    }
                }
                Text(memory.thought ?: "", fontSize = 12.sp, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun AddMemoryDialog(userName: String, onDismiss: () -> Unit, onAdded: (ApiMemory) -> Unit) {
    var eventTitle by remember { mutableStateOf("") }
    var thought by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Share a Memory") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = eventTitle, onValueChange = { eventTitle = it }, label = { Text("Event Name") })
                OutlinedTextField(value = thought, onValueChange = { thought = it }, label = { Text("Your Thought") })
                Text("Rating: $rating/5")
                Slider(value = rating.toFloat(), onValueChange = { rating = it.toInt() }, valueRange = 1f..5f, steps = 3)
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdded(ApiMemory(userName = userName, eventTitle = eventTitle, thought = thought, rating = rating))
            }) { Text("Post") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(1) } // 1: Email, 2: OTP & New Password
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == 1) "Forgot Password" else "Reset Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (step == 1) {
                    Text("Enter your email to receive a password reset OTP.")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                } else {
                    Text("Enter the OTP sent to your email and your new password.")
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("4-digit OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            if (step == 1) {
                                val response = RetrofitClient.instance.forgotPassword(mapOf("email" to email))
                                if (response.contains("Success", ignoreCase = true)) {
                                    step = 2
                                    Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, response, Toast.LENGTH_LONG).show()
                                }
                            } else {
                                val response = RetrofitClient.instance.resetPassword(mapOf(
                                    "email" to email,
                                    "otp" to otp,
                                    "newPassword" to newPassword
                                ))
                                Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (step == 1) "Send OTP" else "Reset Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
        }
    )
}

@Composable
fun NotificationsDialog(notifications: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications") },
        text = {
            LazyColumn(modifier = Modifier.height(300.dp)) {
                if (notifications.isEmpty()) {
                    item { Text("No new notifications", modifier = Modifier.padding(16.dp)) }
                } else {
                    items(notifications) { msg ->
                        Column {
                            Text(msg, modifier = Modifier.padding(vertical = 8.dp))
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
fun EditEventDialog(event: Event, onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf(event.title) }
    var desc by remember { mutableStateOf(event.description) }
    var loc by remember { mutableStateOf(event.location) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") })
                OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, desc, loc) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateEventDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") })
                OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("Location") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, desc, loc) }) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EventItem(
    event: Event,
    isAdmin: Boolean = false,
    showRegister: Boolean = true,
    isRegistered: Boolean = false,
    isPending: Boolean = false,
    showCancel: Boolean = false,
    showCertificate: Boolean = false,
    onRegister: () -> Unit = {},
    onCancel: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onDownloadCertificate: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Event Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFEEEEEE))
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).align(Alignment.Center),
                    tint = Color.LightGray
                )
                
                // Category Badge Overlay
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = when (event.category) {
                            "Technical" -> Color(0xFF6200EE)
                            "Sports" -> Color(0xFF03DAC5)
                            "Fun" -> Color(0xFFFF4081)
                            else -> Color(0xFF764BA2)
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = event.category?.uppercase() ?: "OTHER",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Status Badge (Pending/Registered/Archived)
                    Surface(
                        color = when {
                            isRegistered -> Color(0xFFE8F5E9)
                            isPending -> Color(0xFFFFF3E0)
                            else -> {
                                val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), event.date)
                                if (daysRemaining >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            }
                        },
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), event.date)
                            Icon(
                                imageVector = when {
                                    isRegistered -> Icons.Default.CheckCircle
                                    isPending -> Icons.Default.Pending
                                    daysRemaining >= 0 -> Icons.Default.Timer
                                    else -> Icons.Default.History
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = when {
                                    isRegistered -> Color(0xFF2E7D32)
                                    isPending -> Color(0xFFEF6C00)
                                    daysRemaining >= 0 -> Color(0xFF2E7D32)
                                    else -> Color(0xFFD32F2F)
                                }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = when {
                                    isRegistered -> "REGISTERED"
                                    isPending -> "PAYMENT PENDING"
                                    daysRemaining == 0L -> "TODAY"
                                    daysRemaining > 0L -> "$daysRemaining DAYS TO GO"
                                    else -> "FINISHED"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isRegistered -> Color(0xFF2E7D32)
                                    isPending -> Color(0xFFEF6C00)
                                    daysRemaining >= 0 -> Color(0xFF2E7D32)
                                    else -> Color(0xFFD32F2F)
                                }
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = event.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        if (isRegistered || isPending) {
                            Text(
                                text = if (isRegistered) "Payment Done" else "Payment Pending",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRegistered) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                            )
                        }
                    }
                    if (isAdmin) {
                        Row {
                            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray) }
                            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = event.date.toString(), fontSize = 10.sp, color = Color.Gray)
                    }
                    Text("|", color = Color.LightGray, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = event.location, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
                    }
                    Text("|", color = Color.LightGray, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFC6A700))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "₹100", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RULES:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.description, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Slots: 0 / 10", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showRegister) {
                    Button(
                        onClick = onRegister,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Register Now", color = Color.White)
                    }
                } else if (isRegistered) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = CircleShape
                            ) {
                                Text(
                                    "Registered",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            if (showCancel) {
                                TextButton(onClick = onCancel) {
                                    Text("Cancel", color = Color.Red, fontSize = 12.sp)
                                }
                            }
                        }
                        if (showCertificate) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onDownloadCertificate,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Get Certificate")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String, String, String, Long) -> Unit = { _, _, _, _ -> },
    onSignUpClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Event Portal Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0047BA)
        )
        Text(
            text = if (isAdmin) "Admin Portal" else "Student Portal",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val studentColors = if (!isAdmin) ButtonDefaults.buttonColors(containerColor = Color(0xFF0047BA)) else ButtonDefaults.outlinedButtonColors()
            val adminColors = if (isAdmin) ButtonDefaults.buttonColors(containerColor = Color(0xFF0047BA)) else ButtonDefaults.outlinedButtonColors()

            Button(
                onClick = { isAdmin = false },
                modifier = Modifier.weight(1f),
                colors = studentColors
            ) {
                Text("Student")
            }

            Button(
                onClick = { isAdmin = true },
                modifier = Modifier.weight(1f),
                colors = adminColors
            ) {
                Text("Admin")
            }
        }

        if (!otpSent) {
            if (!isAdmin) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name (for new users)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            var showForgotPassword by remember { mutableStateOf(false) }

            TextButton(
                onClick = { showForgotPassword = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = Color(0xFF0047BA), fontSize = 12.sp)
            }

            if (showForgotPassword) {
                ForgotPasswordDialog(onDismiss = { showForgotPassword = false })
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.instance.sendOtp(ApiUser(
                                email = email,
                                password = password,
                                name = if (name.isNotEmpty()) name else null,
                                role = if (isAdmin) "Admin" else "Student"
                            ))
                            if (response.startsWith("Success")) {
                                otpSent = true
                                Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, response, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047BA))
            ) {
                Text("Send OTP", fontSize = 18.sp)
            }
        } else {
            Text(
                text = "OTP sent to $email",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 4) otp = it },
                label = { Text("Enter 4-digit OTP") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val user = RetrofitClient.instance.verifyOtp(mapOf(
                                "email" to email,
                                "otp" to otp
                            ))
                            onLoginSuccess(user.role ?: "Student", user.email, user.name ?: "User", user.id ?: 0L)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Invalid OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0047BA))
            ) {
                Text("Verify & Login", fontSize = 18.sp)
            }

            TextButton(onClick = { otpSent = false }) {
                Text("Back to Login")
            }
        }
    }
}

@Deprecated("Use OTP flow in LoginScreen")
@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSignUpSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Join your college event portal",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isAdmin, onCheckedChange = { isAdmin = it })
            Text("Register as Admin")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        RetrofitClient.instance.signup(ApiUser(
                            name = name,
                            email = email,
                            password = password,
                            role = if (isAdmin) "Admin" else "Student"
                        ))
                        Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                        onSignUpSuccess()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Sign Up", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Already have an account? Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    CollegeEventManagementSystemTheme {
        DashboardScreen(
            events = sampleEvents,
            registeredEventIds = emptyList(),
            notifications = emptyList(),
            onRegister = {},
            onCancelRegistration = {},
            onAddEvent = {},
            onDeleteEvent = {},
            onEditEvent = { _, _ -> },
            onLogout = {},
            onUpdateProfile = {},
            userId = 1L
        )
    }
}
