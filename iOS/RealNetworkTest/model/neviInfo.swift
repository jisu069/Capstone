import Foundation
import NMapsMap

class NeviInfo: ObservableObject{
    @Published var neviInfo:[Int] = []
    
    func getTheta(p1:NMGLatLng, p2:NMGLatLng, p3: NMGLatLng) -> Double {
        var theta = (atan2((p1.lat - p2.lat), (p1.lng - p2.lng)) - atan2((p2.lat - p3.lat), (p2.lng - p3.lng))) * 180 / .pi
        if theta < -180 {
            theta += 360
        } else if theta > 180 {
            theta -= 360
        }
        
        return theta
    }
}
