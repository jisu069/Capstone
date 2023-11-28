import SwiftUI
import Alamofire
import SwiftyJSON
import CoreData

struct NetworkingButton: View {
    @Binding var path: [[Point]]?
    @Binding var startPoint: Point?
    @Binding var endPoint: Point?
    
    var body: some View {
        NavigationLink(destination: PathView(path: $path, startPoint: $startPoint, endPoint: $endPoint)){
            Text("경로 요청")
        }.simultaneousGesture(TapGesture().onEnded{
            path = nil
            fetchData()
        })
    }
    
    func fetchData() {
        let sendData = SendData(from: ["lon": startPoint!.lon, "lat": startPoint!.lat], to: ["lon": endPoint!.lon, "lat": endPoint!.lat])
        let headers: HTTPHeaders = ["Content-Type": "application/json"]
        
        AF.request("http://1.240.41.53:19000/api",
                   method: .post,
                   parameters: sendData,
                   encoder: JSONParameterEncoder.default,
                   headers: headers)
        //body: .data(jsonData))
        .responseJSON() { response in
            switch response.result {
            case .success(let value):
                let res = JSON(value)
                var allPaths: [[Point]] = []
                
                for (_, t) in res {
                    let tst = t["features"]
                    var lst: [Point] = []
                    
                    for (_, subset) in tst {
                        if subset["geometry"]["type"] == "LineString" {
                            for i in subset["geometry"]["coordinates"] {
                                var value = i.1.arrayValue
                                lst.append(Point(lon: value[0].doubleValue, lat: value[1].doubleValue))
                            }
                        }
                    }
                    allPaths.append(lst)
                }
                var lst: [[Point]] = []
                
                for c in allPaths {
                    var l: [Point] = []
                    for d in c {
                        if l.isEmpty || (l.last?.lat != d.lat && l.last?.lon != d.lon) {
                            l.append(Point(lon: d.lon, lat: d.lat))
                        }
                    }
                    lst.append(l)
                }
                
                
                path = lst
                
                
                //                var persistController = PersistenceController.shared
                //                persistController.clearDatabase()
                //
                //                for i in lst {
                //                    var data = persistController.openDatabse()
                //                    data.setValue(i.lon, forKey: "lon")
                //                    data.setValue(i.lat, forKey: "lat")
                //                    persistController.saveData(LineStringOBJ: data)
                //                }
                
            case .failure(let err):
                print("conneterr")
            }
        }
    }
}

//struct NetworkingButton_Previews: PreviewProvider {
//    static var previews: some View {
//        NetworkingButton(coordinates: .constant([(127.046074, 37.709538)]),startPoint: .constant(Point(lon: 127.046624, lat: 37.724142)))
//    }
//}
