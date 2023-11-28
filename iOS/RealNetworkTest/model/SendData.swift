import Foundation

struct SendData: Codable {
    var from: [String: Double]
    var to: [String: Double]
}
