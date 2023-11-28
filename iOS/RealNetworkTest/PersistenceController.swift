import CoreData

struct PersistenceController {
    static let shared = PersistenceController()
    
    let container: NSPersistentContainer
    
    init(inMemory: Bool = false) {
        container = NSPersistentContainer(name: "Model")
        container.loadPersistentStores { _, error in
            if let error = error as NSError? {
                fatalError("Unresolved error \(error), \(error.userInfo)")
            }
        }
    }
    
    func openDatabse() -> NSManagedObject {
        let entity = NSEntityDescription.entity(forEntityName: "Entity", in: self.container.viewContext)
        let lineString = NSManagedObject(entity: entity!, insertInto: self.container.viewContext)
        
        return lineString
    }
    
    public func clearDatabase() {
        guard let url = PersistenceController.shared.container.persistentStoreDescriptions.first?.url else { return }
        let persistentStoreCoordinator = PersistenceController.shared.container.persistentStoreCoordinator
        
        do {
            try persistentStoreCoordinator.destroyPersistentStore(at:url, ofType: NSSQLiteStoreType, options: nil)
            try persistentStoreCoordinator.addPersistentStore(ofType: NSSQLiteStoreType, configurationName: nil, at: url, options: nil)
        } catch {
            print("error")
        }
    }
    
    func saveData(LineStringOBJ:NSManagedObject) {
        do {
            try self.container.viewContext.save()
        } catch {
            print("error")
        }
    }
    
    func fetch() -> [NSFetchRequestResult]? {
        let request = NSFetchRequest<NSFetchRequestResult>(entityName: "Entity")
        request.returnsObjectsAsFaults = false
        
        var result: [NSFetchRequestResult]?
        do {
            result = try self.container.viewContext.fetch(request)
            for data in result as! [NSManagedObject] {
                let lon = data.value(forKey: "lon") as! Float
                let lat = data.value(forKey: "lat") as! Float
            }
        } catch {
            print("Error")
        }
        
        return result
    }
}
