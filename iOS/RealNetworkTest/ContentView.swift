import SwiftUI

struct ContentView: View {
    //@State private var startPoint: Point = Point(lon: 127.046624, lat: 37.724142)30.257889, 120.187375
    @State private var startPoint: Point?// = Point(lon: 120.187375, lat: 30.257889)
    @State private var path: [[Point]]?
    @State private var endPoint: Point?

    var body: some View {
        NavigationView{
            VStack {
                SerchButton(endPoint: $endPoint)
                    .padding(.bottom, 10)
                UIMapView(startPoint: $startPoint, endPoint: $endPoint)
                
                if(endPoint != nil){
                    HStack{
                        NetworkingButton(path: $path, startPoint: $startPoint, endPoint: $endPoint)
                            .frame(maxWidth:.infinity)
                        Button(action:{endPoint = nil}){
                            Text("취소")
                                .frame(maxWidth: .infinity)
                        }
                    }
                    .padding()
                }
                
            }
            .padding()
        }
    }
   
}
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
