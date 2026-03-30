# Doto — iOS Implementation Specification
**Version:** 1.0 — MVP  
**Language:** Swift 5.9  
**UI Framework:** SwiftUI  
**Architecture:** MVVM  
**Min iOS Target:** 16.0  
**Xcode:** 15+

---

## 1. Project Setup

### 1.1 Create the Project
- New Xcode project → App template
- Product Name: `Doto`
- Bundle ID: `com.yourcompany.doto`
- Interface: SwiftUI
- Language: Swift
- Minimum Deployments: iOS 16.0
- No Core Data, No CloudKit

### 1.2 No Third-Party Dependencies for MVP
The MVP uses only Apple frameworks:
- `SwiftUI` — UI
- `Foundation` / `URLSession` — Networking
- `Security` — Keychain
- `Combine` — where needed (optional, async/await preferred)

No Swift Package Manager dependencies required.

### 1.3 API Base URL (Config)
Create `Core/Network/APIConfig.swift`:
```swift
enum APIConfig {
    #if DEBUG
    static let baseURL = "http://localhost:9000"
    #else
    static let baseURL = "https://api.yourdomain.com"
    #endif
}
```

---

## 2. Core Layer

### 2.1 APIClient.swift

Central networking class. All ViewModels call this, never URLSession directly.

```swift
class APIClient {
    static let shared = APIClient()

    private let session = URLSession.shared
    private let decoder: JSONDecoder = {
        let d = JSONDecoder()
        d.dateDecodingStrategy = .iso8601
        return d
    }()

    func request<T: Decodable>(_ endpoint: APIEndpoint) async throws -> T
    func requestEmpty(_ endpoint: APIEndpoint) async throws  // for 204 responses
}
```

**Implementation notes:**
- Automatically reads JWT from `AuthManager.shared.token` and attaches `Authorization: Bearer <token>` header
- On 401 response: clear token from Keychain, post `AuthManager.logoutNotification`, app returns to login screen
- Throws typed `APIError` on non-2xx responses
- Decodes response body into `T` using the shared decoder

### 2.2 APIEndpoint.swift

Enum that describes every API call:

```swift
enum APIEndpoint {
    // Auth
    case register(username: String, password: String, displayName: String, familyName: String)
    case login(username: String, password: String)
    case me

    // Family
    case getFamily
    case addMember(username: String, password: String, displayName: String, role: String, color: String)
    case updateMember(id: String, displayName: String?, color: String?)
    case deleteMember(id: String)

    // Events
    case getEvents(memberId: String?, from: Date?, to: Date?)
    case createEvent(CreateEventRequest)
    case updateEvent(id: String, UpdateEventRequest)
    case deleteEvent(id: String)

    // Tasks
    case getTasks(assignedTo: String?, status: String?)
    case createTask(CreateTaskRequest)
    case updateTask(id: String, UpdateTaskRequest)
    case completeTask(id: String)
    case deleteTask(id: String)

    // Shopping
    case getShoppingLists
    case createShoppingList(name: String)
    case deleteShoppingList(id: String)
    case getShoppingItems(listId: String)
    case addShoppingItem(listId: String, AddShoppingItemRequest)
    case checkShoppingItem(listId: String, itemId: String)
    case deleteShoppingItem(listId: String, itemId: String)
    case clearCheckedItems(listId: String)

    // Rewards
    case getLeaderboard
    case getGoals
    case createGoal(title: String, pointCost: Int)
    case requestGoalRedemption(id: String)
    case approveGoal(id: String)
    case rejectGoal(id: String)

    // Dashboard
    case getDashboard
}
```

Each case computes `var path: String`, `var method: String`, `var body: Data?`

### 2.3 AuthManager.swift

```swift
class AuthManager: ObservableObject {
    static let shared = AuthManager()

    @Published var isAuthenticated: Bool = false
    @Published var currentUser: User?

    var token: String? { KeychainHelper.load(for: "doto_jwt") }

    func login(token: String, user: User) {
        KeychainHelper.save(token, for: "doto_jwt")
        currentUser = user
        isAuthenticated = true
    }

    func logout() {
        KeychainHelper.delete(for: "doto_jwt")
        currentUser = nil
        isAuthenticated = false
    }

    // Called on app launch to restore session
    func restoreSession() async {
        guard token != nil else { return }
        do {
            let user: User = try await APIClient.shared.request(.me)
            await MainActor.run {
                self.currentUser = user
                self.isAuthenticated = true
            }
        } catch {
            logout()
        }
    }
}
```

### 2.4 KeychainHelper.swift

```swift
struct KeychainHelper {
    static func save(_ value: String, for key: String)
    static func load(for key: String) -> String?
    static func delete(for key: String)
}
```

Uses `SecItemAdd`, `SecItemCopyMatching`, `SecItemDelete` with `kSecClassGenericPassword`.

---

## 3. Models (Decodable)

All models live in `Models/` and conform to `Codable`.

### User.swift
```swift
struct User: Codable, Identifiable {
    let id: String
    let username: String
    let displayName: String
    let role: String          // "parent" | "child"
    let color: String         // hex string e.g. "#FF6B6B"
    let points: Int
    let familyId: String
    let familyName: String
    let createdAt: Date

    var isParent: Bool { role == "parent" }
    var uiColor: Color { Color(hex: color) }
}
```

### Family.swift
```swift
struct Family: Codable, Identifiable {
    let id: String
    let name: String
    let members: [FamilyMember]
    let createdAt: Date
}

struct FamilyMember: Codable, Identifiable {
    let id: String
    let username: String
    let displayName: String
    let role: String
    let color: String
    let points: Int
}
```

### Event.swift
```swift
struct Event: Codable, Identifiable {
    let id: String
    let familyId: String
    let title: String
    let description: String?
    let startAt: Date
    let endAt: Date
    let assignedTo: [String]    // array of member UUIDs
    let location: String?
    let repeatRule: String?     // nil | "daily" | "weekly"
    let createdBy: String
    let createdAt: Date
    let updatedAt: Date
}
```

### Task.swift
```swift
struct Task: Codable, Identifiable {
    let id: String
    let familyId: String
    let title: String
    let description: String?
    let assignedTo: String
    let assignedToName: String
    let assignedToColor: String
    let status: String          // "pending" | "completed"
    let points: Int
    let dueAt: Date?
    let completedAt: Date?
    let createdBy: String
    let createdAt: Date
    let updatedAt: Date

    var isCompleted: Bool { status == "completed" }
    var isOverdue: Bool {
        guard let due = dueAt, !isCompleted else { return false }
        return due < Date()
    }
}
```

### ShoppingList.swift
```swift
struct ShoppingList: Codable, Identifiable {
    let id: String
    let familyId: String
    let name: String
    let itemCount: Int
    let checkedCount: Int
    let createdBy: String
    let createdAt: Date
}

struct ShoppingItem: Codable, Identifiable {
    let id: String
    let listId: String
    let name: String
    let category: String
    let quantity: String?
    let checked: Bool
    let addedBy: String
    let createdAt: Date
}
```

### Reward.swift
```swift
struct LeaderboardEntry: Codable, Identifiable {
    let memberId: String
    let displayName: String
    let color: String
    let role: String
    let points: Int
    let rank: Int
    let streak: Int
    var id: String { memberId }
}

struct RewardGoal: Codable, Identifiable {
    let id: String
    let familyId: String
    let memberId: String
    let memberName: String
    let title: String
    let pointCost: Int
    let status: String     // "active" | "requested" | "approved" | "rejected"
    let requestedAt: Date?
    let approvedAt: Date?
    let approvedBy: String?
    let createdAt: Date
}
```

---

## 4. App Entry Point

### DotoApp.swift
```swift
@main
struct DotoApp: App {
    @StateObject private var authManager = AuthManager.shared

    var body: some Scene {
        WindowGroup {
            Group {
                if authManager.isAuthenticated {
                    MainTabView()
                } else {
                    LoginView()
                }
            }
            .task {
                await authManager.restoreSession()
            }
        }
        .environmentObject(authManager)
    }
}
```

### MainTabView.swift
```swift
struct MainTabView: View {
    var body: some View {
        TabView {
            DashboardView()
                .tabItem { Label("Home", systemImage: "house.fill") }

            ScheduleView()
                .tabItem { Label("Schedule", systemImage: "calendar") }

            TasksView()
                .tabItem { Label("Tasks", systemImage: "checkmark.circle.fill") }

            ShoppingView()
                .tabItem { Label("Shopping", systemImage: "cart.fill") }

            RewardsView()
                .tabItem { Label("Rewards", systemImage: "star.fill") }
        }
    }
}
```

---

## 5. Screen-by-Screen Specification

### 5.1 Auth Screens

**LoginView**
- Username text field
- Password secure field (eye toggle)
- "Log In" button → calls `POST /api/auth/login` → on success: `AuthManager.login(token:user:)`
- "Create Family" link → navigates to `RegisterView`
- Error message display below button

**RegisterView**
- Your name (displayName)
- Family name
- Username
- Password + confirm password
- "Create Family" button → calls `POST /api/auth/register`
- "Already have an account? Log In" link back

---

### 5.2 Dashboard

**ViewModel: DashboardViewModel**
```swift
class DashboardViewModel: ObservableObject {
    @Published var family: Family?
    @Published var todayEvents: [Event] = []
    @Published var myPendingTasks: [Task] = []
    @Published var shoppingSummary: [ShoppingList] = []
    @Published var leaderboard: [LeaderboardEntry] = []
    @Published var isLoading = false
    @Published var error: String?

    func load() async  // calls GET /api/dashboard
}
```

**DashboardView layout (top to bottom):**
1. Family name header + member avatar row (coloured circles with initials)
2. "Today" section — horizontal scroll of today's events as cards (time + title + assigned colour dot)
3. "My Tasks" section — up to 3 pending tasks with quick-complete button
4. "Shopping" section — list name chips showing `unchecked/total` count
5. "Points" section — mini leaderboard (top 3 members, rank + points)

**Behaviour:**
- Loads on `.task { await vm.load() }`
- Pull-to-refresh: `.refreshable { await vm.load() }`
- If `todayEvents` is empty: show "No events today 🎉"
- If `myPendingTasks` is empty: show "All done! ✓"

---

### 5.3 Schedule

**ViewModel: ScheduleViewModel**
```swift
class ScheduleViewModel: ObservableObject {
    @Published var events: [Event] = []
    @Published var selectedDate: Date = Date()
    @Published var viewMode: ViewMode = .week  // .week | .month
    @Published var filterMemberId: String? = nil
    @Published var isLoading = false

    func loadEvents() async     // fetches with from/to based on selectedDate + viewMode
    func deleteEvent(_ id: String) async
}
```

**ScheduleView layout:**
1. View mode toggle (Week / Month) — segmented control
2. Member filter row — horizontal scroll of member avatar chips, tap to filter
3. Calendar grid (week: 7 columns, month: grid)
   - Events rendered as coloured pills within their time slot
   - Conflicting events (same time slot, same member): show orange warning indicator
4. "+" button (parent only) → sheet: `AddEditEventView`
5. Tap event → `EventDetailView`

**AddEditEventView:**
- Title (required)
- Date picker for start date/time
- Date picker for end date/time
- Assign to: multi-select member picker (checkboxes with coloured avatars)
- Location (optional text field)
- Repeat: None / Daily / Weekly (segmented)
- Save / Cancel buttons

---

### 5.4 Tasks

**ViewModel: TasksViewModel**
```swift
class TasksViewModel: ObservableObject {
    @Published var tasks: [Task] = []
    @Published var filterStatus: FilterStatus = .pending  // .all | .pending | .completed
    @Published var isLoading = false

    func loadTasks() async
    func completeTask(_ id: String) async
    func deleteTask(_ id: String) async  // parent only
}
```

**TasksView layout:**
1. Status filter: "Pending" / "Completed" / "All" — segmented control
2. Tasks grouped by assigned member
   - Member name + colour bar as section header
   - Each task row: title, points badge, due date, overdue highlight (red)
   - Swipe right: complete (green checkmark) — calls `PATCH /tasks/:id/complete`
   - Swipe left: delete (parent only)
3. "+" FAB (parent only) → `AddEditTaskView`

**AddEditTaskView:**
- Title (required)
- Description (optional)
- Assign to: single member picker
- Points value (number stepper, default 10)
- Due date picker (optional, toggle)
- Save / Cancel

---

### 5.5 Shopping

**ViewModel: ShoppingViewModel**
```swift
class ShoppingViewModel: ObservableObject {
    @Published var lists: [ShoppingList] = []
    @Published var isLoading = false

    func loadLists() async
    func createList(name: String) async
    func deleteList(_ id: String) async  // parent only
}

class ShoppingListDetailViewModel: ObservableObject {
    @Published var items: [ShoppingItem] = []
    @Published var isLoading = false
    let listId: String

    func loadItems() async
    func addItem(name: String, category: String, quantity: String?) async
    func toggleItem(_ id: String) async
    func deleteItem(_ id: String) async
    func clearChecked() async
}
```

**ShoppingView layout:**
1. List of shopping lists — each row: list name, progress "3/8 items"
2. "+" button (parent only) → alert: "New list name"
3. Swipe to delete (parent only)
4. Tap list → `ShoppingListDetailView`

**ShoppingListDetailView layout:**
1. Navigation title: list name
2. Items grouped by category with category header
3. Each item: checkbox (tap to toggle), name, quantity badge
4. Checked items shown with strikethrough, moved to bottom
5. Refresh button in nav bar + pull-to-refresh
6. "Clear Checked" button in toolbar (appears when ≥1 item is checked)
7. "+" FAB → sheet: `AddItemView`
   - Item name (required)
   - Category picker (segmented or wheel)
   - Quantity (optional text)

---

### 5.6 Rewards

**ViewModel: RewardsViewModel**
```swift
class RewardsViewModel: ObservableObject {
    @Published var leaderboard: [LeaderboardEntry] = []
    @Published var goals: [RewardGoal] = []
    @Published var isLoading = false

    func load() async                         // loads both leaderboard + goals
    func createGoal(title: String, pointCost: Int) async
    func requestRedemption(goalId: String) async
    func approveGoal(goalId: String) async    // parent only
    func rejectGoal(goalId: String) async     // parent only
}
```

**RewardsView layout:**

**Section 1 — Leaderboard:**
- Ranked list of all family members
- Rank badge (🥇🥈🥉 for top 3)
- Member colour avatar + name
- Points total
- Streak indicator (🔥 with count)

**Section 2 — My Goal** (child's current active goal):
- Goal title
- Progress bar: `currentPoints / pointCost`
- Points needed display
- "Claim Reward" button — enabled when `currentPoints >= pointCost`, calls `requestRedemption`

**Section 3 — Pending Approvals** (parent view only, shown when `status == "requested"`):
- For each requested goal: member name, goal title, cost, Approve / Reject buttons

**Section 4 — All Goals:**
- List of all goals in the family with status badges

**"+ Set a Goal" button (any member):**
- Sheet: title text field, points cost stepper
- "Save Goal"

---

## 6. Shared UI Components

Create `Core/Components/` with these reusable views:

**MemberAvatar.swift** — coloured circle with member initial, accepts `color: String, initial: String, size: CGFloat`

**StatusBadge.swift** — pill-shaped badge with text and colour (for task status, goal status)

**PointsBadge.swift** — gold star icon + points number

**LoadingView.swift** — centered `ProgressView` with optional message

**ErrorBanner.swift** — red banner with error message and dismiss button

**EmptyStateView.swift** — icon + title + subtitle for empty lists

**RefreshButton.swift** — circular arrow button for manual refresh in nav bar

---

## 7. Colour Palette for Member Colours

Present these 8 colours as the picker options when creating a family member:

```swift
static let memberColors: [String] = [
    "#FF6B6B",   // coral red
    "#4ECDC4",   // teal
    "#FFD93D",   // yellow
    "#A8E6CF",   // mint
    "#C77DFF",   // purple
    "#FF9A3C",   // orange
    "#74B9FF",   // sky blue
    "#FD79A8",   // pink
]
```

---

## 8. Pull-to-Refresh Pattern

Every list view uses SwiftUI's built-in `.refreshable` modifier:

```swift
List(vm.tasks) { task in
    TaskRowView(task: task)
}
.refreshable {
    await vm.loadTasks()
}
```

For screens with a manual refresh button (e.g. shopping list detail):

```swift
.toolbar {
    ToolbarItem(placement: .navigationBarTrailing) {
        Button {
            Task { await vm.loadItems() }
        } label: {
            Image(systemName: "arrow.clockwise")
        }
        .disabled(vm.isLoading)
    }
}
```

---

## 9. Role-Based UI Rules

Use `AuthManager.shared.currentUser?.isParent` to gate parent-only UI:

```swift
// Hide "+" button for children
if authManager.currentUser?.isParent == true {
    Button(action: { showAddTask = true }) {
        Image(systemName: "plus")
    }
}

// Swipe to delete (parent only)
.swipeActions(edge: .leading) {
    if authManager.currentUser?.isParent == true {
        Button(role: .destructive) {
            Task { await vm.deleteTask(task.id) }
        } label: {
            Label("Delete", systemImage: "trash")
        }
    }
}
```

---

## 10. Error Handling

All ViewModels follow this pattern:

```swift
func loadTasks() async {
    isLoading = true
    error = nil
    do {
        let result: [Task] = try await APIClient.shared.request(.getTasks(assignedTo: nil, status: "pending"))
        await MainActor.run { self.tasks = result }
    } catch let apiError as APIError {
        await MainActor.run { self.error = apiError.message }
    } catch {
        await MainActor.run { self.error = "Something went wrong. Please try again." }
    }
    await MainActor.run { self.isLoading = false }
}
```

Display errors using the shared `ErrorBanner` component at the top of the view.
