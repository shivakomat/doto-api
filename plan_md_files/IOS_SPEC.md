# Doto — iOS Specification
**Version:** 1.0  
**Language:** Swift 5.9  
**UI Framework:** SwiftUI  
**Architecture:** MVVM  
**Min iOS:** 16.0

---

## 1. App Entry Point

### `DotoApp.swift`
```swift
@main
struct DotoApp: App {
    @StateObject private var authVM = AuthViewModel()
    var body: some Scene {
        WindowGroup {
            RootView().environmentObject(authVM)
        }
    }
}
```

### `RootView.swift`
Switches between the three top-level app states:
```swift
struct RootView: View {
    @EnvironmentObject var authVM: AuthViewModel
    var body: some View {
        Group {
            switch authVM.state {
            case .unauthenticated: LoginView()
            case .noFamily:        FamilySetupView()
            case .ready:           MainTabView()
            }
        }
        .task { await authVM.restoreSession() }
    }
}
```

### App State Enum
```swift
enum AppState {
    case unauthenticated   // no JWT in Keychain
    case noFamily          // valid JWT but profile.familyId == nil
    case ready             // valid JWT + familyId set
}
```

---

## 2. Keychain Helper

### `Auth/KeychainHelper.swift`
Stores and retrieves the JWT. Never use UserDefaults for auth tokens.

```swift
struct KeychainHelper {
    private static let service = "com.doto.app"
    private static let account = "jwt_token"

    static func saveToken(_ token: String) {
        let data = Data(token.utf8)
        let query: [CFString: Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account,
            kSecValueData: data
        ]
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }

    static func loadToken() -> String? {
        let query: [CFString: Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account,
            kSecReturnData: true,
            kSecMatchLimit: kSecMatchLimitOne
        ]
        var result: AnyObject?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
              let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    static func deleteToken() {
        let query: [CFString: Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: account
        ]
        SecItemDelete(query as CFDictionary)
    }
}
```

---

## 3. Networking Layer

### `Networking/APIConfig.swift`
```swift
enum APIConfig {
    #if DEBUG
    static let baseURL = "http://localhost:9000/api"
    #else
    static let baseURL = "https://api.getdoto.com/api"
    #endif
}
```

### `Networking/APIError.swift`
```swift
enum APIError: Error, LocalizedError {
    case unauthorized
    case notFound
    case validation(String)
    case conflict(String)
    case serverError(String)
    case decodingError(Error)
    case networkError(Error)
    case unknown

    var errorDescription: String? {
        switch self {
        case .unauthorized:         return "Please log in again."
        case .notFound:             return "Not found."
        case .validation(let msg):  return msg
        case .conflict(let msg):    return msg
        case .serverError(let msg): return "Server error: \(msg)"
        case .decodingError(let e): return "Data error: \(e.localizedDescription)"
        case .networkError(let e):  return "Network error: \(e.localizedDescription)"
        case .unknown:              return "An unexpected error occurred."
        }
    }
}
```

### `Networking/APIClient.swift`
Single shared instance. Automatically injects the JWT header.

```swift
class APIClient {
    static let shared = APIClient()
    private let session = URLSession.shared

    func get<T: Decodable>(_ path: String) async throws -> T {
        try await request(method: "GET", path: path, body: nil as EmptyBody?)
    }

    func post<B: Encodable, T: Decodable>(_ path: String, body: B) async throws -> T {
        try await request(method: "POST", path: path, body: body)
    }

    func put<B: Encodable, T: Decodable>(_ path: String, body: B) async throws -> T {
        try await request(method: "PUT", path: path, body: body)
    }

    func patch<B: Encodable, T: Decodable>(_ path: String, body: B) async throws -> T {
        try await request(method: "PATCH", path: path, body: body)
    }

    func patch<T: Decodable>(_ path: String) async throws -> T {
        try await request(method: "PATCH", path: path, body: nil as EmptyBody?)
    }

    func delete(_ path: String) async throws {
        let _: EmptyResponse = try await request(method: "DELETE", path: path, body: nil as EmptyBody?)
    }

    private func request<B: Encodable, T: Decodable>(method: String, path: String, body: B?) async throws -> T {
        guard let url = URL(string: APIConfig.baseURL + path) else { throw APIError.unknown }
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let token = KeychainHelper.loadToken() {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        if let body = body {
            req.httpBody = try JSONEncoder.iso8601.encode(body)
        }
        let (data, response) = try await session.data(for: req)
        guard let http = response as? HTTPURLResponse else { throw APIError.unknown }
        switch http.statusCode {
        case 200, 201:
            return try JSONDecoder.iso8601.decode(T.self, from: data)
        case 204:
            if let empty = EmptyResponse() as? T { return empty }
            throw APIError.unknown
        case 400:
            let err = try? JSONDecoder.iso8601.decode(APIErrorResponse.self, from: data)
            throw APIError.validation(err?.message ?? "Validation error")
        case 401, 403:
            throw APIError.unauthorized
        case 404:
            throw APIError.notFound
        case 409:
            let err = try? JSONDecoder.iso8601.decode(APIErrorResponse.self, from: data)
            throw APIError.conflict(err?.message ?? "Conflict")
        default:
            let err = try? JSONDecoder.iso8601.decode(APIErrorResponse.self, from: data)
            throw APIError.serverError(err?.message ?? "Unknown error")
        }
    }
}

struct EmptyBody: Encodable {}
struct EmptyResponse: Decodable {}
struct APIErrorResponse: Decodable { let code: String; let message: String }

extension JSONEncoder {
    static var iso8601: JSONEncoder {
        let e = JSONEncoder(); e.dateEncodingStrategy = .iso8601; return e
    }
}
extension JSONDecoder {
    static var iso8601: JSONDecoder {
        let d = JSONDecoder()
        d.dateDecodingStrategy = .iso8601
        d.keyDecodingStrategy = .convertFromSnakeCase
        return d
    }
}
```

---

## 4. Data Models

All structs conform to `Codable` and `Identifiable`. `Doto` prefix used where names conflict with Swift/SwiftUI built-ins.

### `Models/Profile.swift`
```swift
struct Profile: Codable, Identifiable {
    let id: String
    let username: String?
    let displayName: String
    let role: String        // "parent" | "child"
    let color: String       // hex e.g. "#6C63FF"
    var points: Int
    let familyId: String?
    let isAuthAccount: Bool
    let createdAt: Date
}
```

### `Models/Family.swift`
```swift
struct Family: Codable, Identifiable {
    let id: String
    let name: String
    let inviteCode: String
    var members: [Profile]
    let createdAt: Date
}
```

### `Models/DotoEvent.swift`
```swift
// DotoEvent — avoids collision with SwiftUI's Event
struct DotoEvent: Codable, Identifiable {
    let id: String
    let familyId: String
    var title: String
    var description: String?
    var startAt: Date
    var endAt: Date
    var location: String?
    var color: String?
    var assignedTo: [String]   // array of profile IDs
    let createdBy: String
    let createdAt: Date
    let updatedAt: Date
}
```

### `Models/DotoTask.swift`
```swift
// DotoTask — avoids collision with Swift's Task
struct DotoTask: Codable, Identifiable {
    let id: String
    let familyId: String
    var title: String
    var description: String?
    var assignedTo: String?
    var status: String         // "todo" | "in_progress" | "done" | "cancelled"
    var priority: String       // "low" | "medium" | "high"
    var points: Int
    var dueAt: Date?
    var completedAt: Date?
    let createdBy: String
    let createdAt: Date
    let updatedAt: Date

    var isOverdue: Bool {
        guard let due = dueAt, status != "done" else { return false }
        return due < Date()
    }
}
```

### `Models/ShoppingList.swift`
```swift
struct ShoppingList: Codable, Identifiable {
    let id: String
    let familyId: String
    var name: String
    let itemCount: Int
    let checkedCount: Int
    let createdBy: String
    let createdAt: Date
    let updatedAt: Date
}
```

### `Models/ShoppingItem.swift`
```swift
struct ShoppingItem: Codable, Identifiable {
    let id: String
    let listId: String
    var name: String
    var category: String
    var quantity: String?
    var isChecked: Bool
    var checkedBy: String?
    var checkedAt: Date?
    let createdBy: String
    let createdAt: Date
    let updatedAt: Date
}
```

### `Models/Reward.swift`
```swift
struct Reward: Codable, Identifiable {
    let id: String
    let familyId: String
    let memberId: String
    var title: String
    var pointsCost: Int
    var status: String         // "active" | "pending_approval" | "approved" | "redeemed"
    var requestedAt: Date?
    var approvedBy: String?
    var approvedAt: Date?
    let createdAt: Date
    let updatedAt: Date
}
```

---

## 5. AuthViewModel

### `Auth/AuthViewModel.swift`
```swift
@MainActor
class AuthViewModel: ObservableObject {
    @Published var state: AppState = .unauthenticated
    @Published var currentProfile: Profile?
    @Published var errorMessage: String?
    @Published var isLoading = false

    func restoreSession() async {
        guard KeychainHelper.loadToken() != nil else { state = .unauthenticated; return }
        do {
            let profile: Profile = try await APIClient.shared.get("/auth/me")
            currentProfile = profile
            state = profile.familyId == nil ? .noFamily : .ready
        } catch {
            KeychainHelper.deleteToken()
            state = .unauthenticated
        }
    }

    func login(username: String, password: String) async {
        isLoading = true; errorMessage = nil; defer { isLoading = false }
        do {
            let res: AuthResponse = try await APIClient.shared.post(
                "/auth/login",
                body: LoginRequest(username: username, password: password)
            )
            KeychainHelper.saveToken(res.token)
            currentProfile = res.profile
            state = res.profile.familyId == nil ? .noFamily : .ready
        } catch { errorMessage = error.localizedDescription }
    }

    func register(username: String, password: String, displayName: String) async {
        isLoading = true; errorMessage = nil; defer { isLoading = false }
        do {
            let res: AuthResponse = try await APIClient.shared.post(
                "/auth/register",
                body: RegisterRequest(username: username, password: password, displayName: displayName)
            )
            KeychainHelper.saveToken(res.token)
            currentProfile = res.profile
            state = .noFamily
        } catch { errorMessage = error.localizedDescription }
    }

    func logout() {
        KeychainHelper.deleteToken()
        currentProfile = nil
        state = .unauthenticated
    }
}

struct AuthResponse: Decodable { let token: String; let profile: Profile }
struct LoginRequest: Encodable { let username: String; let password: String }
struct RegisterRequest: Encodable { let username, password, displayName: String }
```

---

## 6. Screen-by-Screen Specification

### 6.1 LoginView
**File:** `Auth/Views/LoginView.swift`

Elements:
- App logo at top centre
- Username text field — `.autocapitalization(.never)`, `.autocorrectionDisabled()`
- Password `SecureField`
- "Log In" primary button → `Task { await authVM.login(...) }`
- "Don't have an account? Register" link → `RegisterView`
- `ProgressView` overlaid when `authVM.isLoading`
- Red error text below button when `authVM.errorMessage != nil`

---

### 6.2 RegisterView
**File:** `Auth/Views/RegisterView.swift`

Elements:
- Display Name text field
- Username text field — `.autocapitalization(.never)`
- Password `SecureField` with "Minimum 8 characters" placeholder
- "Create Account" primary button
- "Already have an account? Log in" link

---

### 6.3 FamilySetupView
**File:** `Family/FamilySetupView.swift`

Two modes via a `Picker` segmented control at top:

**Create tab:**
- Family Name text field
- "Create Family" button → `POST /api/families` → on success, `authVM.state = .ready`

**Join tab:**
- Invite Code text field — 6 chars, `.textInputAutocapitalization(.characters)`
- "Join Family" button → `POST /api/families/join` → on success, `authVM.state = .ready`

---

### 6.4 MainTabView
**File:** `MainTabView.swift`

```swift
TabView {
    DashboardView()
        .tabItem { Label("Home", systemImage: "house.fill") }
    ScheduleView()
        .tabItem { Label("Schedule", systemImage: "calendar") }
    TasksView()
        .tabItem { Label("Tasks", systemImage: "checkmark.circle.fill") }
    ShoppingListsView()
        .tabItem { Label("Shopping", systemImage: "cart.fill") }
    RewardsView()
        .tabItem { Label("Rewards", systemImage: "star.fill") }
}
.environmentObject(authVM)
```

---

### 6.5 DashboardView
**File:** `Dashboard/DashboardView.swift`  
**API call:** `GET /api/dashboard`

Layout (vertical ScrollView with pull-to-refresh):
1. **Greeting header** — "Good morning, Sarah" (time-aware: morning/afternoon/evening) + family name subheading
2. **Members row** — horizontal `ScrollView` of `AvatarView` circles with points badge below each
3. **Today's Events section** — `VStack` of event cards: colour stripe, title, time range, assignee avatars. Empty state: "No events today"
4. **Pending Tasks section** — list of incomplete tasks: priority stripe, title, assignee avatar, due date in red if overdue. Empty state: "All caught up! 🎉"
5. **Pending Approvals section** — visible to parents only. Cards with "Approve" and "Decline" buttons for `pending_approval` rewards

---

### 6.6 ScheduleView
**File:** `Schedule/ScheduleView.swift`  
**API calls:** `GET /api/events?from=&to=` on load and week navigation, `DELETE /api/events/:id`

Layout:
1. **Week navigation bar** — `<` / `>` buttons + current week label ("Mar 24–30") + "Today" reset button
2. **Member filter pills** — scrollable row of member avatar buttons to toggle visibility
3. **7-day grid** — day headers + event blocks per day, colour-coded by member
4. Tap event → `AddEditEventView` sheet in edit mode
5. `+` nav bar button → `AddEditEventView` sheet in create mode

---

### 6.7 AddEditEventView
**File:** `Schedule/AddEditEventView.swift`  
**API calls:** `POST /api/events` (create) or `PUT /api/events/:id` (edit)

Form fields:
- Title (required)
- Start date+time `DatePicker` — `.dateAndTime` display
- End date+time `DatePicker` — must be after start, show inline error if violated
- Location (optional text field)
- Description (optional multiline `TextEditor`)
- Assign to — member checklist (multiple selection)
- In edit mode: "Delete Event" destructive button at bottom

---

### 6.8 TasksView
**File:** `Tasks/TasksView.swift`  
**API calls:** `GET /api/tasks`, `PATCH /api/tasks/:id/complete`, `DELETE /api/tasks/:id`

Layout:
1. **Filter bar** — member picker + status chips (All / Todo / In Progress / Done)
2. **Task list** grouped by assigned member
   - Each row: priority colour stripe | checkmark tap-target | title + points badge | due date
   - Overdue tasks: due date in `.dotoRed`
   - Tap checkmark → `PATCH .../complete`, animate row to done state
   - Swipe left → Delete with confirmation
3. `+` FAB → `AddEditTaskView` sheet

Pull-to-refresh re-fetches.

---

### 6.9 AddEditTaskView
**File:** `Tasks/AddEditTaskView.swift`  
**API calls:** `POST /api/tasks` (create) or `PUT /api/tasks/:id` (edit)

Form fields:
- Title (required)
- Assign to — member picker (includes "Unassigned" option)
- Priority — segmented control: Low / Medium / High
- Points — `Stepper` 0–100, step 5
- Due date — toggle on/off; `DatePicker` shown only when on
- Description — optional multiline `TextEditor`

---

### 6.10 ShoppingListsView
**File:** `Shopping/ShoppingListsView.swift`  
**API calls:** `GET /api/shopping/lists`, `POST /api/shopping/lists`, `DELETE /api/shopping/lists/:id`

Layout:
- `List` of shopping list cards — name, item count, `ProgressView` (checked/total)
- Swipe left → Delete (with confirmation alert)
- Tap → navigate to `ShoppingItemsView`
- `+` nav bar button → alert with text field for new list name

---

### 6.11 ShoppingItemsView
**File:** `Shopping/ShoppingItemsView.swift`  
**API calls:** `GET /api/shopping/lists/:id/items`, `PATCH .../check`, `DELETE .../items/:itemId`, `DELETE .../items/checked`

Layout:
1. Nav title: list name + "(3 / 12)"
2. Items grouped by category, unchecked items first
3. Each row: checkbox button | item name | quantity in secondary text
4. Checked items: strikethrough + reduced opacity
5. Swipe left → Delete item
6. Toolbar button: "Clear Checked" (shown when checkedCount > 0)
7. `+` FAB → `AddItemView` sheet

**Optimistic UI:** Toggle `isChecked` in local array immediately on tap, then sync to API. Revert if API call fails.

Pull-to-refresh re-fetches items.

---

### 6.12 AddItemView
**File:** `Shopping/AddItemView.swift`  
**API call:** `POST /api/shopping/lists/:id/items`

Form fields:
- Item name (required, auto-focused `.onAppear`)
- Category picker — 9 options
- Quantity text field (optional)
- "Add Item" button — adds and dismisses
- "Add & Continue" button — adds and clears form to add another

---

### 6.13 RewardsView
**File:** `Rewards/RewardsView.swift`  
**API calls:** `GET /api/rewards`, `PATCH .../request`, `PATCH .../approve`, `PATCH .../redeem`, `DELETE /api/rewards/:id`

**Parent view:**
1. Leaderboard — horizontal scroll of member cards: avatar + name + points + rank medal
2. Pending Approvals — cards with Approve / Decline action buttons
3. All Goals — full list grouped by child, each card shows title + points cost + status badge

**Child view** (when `currentProfile.role == "child"`):
1. Large points balance at top
2. Goal cards — progress bar showing my points vs cost
3. "Redeem" button appears when `profile.points >= reward.pointsCost` — calls `.../request`

`+` button (parents only) → `AddRewardView` sheet

Pull-to-refresh re-fetches.

---

### 6.14 AddRewardView
**File:** `Rewards/AddRewardView.swift`  
**API call:** `POST /api/rewards`

Form fields:
- "For" picker — child members only
- Reward title text field
- Points cost `Stepper` — 25–500, step 25
- "Add Reward Goal" button

---

### 6.15 FamilyManageView
**File:** `Family/FamilyManageView.swift`  
Accessible from settings icon in Dashboard nav bar.

Layout:
1. Family name heading
2. Invite code display with copy-to-clipboard button
3. Members list — avatar + name + role badge + points. Swipe left on child rows → Delete (parent only)
4. "Add Child" button → inline form: display name + colour picker (8 preset hex options)
5. "Log Out" destructive button at bottom → `authVM.logout()`

---

## 7. Shared Components

### `Shared/Components/AvatarView.swift`
Circle with member's colour as background, their initials in white.

```swift
struct AvatarView: View {
    let name: String
    let color: String
    var size: CGFloat = 40
    var initials: String {
        name.split(separator: " ").prefix(2)
            .compactMap { $0.first.map(String.init) }
            .joined().uppercased()
    }
    var body: some View {
        Text(initials)
            .font(.system(size: size * 0.4, weight: .semibold))
            .foregroundColor(.white)
            .frame(width: size, height: size)
            .background(Color(hex: color))
            .clipShape(Circle())
    }
}
```

### `Shared/Extensions/Color+Doto.swift`
```swift
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        self.init(
            red: Double((int & 0xFF0000) >> 16) / 255,
            green: Double((int & 0x00FF00) >> 8) / 255,
            blue: Double(int & 0x0000FF) / 255
        )
    }
    static let dotoPurple = Color(hex: "#6C63FF")
    static let dotoRed    = Color(hex: "#FF6B6B")
    static let dotoGreen  = Color(hex: "#51CF66")
    static let dotoAmber  = Color(hex: "#FFB347")
    static let dotoBlue   = Color(hex: "#4DABF7")
}
```

### `Shared/Extensions/Date+Formatting.swift`
```swift
extension Date {
    var shortTime: String {
        let f = DateFormatter(); f.timeStyle = .short; f.dateStyle = .none
        return f.string(from: self)
    }
    var shortDate: String {
        let f = DateFormatter(); f.dateStyle = .medium; f.timeStyle = .none
        return f.string(from: self)
    }
    var relativeDue: String {
        if Calendar.current.isDateInToday(self)     { return "Today" }
        if Calendar.current.isDateInTomorrow(self)  { return "Tomorrow" }
        if Calendar.current.isDateInYesterday(self) { return "Yesterday" }
        return shortDate
    }
    var isPast: Bool { self < Date() }
}
```

---

## 8. Standard ViewModel Pattern

Every ViewModel follows this exact pattern. All async calls are dispatched via `Task { }` from the View's `.task` modifier or button actions.

```swift
@MainActor
class ExampleViewModel: ObservableObject {
    @Published var items: [SomeModel] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    func load() async {
        isLoading = true; errorMessage = nil; defer { isLoading = false }
        do {
            items = try await APIClient.shared.get("/some-endpoint")
        } catch APIError.unauthorized {
            // Handled by AuthViewModel via environment — post notification or use shared state
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
```

Error alert — add this modifier to the root view of every screen:
```swift
.alert("Something went wrong", isPresented: Binding(
    get: { viewModel.errorMessage != nil },
    set: { if !$0 { viewModel.errorMessage = nil } }
)) {
    Button("OK", role: .cancel) {}
} message: {
    Text(viewModel.errorMessage ?? "")
}
```

---

## 9. Loading & Empty States

Every list view must handle three states:

| State | Condition | Display |
|---|---|---|
| Loading | `items.isEmpty && isLoading` | `ProgressView()` centred |
| Empty | `items.isEmpty && !isLoading` | `EmptyStateView(message:)` centred |
| Populated | `!items.isEmpty` | Normal list |

Do not show a spinner during pull-to-refresh — the system refresh indicator handles that.

```swift
var body: some View {
    Group {
        if items.isEmpty && isLoading {
            ProgressView()
        } else if items.isEmpty {
            EmptyStateView(message: "No tasks yet")
        } else {
            List(items) { item in ... }
                .refreshable { await viewModel.load() }
        }
    }
    .task { await viewModel.load() }
}
```
