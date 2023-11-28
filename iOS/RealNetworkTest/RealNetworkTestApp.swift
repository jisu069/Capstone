import SwiftUI

@main
struct RealNetworkTestApp: App {
    @State private var responseData: Node=Node(LineString: [["String" : 10.0]])
    let persistenceController = PersistenceController.shared
    
    var body: some Scene {
        WindowGroup {
            ContentView().environment(\.managedObjectContext, persistenceController.container.viewContext)
        }
    }
}
