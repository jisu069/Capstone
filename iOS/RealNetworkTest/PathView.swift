import SwiftUI

struct PathView: View {
    @Binding var path: [[Point]]?
    @Binding var startPoint: Point?
    @Binding var endPoint: Point?
    @State private var selectPath: Int = 0
    @State private var isStartNevi: Bool = false
    @State var direction: [Int]?
    @State var neviPath:[Point] = []
    //    @State private var distance: Double
    //    @State private var nextNodeIdx: Int
    @Environment(\.presentationMode) var presentationMode: Binding<PresentationMode>
    var body: some View {
        VStack{
            if isStartNevi == true{
                NevigationView(path: $path, startPoint: $startPoint, selectPath: $selectPath, direction: $direction, neviPath: $neviPath)
                    .padding()
            }
            PathMapView(path: $path, startPoint: $startPoint, endPoint: $endPoint, selectPath: $selectPath, isStartNevi: $isStartNevi, direction: $direction)
            if isStartNevi == false{
                HStack{
                    Button(action:{selectPath = 1}){
                        VStack{
                            Rectangle()
                                .fill(.green)
                                .frame(width: 50, height: 10)
                                .cornerRadius(10)
                            Text("대로우선")
                        }
                    }
                    Button(action:{selectPath = 2}){
                        VStack{
                            Rectangle()
                                .fill(.red)
                                .frame(width: 50, height: 10)
                                .cornerRadius(10)
                            Text("API최단거리")
                        }
                    }
                    Button(action:{selectPath = 3}){
                        VStack{
                            Rectangle()
                                .fill(.yellow)
                                .frame(width: 50, height: 10)
                                .cornerRadius(10)
                            Text("CCTV경로")
                        }
                    }
                    Button(action:{selectPath = 5}){
                        VStack{
                            Rectangle()
                                .fill(.orange)
                                .frame(width: 50, height: 10)
                                .cornerRadius(10)
                            Text("계산최단경로")
                        }
                    }
                }
                .padding()
            }
            HStack{
                Button(action:{
                    isStartNevi = true
                    
                    var result: [Int] = []
                    var neviPoint: [Point] = []
                    var selPath = path![selectPath - 1]
                    for idx in 1..<selPath.count - 1 {
                        var at1 = atan2((selPath[idx - 1].lat - selPath[idx].lat), (selPath[idx - 1].lon - selPath[idx].lon))
                        at1 -= atan2((selPath[idx].lat - selPath[idx + 1].lat), (selPath[idx].lon - selPath[idx + 1].lon))
                        
                        var theta = at1 * (180 / .pi)
                        if theta < -180 {
                            theta += 360
                        } else if theta > 180 {
                            theta -= 360
                        }
                        if theta < -30 {
                            result.append(1)
                            neviPoint.append(selPath[idx])
                        } else if theta > 30 {
                            result.append(2)
                            neviPoint.append(selPath[idx])
                        }
                    }
                    direction = result
                    neviPath = neviPoint
                }){
                    Text("경로안내")
                        .frame(maxWidth: .infinity)
                }
                Button(action:{presentationMode.wrappedValue.dismiss()}){
                    Text("취소")
                        .frame(maxWidth: .infinity)
                }
            }
            
        }
        .navigationBarBackButtonHidden(true)
    }
}
//struct PathView_Previews: PreviewProvider {
//    static var previews: some View {
//        PathView(path: nil, startPoint: nil, endPoint: nil)
//    }
//}
