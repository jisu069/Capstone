import SwiftUI

struct NevigationView: View {
    @Binding var path: [[Point]]?
    @Binding var startPoint: Point?
    @Binding var selectPath: Int
    @Binding var direction: [Int]?
    @Binding var neviPath: [Point]
    var body: some View {
        var distance = sqrt(pow(neviPath[nextNodeIdx-1].lat-startPoint!.lat,2)+pow(neviPath[nextNodeIdx-1].lon-startPoint!.lon,2))
        let imgArr = ["arrow.up","arrow.turn.up.left","arrow.turn.up.right"]
        let strArr = ["직진","좌회전","우회전"]
        
//        if distance == 0.0001{
//            nextNodeIdx = nextNodeIdx+1
//        }
        HStack{
            Image(systemName: Int((distance*111190)/10)*10 > 30 ? imgArr[0] : imgArr[direction![nextNodeIdx-1]])
                .resizable()
                .frame(maxWidth:80, alignment: .leading)
                .padding()
                .frame(width: 80, height: 80)
            VStack{
                Text("\(Int((distance*111190)/10)*10)m")
                Text("\(Int((distance*111190)/10)*10 > 30 ? strArr[0] : strArr[direction![nextNodeIdx-1]])")
                     //\(strArr[direction![nextNodeIdx-1]])")
            }
            .frame(maxWidth: .infinity, alignment: .center)
        }
        .border(Color.black, width: 2)
        .frame(height: 50)
        .onChange(of: distance) { val in
            if val <= 0.0001{
                nextNodeIdx = nextNodeIdx+1
            }
            
        }
        
    }
        
}
