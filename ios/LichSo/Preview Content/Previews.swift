import SwiftUI

// MARK: - Preview Content
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environmentObject(SettingsViewModel())
            .preferredColorScheme(.dark)

        ContentView()
            .environmentObject(SettingsViewModel())
            .preferredColorScheme(.light)
    }
}

struct HomeScreen_Previews: PreviewProvider {
    static var previews: some View {
        HomeScreen(viewModel: HomeViewModel(), chatViewModel: ChatViewModel())
            .environment(\.lichSoColors, .dark)
            .preferredColorScheme(.dark)
    }
}

struct CalendarScreen_Previews: PreviewProvider {
    static var previews: some View {
        CalendarScreen(viewModel: CalendarViewModel())
            .environment(\.lichSoColors, .dark)
            .preferredColorScheme(.dark)
    }
}

struct ChatScreen_Previews: PreviewProvider {
    static var previews: some View {
        ChatScreen(viewModel: ChatViewModel(), onClose: {})
            .environment(\.lichSoColors, .dark)
            .preferredColorScheme(.dark)
    }
}

struct TasksScreen_Previews: PreviewProvider {
    static var previews: some View {
        TasksScreen(viewModel: TasksViewModel())
            .environment(\.lichSoColors, .dark)
            .preferredColorScheme(.dark)
    }
}
