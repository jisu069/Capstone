import SwiftUI

struct SerchButton: View {
    @Binding var endPoint: Point?
    var body: some View {
        NavigationLink(destination: SerchView(endPoint: $endPoint)) {
            HStack{
                Text("검색")
                    .padding(7.0)
                    .frame(maxWidth: .infinity)
                    .border(Color.black)
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.leading)
                
                Image(systemName: "chevron.right")
                    .frame(width: 30)
            }
        }
    }
}

//struct SerchButton_Previews: PreviewProvider {
//    static var previews: some View {
//        SerchButton(endPoint: .constant([(127.046624, 37.724142)]))
//    }
//}
