import SwiftUI
import Alamofire
import SwiftyJSON

struct Data{
    var nameList: [NameAddr] = []
    var coorList: [Point] = []
}

struct NameAddr: Hashable {
    var name = ""
    var addr = ""
}

struct SerchView: View {
    @State private var text:String = ""
    @State private var data:Data = Data()
    @Binding var endPoint: Point?
    
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    var body: some View {
        VStack{
            HStack{
                Button(action:{self.presentationMode.wrappedValue.dismiss()}){
                    Image(systemName: "chevron.left")
                        .frame(width: 30)
                }
                HStack{
                    TextField("검색", text: $text)
                        .padding(7.0)
                }
                .border(Color.black)
                Button(action:{
                    data.nameList.removeAll()
                    data.coorList.removeAll()
                    serching()
                }){
                    Image(systemName: "magnifyingglass")
                        .frame(width: 30)
                }
                .padding(.trailing, 7.0)
            }
           
            List(0..<data.nameList.count, id:\.self) { i in
                Button(action:{
                    let tmp = data.coorList[i]
                    endPoint = Point(lon: tmp.lon, lat: tmp.lat)
                    self.presentationMode.wrappedValue.dismiss()
                }){
                    VStack(alignment:.leading){
                        Text(data.nameList[i].name)
                            .foregroundColor(.black)
                        Text(data.nameList[i].addr)
                            .foregroundColor(.black)
                    }
                }
            }
        }
        .navigationBarBackButtonHidden(true)
    }
    
    func serching() {
        let headers: HTTPHeaders = [
            "Content-Type": "application/json",
            "appKey": "e8wHh2tya84M88aReEpXCa5XTQf3xgo01aZG39k5"
        ]
        
        let parameters =
        ["version":"1",
         "searchKeyword":text,
         "searchType":"all",
         "searchtypCd":"R",
         "centerLon":"127.046074",
         "centerLat":"37.709538",
         "reqCoordType":"WGS84GEO",
         "resCoordType":"WGS84GEO",
         "radius":"5",
         "page":"1",
         "count":"10",
         "multiPoint":"N",
         "poiGroupYn":"N"
        ]
        
        AF.request("https://apis.openapi.sk.com/tmap/pois",
                   method: .get,
                   parameters: parameters,
                   //encoder: JSONParameterEncoder.default,
                   headers: headers)
        //body: .data(jsonData))
        .responseJSON() { response in
            switch response.result {
            case .success(let value):
                let res = JSON(value)
                let tst = res["searchPoiInfo"]["pois"]["poi"]
                
                for(_, subset) in tst {
                    var n = NameAddr(name: subset["name"].stringValue,
                                     addr: subset["newAddressList"]["newAddress"][0]["fullAddressRoad"].stringValue)
                    
                    data.nameList.append(n)
                    var point = Point(lon: subset["frontLon"].doubleValue, lat: subset["frontLat"].doubleValue)
                    data.coorList.append(point)
                }
            case .failure(let err):
                print(err)
            }
        }
    }
}


//struct SerchView_Previews: PreviewProvider {
//    static var previews: some View {
//        SerchView(coordinates: .constant([(127.046624, 37.724142)]), endPoint: .constant(nil))
//    }
//}
